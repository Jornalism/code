# CampusQ offline implementation and verification report
Date: 2026-11-13 (Asia/Manila)

## Status
**IMPLEMENTED:** persistent local-server workflows behind the existing CampusQ application.
**TESTED:** actual HTTP/SQLite backend tests, Android-to-server integration, role workflows, persistence and existing screen regression coverage detailed below.
**FINAL DEVICE RESULT:** PASS:81 XML cases,76 passed,0 failures/errors,5 intentional Firebase-only skips; Gradle exit0 in14m22s
**NOT CLAIMED:** full physical-device/classroom acceptance, exhaustive accessibility acceptance, or public-network production readiness.

This work follows the user's new offline full-stack authorization. Firebase is no longer the active normal authentication/data path. No Firebase cloud resource, provider, rule, index, Function, IAM or billing operation was performed.

## Implementation
### Backend and database
Node24.19.0 was already installed and provides built-in HTTP/HTTPS, SQLite and crypto. backend/local was selected instead of Ktor to avoid another Gradle server build, downloaded server framework and unnecessary Android dependency changes. No npm dependency is needed for this server.

The laptop persists accounts, profiles, services, tickets, appointment reservations, completion transactions, notifications, activity, settings, session digests and idempotency records in SQLite. Schema1 migrations are transactional and non-destructive; foreign keys, WAL, full synchronous commits, busy timeout and uniqueness constraints are enabled. The normal database is backend/local/data/campusq.sqlite (or CAMPUSQ_DB). The .cmd launcher was tested on loopback8788: health/schema1 passed, all operational counts were0, and the .cmd online-backup command succeeded. No Administrator or sample record was added to that database. Test fixtures use a separate persistent acceptance database. No operational sample records are inserted on normal startup.

### Authentication and permissions
Real local registration/login/logout/session validation are implemented. Student registration assigns STUDENT on the server and rejects role fields. It does not prove enrollment. Duplicate email and Student numbers are rejected. Invalid credentials are generic; disabled/expired sessions fail closed.

The trusted first Administrator is provisioned through the hidden-password local operator command. ADMIN creates/edits/disables Staff through authenticated APIs/UI. There is no public administrator provisioning route or default password. Roles are checked on every backend action; hiding a route is only additional protection. Student ownership is checked independently of the UI.

Passwords are randomly salted scrypt hashes (N32768/r8/p3); comparison is timing-safe. Server session credentials are32 random bytes, expire after12 hours, and are stored only as digests. Failed logins and hash/request concurrency are bounded. Reset/disable revokes sessions. Android encrypts the credential using Keystore AES-GCM with backend-origin binding and excludes it from backup/transfer. Passwords are transient and not placed in routes/logs/preferences.

Recovery is an honest trusted-operator reset, not fake email delivery. Account creation interrupted before automatic login leaves a saved account that can sign in normally. Session restoration revalidates with the server. Sign-out clears the protected navigation root and its ViewModelStore; Back cannot reopen it. A failed remote logout is disclosed, rather than claiming revocation.

### Student workflows
Existing registration, login, dashboard, catalog, queue status, appointment, history and profile screens use LocalCampusQRepository. Only owned private records are returned. Service availability and queue/appointment enablement are enforced server-side. Profile edits cannot change role/status. Shared queue numbers and appointment occupancy reveal no other Student personal information.

Queue creation allocates a transactional service/day sequence with one active ticket per Student, capacity enforcement, unique indexes and idempotent retries. Staff updates are visible through polling. Completed records populate real history; no fabricated ticket fallback is used.

Appointments validate future weekday slots in Asia/Manila, active service and conflicts. Unique active service/slot and Student/slot indexes protect simultaneous bookings. Rescheduling/cancellation is limited to editable owned records; rescheduling returns to BOOKED for review.

### Staff and Administrator
Staff see actual operational queues and appointments, call/start/complete/cancel eligible tickets, review appointment status, and create persisted completion transactions. FIFO and optimistic versions reject out-of-order/stale updates. Student state reflects Staff actions through the same database.

ADMIN sees actual metrics, accounts/activity, queues, appointments, reports, services and Staff. Service create/edit/toggle/delete-unused is implemented; services with history must be deactivated. Staff creation requires an initial password; disabling revokes existing sessions. Reports count actual completed transactions over rolling7/31/183-day periods. Settings persist descriptions and daily capacity; unsupported cloud push/SMS settings cannot be enabled.

