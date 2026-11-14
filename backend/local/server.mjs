import http from 'node:http';
import https from 'node:https';
import { readFileSync } from 'node:fs';

async function bodyOf(req) {
 requireValue((req.headers['content-type']??'').split(';')[0]==='application/json','Use application/json.',415,'CONTENT_TYPE');
 let size=0,parts=[];
 for await(const chunk of req){size+=chunk.length;requireValue(size<=32768,'Request too large.',413,'TOO_LARGE');parts.push(chunk);}
 try{return JSON.parse(Buffer.concat(parts).toString('utf8'));}
 catch(error){if(error instanceof ApiError)throw error;throw new ApiError(400,'INVALID_JSON','Invalid JSON request.');}
}
   try {
     requireValue(pending<=64,'Server is busy.',503,'BUSY');
     requireValue(!req.headers.origin,'Browser cross-origin requests are not supported.',403,'FORBIDDEN');
     const path=new URL(req.url,'http://localhost').pathname,method=req.method;
     if(path==='/health' && method==='GET'){db.prepare('SELECT 1').get();return send(200,{status:'ok',service:'CampusQ',schema:db.prepare('PRAGMA user_version').get().user_version});}
     if(path==='/api/auth/register' && method==='POST')return send(201,{user:await service.createUser(await bodyOf(req))});
     if(path==='/api/auth/login' && method==='POST')return send(200,await service.login(await bodyOf(req),req.socket.remoteAddress));
     if(path==='/api/auth/password-recovery' && method==='POST'){
       const body=await bodyOf(req);fields(body,['email']);
       return send(200,{message:'No email is sent by this offline server. Contact the authorized Registrar operator for identity verification and password recovery.'});
     }
     const token=(req.headers.authorization??'').replace(/^Bearer /,'');
     const user=service.authenticate(token),key=req.headers['idempotency-key'];
     if(path==='/api/session' && method==='GET')return send(200,{user:service.safeUser(user)});
     if(path==='/api/auth/logout' && method==='POST'){service.logout(token);return send(200,{signedOut:true});}
     if(path==='/api/state' && method==='GET')return send(200,service.state(user));
     if(path==='/api/tickets' && method==='POST')return send(201,service.createTicket(user,key,await bodyOf(req)));
     if(path==='/api/tickets/next' && method==='POST')return send(200,service.nextTicket(user));
     let match;
     if((match=path.match(/^\/api\/tickets\/([A-Za-z0-9-]+)$/)) && method==='PATCH')return send(200,service.changeTicket(user,match[1],await bodyOf(req)));
     if(path==='/api/appointments' && method==='POST')return send(201,service.createAppointment(user,key,await bodyOf(req)));
     if((match=path.match(/^\/api\/appointments\/([A-Za-z0-9-]+)$/)) && method==='PATCH')return send(200,service.changeAppointment(user,match[1],await bodyOf(req)));
     if(path==='/api/services' && method==='POST')return send(201,service.saveService(user,null,await bodyOf(req)));
     if((match=path.match(/^\/api\/services\/([A-Za-z0-9-]+)$/))){
       if(method==='PATCH')return send(200,service.saveService(user,match[1],await bodyOf(req)));
       if(method==='DELETE')return send(200,service.deleteService(user,match[1]));
     }
  
   }catch(error){
     if(error instanceof ApiError)return send(error.status,{error:{code:error.code,message:error.message}});
     if(error.code?.startsWith('ERR_SQLITE') && /constraint|UNIQUE|FOREIGN KEY/i.test(error.message))return send(409,{error:{code:'CONFLICT',message:'This operation conflicts with an existing record. Refresh and check the booking or account details.'}});
     // Do not log request bodies, headers, tokens, SQL or raw exceptions.
     console.error('CampusQ request failed:',error.code==='ERR_SQLITE_ERROR'?'DATABASE_ERROR':'INTERNAL_ERROR');
     send(500,{error:{code:'SERVER_ERROR',message:'The operation could not be completed. Try again.'}});
   }finally{pending--;}
 };

if(process.argv[1] && import.meta.url===pathToFileURL(resolve(process.argv[1])).href){
 const host=process.env.CAMPUSQ_HOST??'127.0.0.1',port=Number(process.env.CAMPUSQ_PORT??8787);
 const cert=process.env.CAMPUSQ_TLS_CERT,key=process.env.CAMPUSQ_TLS_KEY;
 requireValue(Boolean(cert)===Boolean(key),'Provide both TLS certificate and key.');
 const tls=cert?{cert:readFileSync(cert),key:readFileSync(key)}:null;
 requireValue(tls || ['127.0.0.1','localhost','::1'].includes(host) || process.env.CAMPUSQ_ALLOW_LAN_HTTP==='true','LAN HTTP requires explicit CAMPUSQ_ALLOW_LAN_HTTP=true. Use an isolated trusted network and presentation-only credentials.');
 const app=createCampusQServer({databasePath:resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite'),tls});
 app.server.listen(port,host,()=>console.log('CampusQ local server listening on '+(tls?'https':'http')+'://'+host+':'+port));
 for(const signal of ['SIGINT','SIGTERM'])process.once(signal,()=>{app.close().then(()=>process.exit(0),()=>process.exit(1));});
}
