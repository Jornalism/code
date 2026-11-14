import test,{before,after} from 'node:test';
import assert from 'node:assert/strict';
import {mkdtempSync,rmSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {randomUUID} from 'node:crypto';
import {createCampusQServer} from './server.mjs';
import {passwordMatches} from './security.mjs';

const directory=mkdtempSync(join(tmpdir(),'campusq-offline-')),file=join(directory,'acceptance.sqlite');
const secret='Presentation-'+randomUUID();
let app,base,admin,staff,student,second,serviceId,ticketId,appointmentId;
async function start(){app=createCampusQServer({databasePath:file});await new Promise(resolve=>app.server.listen(0,'127.0.0.1',resolve));base='http://127.0.0.1:'+app.server.address().port;}
async function api(method,path,body,session,key) {
 const response=await fetch(base+path,{method,headers:{...(body?{'Content-Type':'application/json'}:{}),...(session?{Authorization:'Bearer '+session.token}:{}),...(key?{'Idempotency-Key':key}:{})},body:body?JSON.stringify(body):undefined});
 return {status:response.status,data:await response.json()};
}
const login=async address=>{const r=await api('POST','/api/auth/login',{email:address,password:secret});assert.equal(r.status,200);return r.data;};
const register=async(n)=>{const email='student'+n+'@example.test';const r=await api('POST','/api/auth/register',{fullName:'Student '+n,email,password:secret,studentId:'S-'+String(n).padStart(5,'0')});assert.equal(r.status,201);return login(email);};
const state=async session=>{const r=await api('GET','/api/state',null,session);assert.equal(r.status,200);return r.data;};
const serviceBody={name:'Registrar Documents',description:'Official document requests',requirementsText:'Student ID',iconType:'DOCUMENT_REQUEST',isAvailable:true,isQueueEnabled:true,isAppointmentEnabled:true,estimatedMinutes:10};
before(async()=>{await start();await app.service.bootstrap({fullName:'Authorized Operator',email:'admin@example.test',password:secret});admin=await login('admin@example.test');});
after(async()=>{await app.close();rmSync(directory,{recursive:true,force:true});});

test('health is non-sensitive and unauthenticated protected requests fail',async()=>{
 const h=await api('GET','/health');assert.deepEqual(Object.keys(h.data).sort(),['schema','service','status']);
 assert.equal((await api('GET','/api/state')).status,401);
 assert.equal((await api('GET','/api/state',null,{token:'forged'})).status,401);
});
test('administrator bootstrap is single-use; public signup cannot select role',async()=>{
 await assert.rejects(app.service.bootstrap({}),error=>error.code==='CONFLICT');
 assert.equal((await api('POST','/api/auth/register',{fullName:'Attacker',email:'attack@example.test',password:secret,studentId:'ATTACK',role:'ADMIN'})).status,400);
});
test('registration saves one student without a session; only explicit login grants access',async()=>{
 const body={fullName:'Registration Only Student',studentId:'REG-ONLY-001',email:'registration-only@example.test',password:secret};
 const sessionCount=app.service.one('SELECT count(*) AS count FROM sessions').count;
 const created=await api('POST','/api/auth/register',body);
 assert.equal(created.status,201);assert.deepEqual(Object.keys(created.data),['user']);
 assert.equal(created.data.user.role,'STUDENT');
 assert.equal(app.service.one('SELECT count(*) AS count FROM users WHERE email=?',body.email).count,1);
 assert.equal(app.service.one('SELECT count(*) AS count FROM sessions').count,sessionCount);
 assert.equal(app.service.one('SELECT count(*) AS count FROM sessions WHERE user_id=?',created.data.user.id).count,0);
 assert.equal((await api('GET','/api/session')).status,401);
 assert.equal((await api('GET','/api/state',null,{token:created.data.user.id})).status,401);
 assert.equal((await api('POST','/api/auth/register',body)).status,409);
 assert.equal(app.service.one('SELECT count(*) AS count FROM users WHERE email=?',body.email).count,1);
 assert.equal(app.service.one('SELECT count(*) AS count FROM sessions').count,sessionCount);
 const loggedIn=await login(body.email);
 assert.equal(loggedIn.user.id,created.data.user.id);assert.equal((await api('GET','/api/session',null,loggedIn)).status,200);
 assert.equal((await api('POST','/api/auth/logout',{},loggedIn)).status,200);
 assert.equal((await api('GET','/api/session',null,loggedIn)).status,401);
 assert.equal((await api('GET','/api/session',null,admin)).status,200);
 assert.equal(app.service.one('SELECT count(*) AS count FROM users WHERE email=?',body.email).count,1);
});
test('student registration persists securely and rejects duplicate identity',async()=>{
 student=await register(1);second=await register(2);
 assert.equal(student.user.role,'STUDENT');
 const row=app.service.one('SELECT * FROM users WHERE id=?',student.user.id);
 assert.notEqual(row.password_hash,secret);assert.ok(row.password_hash.startsWith('scrypt$32768$8$3$'));assert.ok(await passwordMatches(secret,row.password_hash));
 const r=await api('POST','/api/auth/register',{fullName:'Duplicate',email:'STUDENT1@example.test',password:secret,studentId:'other-123'});assert.equal(r.status,409);
 assert.equal((await api('POST','/api/auth/register',{fullName:'Duplicate',email:'other@example.test',password:secret,studentId:'S-00001'})).status,409);
});
test('invalid password and disabled accounts return generic errors',async()=>{
 const r=await api('POST','/api/auth/login',{email:'student1@example.test',password:'wrong-password'});assert.equal(r.status,401);assert.equal(r.data.error.code,'INVALID_CREDENTIALS');
 const missing=await api('POST','/api/auth/login',{email:'absent@example.test',password:'wrong-password'});assert.equal(missing.data.error.message,r.data.error.message);
});
test('students cannot create services or staff; administrator provisions staff',async()=>{
 assert.equal((await api('POST','/api/services',serviceBody,student)).status,403);
 const b={fullName:'Registrar Staff',email:'staff@example.test',password:secret,counterName:'Window 1',isActive:true};
 assert.equal((await api('POST','/api/staff',b,student)).status,403);
 assert.equal((await api('POST','/api/staff',b,admin)).status,201);
 staff=await login('staff@example.test');assert.equal(staff.user.role,'STAFF');
 assert.equal((await api('POST','/api/services',serviceBody,staff)).status,403);
 const r=await api('POST','/api/services',serviceBody,admin);assert.equal(r.status,201);serviceId=r.data.id;
});
test('one queue request is idempotent, allocated by server and shared with staff',async()=>{
 const key=randomUUID();
 const [a,b]=await Promise.all([api('POST','/api/tickets',{serviceId},student,key),api('POST','/api/tickets',{serviceId},student,key)]);
 assert.equal(a.status,201);assert.deepEqual(a.data,b.data);ticketId=a.data.id;
 assert.equal((await api('POST','/api/tickets',{serviceId},student,randomUUID())).status,409);
 assert.ok((await state(staff)).queueTickets.some(x=>x.id===ticketId));
 assert.ok(!(await state(second)).queueTickets.some(x=>x.id===ticketId));
 assert.equal((await api('POST','/api/tickets',{serviceId:'different'},student,key)).status,409);
});
test('ticket transitions enforce ownership, FIFO and optimistic version',async()=>{
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'COMPLETED',version:1},student)).status,403);
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'CANCELLED',version:1},second)).status,403);
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'COMPLETED',version:1},staff)).status,409);
 assert.equal((await api('POST','/api/tickets/next',{},staff)).status,200);
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'SERVING',version:1},staff)).status,409);
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'SERVING',version:2},staff)).status,200);
 assert.equal((await api('PATCH','/api/tickets/'+ticketId,{status:'COMPLETED',version:3},staff)).status,200);
 const s=await state(student);assert.equal(s.queueTickets.find(x=>x.id===ticketId).status,'COMPLETED');assert.ok(s.transactions.some(x=>x.referenceNumber===s.queueTickets[0].ticketNumber));
});
test('disabled services block queue and appointments',async()=>{
 const current=(await state(admin)).services.find(x=>x.id===serviceId);
 assert.equal((await api('PATCH','/api/services/'+serviceId,{...serviceBody,isAvailable:false,version:current.version},admin)).status,200);
 assert.equal((await api('POST','/api/tickets',{serviceId},student,randomUUID())).status,409);
 assert.equal((await api('POST','/api/appointments',{serviceId,dateText:'Jan 01, 2030',timeSlot:'9:00 AM'},student,randomUUID())).status,409);
 assert.equal((await api('PATCH','/api/services/'+serviceId,{...serviceBody,version:current.version+1},admin)).status,200);
});
test('appointment conflicts, status updates and rescheduling use real records',async()=>{
 const d=new Date();d.setUTCDate(d.getUTCDate()+3);while([0,6].includes(d.getUTCDay()))d.setUTCDate(d.getUTCDate()+1);
 const dateText=d.toLocaleDateString('en-US',{timeZone:'UTC',month:'short',day:'2-digit',year:'numeric'}),body={serviceId,dateText,timeSlot:'9:00 AM'},key=randomUUID();
 const r=await api('POST','/api/appointments',body,student,key);assert.equal(r.status,201);appointmentId=r.data.id;
 assert.deepEqual((await api('POST','/api/appointments',body,student,key)).data,r.data);
 assert.equal((await api('POST','/api/appointments',body,second,randomUUID())).status,409);
 assert.equal((await api('PATCH','/api/appointments/'+appointmentId,{status:'CONFIRMED',version:1},second)).status,403);
 assert.equal((await api('PATCH','/api/appointments/'+appointmentId,{status:'CONFIRMED',version:1},staff)).status,200);
 assert.equal((await state(student)).appointments[0].status,'CONFIRMED');
 assert.equal((await api('PATCH','/api/appointments/'+appointmentId,{...body,timeSlot:'10:00 AM',version:2},student)).status,200);
 assert.equal((await state(student)).appointments[0].status,'BOOKED');
});
test('unknown IDs never return fallback data; profile role escalation rejected',async()=>{
 assert.equal((await api('PATCH','/api/tickets/'+randomUUID(),{status:'CALLED',version:1},staff)).status,404);
 assert.equal((await api('PATCH','/api/profile',{fullName:'Student One',email:'student1@example.test',studentId:'S-00001',role:'ADMIN'},student)).status,400);
 assert.equal((await api('DELETE','/api/services/'+serviceId,null,admin)).status,409);
 const raw=JSON.stringify(await state(admin));assert.ok(!raw.includes('password_hash'));assert.ok(!raw.includes('token_hash'));assert.ok(!raw.includes(secret));
});
test('simultaneous queue allocation is unique and ordered',async()=>{
 const accounts=[];for(let n=10;n<18;n++)accounts.push(await register(n));
 const responses=await Promise.all(accounts.map(s=>api('POST','/api/tickets',{serviceId},s,randomUUID())));
 assert.ok(responses.every(r=>r.status===201));assert.equal(new Set(responses.map(x=>x.data.ticketNumber)).size,8);
 const tickets=(await state(staff)).queueTickets.filter(x=>x.status==='WAITING');
 assert.equal(tickets.length,8);assert.deepEqual(tickets.map(x=>x.peopleAhead),[0,1,2,3,4,5,6,7]);
 const last=tickets.at(-1);assert.equal((await api('PATCH','/api/tickets/'+last.id,{status:'CALLED',version:last.version},staff)).status,409);
});
test('account disabling revokes sessions; staff cannot administer settings',async()=>{
 assert.equal((await api('PATCH','/api/settings',{},staff)).status,403);
 const member=(await state(admin)).staffMembers[0];
 assert.equal((await api('PATCH','/api/staff/'+member.id,{fullName:member.fullName,email:member.email,counterName:member.counterName,isActive:false},admin)).status,200);
 assert.equal((await api('GET','/api/session',null,staff)).status,401);
 assert.equal((await api('POST','/api/auth/login',{email:member.email,password:secret})).status,401);
 await api('PATCH','/api/staff/'+member.id,{fullName:member.fullName,email:member.email,counterName:member.counterName,isActive:true},admin);staff=await login(member.email);
});
test('accounts, sessions, tickets, appointments and transactions survive server restart',async()=>{
 const before=await state(admin);await app.close();await start();
 const after=await state(admin);
 for(const key of ['services','queueTickets','appointments','transactions','staffMembers'])assert.equal(after[key].length,before[key].length);
 assert.equal((await state(student)).appointments[0].id,appointmentId);
 assert.equal(app.db.prepare('PRAGMA foreign_keys').get().foreign_keys,1);
 assert.equal(app.db.prepare('PRAGMA journal_mode').get().journal_mode,'wal');
});
test('logout and operator recovery invalidate sessions without pretending to email',async()=>{
 const session=await login('student2@example.test');
 assert.equal((await api('POST','/api/auth/logout',{},session)).status,200);
 assert.equal((await api('GET','/api/session',null,session)).status,401);
 const recovery=await api('POST','/api/auth/password-recovery',{email:'student1@example.test'});
 assert.match(recovery.data.message,/No email is sent/);
 await app.service.resetPassword(student.user.id,'Changed-'+randomUUID());
 assert.equal((await api('GET','/api/session',null,student)).status,401);
});
test('failed login attempts are throttled',async()=>{
 for(let n=0;n<5;n++)assert.equal((await api('POST','/api/auth/login',{email:'throttle@example.test',password:'wrong-password'})).status,401);
 assert.equal((await api('POST','/api/auth/login',{email:'throttle@example.test',password:'wrong-password'})).status,429);
});

