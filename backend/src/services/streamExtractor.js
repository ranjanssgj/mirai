const axios = require('axios');
const cheerio = require('cheerio');

class StreamExtractor {
    constructor() {
        this.userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
        this.gogoanimeBase = 'https://anitaku.to';
        this.timeout = 30000; // 30 seconds
    }

    /**
     * Main entry point: Get stream URL for anime episode
     * @param {string} animeName - Name of the anime
     * @param {number} episode - Episode number
     * @returns {Promise<{url: string, referer: string}>}
     */
    async getStreamUrl(animeName, episode) {
        try {
            console.log(`[StreamExtractor] Searching for: ${animeName} Episode ${episode}`);

            // Step 1: Search for the anime
            const animeId = await this.searchAnime(animeName);
            if (!animeId) {
                throw new Error('Anime not found in search results');
            }

            // Step 2: Get episode page URL
            const episodePageUrl = `${this.gogoanimeBase}/${animeId}-episode-${episode}`;
            console.log(`[StreamExtractor] Episode page: ${episodePageUrl}`);

            // Step 3: Extract iframe source from episode page
            const iframeUrl = await this.extractIframeUrl(episodePageUrl);
            if (!iframeUrl) {
                throw new Error('Failed to extract iframe URL from episode page');
            }

            // Step 4: Extract .m3u8 URL from player
            const streamUrl = await this.extractM3u8Url(iframeUrl);
            if (!streamUrl) {
                throw new Error('Failed to extract m3u8 URL from player');
            }

            console.log(`[StreamExtractor] Stream URL found: ${streamUrl}`);
            return {
                url: streamUrl,
                referer: this.gogoanimeBase
            };
        } catch (error) {
            console.error('[StreamExtractor] Error:', error.message);
            throw error;
        }
    }

    /**
     * Search for anime and return the anime ID
     * @param {string} query - Anime name
     * @returns {Promise<string|null>}
     */
    async searchAnime(query) {
        try {
            const searchUrl = `${this.gogoanimeBase}/search.html?keyword=${encodeURIComponent(query)}`;
            const response = await axios.get(searchUrl, {
                headers: { 'User-Agent': this.userAgent },
                timeout: this.timeout
            });

            const $ = cheerio.load(response.data);

            // Find the first result
            const firstResult = $('.last_episodes ul.items li').first();
            if (firstResult.length === 0) {
                return null;
            }

            // Extract the anime ID from the link
            const link = firstResult.find('p.name a').attr('href');
            if (!link) {
                return null;
            }

            // Link format: /category/anime-name
            const animeId = link.replace('/category/', '');
            console.log(`[StreamExtractor] Found anime ID: ${animeId}`);
            return animeId;
        } catch (error) {
            console.error('[StreamExtractor] Search error:', error.message);
            return null;
        }
    }

    /**
     * Extract iframe URL from episode page
     * @param {string} episodePageUrl - Episode page URL
     * @returns {Promise<string|null>}
     */
    async extractIframeUrl(episodePageUrl) {
        try {
            const response = await axios.get(episodePageUrl, {
                headers: { 'User-Agent': this.userAgent },
                timeout: this.timeout
            });

            const $ = cheerio.load(response.data);

            // Priority 1: Check for server list with data-video attribute (Gogoanime/Anitaku pattern)
            const serverVideo = $('.anime_muti_link .server-video').first();
            if (serverVideo.length > 0) {
                const dataVideo = serverVideo.attr('data-video');
                if (dataVideo) {
                    const iframeUrl = this.normalizeUrl(dataVideo);
                    console.log(`[StreamExtractor] Found iframe via data-video: ${iframeUrl}`);
                    return iframeUrl;
                }
            }

            // Priority 2: Look for the iframe in the player container
            const iframe = $('.anime_muti_link iframe').first();
            if (iframe.length > 0) {
                const iframeUrl = this.normalizeUrl(iframe.attr('src'));
                console.log(`[StreamExtractor] Iframe URL via src: ${iframeUrl}`);
                return iframeUrl;
            }

            // Priority 3: Try alternative selector
            const altIframe = $('iframe').first();
            if (altIframe.length > 0) {
                return this.normalizeUrl(altIframe.attr('src'));
            }

            return null;
        } catch (error) {
            console.error('[StreamExtractor] Iframe extraction error:', error.message);
            return null;
        }
    }

