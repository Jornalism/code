import { openDatabase } from './database.mjs';
import { CampusQService } from './service.mjs';
import { createInterface } from 'node:readline/promises';
import { stdin,stdout } from 'node:process';
import { resolve } from 'node:path';

async function secret(prompt) {
 if(!stdin.isTTY)throw new Error('Use an interactive terminal. Passwords are never accepted as command-line arguments.');
 stdout.write(prompt);
 return new Promise((accept,reject)=>{
   let value='';
   stdin.setRawMode(true);stdin.resume();
   const handler=chunk=>{
     for(const ch of chunk.toString()){
       if(ch==='\u0003'){cleanup();reject(new Error('Cancelled.'));return;}
       if(ch==='\r'||ch==='\n'){cleanup();stdout.write('\n');accept(value);return;}
       if(ch==='\u007f'||ch==='\b')value=value.slice(0,-1);
       else if(ch>=' ')value+=ch;
     }
   };
   function cleanup(){stdin.off('data',handler);stdin.setRawMode(false);stdin.pause();}
   stdin.on('data',handler);
 });
}
const db=openDatabase(resolve(process.env.CAMPUSQ_DB??'backend/local/data/campusq.sqlite')),service=new CampusQService(db);
try {
 const command=process.argv[2];
 if(!['bootstrap-admin','reset-password'].includes(command))throw new Error('Use bootstrap-admin or reset-password.');
 const rl=createInterface({input:stdin,output:stdout});
 let fullName,address;
 if(command==='bootstrap-admin')fullName=await rl.question('Administrator full name: ');
 address=await rl.question('Account email: ');
 rl.close();
 const first=await secret('Password (input hidden, at least 8 characters): '),second=await secret('Confirm password: ');
 if(first!==second)throw new Error('Passwords do not match.');
 if(command==='bootstrap-admin')await service.bootstrap({fullName,email:address,password:first});
 else {
   const user=service.one('SELECT id FROM users WHERE email=?',address.trim().toLowerCase());
   if(!user)throw new Error('Account not found.');
   await service.resetPassword(user.id,first);
 }
 console.log('Operator action completed. No password is printed. Existing sessions were revoked for a reset.');
}catch(error){console.error(error instanceof Error && !error.code ? error.message : 'Operator action failed. Check the input and database.');process.exitCode=1;}
finally{db.close();}
