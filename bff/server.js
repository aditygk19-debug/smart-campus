import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { testConnection } from './db.js';
import departmentsRouter from './routes/departments.js';
import authRouter from './routes/auth.js';
import resourceTypesRouter from './routes/resource-types.js';
import resourcesRouter from './routes/resources.js';
import campusNodesRouter from './routes/campus-nodes.js';
import bookingsRouter from './routes/bookings.js';

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
app.use('/api/auth', authRouter);
app.use('/api/departments/:deptId/resource-types', resourceTypesRouter);
app.use('/api/resources', resourcesRouter);
app.use('/api/campus-nodes', campusNodesRouter);
app.use('/api/bookings', bookingsRouter);

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