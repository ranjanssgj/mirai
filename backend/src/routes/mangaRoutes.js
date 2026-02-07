const express = require('express');
const router = express.Router();
const mangaExtensionEngine = require('../services/mangaExtensionEngine');

// GET /manga/search?query=naruto&extension=comix.to
router.get('/search', async (req, res) => {
    try {
        const { query, extension } = req.query;
        if (!query) return res.status(400).json({ error: 'Missing query' });

        const results = await mangaExtensionEngine.execute(extension || 'comix.to', 'search', [query]);
        res.json(results);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /manga/details?id=...&extension=...
router.get('/details', async (req, res) => {
    try {
        const { id, extension } = req.query;
        if (!id) return res.status(400).json({ error: 'Missing id' });

        const details = await mangaExtensionEngine.execute(extension || 'comix.to', 'getDetails', [id]);
        res.json(details);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /manga/chapters?id=...&extension=...
router.get('/chapters', async (req, res) => {
    try {
        const { id, extension } = req.query;
        if (!id) return res.status(400).json({ error: 'Missing id' });

        const chapters = await mangaExtensionEngine.execute(extension || 'comix.to', 'getChapters', [id]);
        res.json(chapters);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /manga/pages?chapterId=...&extension=...
router.get('/pages', async (req, res) => {
    try {
        const { chapterId, extension } = req.query;
        if (!chapterId) return res.status(400).json({ error: 'Missing chapterId' });

        const pages = await mangaExtensionEngine.execute(extension || 'comix.to', 'getPages', [chapterId]);
        res.json(pages);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /manga-home
// Returns content from all installed extensions in a structured format
router.get('/manga-home', async (req, res) => {
    try {
        console.log('Fetching manga-home content from all extensions');

        // For now, we only have comix.to extension
        // In future, loop through all registered extensions
        const extensions = [];

        // Comix.to extension
        try {
            const comixLatest = await mangaExtensionEngine.execute('comix.to', 'search', ['latest']);
            extensions.push({
                id: 'comix.to',
                name: 'Comix',
                icon: 'https://comick.cc/favicon.ico',
                latestUpdates: comixLatest.slice(0, 5).map(manga => ({
                    id: manga.id,
                    title: manga.title,
                    cover: manga.cover,
                    chapter: 'Latest' // Could be enhanced with actual chapter data
                }))
            });
        } catch (error) {
            console.error('Comix extension failed:', error.message);
            // Continue with other extensions even if one fails
        }

        res.json({ extensions });
    } catch (error) {
        console.error('Manga-home failed:', error);
        res.status(500).json({ error: 'Failed to fetch manga-home', details: error.message });
    }
});

module.exports = router;
