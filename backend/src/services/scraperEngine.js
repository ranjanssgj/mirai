const axios = require('axios');
const NodeCache = require('node-cache');

class ScraperEngine {
    constructor() {
        this.cache = new NodeCache({ stdTTL: 86400 }); // 24 hours TTL for search
        this.streamCache = new NodeCache({ stdTTL: 1800 }); // 30 minutes TTL for streams
        this.userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
        this.baseUrl = 'https://allanime.day';
        this.apiUrl = 'https://api.allanime.day/api';
        this.referer = 'https://allmanga.to';
    }

    /**
     * Search for anime using AllAnime GraphQL
     * @param {string} query 
     * @returns {Promise<Array<{id: string, title: string, episodes: number}>>}
     */
    async search(query) {
        const cacheKey = `search:${query.toLowerCase()}`;
        if (this.cache.get(cacheKey)) {
            console.log(`[ScraperEngine] Returning cached search for: ${query}`);
            return this.cache.get(cacheKey);
        }

        const searchQuery = `query( $search: SearchInput $limit: Int $page: Int $translationType: VaildTranslationTypeEnumType $countryOrigin: VaildCountryOriginEnumType ) { shows( search: $search limit: $limit page: $page translationType: $translationType countryOrigin: $countryOrigin ) { edges { _id name availableEpisodes __typename } }}`;

        try {
            const response = await axios.get(this.apiUrl, {
                params: {
                    variables: JSON.stringify({
                        search: {
                            allowAdult: false,
                            allowUnknown: false,
                            query: query
                        },
                        limit: 20,
                        page: 1,
                        translationType: "sub",
                        countryOrigin: "ALL"
                    }),
                    query: searchQuery
                },
                headers: {
                    'User-Agent': this.userAgent,
                    'Referer': this.referer
                }
            });

            if (response.data.data && response.data.data.shows) {
                const results = response.data.data.shows.edges.map(edge => ({
                    id: edge._id,
                    title: edge.name,
                    episodes: edge.availableEpisodes?.sub || 0
                }));

                this.cache.set(cacheKey, results);
                return results;
            }
            return [];
        } catch (error) {
            console.error('[ScraperEngine] Search failed:', error.message);
            return [];
        }
    }

    /**
     * Get episodes for an anime using AllAnime GraphQL
     * @param {string} animeId 
     * @returns {Promise<Array<{id: string, number: number}>>}
     */
    async getEpisodes(animeId) {
        const cacheKey = `episodes:${animeId}`;
        const cached = this.cache.get(cacheKey);
        // Reduce TTL for episodes as they update
        if (cached) return cached;

        const query = `query ($showId: String!) { show( _id: $showId ) { _id availableEpisodesDetail }}`;

        try {
            const response = await axios.get(this.apiUrl, {
                params: {
                    variables: JSON.stringify({
                        showId: animeId
                    }),
                    query: query
                },
                headers: {
                    'User-Agent': this.userAgent,
                    'Referer': this.referer
                }
            });

            if (response.data.data && response.data.data.show) {
                const details = response.data.data.show.availableEpisodesDetail;
                if (!details || !details.sub) return [];

                // Parse "sub" episodes
                // details.sub is an array of strings like ["1", "2", "3", "3.5", ...]
                // We need to map them. Since AllAnime maps episode number directly to ID often
                const episodes = details.sub
                    .map(ep => ({
                        id: ep, // Episode "number" string is the ID in this context for next query
                        number: parseFloat(ep)
                    }))
                    .sort((a, b) => b.number - a.number); // Descending

                this.cache.set(cacheKey, episodes, 3600); // 1 hour cache
                return episodes;
            }
            return [];
        } catch (error) {
            console.error('[ScraperEngine] GetEpisodes failed:', error.message);
            return [];
        }
    }

