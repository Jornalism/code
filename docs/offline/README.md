# CampusQ offline classroom environment

This is the existing CampusQ Android application connected to a laptop REST server and persistent SQLite. Normal operation needs the laptop server and local-network connectivity, not internet or Firebase. See the [completion report](../../CAMPUSQ_OFFLINE_COMPLETION_REPORT.md) and [screen checklist](../../CAMPUSQ_OFFLINE_PROGRESS.md) for actual verification coverage.

## Technology choice
The installed Node24.19.0 runtime includes HTTP/HTTPS, SQLite and scrypt. This project already uses JavaScript backend tools. A separate backend/local directory avoids new Ktor/Gradle/server dependencies in the APK and avoids npm installation at runtime. No existing Android dependency version was upgraded.
Primary references: [Node SQLite](https://nodejs.org/download/release/latest-v24.x/docs/api/sqlite.html), [Node crypto](https://nodejs.org/download/release/v24.22.1/docs/api/crypto.html), [OWASP scrypt parameters](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html), [Android Keystore](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec), [Android network configuration](https://developer.android.com/privacy-and-security/security-config).

## First setup on the Windows laptop
Open Android Studio Terminal in the CampusQ project root. Node24.12+ is required; the installed24.19.0 was tested. After initial Android dependencies are cached, no package download is needed to run the server.
```powershell
node --version
node backend/local/operator.mjs bootstrap-admin
```
Enter your administrator name, email and a new password in the interactive terminal. Password input is hidden. There is NO default administrator password. This local command only permits the first administrator; it is not exposed through HTTP. It creates/migrates backend/local/data/campusq.sqlite without recreating existing tables or deleting records.

Start the server in a terminal and leave it running:
```powershell
.\backend\local\Start-CampusQ.cmd
```
Equivalent: node backend/local/server.mjs. PowerShell .ps1 wrappers are also supplied, but this machine blocks unsigned scripts; use .cmd or node without changing execution policy. The default binds only127.0.0.1:8787. GET http://127.0.0.1:8787/health returns status/schema only. Ctrl+C stops the server cleanly. Do not run an acceptance fixture server and the presentation server on the same port.

## Android emulator
The default debug backend origin is http://10.0.2.2:8787 (Android emulator host mapping).
```powershell
.\gradlew.bat :app:assembleDebug
```
Run the app from Android Studio. The APK is app/build/outputs/apk/debug/app-debug.apk. campusq.firebase.enabled stays false by default. Do not enable Firebase for offline operation.

## Physical Android devices
1. Connect laptop and phones to the same isolated, trusted Wi-Fi/hotspot. A phone may report no internet; keep the local Wi-Fi connected.
2. Find the laptop's active IPv4 address using ipconfig.
3. Explicitly start LAN HTTP for presentation-only credentials:
```powershell
$env:CAMPUSQ_HOST='0.0.0.0'
$env:CAMPUSQ_ALLOW_LAN_HTTP='true'
.\backend\local\Start-CampusQ.cmd
.\gradlew.bat '-Pcampusq.backend.url=http://YOUR-LAPTOP-IP:8787' :app:assembleDebug
```
Replace YOUR-LAPTOP-IP with the actual address. Install that debug APK on each phone. The build generates a debug-only cleartext allowance scoped to that configured host plus loopback/emulator hosts. No changing laptop IP is hardcoded into source. Changing the origin requires rebuilding and signing in again; encrypted sessions are bound to one origin.
4. Windows Firewall may require a Private-network inbound rule for TCP8787/Node. Ask the laptop owner to approve and scope it to the local subnet. The agent does not change firewall rules. Disable AP/client isolation if it blocks peer access; this requires network-owner action.
5. Physical-device connectivity is not verified unless explicitly recorded in the progress report.

## Transport security
HTTP exposes passwords and bearer tokens to network observers. Use only an isolated trusted classroom network and non-reused presentation credentials. This configuration is not safe on a public/untrusted network.
The server optionally accepts CAMPUSQ_TLS_CERT and CAMPUSQ_TLS_KEY paths and uses HTTPS. Supply a properly trusted certificate matching the configured host. Android retains platform certificate and hostname validation; there is no trust-all manager. Certificate issuance/trust installation is not automatically configured. Release builds retain Android's normal HTTPS requirement; narrow HTTP allowances are debug-only.
Do not commit TLS private keys, SQLite files, fixture credentials or backups. Database files contain private account data and hashes; keep the laptop account, directory permissions, disk and backups protected.

## Operate the application
- Sign in as the administrator created by the operator command.
- Open Manage Services and add actual Registrar services; an empty database deliberately has no fabricated services, queues or appointments.
- Open Staff Management and add name/email/counter, an initial password (at least8 characters) and active status. Staff creation is restricted to authenticated ADMIN requests. There is no public role selector.
- Register Students with the existing registration form. Generic email and existing basic student-number length/uniqueness checks apply. Registration creates STUDENT only, shows Registration Successful and returns to Login without creating or saving a session. Enter the email and password manually to sign in. Registration is not proof of institutional enrollment.
- Staff are authorized for the Registrar office queue/appointments as a whole in this capstone. Counter names are recorded. No per-service staff assignment policy has been supplied.
- Student: choose an available service, request a ticket, view Queue Status. Staff: call the next waiting ticket, start service, complete it. Student status/history refresh through the same server.
- Student: book a future weekday slot. Staff: review/confirm/cancel and update valid appointment statuses. Student can cancel/reschedule editable bookings.
- Admin: inspect stored activity/accounts from the drawer, services, staff, queues, appointments, reports and settings. Reports use completed transaction records, not fabricated offsets.
- Profiles allow own name/email/student-number correction, not role/status edits.
- Sign out revokes the server session when reachable and clears the local encrypted credential. If server revocation cannot be confirmed, the app says so; the server token expires after12 hours.

## Rules and storage
See backend/local/migrations/001_initial.sql for the schema and backend/local/service.mjs for validation.
- Stable UUID user IDs own records; student numbers are not authentication/ownership keys.
- Passwords: random salt, scrypt N32768/r8/p3, timing-safe verification. No plaintext/hash in API responses.
- Sessions:32 random bytes, SHA256 digest stored on server,12-hour expiry; server checks active account on every protected request. Disabling staff/resetting password revokes sessions.
- Android bearer credential: Keystore AES-GCM encryption, origin-associated data; excluded from backup/device transfer. No password persistence.
- SQLite: foreign keys, WAL, full synchronous commits,5-second busy timeout, transactional versioned migrations. No destructive startup recreation.
- Queue identity: service/day/sequence. One active ticket per Student, atomic counter allocation, FIFO per service, unique index and idempotency keys. Staff mutations use optimistic versions.
- Appointments: one active reservation per service/day/slot and per Student/day/slot; server validation and transactions. Display calendar uses weekday slots9,10,11AM and1,2PM. Business timezone is Asia/Manila.
- Operating-day/hour strings are descriptive settings; the booking policy remains those fixed weekday slots. Daily queue limit is enforced. Cloud push/SMS are unavailable; in-app polling is active only while the app is started.
- A network failure is never treated as a saved mutation. Retried queue/booking requests reuse their request key within the client session.

## Password recovery
No email service is configured and no email is claimed to be sent. An authorized laptop operator must verify the person's identity, then run:
```powershell
node backend/local/operator.mjs reset-password
```
Enter the account email and a new password interactively. This resets the hash and revokes sessions. Do not paste passwords into chat or use command-line password arguments.

## Back up and restore
Create a private backup with SQLite's online backup API (includes committed WAL content):
```powershell
.\backend\local\Backup-CampusQ.cmd C:\PrivateBackups\campusq.sqlite-backup
```
Create the destination directory first. Existing backup files are never overwritten.
Before an upgrade, make a backup. To restore, stop the server and retain the current database as a separate backup; point CAMPUSQ_DB to a copy of the chosen backup, then start the matching server version. Never overwrite a running database or delete WAL files as a debugging shortcut. Downgrading to an older schema requires a compatible pre-upgrade backup; no destructive down-migration is provided.

## Tests without production accounts
```powershell
node --test --test-concurrency=1 backend/local/acceptance.test.mjs
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The backend acceptance suite uses a temporary directory/database and real HTTP. Android integration requires an explicitly isolated fixture:
```powershell
$env:CAMPUSQ_TEST_FIXTURE='true'
node backend/local/fixture.mjs
```
In another terminal run all available offline Android tests:
```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.localBackend=true' --max-workers=1 --console=plain
```
Historical Firebase-only cases intentionally skip without their explicit cloud/emulator opt-ins. For a focused run, add '-Pandroid.testInstrumentationRunnerArguments.class=com.example.campusq.LocalBackendUiTest,com.example.campusq.LocalBackendIntegrationTest,com.example.campusq.LocalMainActivityTest'. The fixture generates random local test credentials into ignored files and an androidTest-only asset, never the application APK or source history. Its database is separate from campusq.sqlite. Use fixture.mjs --resume to restart that same acceptance database without resets.
When opening a copied checkout, preserve the old acceptance-fixture.json before creating a fresh fixture. The --resume command rejects database paths outside the current checkout's backend/local/data directory; copied absolute paths must never reopen the original project's database. Keep the copied SQLite/WAL files intact. Run fixture commands from the authorized working-copy root.

The historical Firebase emulator tests remain available but are not required for offline workflows. No Firebase cloud operation is part of these commands.

## API overview
All protected routes require Authorization: Bearer <session>. JSON request/response, no redirects/cookies/CORS.
Public: POST /api/auth/register, /api/auth/login, /api/auth/password-recovery; GET /health.
Session: GET /api/session; POST /api/auth/logout.
Shared role-scoped data: GET /api/state.
Student: POST /api/tickets and /api/appointments (Idempotency-Key required); PATCH /api/profile.
Student/Staff/Admin with ownership/role checks: PATCH /api/tickets/{id}, /api/appointments/{id}; status updates require version.
Staff/Admin: POST /api/tickets/next.
Admin: POST /api/services, PATCH/DELETE /api/services/{id}, POST /api/staff, PATCH /api/staff/{id}, PATCH /api/settings.
Own inbox: POST /api/notifications/read.
Responses use structured error code/message, appropriate400/401/403/404/409/429/5xx; SQL/exception internals and credentials are not exposed.

## Troubleshooting and presentation sequence
- Connection refused: verify the server terminal, /health, correct origin, same network and firewall. 10.0.2.2 is emulator-only; use laptop LAN IPv4 on phones.
-401: expired/revoked/disabled session; sign in again.403: account lacks permission.409: refresh and resolve existing active ticket/slot/stale version.429: wait before retrying.
- Use separate devices/sessions for Student/Staff/Admin. Check the actual final acceptance report for which multi-client scenarios were run.
- Present registration → actual Student ticket → Staff call/start/complete → Student history → appointment/review → Admin service/staff update.
- Restart the app and server with the same database file, reconnect, and show the records still exist.
- Previously reported PixelCopy capture timeouts are not authentication success/failure evidence. Preserve failing results and distinguish capture failures from UI assertions.
