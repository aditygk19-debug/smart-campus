import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { testConnection } from './db.js';
import departmentsRouter from './routes/departments.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;

// ---- Middleware ----
app.use(cors());                     // allow requests from the React frontend
app.use(express.json());             // parse JSON request bodies

// ---- Health check ----
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', service: 'smart-campus-bff', time: new Date().toISOString() });
});

// ---- Routes ----

app.use('/api/departments', departmentsRouter);

// ---- Start server ----
async function start() {
  const dbOk = await testConnection();
  if (!dbOk) {
    console.error('⚠️  Starting server anyway — DB may be down.');
  }

  app.listen(PORT, () => {
    console.log(`🚀 BFF running at http://localhost:${PORT}`);
    console.log(`   Health check: http://localhost:${PORT}/api/health`);
  });
}

start();