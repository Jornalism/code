const fixture=process.argv.includes('--fixture');
const path=fixture?JSON.parse(readFileSync('backend/local/data/acceptance-fixture.json','utf8')).databasePath:resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite');
if(!existsSync(path))throw new Error('Database does not exist. No file was created.');
const db=new DatabaseSync(path,{readOnly:true});

