// Disposable local acceptance environment, never the presentation database.
import {createCampusQServer} from './server.mjs';
import {randomUUID,randomBytes} from 'node:crypto';
import {resolve,dirname,relative} from 'node:path';
import {mkdirSync,writeFileSync,readFileSync,existsSync} from 'node:fs';
if(process.env.CAMPUSQ_TEST_FIXTURE!=='true')throw new Error('Set CAMPUSQ_TEST_FIXTURE=true to use an isolated acceptance database.');
const metadataPath=resolve('backend/local/data/acceptance-fixture.json');
const resume=process.argv.includes('--resume');
let metadata;
if(resume)metadata=JSON.parse(readFileSync(metadataPath,'utf8'));
else {
 const id=randomUUID();
 metadata={databasePath:resolve('backend/local/data/acceptance-'+id+'.sqlite'),secret:randomBytes(24).toString('base64url'),
   adminEmail:'admin-'+id+'@example.test',staffEmail:'staff-'+id+'@example.test'};
 if(existsSync(metadata.databasePath))throw new Error('Refusing to overwrite an existing database.');
}
// Copied metadata must never reopen a database in another project checkout.
const fixtureDirectory=resolve('backend/local/data');
const fixtureFile=relative(fixtureDirectory,resolve(metadata.databasePath));
if(!/^acceptance-[0-9a-f-]+\.sqlite$/i.test(fixtureFile))
 throw new Error('Fixture database must be inside this working copy backend/local/data directory. Preserve copied metadata and create a new fixture.');
const app=createCampusQServer({databasePath:metadata.databasePath});
if(!resume) {
 const admin=await app.service.bootstrap({fullName:'Acceptance Administrator',email:metadata.adminEmail,password:metadata.secret});
 const actor=app.service.one('SELECT * FROM users WHERE id=?',admin.id);
 await app.service.createUser({fullName:'Acceptance Registrar',email:metadata.staffEmail,password:metadata.secret,counterName:'Window 1',isActive:true},'STAFF',admin.id);
 metadata.serviceId=app.service.saveService(actor,null,{name:'Acceptance Registrar Service',description:'Local integration acceptance service',
   requirementsText:'Student ID',iconType:'DOCUMENT_REQUEST',isAvailable:true,isQueueEnabled:true,isAppointmentEnabled:true,estimatedMinutes:10}).id;
 mkdirSync(dirname(metadataPath),{recursive:true});writeFileSync(metadataPath,JSON.stringify(metadata));
}
const assetPath=resolve('app/src/androidTest/assets/local-acceptance.json');
mkdirSync(dirname(assetPath),{recursive:true});
writeFileSync(assetPath,JSON.stringify({baseUrl:'http://10.0.2.2:8787',adminEmail:metadata.adminEmail,staffEmail:metadata.staffEmail,
  password:metadata.secret,serviceId:metadata.serviceId}));
app.server.listen(8787,'127.0.0.1',()=>console.log('Isolated CampusQ Android acceptance server ready on loopback:8787. Credentials are in ignored test fixture files, never printed.'));
for(const signal of ['SIGINT','SIGTERM'])process.once(signal,()=>app.close().then(()=>process.exit(0)));
