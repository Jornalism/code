import { DatabaseSync } from 'node:sqlite';
import { mkdirSync, readFileSync, readdirSync } from 'node:fs';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

export function openDatabase(path) {
  if (path !== ':memory:') mkdirSync(dirname(path), { recursive: true });
  const db = new DatabaseSync(path);
  db.exec('PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL; PRAGMA busy_timeout=5000; PRAGMA synchronous=FULL;');
  const current = Number(db.prepare('PRAGMA user_version').get().user_version);
  const directory = fileURLToPath(new URL('./migrations/', import.meta.url));
  const migrations = readdirSync(directory).filter(x => /^\d+.*\.sql$/.test(x)).sort();
  if (current > migrations.length) throw new Error('Database schema is newer than this server.');
  for (let index = current; index < migrations.length; index++) {
    db.exec('BEGIN IMMEDIATE');
    try { db.exec(readFileSync(directory + '/' + migrations[index], 'utf8')); db.exec('PRAGMA user_version=' + (index + 1)); db.exec('COMMIT'); }
    catch (error) { db.exec('ROLLBACK'); db.close(); throw error; }
  }
  return db;
}
export function transaction(db, action) {
  db.exec('BEGIN IMMEDIATE');
  try { const value = action(); db.exec('COMMIT'); return value; }
  catch (error) { db.exec('ROLLBACK'); throw error; }
}
