# CampusQ SQLite schema and invariants

Source of truth: backend/local/migrations/001_initial.sql. Schema version is PRAGMA user_version=1. Startup applies unapplied numbered migrations inside a transaction; it refuses an unsupported newer schema and never recreates an existing database.

## Tables
| Table | Identity and purpose |
| --- | --- |
| users | UUID primary key, unique case-insensitive email, name, password hash, server role STUDENT/STAFF/ADMIN, ACTIVE/DISABLED, created/updated UTC timestamps |
| student_profiles | User UUID FK/PK, unique student number; number is not an ownership or authorization key |
| staff_profiles | User UUID FK/PK, counter name |
| sessions | SHA256 token digest PK, user FK, expiry milliseconds; plaintext bearer credential is not stored |
| login_attempts | Hashed email/IP attempt key, failure count/window |
| registrar_services | UUID, unique name, description, JSON requirements, icon enum, availability/queue/appointment flags, estimate, version, timestamps |
| queue_counters | Composite service/day PK, next sequence |
| queue_tickets | UUID, Student/service FKs, day/sequence, displayed number, status/version, joined/called/started/completed times |
| appointments | UUID, Student/service FKs, day/slot, status/version, timestamps |
| transactions | UUID, Student/Staff FKs, exactly one unique ticket or appointment FK, completion timestamp |
| notifications | UUID, owner FK, title/message/timestamp/read flag |
| activity_logs | Integer PK, actor FK, action/resource/time; never credentials |
| system_settings | Single row, institution/day/hour descriptions, daily capacity, unsupported delivery flags false |
| idempotency | Composite user/request-key PK, operation/payload digest/serialized result |

Foreign keys are enabled on every connection. WAL, synchronous FULL and a5-second busy timeout support durable commits and contention handling. No production/sample account or service is seeded at startup. The only initial settings row contains configuration, not fabricated operational records.

## Queue allocation
A transaction checks Student identity, service availability, one-active-ticket restriction and daily capacity; allocates the service/day counter; inserts the ticket and request result; then commits. UNIQUE(service_id,day,sequence) is the database backstop. A partial unique Student index covers WAITING/CALLED/SERVING/SKIPPED.

Displayed ticket numbers contain a service UUID prefix, local date and sequence. Their authoritative uniqueness scope is the full service/day/sequence; the UUID ticket ID is the stable API identity. FIFO is enforced per service. Call-next selects today's earliest waiting record. Version checks reject simultaneous conflicting mutations.

Transitions:
- WAITING → CALLED, CANCELLED, EXPIRED
- CALLED → SERVING, SKIPPED, NO_SHOW, CANCELLED
- SERVING → COMPLETED, NO_SHOW
- SKIPPED → CALLED, CANCELLED, EXPIRED

A Student can only cancel their own eligible ticket; Staff/Admin process office tickets. Completion creates one transaction and a notification. Terminal records remain stored. Expiry is an explicit authorized transition; no unimplemented midnight expiry scheduler is claimed.

## Appointments
Business date/time zone: Asia/Manila. Current booking policy is future weekdays with9:00,10:00,11:00AM and1:00,2:00PM slots. Settings' operating-day/hour strings are descriptive and do not dynamically change this policy.

Partial unique indexes reserve one active service/day/slot and one active Student/day/slot across BOOKED/CONFIRMED/CHECKED_IN/SERVING. Transactional create/reschedule, validation, idempotency and version checks prevent conflicts even when requests race.

Transitions:
- BOOKED → CONFIRMED, CHECKED_IN, CANCELLED
- CONFIRMED → CHECKED_IN, CANCELLED, NO_SHOW
- CHECKED_IN → SERVING, NO_SHOW
- SERVING → COMPLETED

Student cancellation/rescheduling is limited to their own BOOKED/CONFIRMED records. Rescheduling returns to BOOKED for review. Staff/Admin perform authorized status changes. Completion creates a unique transaction. Shared occupancy metadata omits other Students' appointment IDs and personal data.

## Privacy, retention and maintenance
Student responses contain only owned private records. Roles and account status cannot be edited through profile APIs. Services with ticket/appointment history cannot be deleted; deactivate them instead. No automated record purge or account deletion is implemented.

SQLite files contain personal data and hashes and are not encrypted by this application. Use protected laptop storage/disk encryption and private backups. Database, WAL/SHM, backups, TLS keys and acceptance credentials are Git-ignored.

Use Backup-CampusQ.cmd (SQLite online backup API) before changes. Never copy only the live main file or delete WAL files. Restore by retaining the current file and starting with a separate backup copy via CAMPUSQ_DB. No destructive downgrade/reset is supplied.
