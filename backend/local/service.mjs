import { randomUUID } from 'node:crypto';
import { transaction } from './database.mjs';
import { ApiError, requireValue, text, email, password, passwordHash, passwordMatches, digest, newToken, fields } from './security.mjs';

const queueTransitions = {
 WAITING:['CALLED','CANCELLED','EXPIRED'], CALLED:['SERVING','SKIPPED','NO_SHOW','CANCELLED'],
 SERVING:['COMPLETED','NO_SHOW'], SKIPPED:['CALLED','CANCELLED','EXPIRED']
};
const appointmentTransitions = {
 BOOKED:['CONFIRMED','CHECKED_IN','CANCELLED'], CONFIRMED:['CHECKED_IN','CANCELLED','NO_SHOW'],
 CHECKED_IN:['SERVING','NO_SHOW'], SERVING:['COMPLETED']
};
const activeQueue = ['WAITING','CALLED','SERVING','SKIPPED'];
const timeSlots = ['9:00 AM','10:00 AM','11:00 AM','1:00 PM','2:00 PM'];
const iso = () => new Date().toISOString();
const dateParts = date => Object.fromEntries(new Intl.DateTimeFormat('en-US',{timeZone:'Asia/Manila',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',hourCycle:'h23'}).formatToParts(date).map(x=>[x.type,x.value]));
const dayNow = () => { const p=dateParts(new Date());return p.year+'-'+p.month+'-'+p.day; };
const dateText = day => new Date(day+'T00:00:00Z').toLocaleDateString('en-US',{timeZone:'UTC',month:'short',day:'2-digit',year:'numeric'});
const clockText = stamp => stamp ? new Date(stamp).toLocaleTimeString('en-US',{timeZone:'Asia/Manila',hour:'numeric',minute:'2-digit'}) : null;

export class CampusQService {
 constructor(db) { this.db=db; this.dummyHash=passwordHash(newToken()); this.pendingHashes=0; }
 one(sql,...args){return this.db.prepare(sql).get(...args);}
 all(sql,...args){return this.db.prepare(sql).all(...args);}
 run(sql,...args){return this.db.prepare(sql).run(...args);}
 audit(actor,action,id){this.run('INSERT INTO activity_logs(actor_id,action,resource_id,created_at) VALUES(?,?,?,?)',actor,action,id,iso());}
 notify(uid,title,message){this.run('INSERT INTO notifications VALUES(?,?,?,?,?,0)',randomUUID(),uid,title,message,iso());}
 role(user,...roles){requireValue(user && roles.includes(user.role),'This operation is not permitted.',403,'FORBIDDEN');}
 safeUser(row){return {id:row.id,fullName:row.full_name,email:row.email,role:row.role,accountStatus:row.account_status};}
 async hash(value) {
   requireValue(this.pendingHashes<4,'Authentication is busy. Try again shortly.',429,'THROTTLED');
   this.pendingHashes++;try{return await passwordHash(value);}finally{this.pendingHashes--;}
 }
 async createUser(body,role='STUDENT',actor=null) {
   fields(body,role==='STUDENT'?['fullName','email','password','studentId']:['fullName','email','password','counterName','isActive']);
   const name=text(body.fullName,'full name',3,120), address=email(body.email), secret=password(body.password);
   const student=role==='STUDENT'?text(body.studentId,'student ID',5,60):null;
   const counter=role==='STAFF'?text(body.counterName,'counter',1,80):null;
   if(role==='STAFF')requireValue(typeof body.isActive==='boolean','Choose staff account status.');
   const encoded=await this.hash(secret), id=randomUUID(), now=iso();
   return transaction(this.db,()=>{
     requireValue(!this.one('SELECT id FROM users WHERE email=?',address),'Account details are already registered.',409,'DUPLICATE');
     if(student) requireValue(!this.one('SELECT user_id FROM student_profiles WHERE student_number=?',student),'Account details are already registered.',409,'DUPLICATE');
     this.run('INSERT INTO users VALUES(?,?,?,?,?,?,?,?)',id,name,address,encoded,role,body.isActive===false?'DISABLED':'ACTIVE',now,now);
     if(student)this.run('INSERT INTO student_profiles VALUES(?,?)',id,student);
     if(counter)this.run('INSERT INTO staff_profiles VALUES(?,?)',id,counter);
     this.audit(actor,'ACCOUNT_CREATED',id);
     return this.safeUser(this.one('SELECT * FROM users WHERE id=?',id));
   });
 }
 async bootstrap(body) {
   requireValue(!this.one("SELECT id FROM users WHERE role='ADMIN'"),'An administrator already exists.',409,'CONFLICT');
   // CLI-only method, never routed over HTTP.
   const name=text(body.fullName,'full name',3,120), address=email(body.email), encoded=await this.hash(password(body.password));
   return transaction(this.db,()=>{
     requireValue(!this.one("SELECT id FROM users WHERE role='ADMIN'"),'An administrator already exists.',409,'CONFLICT');
     const id=randomUUID(), now=iso();
     this.run('INSERT INTO users VALUES(?,?,?,?,?,?,?,?)',id,name,address,encoded,'ADMIN','ACTIVE',now,now);
     this.audit(id,'ADMIN_BOOTSTRAP',id);
     return this.safeUser(this.one('SELECT * FROM users WHERE id=?',id));
   });
 }
 async login(body,ip) {
   fields(body,['email','password']);
   const address=email(body.email);
   requireValue(typeof body.password==='string' && body.password.length<=256,'Invalid credentials.',401,'INVALID_CREDENTIALS');
   const key=digest(address+'|'+ip), now=Date.now();
   const attempt=this.one('SELECT * FROM login_attempts WHERE attempt_key=?',key);
   requireValue(!attempt || now-attempt.window_start>900000 || attempt.failures<5,'Too many attempts. Try again later.',429,'THROTTLED');
   const user=this.one('SELECT * FROM users WHERE email=?',address);
   requireValue(this.pendingHashes<4,'Authentication is busy. Try again shortly.',429,'THROTTLED');
   this.pendingHashes++;
   let valid;
   try { valid=await passwordMatches(body.password,user?.password_hash ?? await this.dummyHash); }
   finally{this.pendingHashes--;}
   if(!valid || !user || user.account_status!=='ACTIVE') {
     this.run('INSERT INTO login_attempts VALUES(?,1,?) ON CONFLICT(attempt_key) DO UPDATE SET failures=CASE WHEN ?-window_start>900000 THEN 1 ELSE failures+1 END,window_start=CASE WHEN ?-window_start>900000 THEN ? ELSE window_start END',key,now,now,now,now);
     throw new ApiError(401,'INVALID_CREDENTIALS','Email or password is incorrect, or the account is unavailable.');
   }
   // Check again after asynchronous hashing: an operator could disable/reset this account meanwhile.
   const current=this.one('SELECT * FROM users WHERE id=?',user.id);
   requireValue(current.account_status==='ACTIVE' && current.password_hash===user.password_hash,'Account unavailable.',401,'INVALID_CREDENTIALS');
   this.run('DELETE FROM login_attempts WHERE attempt_key=?',key);
   this.run('DELETE FROM sessions WHERE expires_at<?',now);
   const token=newToken(),expiresAt=now+12*60*60*1000;
   this.run('INSERT INTO sessions VALUES(?,?,?)',digest(token),user.id,expiresAt);
   return {token,expiresAt,user:this.safeUser(current)};
 }
 authenticate(token) {
   requireValue(typeof token==='string' && /^[A-Za-z0-9_-]{43}$/.test(token),'Please sign in.',401,'UNAUTHENTICATED');
   const user=this.one('SELECT u.* FROM sessions s JOIN users u ON u.id=s.user_id WHERE s.token_hash=? AND s.expires_at>? AND u.account_status=?',digest(token),Date.now(),'ACTIVE');
   requireValue(user,'Session expired. Please sign in.',401,'UNAUTHENTICATED');
   return user;
 }
 logout(token){this.run('DELETE FROM sessions WHERE token_hash=?',digest(token));}
 async resetPassword(uid,secret) {
   const encoded=await this.hash(password(secret));
   transaction(this.db,()=>{
     requireValue(this.one('SELECT id FROM users WHERE id=?',uid),'Account not found.',404,'NOT_FOUND');
     this.run('UPDATE users SET password_hash=?,updated_at=? WHERE id=?',encoded,iso(),uid);
     this.run('DELETE FROM sessions WHERE user_id=?',uid);
     this.audit(null,'OPERATOR_PASSWORD_RESET',uid);
   });
 }
 settings(){return JSON.parse(this.one('SELECT data FROM system_settings WHERE id=1').data);}
 serviceRow(row){return {id:row.id,name:row.name,description:row.description,requirements:JSON.parse(row.requirements),iconType:row.icon_type,isAvailable:!!row.available,isQueueEnabled:!!row.queue_enabled,isAppointmentEnabled:!!row.appointment_enabled,estimatedMinutes:row.estimated_minutes,version:row.version};}
 requireService(id,operation) {
   const row=this.one('SELECT * FROM registrar_services WHERE id=?',text(id,'service'));
   requireValue(row,'Service not found.',404,'NOT_FOUND');
   requireValue(row.available && (operation==='queue'?row.queue_enabled:row.appointment_enabled),'This service is unavailable.',409,'SERVICE_UNAVAILABLE');
   return row;
 }
 idempotent(user,key,operation,body,action) {
   requireValue(typeof key==='string' && /^[A-Za-z0-9-]{16,80}$/.test(key),'A valid idempotency key is required.');
   const hash=digest(JSON.stringify(body));
   return transaction(this.db,()=>{
     const previous=this.one('SELECT * FROM idempotency WHERE user_id=? AND request_key=?',user.id,key);
     if(previous){requireValue(previous.operation===operation && previous.payload_hash===hash,'Request key reused with different data.',409,'CONFLICT');return JSON.parse(previous.response);}
     const result=action();
     this.run('INSERT INTO idempotency VALUES(?,?,?,?,?)',user.id,key,operation,hash,JSON.stringify(result));
     return result;
   });
 }
 ticketView(row,all) {
   const ahead=activeQueue.includes(row.status)?all.filter(x=>x.service_id===row.service_id && x.day===row.day && ['WAITING','CALLED','SERVING'].includes(x.status) && (x.created_at<row.created_at || (x.created_at===row.created_at && x.sequence<row.sequence))).length:0;
   return {id:row.id,ticketNumber:row.ticket_number,serviceId:row.service_id,serviceName:row.service_name,studentName:row.student_name,studentId:row.student_number,status:row.status,positionInLine:activeQueue.includes(row.status)?ahead+1:0,peopleAhead:ahead,estimatedWaitMinutes:ahead*row.estimated_minutes,timeJoined:clockText(row.created_at),waitingDurationText:Math.max(0,Math.floor((Date.parse(row.started_at??row.completed_at??iso())-Date.parse(row.created_at))/60000))+' min',startTime:clockText(row.started_at),completedTime:clockText(row.completed_at),version:row.version};
 }
 tickets() {return this.all('SELECT q.*,u.full_name AS student_name,p.student_number,s.name AS service_name,s.estimated_minutes FROM queue_tickets q JOIN users u ON u.id=q.student_id JOIN student_profiles p ON p.user_id=u.id JOIN registrar_services s ON s.id=q.service_id ORDER BY q.created_at,q.sequence,q.id');}
 createTicket(user,key,body) {
   this.role(user,'STUDENT');fields(body,['serviceId']);
   return this.idempotent(user,key,'ticket',body,()=>{
     const service=this.requireService(body.serviceId,'queue'),day=dayNow();
     requireValue(!this.one("SELECT id FROM queue_tickets WHERE student_id=? AND status IN ('WAITING','CALLED','SERVING','SKIPPED')",user.id),'You already have an active ticket.',409,'ACTIVE_TICKET');
     requireValue(this.one('SELECT count(*) AS n FROM queue_tickets WHERE day=?',day).n<this.settings().maxDailyQueueLimit,'Daily queue capacity reached.',409,'CAPACITY');
     this.run('INSERT INTO queue_counters VALUES(?,?,1) ON CONFLICT DO NOTHING',service.id,day);
     const sequence=this.one('UPDATE queue_counters SET next_sequence=next_sequence+1 WHERE service_id=? AND day=? RETURNING next_sequence-1 AS n',service.id,day).n;
     const id=randomUUID(), number='Q-'+service.id.slice(0,6).toUpperCase()+'-'+day.replaceAll('-','')+'-'+String(sequence).padStart(3,'0');
     this.run("INSERT INTO queue_tickets(id,student_id,service_id,day,sequence,ticket_number,status,created_at) VALUES(?,?,?,?,?,?,'WAITING',?)",id,user.id,service.id,day,sequence,number,iso());
     this.audit(user.id,'TICKET_CREATED',id);this.notify(user.id,'Queue ticket created',number+' is waiting.');
     return {id,ticketNumber:number};
   });
 }
 changeTicket(user,id,body) {
   fields(body,['status','version']);
   return transaction(this.db,()=>{
     const row=this.one('SELECT * FROM queue_tickets WHERE id=?',id);
     requireValue(row,'Ticket not found.',404,'NOT_FOUND');
     if(user.role==='STUDENT') requireValue(row.student_id===user.id && body.status==='CANCELLED','This ticket action is not permitted.',403,'FORBIDDEN');
     else this.role(user,'STAFF','ADMIN');
     requireValue(Number.isInteger(body.version) && row.version===body.version,'Ticket changed. Refresh and try again.',409,'STALE_VERSION');
     requireValue(queueTransitions[row.status]?.includes(body.status),'Invalid ticket status transition.',409,'INVALID_TRANSITION');
     if(body.status==='CALLED') {
       const first=this.one("SELECT id FROM queue_tickets WHERE service_id=? AND day=? AND status='WAITING' ORDER BY created_at,sequence,id LIMIT 1",row.service_id,row.day);
       requireValue(row.status==='SKIPPED' || first?.id===row.id,'Call the first waiting ticket for this service.',409,'QUEUE_ORDER');
     }
     const now=iso();
     this.run('UPDATE queue_tickets SET status=?,version=version+1,called_at=CASE WHEN ?=\'CALLED\' THEN ? ELSE called_at END,started_at=CASE WHEN ?=\'SERVING\' THEN ? ELSE started_at END,completed_at=CASE WHEN ? IN (\'COMPLETED\',\'NO_SHOW\',\'CANCELLED\',\'EXPIRED\') THEN ? ELSE completed_at END WHERE id=?',body.status,body.status,now,body.status,now,body.status,now,id);
     if(body.status==='COMPLETED')this.run('INSERT INTO transactions VALUES(?,?,?,?,?,?)',randomUUID(),row.student_id,user.id,id,null,now);
     this.audit(user.id,'TICKET_'+body.status,id);this.notify(row.student_id,'Queue status updated',row.ticket_number+': '+body.status);
     return {id};
   });
 }
 nextTicket(user) {
   this.role(user,'STAFF','ADMIN');
   // No await between selection and transition: synchronous transaction is serialized by SQLite.
   const row=this.one("SELECT id,version FROM queue_tickets WHERE status='WAITING' AND day=? ORDER BY created_at,sequence,id LIMIT 1",dayNow());
   requireValue(row,'No waiting tickets.',409,'EMPTY_QUEUE');
   return this.changeTicket(user,row.id,{status:'CALLED',version:row.version});
 }
 validateSlot(body) {
   const display=text(body.dateText,'appointment date',8,30);
   const parsed=new Date(display+' 00:00:00 GMT');
   requireValue(!Number.isNaN(parsed.getTime()),'Choose a valid date.');
   const day=parsed.toISOString().slice(0,10), today=dayNow();
   requireValue(dateText(day)===display && day>=today && ![0,6].includes(parsed.getUTCDay()),'Choose a present or future weekday.');
   requireValue(timeSlots.includes(body.timeSlot),'Choose a valid time slot.');
   if(day===today){const p=dateParts(new Date()),hours={'9:00 AM':9,'10:00 AM':10,'11:00 AM':11,'1:00 PM':13,'2:00 PM':14};requireValue(hours[body.timeSlot]*60>Number(p.hour)*60+Number(p.minute),'Choose a future time.');}
   return day;
 }
 appointmentView(row){return {id:row.id,referenceNumber:'APT-'+row.id.slice(0,8).toUpperCase(),serviceId:row.service_id,serviceName:row.service_name,studentName:row.student_name,studentId:row.student_number,dateText:dateText(row.day),timeSlot:row.slot,status:row.status,isToday:row.day===dayNow(),version:row.version};}
 appointments(){return this.all('SELECT a.*,u.full_name AS student_name,p.student_number,s.name AS service_name FROM appointments a JOIN users u ON u.id=a.student_id JOIN student_profiles p ON p.user_id=u.id JOIN registrar_services s ON s.id=a.service_id ORDER BY a.day,a.slot,a.id');}
 createAppointment(user,key,body) {
   this.role(user,'STUDENT');fields(body,['serviceId','dateText','timeSlot']);
   return this.idempotent(user,key,'appointment',body,()=>{
     const service=this.requireService(body.serviceId,'appointment'),day=this.validateSlot(body),id=randomUUID(),now=iso();
     this.run("INSERT INTO appointments VALUES(?,?,?,?,?,'BOOKED',1,?,?)",id,user.id,service.id,day,body.timeSlot,now,now);
     this.audit(user.id,'APPOINTMENT_BOOKED',id);this.notify(user.id,'Appointment requested',dateText(day)+' '+body.timeSlot);
     return {id};
   });
 }
 changeAppointment(user,id,body) {
   fields(body,['version','status','serviceId','dateText','timeSlot']);
   return transaction(this.db,()=>{
     const row=this.one('SELECT * FROM appointments WHERE id=?',id);
     requireValue(row,'Appointment not found.',404,'NOT_FOUND');
     requireValue(user.role!=='STUDENT' || row.student_id===user.id,'This appointment is not yours.',403,'FORBIDDEN');
     requireValue(Number.isInteger(body.version) && body.version===row.version,'Appointment changed. Refresh and retry.',409,'STALE_VERSION');
     if(body.serviceId!==undefined) {
       this.role(user,'STUDENT');requireValue(['BOOKED','CONFIRMED'].includes(row.status),'This appointment cannot be rescheduled.',409,'INVALID_TRANSITION');
       const service=this.requireService(body.serviceId,'appointment'),day=this.validateSlot(body);
       this.run("UPDATE appointments SET service_id=?,day=?,slot=?,status='BOOKED',version=version+1,updated_at=? WHERE id=?",service.id,day,body.timeSlot,iso(),id);
     } else {
       if(user.role==='STUDENT')requireValue(body.status==='CANCELLED' && ['BOOKED','CONFIRMED'].includes(row.status),'This action is not permitted.',403,'FORBIDDEN');
       else this.role(user,'STAFF','ADMIN');
       requireValue(appointmentTransitions[row.status]?.includes(body.status),'Invalid appointment status transition.',409,'INVALID_TRANSITION');
       this.run('UPDATE appointments SET status=?,version=version+1,updated_at=? WHERE id=?',body.status,iso(),id);
       if(body.status==='COMPLETED')this.run('INSERT INTO transactions VALUES(?,?,?,?,?,?)',randomUUID(),row.student_id,user.id,null,id,iso());
     }
     this.audit(user.id,'APPOINTMENT_UPDATED',id);this.notify(row.student_id,'Appointment updated','Your appointment status or schedule changed.');
     return {id};
   });
 }
 saveService(user,id,body) {
   this.role(user,'ADMIN');fields(body,['name','description','requirementsText','iconType','isAvailable','isQueueEnabled','isAppointmentEnabled','estimatedMinutes','version']);
   const name=text(body.name,'service name',3,100),description=text(body.description,'description',3,1000),req=text(body.requirementsText??'None','requirements',0,2000);
   requireValue(['DOCUMENT_REQUEST','ENROLLMENT_VERIFICATION','GOOD_MORAL','TRANSCRIPT','OTHER'].includes(body.iconType),'Invalid service icon.');
   for(const k of ['isAvailable','isQueueEnabled','isAppointmentEnabled'])requireValue(typeof body[k]==='boolean','Check service options.');
   requireValue(Number.isInteger(body.estimatedMinutes) && body.estimatedMinutes>=1 && body.estimatedMinutes<=240,'Estimated minutes must be 1–240.');
   return transaction(this.db,()=>{
     const now=iso(),key=id??randomUUID(),requirements=JSON.stringify(req.split('\n').map(x=>x.trim()).filter(Boolean));
     if(id){const old=this.one('SELECT * FROM registrar_services WHERE id=?',id);requireValue(old,'Service not found.',404,'NOT_FOUND');requireValue(body.version===old.version,'Service changed. Refresh and retry.',409,'STALE_VERSION');
       this.run('UPDATE registrar_services SET name=?,description=?,requirements=?,icon_type=?,available=?,queue_enabled=?,appointment_enabled=?,estimated_minutes=?,version=version+1,updated_at=? WHERE id=?',name,description,requirements,body.iconType,+body.isAvailable,+body.isQueueEnabled,+body.isAppointmentEnabled,body.estimatedMinutes,now,id);
     } else this.run('INSERT INTO registrar_services VALUES(?,?,?,?,?,?,?,?,?,1,?,?)',key,name,description,requirements,body.iconType,+body.isAvailable,+body.isQueueEnabled,+body.isAppointmentEnabled,body.estimatedMinutes,now,now);
     this.audit(user.id,'SERVICE_SAVED',key);return {id:key};
   });
 }
 deleteService(user,id) {
   this.role(user,'ADMIN');
   transaction(this.db,()=>{requireValue(this.one('SELECT id FROM registrar_services WHERE id=?',id),'Service not found.',404,'NOT_FOUND');requireValue(!this.one('SELECT id FROM queue_tickets WHERE service_id=? UNION ALL SELECT id FROM appointments WHERE service_id=? LIMIT 1',id,id),'Service has history; deactivate it instead.',409,'HAS_HISTORY');this.run('DELETE FROM queue_counters WHERE service_id=?',id);this.run('DELETE FROM registrar_services WHERE id=?',id);this.audit(user.id,'SERVICE_DELETED',id);});
   return {deleted:true};
 }
 updateStaff(user,id,body) {
   this.role(user,'ADMIN');fields(body,['fullName','email','counterName','isActive']);
   const name=text(body.fullName,'full name',3,120),address=email(body.email),counter=text(body.counterName,'counter',1,80);
   requireValue(typeof body.isActive==='boolean','Choose account status.');
   return transaction(this.db,()=>{
     requireValue(this.one("SELECT id FROM users WHERE id=? AND role='STAFF'",id),'Staff account not found.',404,'NOT_FOUND');
     this.run('UPDATE users SET full_name=?,email=?,account_status=?,updated_at=? WHERE id=?',name,address,body.isActive?'ACTIVE':'DISABLED',iso(),id);
     this.run('UPDATE staff_profiles SET counter_name=? WHERE user_id=?',counter,id);
     if(!body.isActive)this.run('DELETE FROM sessions WHERE user_id=?',id);
     this.audit(user.id,'STAFF_UPDATED',id);return {id};
   });
 }
 updateProfile(user,body) {
   this.role(user,'STUDENT');fields(body,['fullName','email','studentId']);
   const name=text(body.fullName,'full name',3,120),address=email(body.email),number=text(body.studentId,'student ID',5,60);
   return transaction(this.db,()=>{
     // Student number is an identifier, not proof of enrollment. Allow corrections until an institution policy is supplied.
     this.run('UPDATE users SET full_name=?,email=?,updated_at=? WHERE id=?',name,address,iso(),user.id);
     this.run('UPDATE student_profiles SET student_number=? WHERE user_id=?',number,user.id);
     this.audit(user.id,'PROFILE_UPDATED',user.id);return {saved:true};
   });
 }
 updateSettings(user,body) {
   this.role(user,'ADMIN');fields(body,['institutionName','operatingDays','operatingHours','maxDailyQueueLimit','pushAlertsEnabled','smsFallbackEnabled']);
   text(body.institutionName,'institution',3,200);text(body.operatingDays,'operating days',3,100);text(body.operatingHours,'operating hours',3,100);
   requireValue(Number.isInteger(body.maxDailyQueueLimit) && body.maxDailyQueueLimit>=1 && body.maxDailyQueueLimit<=10000,'Queue limit must be 1–10000.');
   requireValue(body.pushAlertsEnabled===false && body.smsFallbackEnabled===false,'Cloud push and SMS are not available offline. In-app updates use polling.');
   transaction(this.db,()=>{this.run('UPDATE system_settings SET data=? WHERE id=1',JSON.stringify(body));this.audit(user.id,'SETTINGS_UPDATED','1');});
   return {saved:true};
 }
 state(user) {
   const tickets=this.tickets(),appointments=this.appointments(),student=user.role==='STUDENT';
   const selected=tickets.filter(x=>!student||x.student_id===user.id), selectedAppointments=appointments.filter(x=>!student||x.student_id===user.id);
   const profile=this.one('SELECT * FROM student_profiles WHERE user_id=?',user.id);
   const staff=this.all("SELECT u.*,s.counter_name FROM users u JOIN staff_profiles s ON s.user_id=u.id WHERE u.role='STAFF'").map(x=>({id:x.id,fullName:x.full_name,email:x.email,roleTitle:'Registrar Staff',counterName:x.counter_name,isActive:x.account_status==='ACTIVE'}));
   const records=this.all('SELECT t.*,u.full_name AS student_name,s.name AS service_name,COALESCE(q.ticket_number,\'APT-\'||substr(a.id,1,8)) AS reference_number,q.status AS queue_status,a.status AS appointment_status,COALESCE(q.service_id,a.service_id) AS service_id FROM transactions t JOIN users u ON u.id=t.student_id LEFT JOIN queue_tickets q ON q.id=t.ticket_id LEFT JOIN appointments a ON a.id=t.appointment_id JOIN registrar_services s ON s.id=COALESCE(q.service_id,a.service_id) ORDER BY t.created_at DESC').filter(x=>!student||x.student_id===user.id).map(x=>({id:x.id,referenceNumber:x.reference_number,type:x.ticket_id?'QUEUE':'APPOINTMENT',serviceName:x.service_name,studentName:x.student_name,dateTimeText:new Date(x.created_at).toLocaleString('en-US',{timeZone:'Asia/Manila'}),queueStatus:x.queue_status,appointmentStatus:x.appointment_status,ownerId:x.student_id,serviceId:x.service_id}));
   const services=this.all('SELECT * FROM registrar_services ORDER BY name').map(x=>this.serviceRow(x));
   return {
     user:this.safeUser(user),serverTime:iso(),
     studentProfile:{id:user.id,fullName:user.full_name,studentId:profile?.student_number??'',email:user.email,program:'',yearLevel:''},
     currentStaffProfile:staff.find(x=>x.id===user.id)??{id:user.id,fullName:user.full_name,email:user.email,roleTitle:user.role,counterName:'Registrar',isActive:true},
     nowServing:tickets.filter(x=>x.status==='SERVING').map(x=>({serviceId:x.service_id,ticketNumber:x.ticket_number})),
     reservedSlots:appointments.filter(x=>['BOOKED','CONFIRMED','CHECKED_IN','SERVING'].includes(x.status) && x.day>=dayNow()).map(x=>({
       serviceId:x.service_id,dateText:dateText(x.day),timeSlot:x.slot,ownAppointmentId:x.student_id===user.id?x.id:null
     })),
     services,staffMembers:user.role==='ADMIN'?staff:[],
     queueTickets:selected.map(x=>this.ticketView(x,tickets)),
     studentTicketId:selected.find(x=>activeQueue.includes(x.status))?.id??null,
     appointments:selectedAppointments.map(x=>this.appointmentView(x)),transactions:records,
     notifications:this.all('SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 100',user.id).map(x=>({id:x.id,title:x.title,message:x.message,timestamp:clockText(x.created_at),isRead:!!x.is_read})),
     institutionSettings:this.settings(),
     users:user.role==='ADMIN'?this.all("SELECT u.id,u.full_name,u.email,u.role,u.account_status,p.student_number FROM users u LEFT JOIN student_profiles p ON p.user_id=u.id ORDER BY u.full_name"):[],
     activity:user.role==='ADMIN'?this.all('SELECT action,resource_id,created_at FROM activity_logs ORDER BY id DESC LIMIT 100'):[],
     analytics:[7,31,183].map(days=>services.map(s=>({serviceName:s.name,shortLabel:s.name.substring(0,12),count:records.filter(t=>t.serviceId===s.id && Date.parse(this.one('SELECT created_at FROM transactions WHERE id=?',t.id).created_at)>=Date.now()-days*86400000).length})))
   };
 }
}

