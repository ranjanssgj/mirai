const axios = require('axios');

class JikanService {
    constructor() {
        this.baseUrl = 'https://api.jikan.moe/v4';
        this.timeout = 10000;

        // Rate limiting: Jikan allows 60 requests/min (3 req/sec)
        this.lastRequestTime = 0;
        this.minRequestInterval = 350; // 350ms between requests to be safe
    }

    /**
     * Rate limiting helper
     */
    async rateLimit() {
        const now = Date.now();
        const timeSinceLastRequest = now - this.lastRequestTime;

        if (timeSinceLastRequest < this.minRequestInterval) {
            await new Promise(resolve =>
                setTimeout(resolve, this.minRequestInterval - timeSinceLastRequest)
            );
        }

        this.lastRequestTime = Date.now();
    }

    /**
     * Search for anime
     * @param {string} query - Search query
     * @param {number} page - Page number (default: 1)
     * @param {number} limit - Results per page (default: 25, max: 25)
     * @returns {Promise<Array>}
     */
    async searchAnime(query, page = 1, limit = 25) {
        try {
            await this.rateLimit();

            const response = await axios.get(`${this.baseUrl}/anime`, {
                params: {
                    q: query,
                    page: page,
                    limit: Math.min(limit, 25) // Max 25 per Jikan docs
                },
                timeout: this.timeout
            });

            if (response.data && response.data.data) {
                return response.data.data.map(anime => ({
                    malId: anime.mal_id,
                    title: anime.title,
                    titleEnglish: anime.title_english,
                    image: anime.images?.jpg?.large_image_url || anime.images?.jpg?.image_url,
                    score: anime.score,
                    episodes: anime.episodes,
                    status: anime.status,
                    year: anime.year,
                    type: anime.type
                }));
            }

            return [];
        } catch (error) {
            console.error('[JikanService] Search error:', error.message);
            throw new Error(`Jikan search failed: ${error.message}`);
        }
    }

    /**
     * Get anime details by MAL ID
     * @param {number} malId - MyAnimeList ID
     * @returns {Promise<Object>}
     */
    async getAnimeDetails(malId) {
        try {
            await this.rateLimit();

            const response = await axios.get(`${this.baseUrl}/anime/${malId}`, {
                timeout: this.timeout
            });

            const anime = response.data.data;

            return {
                malId: anime.mal_id,
                title: anime.title,
                titleEnglish: anime.title_english,
                titleJapanese: anime.title_japanese,
                synopsis: anime.synopsis,
                image: anime.images?.jpg?.large_image_url || anime.images?.jpg?.image_url,
                trailer: anime.trailer?.url,
                score: anime.score,
                scoredBy: anime.scored_by,
                rank: anime.rank,
                popularity: anime.popularity,
                episodes: anime.episodes,
                status: anime.status,
                airing: anime.airing,
                aired: {
                    from: anime.aired?.from,
                    to: anime.aired?.to,
                    string: anime.aired?.string
                },
                duration: anime.duration,
                rating: anime.rating,
                source: anime.source,
                season: anime.season,
                year: anime.year,
                studios: anime.studios?.map(s => s.name) || [],
                genres: anime.genres?.map(g => g.name) || [],
                themes: anime.themes?.map(t => t.name) || [],
                demographics: anime.demographics?.map(d => d.name) || []
            };
        } catch (error) {
            console.error('[JikanService] Details error:', error.message);
            throw new Error(`Jikan details failed: ${error.message}`);
        }
    }

    /**
     * Get anime relations (related anime/manga)
     * @param {number} malId - MyAnimeList ID
     * @returns {Promise<Object>}
     */
    async getAnimeRelations(malId) {
        try {
            await this.rateLimit();

            const response = await axios.get(`${this.baseUrl}/anime/${malId}/relations`, {
                timeout: this.timeout
            });

            if (response.data && response.data.data) {
                return response.data.data.map(relation => ({
                    relation: relation.relation,
                    entries: relation.entry.map(entry => ({
                        malId: entry.mal_id,
                        type: entry.type, // 'anime' or 'manga'
                        name: entry.name,
                        url: entry.url
                    }))
                }));
            }

            return [];
        } catch (error) {
            console.error('[JikanService] Relations error:', error.message);
            throw new Error(`Jikan relations failed: ${error.message}`);
        }
    }

    /**
     * Get trending anime (top airing)
     * @param {number} limit - Max results (default: 10)
     * @returns {Promise<Array>}
     */
    async getTrendingAnime(limit = 10) {
        try {
            await this.rateLimit();

            const response = await axios.get(`${this.baseUrl}/top/anime`, {
                params: {
                    filter: 'airing',
                    limit: Math.min(limit, 25)
                },
                timeout: this.timeout
            });

            if (response.data && response.data.data) {
                return response.data.data.map(anime => ({
                    malId: anime.mal_id,
                    title: anime.title,
                    image: anime.images?.jpg?.large_image_url || anime.images?.jpg?.image_url,
                    score: anime.score,
                    episodes: anime.episodes,
                    status: anime.status
                }));
            }

            return [];
        } catch (error) {
            console.error('[JikanService] Trending error:', error.message);
            throw new Error(`Jikan trending failed: ${error.message}`);
        }
    }

    /**
     * Get upcoming anime
     * @param {number} limit - Max results (default: 10)
     * @returns {Promise<Array>}
     */
    async getUpcomingAnime(limit = 10) {
        try {
            await this.rateLimit();

            const response = await axios.get(`${this.baseUrl}/top/anime`, {
                params: {
                    filter: 'upcoming',
                    limit: Math.min(limit, 25)
                },
                timeout: this.timeout
            });

            if (response.data && response.data.data) {
                return response.data.data.map(anime => ({
                    malId: anime.mal_id,
                    title: anime.title,
                    image: anime.images?.jpg?.large_image_url || anime.images?.jpg?.image_url,
                    score: anime.score,
                    episodes: anime.episodes,
                    status: anime.status,
                    airingStart: anime.aired?.from
                }));
            }

            return [];
        } catch (error) {
            console.error('[JikanService] Upcoming error:', error.message);
            throw new Error(`Jikan upcoming failed: ${error.message}`);
        }
    }
}

module.exports = new JikanService();