Staff authority is office-wide in this capstone because no service-assignment policy exists in the current UI. Counter names are recorded. Initial Student eligibility verification and additional institutional staff restrictions require policy decisions; no unsupported verification claim is made.

### Android integration and preservation
MainActivity now opens CampusQLocalApp. Existing LoginScreen/RegistrationScreen and all23 screen components, theme, layout structure, five PNG assets and reusable components are retained. Small necessary additions include connection/errors/loading, initial Staff password, appointment details, delete confirmation and honest unavailable descriptions.

CampusQViewModel retains the Demo path but uses a remote StateSource and real command branches in normal operation. Confirmed server results drive navigation. Lifecycle-aware4-second polling pauses when the app is stopped. Mutations/refreshes serialize; duplicate submissions are blocked. Connection loss is displayed and never becomes fake save success. Last received records can remain visible with a connection warning.

DemoRepository and old previews/tests remain isolated in source. There is no normal MainActivity Demo selector, role-preview shortcut or restore-sample path. Firebase adapters/configuration and the opt-in flag are retained as historical work, not a second active authentication path. No existing dependency version was upgraded.

## Actual verification
| Check | Result |
| --- | --- |
| Gradle Sync | PASS after correcting AGP9 generated-resource integration |
| :app:testDebugUnitTest |70 passed,0 failed/errors/skipped |
| :app:assembleDebug | PASS; debug APK produced |
| :app:lintDebug | PASS:0 errors,24 warnings |
| Backend real HTTP/SQLite acceptance |20 passed,0 failures/skips; archived final run33.1 seconds |
| Full Android suite | PASS:81 XML cases,76 passed,0 failures/errors,5 intentional Firebase-only skips; Gradle exit0 in14m22s |
| New actual local-server Android cases | 8/8 passed against the actual local HTTP/SQLite server |
| Existing screen captures | 48/48 passed, covering23 screens plus hub at phone and320dp/1.5font |
| New Compose previews | Local login and connection panel rendered and reviewed. Initial login preview dependency was stale; rendering AuthScreens then refreshing LocalLoginPreview corrected it. Installed APK independently confirmed no Demo selector. |
| Main APK credential/server exclusion | PASS:0 fixture/server/SQLite entries |
| Git whitespace/checkpoints | PASS after trimming one extra EOF blank line;49b6315 and2f323bf still exist |
| Five PNG hashes / Firestore rules | Unchanged from preimplementation inspection |

The Gradle console over-counted skipped-test callback events as86; the final XML contains81 unique cases, used for this report. Five skips are2 cloud checks,1 Firebase emulator foundation check and2 Firebase Auth emulator checks. No cloud/emulator opt-in was enabled for this offline run.

Full integrated command:
```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.localBackend=true' --max-workers=1 --console=plain
```
Backend command: node --test --test-concurrency=1 backend/local/acceptance.test.mjs.
The Android fixture was explicitly isolated, loopback-only and used generated private test credentials. No production Firebase account was created.

### Coverage
Backend tests use real HTTP and SQLite, not fake database responses: registration/duplicates/hash verification, login/invalid password, role escalation/ownership/anonymous denial, session logout/reset/disable/expiry/throttle, service flags, queue sequence/idempotency/FIFO/transitions, appointment conflicts/rescheduling, profile/settings, concurrent requests and status races, protected deletion, online backup/readback, restart durability, privacy of shared slots and actual now-serving metadata.

New Android cases:
- LocalBackendIntegrationTest: independent real Student/Staff/Admin requests, shared queue completion/appointment review, Keystore encryption/origin binding, session revocation and request identity isolation.
- LocalBackendUiTest: actual Student registration/queue journey and Staff updates; Staff call/start/complete; Admin service/Staff creation and new account login; Student book/review/reschedule/cancel; Staff appointment review; unavailable-server login.
- LocalMainActivityTest: actual activity launch, close/relaunch and encrypted-session/ticket restoration.

Existing AllScreens tests cover23 screens plus the historical hub at phone and320dp/1.5font configurations (48 cases). They exercise the isolated component/Demo host; they do not prove every live mutation or TalkBack behavior. Phase2 interactions/appointments, Phase4 fake-auth UI and the existing smoke test remain separate regression coverage.

