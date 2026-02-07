const streamExtractor = require('./src/services/streamExtractor');
const scraperEngine = require('./src/services/scraperEngine');
const jikanService = require('./src/services/jikanService');
const mangaExtensionEngine = require('./src/services/mangaExtensionEngine');

// Helper to run only scraper test
async function testScraper() {
    console.log('\n[4] Testing ScraperEngine (AllAnime)...');
    try {
        const query = 'Naruto';
        console.log(`Searching for: ${query}`);
        const results = await scraperEngine.search(query);
        console.log(`Found ${results.length} results`);

        if (results.length > 0) {
            const first = results[0];
            console.log(`First result: ${first.title} (ID: ${first.id})`);

            console.log(`Fetching episodes for ${first.id}...`);
            const episodes = await scraperEngine.getEpisodes(first.id);
            console.log(`Found ${episodes.length} episodes`);

            if (episodes.length > 0) {
                const ep = episodes[0];
                console.log(`Extracting stream for Episode ${ep.number}...`);
                const stream = await scraperEngine.extractStream(first.id, String(ep.number));

                if (stream) {
                    console.log('✅ Stream Found:', stream);
                } else {
                    console.error('❌ Stream Extraction Failed');
                }
            }
        }
    } catch (e) {
        console.error('❌ ScraperEngine Test Failed:', e.message);
    }
}

(async () => {
    await testScraper();
})();
