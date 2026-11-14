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
t.equal((await api('POST','/api/auth/register',{fullName:'Attacker',email:'attack@example.test',password:secret,studentId:'ATTACK',role:'ADMIN'})).status,400);

 student=await register(1);second=await register(2);
 as
 assert.ok(!(await state(second)).queueTickets.some(x=>x.id===ticketId));
 assert.equal((await api('POST','/api/tickets',{serviceId:'different'},student,key)).status,409);
});
test('ticket transitions enforce ownership, FIFO and optimistic version',async()=>{
 assert.equal((await a'+ticketId,{status:'SERVING',version:2},staff)).status,200);
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