### End-to-end acceptance evidence and limits
| Target | Evidence / boundary |
| --- | --- |
| Admin/Staff provisioning | Real backend bootstrap tested; actual Admin UI creates Staff and that account logs in; interactive hidden-password operator UI itself not separately automated |
| Student registration/login/dashboard | Actual Android UI + real stored account |
| Student ticket → Staff call/start/complete → Student status/history | Actual Student UI with independent Staff API session, and actual Staff UI processing a Student API-created ticket |
| Appointment booking/review/reschedule/cancel | Actual Student and Staff UI cases with independent opposite-role API sessions |
| Administrator service/Staff management | Actual Admin UI creates records; backend covers edits/disable/delete restrictions/permissions |
| App restart | Actual MainActivity close/relaunch retains validated session/ticket; full OS process death is unverified |
| Server restart | Actual fixture process restart retained identical counts:23 users,23 services,9 tickets,9 appointments,7 transactions,100 activity rows; backend acceptance also closes/reopens the database |
| Concurrency |8 near-simultaneous Student ticket requests, retry idempotency, competing status changes and competing appointment slots tested against SQLite |
| Multiple clients | Independent authenticated API sessions plus Android emulator tested; two simultaneous Android UI devices and physical-phone LAN connectivity unverified |
| No internet | Runtime code communicates with local origin and uses installed modules; disabling WAN and rehearsing the complete classroom network was not physically tested |

The core workflows have substantial actual integration evidence. A complete two-device classroom rehearsal, every profile/settings edge interaction, trusted HTTPS setup and exhaustive cross-device/accessibility acceptance remain outside verified coverage. Do not equate component capture tests with these missing checks.

### Failures investigated and repaired
- AGP9 generated resource source registration mismatch: replaced with a typed task and Android Components generated-resource API.
- Initial instrumentation method returned a non-Unit value: corrected its JUnit signature and retested.
- UI selectors matched duplicate navigation labels or uncomposed lazy rows: scoped selectors/search/scrolling to actual controls; assertions retained.
- Keyboard transitions interfered with service selection: closed the keyboard explicitly in the real journey test.
- Success snackbars overlaid rapid queue/appointment action taps: removed redundant queue/appointment save notices; visible confirmed status remains, configuration-save/error notices remain. Targeted repaired cases passed2/2 before the full rerun.
- The machine blocks unsigned PowerShell scripts. Added .cmd startup/backup wrappers instead of changing system execution policy; their normal-database checks are recorded below.
- Old emulator/ADB became unresponsive with severe host memory pressure: cold-started the existing AVD without wiping data, with temporary lower-memory/headless software-renderer options. No system configuration or unrelated process was changed.
- Historical PixelCopy failures were not suppressed; all48 cases passed on both full offline runs after the AVD recovery.
Earlier failing XML is retained separately from final evidence. No tests/assertions were removed to force a pass.

### Remaining lint warnings
24 total: OldTargetApi1, AndroidGradlePluginVersion1, GradleDependency10, NewerVersionAvailable1, UnusedResources8, IconXmlAndPng2, UseKtx1.
Dependency/target suggestions were not used to justify unrelated upgrades. Seven old unused template colors and the retained historical debug-network XML account for unused resources. Keystore preference commit uses explicit success checks; the remaining KTX style suggestion was not suppressed. Icon assets were preserved. No lint baseline/suppression was added.

## Files
### New backend files
backend/local/migrations/001_initial.sql; database.mjs; security.mjs; service.mjs; server.mjs; operator.mjs; backup.mjs; status.mjs; fixture.mjs; acceptance.test.mjs; Start-CampusQ.ps1; Backup-CampusQ.ps1; Start-CampusQ.cmd; Backup-CampusQ.cmd.

### New Android files
- app/src/main/java/com/example/campusq/data/local/{LocalApi,LocalCampusQRepository,LocalSessionStore}.kt
- app/src/main/java/com/example/campusq/data/model/AppointmentAvailability.kt
- app/src/main/java/com/example/campusq/viewmodel/LocalSessionViewModel.kt
- app/src/main/java/com/example/campusq/ui/navigation/{CampusQLocalApp,LocalServerMode}.kt
- app/src/main/res/values/local_strings.xml
- app/src/test/java/com/example/campusq/{LocalRoleNavigationTest,AppointmentAvailabilityTest}.kt
- app/src/androidTest/java/com/example/campusq/{LocalBackendIntegrationTest,LocalBackendUiTest,LocalMainActivityTest}.kt

