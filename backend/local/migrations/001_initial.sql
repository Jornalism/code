CREATE TABLE users (
 id TEXT PRIMARY KEY, full_name TEXT NOT NULL, email TEXT NOT NULL COLLATE NOCASE UNIQUE,
 password_hash TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('STUDENT','STAFF','ADMIN')),
 account_status TEXT NOT NULL CHECK(account_status IN ('ACTIVE','DISABLED')),
 created_at TEXT NOT NULL, updated_at TEXT NOT NULL
);
CREATE TABLE student_profiles(user_id TEXT PRIMARY KEY REFERENCES users(id),student_number TEXT NOT NULL UNIQUE);
CREATE TABLE staff_profiles(user_id TEXT PRIMARY KEY REFERENCES users(id),counter_name TEXT NOT NULL);
CREATE TABLE sessions(token_hash TEXT PRIMARY KEY,user_id TEXT NOT NULL REFERENCES users(id),expires_at INTEGER NOT NULL);
CREATE INDEX sessions_user ON sessions(user_id);
CREATE TABLE login_attempts(attempt_key TEXT PRIMARY KEY,failures INTEGER NOT NULL,window_start INTEGER NOT NULL);
CREATE TABLE registrar_services(
 id TEXT PRIMARY KEY,name TEXT NOT NULL UNIQUE,description TEXT NOT NULL,requirements TEXT NOT NULL,
 icon_type TEXT NOT NULL,available INTEGER NOT NULL CHECK(available IN (0,1)),
 queue_enabled INTEGER NOT NULL CHECK(queue_enabled IN (0,1)),appointment_enabled INTEGER NOT NULL CHECK(appointment_enabled IN (0,1)),
 estimated_minutes INTEGER NOT NULL CHECK(estimated_minutes BETWEEN 1 AND 240),
 version INTEGER NOT NULL DEFAULT 1,created_at TEXT NOT NULL,updated_at TEXT NOT NULL
);
CREATE TABLE queue_counters(service_id TEXT NOT NULL REFERENCES registrar_services(id),day TEXT NOT NULL,next_sequence INTEGER NOT NULL,PRIMARY KEY(service_id,day));
CREATE TABLE queue_tickets(
 id TEXT PRIMARY KEY,student_id TEXT NOT NULL REFERENCES users(id),service_id TEXT NOT NULL REFERENCES registrar_services(id),
 day TEXT NOT NULL,sequence INTEGER NOT NULL,ticket_number TEXT NOT NULL,
 status TEXT NOT NULL CHECK(status IN ('WAITING','CALLED','SERVING','COMPLETED','SKIPPED','NO_SHOW','CANCELLED','EXPIRED')),
 version INTEGER NOT NULL DEFAULT 1,created_at TEXT NOT NULL,called_at TEXT,started_at TEXT,completed_at TEXT,
 UNIQUE(service_id,day,sequence)
);
CREATE UNIQUE INDEX one_active_ticket ON queue_tickets(student_id) WHERE status IN ('WAITING','CALLED','SERVING','SKIPPED');
CREATE INDEX tickets_order ON queue_tickets(day,status,created_at,sequence,id);
CREATE TABLE appointments(
 id TEXT PRIMARY KEY,student_id TEXT NOT NULL REFERENCES users(id),service_id TEXT NOT NULL REFERENCES registrar_services(id),
 day TEXT NOT NULL,slot TEXT NOT NULL,
 status TEXT NOT NULL CHECK(status IN ('BOOKED','CONFIRMED','CHECKED_IN','SERVING','COMPLETED','CANCELLED','NO_SHOW')),
 version INTEGER NOT NULL DEFAULT 1,created_at TEXT NOT NULL,updated_at TEXT NOT NULL
);
CREATE UNIQUE INDEX service_slot ON appointments(service_id,day,slot) WHERE status IN ('BOOKED','CONFIRMED','CHECKED_IN','SERVING');
CREATE UNIQUE INDEX student_slot ON appointments(student_id,day,slot) WHERE status IN ('BOOKED','CONFIRMED','CHECKED_IN','SERVING');
CREATE TABLE transactions(
 id TEXT PRIMARY KEY,student_id TEXT NOT NULL REFERENCES users(id),staff_id TEXT NOT NULL REFERENCES users(id),
 ticket_id TEXT UNIQUE REFERENCES queue_tickets(id),appointment_id TEXT UNIQUE REFERENCES appointments(id),
 created_at TEXT NOT NULL,CHECK((ticket_id IS NULL)!=(appointment_id IS NULL))
);
CREATE TABLE notifications(id TEXT PRIMARY KEY,user_id TEXT NOT NULL REFERENCES users(id),title TEXT NOT NULL,message TEXT NOT NULL,created_at TEXT NOT NULL,is_read INTEGER NOT NULL DEFAULT 0);
CREATE INDEX notifications_owner ON notifications(user_id,created_at);
CREATE TABLE activity_logs(id INTEGER PRIMARY KEY,actor_id TEXT REFERENCES users(id),action TEXT NOT NULL,resource_id TEXT,created_at TEXT NOT NULL);
CREATE TABLE system_settings(id INTEGER PRIMARY KEY CHECK(id=1),data TEXT NOT NULL);
INSERT INTO system_settings VALUES(1,'{"institutionName":"CampusQ Registrar","operatingDays":"Monday – Friday","operatingHours":"8:00 AM – 5:00 PM","maxDailyQueueLimit":150,"pushAlertsEnabled":false,"smsFallbackEnabled":false}');
CREATE TABLE idempotency(user_id TEXT NOT NULL REFERENCES users(id),request_key TEXT NOT NULL,operation TEXT NOT NULL,payload_hash TEXT NOT NULL,response TEXT NOT NULL,PRIMARY KEY(user_id,request_key));
