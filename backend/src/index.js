const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const animeRoutes = require('./routes/animeRoutes');
const mangaRoutes = require('./routes/mangaRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(helmet());
app.use(morgan('dev'));
app.use(express.json());

// Routes
app.use('/anime', animeRoutes);
app.use('/manga', mangaRoutes);

// Health Check
app.get('/', (req, res) => {
    res.json({ status: 'ok', version: '1.0.0' });
});

// Error Handling
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Internal Server Error', message: err.message });
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
});