### Modified for this offline work
- MainActivity.kt: active local-server root.
- data/model/CampusQModels.kt: default version fields for optimistic updates, preserving legacy callers.
- viewmodel/CampusQViewModel.kt: remote source/commands/synchronization/events; Demo branches retained.
- ui/navigation/CampusQAppNavHost.kt: trusted-role routing, actual sign-out, confirmed navigation, shared slots/serving data.
- ui/auth/AuthScreens.kt and ui/components/CampusQNavigationWidgets.kt: reuse forms/design, isolate Demo, accurate local recovery.
- ui/student/StudentDashboardServicesQueueScreens.kt and StudentAppointmentHistoryProfileScreens.kt: real-state actions/availability/details/unsupported-delivery descriptions.
- ui/staff/StaffDashboardQueueScreens.kt and StaffAppointmentStatusProfileScreens.kt: real transitions and honest operational descriptions.
- ui/admin/AdminDashboardServicesStaffScreens.kt and AdminQueueAppointmentsReportsSettingsScreens.kt: Staff password, protected service deletion, actual period/settings/security descriptions.
- app/build.gradle.kts and app/src/debug/AndroidManifest.xml: configurable origin and narrow generated debug cleartext policy.
- app/src/main/res/xml/{backup_rules,data_extraction_rules}.xml: encrypted session backup/transfer exclusion.
- .gitignore: SQLite/private fixture/TLS/backup/evidence exclusions.
- package.json: local backend convenience scripts; dependency versions/lockfiles unchanged.
- CAMPUSQ_UI_PROGRESS.md: current offline summary prepended; previous history retained.

### Documentation
CAMPUSQ_OFFLINE_PROGRESS.md; this report; docs/offline/{README,ARCHITECTURE,SCHEMA,PRESENTATION}.md.
Ignored local evidence lives under docs/offline/evidence/2026-11-13: final-full XML, android-final-summary.json, jvm-final XML, lint-final.xml, backend-final.log, local-login-runtime PNG/XML and earlier failed runs. The installed debug APK SHA256 is1550D2B6B41E35096D1D6D220EC8C51DBA6EB49969F73136243EE9A36C6D6D53. Private generated acceptance credentials/database remain ignored and are not part of the normal APK. No files deleted.

The initial working tree already contained pending Phase3/Phase4 source, reports and configuration changes, including auth adapters/tests, Firebase setup docs, catalog and debug Firebase wiring. Those were preserved and are not all newly attributable to this offline implementation. Git status therefore includes more work than this file inventory.

## Presentation setup and remaining actions
1. Use the normal presentation database, not the test fixture. Agent-owned fixture and verification servers are stopped at handoff; their database files are preserved.
2. Run node backend/local/operator.mjs bootstrap-admin in your interactive terminal. Choose a private Administrator password locally; no secret needs to be shared in chat.
3. Run .\backend\local\Start-CampusQ.cmd. Sign in as Admin and create real services/Staff; register Students in the app.
4. Emulator default origin is http://10.0.2.2:8787. For phones, explicitly opt into LAN HTTP and rebuild with '-Pcampusq.backend.url=http://ACTUAL-LAPTOP-IP:8787'. Any Windows Firewall change needs laptop-owner approval.
5. Follow docs/offline/PRESENTATION.md for queue, appointment and restart demonstrations; README.md for backups and recovery.

HTTP carries credentials/tokens without transport encryption. Use an isolated trusted network and disposable/non-reused presentation credentials. Do not claim public-network security. HTTPS certificate issuance/trust and physical-device rehearsal remain manual/unverified. No paid feature or cloud permission is required for the core offline system.

## Git and checkpoint recommendation
Existing checkpoints49b6315 and2f323bf are preserved. No reset, forced checkout, commit or staging was performed. Review the pending mixed Phase3/Phase4/offline changes and ignored-secret exclusions before creating a local checkpoint. Do not indiscriminately stage database, fixture asset, certificates or credential files.

## Final acceptance
Core offline Student/Staff/Admin workflows are implemented and verified against real local persistence, including Android UI-to-server actions. The available automated checks are clean with5 explicitly skipped inactive Firebase cases. This is not a claim that the entire physical classroom acceptance scenario has passed: two simultaneous Android devices, LAN/firewall conditions, HTTPS provisioning, full process death and comprehensive accessibility remain unverified. Initial normal-database Administrator credentials must be provisioned privately by the laptop operator before presentation.