test('expired sessions are rejected and unused services can be deleted',async()=>{
 const user=await register(99);
 app.service.run('UPDATE sessions SET expires_at=? WHERE user_id=?',Date.now()-1,user.user.id);
 assert.equal((await api('GET','/api/session',null,user)).status,401);
 const created=await api('POST','/api/services',{...serviceBody,name:'Unused disposable service'},admin);
 assert.equal(created.status,201);
 assert.equal((await api('DELETE','/api/services/'+created.data.id,null,admin)).status,200);
 assert.ok(!(await state(admin)).services.some(x=>x.id===created.data.id));
});
test('concurrent status changes and appointment reservations have one winner',async()=>{
 const a=await register(100),b=await register(101);
 const service=await api('POST','/api/services',{...serviceBody,name:'Concurrency service'},admin);
 const sid=service.data.id;
 const ticket=await api('POST','/api/tickets',{serviceId:sid},a,randomUUID());
 const changes=await Promise.all([api('PATCH','/api/tickets/'+ticket.data.id,{status:'CALLED',version:1},staff),api('PATCH','/api/tickets/'+ticket.data.id,{status:'CALLED',version:1},staff)]);
 assert.deepEqual(changes.map(x=>x.status).sort(),[200,409]);
 const date=new Date();date.setUTCDate(date.getUTCDate()+5);while([0,6].includes(date.getUTCDay()))date.setUTCDate(date.getUTCDate()+1);
 const body={serviceId:sid,dateText:date.toLocaleDateString('en-US',{timeZone:'UTC',month:'short',day:'2-digit',year:'numeric'}),timeSlot:'1:00 PM'};
 const appointments=await Promise.all([api('POST','/api/appointments',body,a,randomUUID()),api('POST','/api/appointments',body,b,randomUUID())]);
 assert.deepEqual(appointments.map(x=>x.status).sort(),[201,409]);
});
test('online SQLite backup preserves committed records without resetting the source',async()=>{
 const {backup,DatabaseSync}=await import('node:sqlite');
 const target=join(directory,'snapshot.sqlite');
 const before=app.service.one('SELECT count(*) AS n FROM users').n;
 await backup(app.db,target);
 const restored=new DatabaseSync(target,{readOnly:true});
 try {assert.equal(restored.prepare('SELECT count(*) AS n FROM users').get().n,before);
 assert.equal(restored.prepare('PRAGMA user_version').get().user_version,1);}
 finally{restored.close();}
 assert.equal(app.service.one('SELECT count(*) AS n FROM users').n,before);
});