    /**
     * extract stream for an episode
     * @param {string} animeId 
     * @param {string} episodeString 
     * @returns {Promise<{url: string, referer: string, quality: string} | null>}
     */
    async extractStream(animeId, episodeString) {
        const cacheKey = `stream:${animeId}:${episodeString}`;
        if (this.streamCache.get(cacheKey)) return this.streamCache.get(cacheKey);

        const query = `query ($showId: String!, $translationType: VaildTranslationTypeEnumType!, $episodeString: String!) { episode( showId: $showId translationType: $translationType episodeString: $episodeString ) { episodeString sourceUrls }}`;

        try {
            const response = await axios.get(this.apiUrl, {
                params: {
                    variables: JSON.stringify({
                        showId: animeId,
                        translationType: "sub",
                        episodeString: episodeString
                    }),
                    query: query
                },
                headers: {
                    'User-Agent': this.userAgent,
                    'Referer': this.referer
                }
            });

            const episodeData = response.data.data?.episode;
            if (!episodeData?.sourceUrls) return null;

            console.log('[DEBUG] Available Sources:', episodeData.sourceUrls.map(s => s.sourceName));

            // Priority order
            const priority = ['S-mp4', 'Yt-mp4', 'Luf-Mp4', 'Mp4', 'Ok'];
            const sortedSources = episodeData.sourceUrls.sort((a, b) => {
                const ixA = priority.indexOf(a.sourceName);
                const ixB = priority.indexOf(b.sourceName);
                if (ixA === -1 && ixB === -1) return 0;
                if (ixA === -1) return 1;
                if (ixB === -1) return -1;
                return ixA - ixB;
            });

            for (const source of sortedSources) {
                // console.log(`[ScraperEngine] Trying source: ${source.sourceName}`);
                try {
                    let url = source.sourceUrl;
                    if (url.startsWith('--')) {
                        url = this.decryptAllAnime(url.substring(2));
                    }

                    if (url.startsWith('/')) {
                        // It's an internal API path (requiring clock.json fetch)
                        url = this.baseUrl + url.replace('/clock', '/clock.json');

                        const embedResponse = await axios.get(url, {
                            headers: { 'User-Agent': this.userAgent, 'Referer': this.referer }
                        });

                        const finalUrl = embedResponse.data.links?.[0]?.link;
                        if (finalUrl) {
                            console.log(`[ScraperEngine] Stream found via ${source.sourceName}: ${finalUrl}`);
                            // Ensure the URL is absolute
                            const absoluteUrl = finalUrl.startsWith('//') ? 'https:' + finalUrl : finalUrl;
                            const result = { url: absoluteUrl, referer: this.referer, quality: 'auto' };
                            this.streamCache.set(cacheKey, result);
                            return result;
                        }
                    } else {
                        // It's a direct URL (e.g. tools.fast4speed.rsvp)
                        console.log(`[ScraperEngine] Direct Stream found via ${source.sourceName}: ${url}`);
                        const absoluteUrl = url.startsWith('//') ? 'https:' + url : url;
                        const result = { url: absoluteUrl, referer: this.referer, quality: 'auto' };
                        this.streamCache.set(cacheKey, result);
                        return result;
                    }

                } catch (e) {
                    console.error(`[ScraperEngine] Source ${source.sourceName} failed: ${e.message}`);
                    if (e.response && (e.response.status === 404 || e.response.status === 500)) {
                        continue; // Try next source
                    }
                }
            }

            return null;

        } catch (error) {
            console.error('[ScraperEngine] ExtractStream failed:', error.message);
            return null;
        }
    }

    // Decrypt AllAnime "encrypted" hex strings
    decryptAllAnime(hexStream) {
        // Mapping from ani-cli provider_init
        const mapping = {
            '01': '9', '02': ':', '03': ';', '04': '<', '05': '=', '06': '>', '07': '?',
            '08': '0', '09': '1', '0a': '2', '0b': '3', '0c': '4', '0d': '5', '0e': '6', '0f': '7',
            '10': '(', '11': ')', '12': '*', '13': '+', '14': ',', '15': '-', '16': '.', '17': '/',
            '18': '0', '19': '!', '1a': ':', '1b': '#', '1c': '$', '1d': '%', '1e': '&', '1f': '\'',
            '40': 'x', '41': 'y', '42': 'z', '43': '{', '44': '|', '45': '}', '46': '~',
            '48': 'p', '49': 'q', '4a': 'r', '4b': 's', '4c': 't', '4d': 'u', '4e': 'v', '4f': 'w',
            '50': 'h', '51': 'i', '52': 'j', '53': 'k', '54': 'l', '55': 'm', '56': 'n', '57': 'o',
            '59': 'a', '5a': 'b', '5b': 'c', '5c': 'd', '5d': 'e', '5e': 'f', '5f': 'g',
            '60': 'X', '61': 'Y', '62': 'Z', '63': '[', '64': '\\', '65': ']', '66': '^', '67': '_',
            '68': 'P', '69': 'Q', '6a': 'R', '6b': 'S', '6c': 'T', '6d': 'U', '6e': 'V', '6f': 'W',
            '70': 'H', '71': 'I', '72': 'J', '73': 'K', '74': 'L', '75': 'M', '76': 'N', '77': 'O',
            '79': 'A', '7a': 'B', '7b': 'C', '7c': 'D', '7d': 'E', '7e': 'F', '7f': 'G'
        };

        // Remove any whitespace/newlines
        hexStream = hexStream.replace(/\s/g, '');

        let decrypted = '';
        // console.log('Mapping keys:', Object.keys(mapping));

        for (let i = 0; i < hexStream.length; i += 2) {
            const hex = hexStream.substring(i, i + 2);
            const val = mapping[hex];
            decrypted += val || hex;
        }
        return decrypted;
    }
}

module.exports = new ScraperEngine();