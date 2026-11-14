import { existsSync } from 'node:fs';
const source=resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite');
if(!existsSync(source) || !process.argv[2])throw new Error('Existing database and destination are required.');
const target=resolve(process.argv[2]);

