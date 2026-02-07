const vm = require('vm');
const axios = require('axios');
const cheerio = require('cheerio');

class MangaExtensionEngine {
    constructor() {
        this.extensions = new Map();
        this.loadDefaultExtensions();
        this.userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    }

    async execute(extensionId, method, args) {
        const extension = this.extensions.get('comix.to');
        if (!extension || typeof extension[method] !== 'function') {
            throw new Error(`Method ${method} not supported`);
        }
        return await extension[method](...args);
    }

    loadDefaultExtensions() {
        const comickDriver = {
            search: async (query) => {
                // Return mock data if searching for 'popular' or 'latest' to ensure page isn't empty
                if (query === 'popular' || query === 'latest') {
                    return [
                        { id: 'naruto', title: 'Naruto (Mock)', cover: 'https://meo.comick.pictures/P981y-m.jpg' },
                        { id: 'one-piece', title: 'One Piece (Mock)', cover: 'https://meo.comick.pictures/981yP-m.jpg' }
                    ];
                }

                try {
                    // Try Comick API
                    const res = await axios.get(`https://api.comick.cc/v1.0/search?q=${encodeURIComponent(query)}&limit=25`, {
                        headers: { 'User-Agent': this.userAgent }
                    });
                    if (res.data && Array.isArray(res.data)) {
                        return res.data.map(m => ({
                            id: m.hid,
                            title: m.title,
                            cover: m.md_covers && m.md_covers[0] ? `https://meo.comick.pictures/${m.md_covers[0].b2key}` : ''
                        }));
                    }
                    return [];
                } catch (e) {
                    console.error('Manga Search Error:', e.message);
                    return [];
                }
            },
            getDetails: async (id) => {
                try {
                    const res = await axios.get(`https://api.comick.cc/comic/${id}`, {
                        headers: { 'User-Agent': this.userAgent }
                    });
                    const comic = res.data.comic;
                    return {
                        id: id,
                        title: comic.title,
                        description: comic.desc,
                        author: 'Unknown',
                        status: comic.status === 1 ? 'Ongoing' : 'Completed',
                        cover: comic.md_covers && comic.md_covers[0] ? `https://meo.comick.pictures/${comic.md_covers[0].b2key}` : '',
                        genres: comic.md_comic_md_genres ? comic.md_comic_md_genres.map(g => g.md_genres.name) : []
                    };
                } catch (e) {
                    return { id, title: 'Unknown Manga', description: 'Failed to load details', status: 'Unknown', cover: '', genres: [] };
                }
            },
            getChapters: async (id) => {
                try {
                    const res = await axios.get(`https://api.comick.cc/comic/${id}/chapters?limit=1000&lang=en`, {
                        headers: { 'User-Agent': this.userAgent }
                    });
                    return res.data.chapters.map(c => ({
                        id: c.hid,
                        number: parseFloat(c.chap) || 0,
                        title: c.title || `Chapter ${c.chap}`,
                        date: c.created_at
                    }));
                } catch (e) {
                    return [];
                }
            },
            getPages: async (chapterId) => {
                try {
                    const res = await axios.get(`https://api.comick.cc/chapter/${chapterId}`, {
                        headers: { 'User-Agent': this.userAgent }
                    });
                    return res.data.chapter.images.map(img => `https://meo.comick.pictures/${img.b2key}`);
                } catch (e) {
                    return [];
                }
            }
        };

        this.extensions.set('comix.to', comickDriver);
    }
}

module.exports = new MangaExtensionEngine();
