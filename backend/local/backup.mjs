import { DatabaseSync, backup } from 'node:sqlite';
import { resolve } from 'node:path';
import { existsSync } from 'node:fs';
const source=resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite');
if(!existsSync(source) || !process.argv[2])throw new Error('Existing database and destination are required.');
const target=resolve(process.argv[2]);
if(existsSync(target))throw new Error('Refusing to overwrite a backup.');
const db=new DatabaseSync(source,{readOnly:true});
try { await backup(db,target);console.log('SQLite backup completed. Protect this file as private account data.'); }
finally {db.close();}