    /**
     * Extract .m3u8 URL from player iframe
     * @param {string} iframeUrl - Player iframe URL
     * @returns {Promise<string|null>}
     */
    async extractM3u8Url(iframeUrl) {
        try {
            // Regex definitions
            const m3u8Regex = /(https?:\/\/[^\s"']+\.m3u8[^\s"']*)/gi;

            const response = await axios.get(iframeUrl, {
                headers: {
                    'User-Agent': this.userAgent,
                    'Referer': this.gogoanimeBase
                },
                timeout: this.timeout
            });

            const html = response.data;

            // Method 0: Check for packed code
            if (html.includes('eval(function(p,a,c,k,e,d)')) {
                console.log('[StreamExtractor] Found packed code, attempting to unpack...');
                const packed = this.extractPacked(html);
                if (packed) {
                    const unpacked = this.unpack(packed);
                    if (unpacked) {
                        console.log('[StreamExtractor] Unpacked code successfully');
                        // Recursive search in unpacked code
                        const m3u8Match = unpacked.match(m3u8Regex);
                        if (m3u8Match && m3u8Match.length > 0) {
                            console.log(`[StreamExtractor] Found m3u8 in unpacked: ${m3u8Match[0]}`);
                            return m3u8Match[0];
                        }
                    }
                }
            }

            // Method 1: Look for direct .m3u8 URLs in the HTML
            // const m3u8Regex = ... (Removed from here)
            const matches = html.match(m3u8Regex);

            if (matches && matches.length > 0) {
                // Prefer the longest URL (often highest quality)
                const bestMatch = matches.sort((a, b) => b.length - a.length)[0];
                console.log(`[StreamExtractor] Found m3u8 URL: ${bestMatch}`);
                return bestMatch;
            }

            // Method 2: Look for encoded/obfuscated URLs
            // Common pattern: sources.push({file: "URL"})
            const sourceRegex = /sources[^\[]*\[\s*{[^}]*file\s*:\s*["']([^"']+)["']/i;
            const sourceMatch = html.match(sourceRegex);

            if (sourceMatch && sourceMatch[1]) {
                console.log(`[StreamExtractor] Found source URL: ${sourceMatch[1]}`);
                return sourceMatch[1];
            }

            // Method 3: Look for any video URLs
            const videoRegex = /(https?:\/\/[^\s"']+\.(m3u8|mp4)[^\s"']*)/gi;
            const videoMatches = html.match(videoRegex);

            if (videoMatches && videoMatches.length > 0) {
                console.log(`[StreamExtractor] Found video URL: ${videoMatches[0]}`);
                return videoMatches[0];
            }

            return null;
        } catch (error) {
            console.error('[StreamExtractor] M3u8 extraction error:', error.message);
            return null;
        }
    }

    extractPacked(html) {
        const startToken = 'eval(function(p,a,c,k,e,d)';
        const startIdx = html.indexOf(startToken);
        if (startIdx === -1) return null;

        let braceCount = 0;
        let endIdx = -1;

        // Find the closure of the eval call
        for (let i = startIdx; i < html.length; i++) {
            if (html[i] === '(') braceCount++;
            else if (html[i] === ')') braceCount--;

            // eval(...) -> braceCount starts at 0 (before eval), becomes 1 at (, ends at 0
            // But we didn't include eval in the loop start check if we assume startIdx points to 'e'

            // Actually simpler: standard packer ends with .split('|')))
            if (html.substring(i).startsWith(".split('|')))")) {
                endIdx = i + ".split('|')))".length;
                break;
            }
        }

        if (endIdx !== -1) {
            return html.substring(startIdx, endIdx);
        }
        return null; // Fallback
    }

    unpack(packed) {
        try {
            // Remove 'eval' and execute the function to get the string
            // WARNING: Only use this on trusted sources (Gogoanime is targeted)
            let code = packed.replace('eval', '');
            return eval(code);
        } catch (e) {
            console.error('[StreamExtractor] Unpack failed:', e.message);
            return null;
        }
    }

    /**
     * Normalize URL (handle relative URLs)
     * @param {string} url - URL to normalize
     * @returns {string}
     */
    normalizeUrl(url) {
        if (!url) return '';
        if (url.startsWith('http')) return url;
        if (url.startsWith('//')) return 'https:' + url;
        if (url.startsWith('/')) return this.gogoanimeBase + url;
        return url;
    }
}

module.exports = new StreamExtractor();
