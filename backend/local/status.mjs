import {DatabaseSync} from 'node:sqlite';
import {readFileSync,existsSync} from 'node:fs';
import {resolve} from 'node:path';
const fixture=process.argv.includes('--fixture');
const path=fixture?JSON.parse(readFileSync('backend/local/data/acceptance-fixture.json','utf8')).databasePath:resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite');
if(!existsSync(path))throw new Error('Database does not exist. No file was created.');
const db=new DatabaseSync(path,{readOnly:true});
try {
 const counts={schema:db.prepare('PRAGMA user_version').get().user_version};
 for(const table of ['users','registrar_services','queue_tickets','appointments','transactions','activity_logs'])counts[table]=db.prepare('SELECT count(*) AS n FROM '+table).get().n;
 console.log(JSON.stringify(counts));
}finally{db.close();}