test('availability reveals occupied slots without other students identities or appointment IDs',async()=>{
 // student was intentionally reset earlier; use a fresh authorized account to inspect shared availability.
 const viewer=await register(102);
 const data=await state(viewer);
 assert.ok(data.reservedSlots.length>0);
 assert.ok(data.reservedSlots.every(x=>x.ownAppointmentId===null));
 assert.ok(data.reservedSlots.every(x=>Object.keys(x).sort().join(',')==='dateText,ownAppointmentId,serviceId,timeSlot'));
 assert.ok(data.appointments.length===0);
});

test('waiting students see actual serving numbers and count a serving student ahead',async()=>{
 const waiting=app.service.all("SELECT * FROM queue_tickets WHERE service_id=? AND status='WAITING' ORDER BY created_at,sequence,id",serviceId);
 assert.ok(waiting.length>=2);
 const first=waiting[0],behind=waiting[1];
 assert.equal((await api('PATCH','/api/tickets/'+first.id,{status:'CALLED',version:first.version},staff)).status,200);
 assert.equal((await api('PATCH','/api/tickets/'+first.id,{status:'SERVING',version:first.version+1},staff)).status,200);
 const address=app.service.one('SELECT email FROM users WHERE id=?',behind.student_id).email;
 const view=await state(await login(address));
 assert.ok(view.queueTickets.find(x=>x.id===behind.id).peopleAhead>=1);
 assert.ok(view.nowServing.some(x=>x.serviceId===serviceId && x.ticketNumber===first.ticket_number));
 assert.ok(view.nowServing.every(x=>Object.keys(x).sort().join(',')==='serviceId,ticketNumber'));
});
