const express = require('express');
const router = express.Router();
const scraperEngine = require('../services/scraperEngine');
const jikanService = require('../services/jikanService');

// GET /anime/search?q=naruto&page=1&limit=25
router.get('/search', async (req, res) => {
    try {
        const { q, page = 1, limit = 25 } = req.query;

        if (!q) {
            return res.status(400).json({ error: 'Missing query parameter' });
        }

        console.log(`Searching anime: ${q} (page ${page})`);
        const results = await jikanService.searchAnime(q, parseInt(page), parseInt(limit));

        res.json(results);
    } catch (error) {
        console.error('Anime search failed:', error);
        res.status(500).json({ error: 'Failed to search anime', details: error.message });
    }
});

// GET /anime/details/:malId
router.get('/details/:malId', async (req, res) => {
    try {
        const { malId } = req.params;

        if (!malId) {
            return res.status(400).json({ error: 'Missing malId parameter' });
        }

        console.log(`Fetching anime details for MAL ID: ${malId}`);
        const details = await jikanService.getAnimeDetails(parseInt(malId));

        res.json(details);
    } catch (error) {
        console.error('Anime details failed:', error);
        res.status(500).json({ error: 'Failed to fetch anime details', details: error.message });
    }
});

// GET /anime/relations/:malId
router.get('/relations/:malId', async (req, res) => {
    try {
        const { malId } = req.params;

        if (!malId) {
            return res.status(400).json({ error: 'Missing malId parameter' });
        }

        console.log(`Fetching anime relations for MAL ID: ${malId}`);
        const relations = await jikanService.getAnimeRelations(parseInt(malId));

        res.json(relations);
    } catch (error) {
        console.error('Anime relations failed:', error);
        res.status(500).json({ error: 'Failed to fetch anime relations', details: error.message });
    }
});

// GET /anime/trending?limit=10
router.get('/trending', async (req, res) => {
    try {
        const { limit = 10 } = req.query;

        console.log(`Fetching trending anime`);
        const trending = await jikanService.getTrendingAnime(parseInt(limit));

        res.json(trending);
    } catch (error) {
        console.error('Trending anime failed:', error);
        res.status(500).json({ error: 'Failed to fetch trending anime', details: error.message });
    }
});

// GET /anime/upcoming?limit=10
router.get('/upcoming', async (req, res) => {
    try {
        const { limit = 10 } = req.query;

        console.log(`Fetching upcoming anime`);
        const upcoming = await jikanService.getUpcomingAnime(parseInt(limit));

        res.json(upcoming);
    } catch (error) {
        console.error('Upcoming anime failed:', error);
        res.status(500).json({ error: 'Failed to fetch upcoming anime', details: error.message });
    }
});

// POST /anime/stream
// Body: { animeName: string, episode: number }
router.post('/stream', async (req, res) => {
    try {
        const { animeName, episode } = req.body;

        if (!animeName || !episode) {
            return res.status(400).json({ error: 'Missing animeName or episode' });
        }

        console.log(`Requesting stream for: ${animeName} EP ${episode} via ScraperEngine`);

        // 1. Search for the anime ID on AllAnime
        // We search using the Jikan title provided by the frontend
        const searchResults = await scraperEngine.search(animeName);

        if (!searchResults || searchResults.length === 0) {
            return res.status(404).json({ error: 'Anime not found on stream provider' });
        }

        // Use the first result (most relevant)
        const animeId = searchResults[0].id;
        console.log(`[Route] Maps to AllAnime ID: ${animeId}`);

        // 2. Extract stream using the ID and episode number
        // We assume episode "1" maps to "1". For special cases (OVAs etc), detailed mapping might be needed later.
        const streamData = await scraperEngine.extractStream(animeId, String(episode));

        if (!streamData) {
            return res.status(404).json({ error: 'Stream not found' });
        }

        console.log(`Stream found: ${streamData.url}`);
        res.json({
            url: streamData.url,
            referer: streamData.referer,
            quality: streamData.quality
        });
    } catch (error) {
        console.error('Stream extraction failed:', error);
        res.status(500).json({ error: 'Failed to extract stream', details: error.message });
    }
});

module.exports = router;
