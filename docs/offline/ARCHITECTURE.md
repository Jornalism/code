# CampusQ local architecture

## Active application path
MainActivity → CampusQLocalApp → LocalSessionViewModel → LocalApi → laptop Node HTTP/HTTPS server → CampusQService → SQLite.

After the server validates the session, the server-confirmed STUDENT, STAFF or ADMIN role selects the existing CampusQWorkflowApp home. CampusQViewModel uses LocalCampusQRepository as its CampusQStateSource. Existing Compose components and routes remain in use. No dependency injection framework or server library was added to Android.

The installed Node24 runtime already supplies SQLite, HTTP/HTTPS and crypto. This avoided introducing a second Gradle server build and new downloaded runtime libraries. Server files live in backend/local and are not packaged in Android.

## Authentication and authorization
Public registration accepts only Student profile fields. Role is assigned STUDENT by the server; a client-supplied role is rejected. The first Administrator is created with the local interactive operator command, never through an HTTP bootstrap endpoint. ADMIN may create/disable Staff; no public privileged registration exists.

Password hashes use randomly salted scrypt N32768/r8/p3. Server sessions use random32-byte bearer credentials, store only SHA256 digests and expire after12 hours. Every protected request checks the current account status and role. Password reset and Staff disable revoke sessions. Failed login attempts are limited; hash work and simultaneous requests are bounded.

Android stores the bearer credential encrypted with an Android Keystore AES-GCM key, bound to the backend origin and excluded from backup/transfer. Passwords remain transient. Requests capture the identity credential before moving to the IO dispatcher; redirects are disabled. There is no password/token logging or custom Firebase token persistence.

Sign-out clears the protected navigation composition and per-session ViewModelStore. Back cannot restore it. An expired/revoked server session clears identity. Failed network operations never report fabricated saved data. An offline sign-out reports that remote revocation could not be confirmed.

## Data isolation and synchronization
GET /api/state provides only the Student's own private tickets, appointments, transactions, profile and notifications. Staff receive office-wide operational records; ADMIN additionally receives account/activity information. Slot occupancy and current serving numbers expose scheduling/queue metadata without other Student names or identifiers.

Polling runs every4 seconds while the application's lifecycle is STARTED. Mutations and refreshes are serialized in the ViewModel; buttons are blocked during a request. Errors and connection loss are displayed. Last received records may remain visible during a connection outage, accompanied by the connection warning; mutations still require the server.

Queue/appointment POST retries reuse idempotency keys within the running client session. Server records the key, operation and payload digest transactionally. Tickets, appointments and services use optimistic versions for conflicting updates. No Android-only shared queue allocator exists.

## Firebase and Demo preservation
Firebase adapters, configuration, opt-in campusq.firebase.enabled and prior tests/documentation remain available for historical work. MainActivity does not use Firebase for the offline workflow, and no cloud operation is performed by the local server.

DemoRepository, original Demo navigation and previews remain in source for regression/reference testing. MainActivity provides no Demo role-selection entry or restore-sample path. Demo records never populate LocalCampusQRepository. Legacy tests explicitly opening CampusQDemoApp test that isolated path, not live authorization.

## Security and scope limits
The default server binds loopback. LAN HTTP requires an explicit opt-in and is appropriate only for an isolated trusted presentation network with non-reused credentials. HTTPS is supported with valid, trusted host certificates; issuance/trust setup is not automated. Certificate validation is never disabled. There is no claim of safety on public Wi-Fi.

The laptop operator has access to the database and provisioning commands and must be trusted. Protect its account, filesystem and backups. There is no enterprise IAM, remote recovery email, SMS, push service or internet dependency. Initial registration does not verify institutional enrollment. Staff have office-wide permissions until an approved service-assignment policy exists.

See README.md for installation, API routes, recovery, backups and troubleshooting; SCHEMA.md for persistence; PRESENTATION.md for the operator runbook.
