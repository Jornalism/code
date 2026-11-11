# CampusQ Phase 3 — Backend foundation report
Date: 2026-11-11, Asia/Manila.
Status: Available local foundation complete and verified. Real Firebase connection remains blocked by external authorization.
Boundary: No Phase 4 authentication workflow or live queue implementation. No billing enabled.

## Firebase configuration
Approved new project display name CampusQ; package com.example.campusq; Standard edition, (default), asia-southeast1. No authorized Firebase CLI account, real Project ID, Android App ID or google-services.json available. CLI login:list reported no authorized accounts; database list failed authentication. Login instructions are in docs/firebase/SETUP.md. Firebase MCP/Developer Knowledge tools unavailable; official Firebase docs consulted instead.

Main-module Auth, Firestore, Functions and Messaging SDKs added using BoM 34.19.0 and Google Services 4.5.0. BoM 35.1.0 was rejected by an actual build: Auth 25.0.0 Kotlin 2.4 metadata incompatible with existing Kotlin compiler 2.2. No metadata-check suppression or toolchain upgrade. Missing JSON does not break normal Demo Mode builds; -Pcampusq.firebase.enabled=true requires genuine config. FCM auto-init and Analytics collection disabled. No Auth provider, remote collection/rules/index or paid function deployed.

## Preservation and checkpoint
Git initialized on main. Baseline checkpoint 49b6315 contains screened Phase 2 source/build/docs/assets. No credentials, private app config, local.properties, node_modules or build output committed. All 23 screens/demo hub, MainActivity, navigation and five PNG assets preserved; SHA256 hashes match Phase 2. Existing accessibility/cross-device limitations remain open.

## Data/security foundation
See [Firestore schema](docs/firebase/FIRESTORE_SCHEMA.md) for all 11 collections, wire fields, IDs, ownerUid/role, server timestamps, enums, validations and proposed indexes. Additional server-only queue/slot/idempotency locks are designed, not implemented. Queue sequence allocation and appointment conflicts require atomic trusted transactions; no client authority.
[Security documentation](docs/firebase/SECURITY_RULES.md) and firestore.rules establish a deny-all prototype. Anonymous/student/staff/admin/forged claims all denied; no UI role is trusted. Future claim provisioning requires trusted IAM/Admin SDK and active authorization checks. Even valid admin claims have zero mobile permissions today. Server SDK bypasses rules, so future callable validation must be independent. security-audit.json records foundation-only review, not a production certification.

## Emulators and repository architecture
firebase.json configures Auth 9099/Firestore 8080/Functions 5001 on 127.0.0.1 using explicit demo-campusq-local; no real project values fabricated. Functions exports only an emulator-gated rejecting probe. Android debug-only factory routes named Firebase app to 10.0.2.2, uses memory cache and scoped debug-only HTTP. Release has no emulator factory.
CampusQStateSource is implemented by DemoRepository and observed by existing ViewModel. Demo mutations remain intact. ServiceCatalogRepository has shared-demo and strict one-shot Firebase adapters. FirebaseBackend is explicit and unused by UI; denied Firebase requests propagate errors, never silently return demo data. No realtime listeners, auth UI wiring or backend mutations.

## Verification
Final combined command:
```powershell
$env:FUNCTIONS_DISCOVERY_TIMEOUT='60'
npm.cmd run test:all
```
This executes the Node security/backend suite and then :app:testDebugUnitTest, :app:assembleDebug, :app:lintDebug and :app:connectedDebugAndroidTest inside Auth/Firestore/Functions emulators, with the explicit Android firebaseEmulators=true argument.

**PASS: exit 0; Gradle BUILD SUCCESSFUL in 6m 22s.**
| Check | Actual result |
|---|---|
| Gradle Sync | Successful after SDK/configuration changes |
| Debug APK | Built successfully; subsequent IDE deployment and MainActivity/demo login launch observed |
| Local JVM tests | 33 passed, 0 failures/errors/skips (26 existing domain tests, 1 inventory, 6 new foundation tests); final combined run reused up-to-date passing results |
| Node rules/backend tests | 59 passed, 0 failed/skipped:55 actor/collection cases plus fixture control, nested denial, escalation batch and Auth/Functions smoke |
| Connected Android tests | 63 passed, 0 failures/errors/skips; Pixel 7 AVD, Android 17/API 37 |
| Device coverage | 48 screen captures (23 screens+hub at phone and 320dp/1.5x), 13 existing behavior/restoration tests, 1 package check, 1 new Auth/Firestore emulator check |
| Android lint | 0 errors,20 warnings; previous 19 plus deliberately retained compatible-BoM update warning |
| npm audit | Root and functions packages:0 vulnerabilities after grpc-js1.14.7 override |
| Missing-config guard | Expected failure/message verified when Firebase opt-in is true without real JSON |
| Genuine cloud connection/deployed rules/indexes | NOT VERIFIED; no authorized account/config |
| Android callable/FCM client | NOT VERIFIED; non-emulated IID/FCM precondition requires genuine config; Node Functions behavior verified |
| Release/physical/other Android APIs/accessibility matrix | NOT RUN; Phase2 limitations remain |

Retained evidence: docs/phase-3/evidence/results/ contains final combined log, JVM/device JUnit XML, lint text/SARIF, APK SHA256/summary, brand hashes and negative guard result; captures/ contains 48 final PNGs. Intermediate failed results are separately labeled under intermediate/. Screenshots are render/smoke evidence, not pixel-golden assertions. Generated evidence stays local and ignored by Git.

No lint suppressions or baseline introduced. The 20 warnings are 11 SDK/dependency notices, 7 original unused XML colors, 2 original launcher-resource notices. No dependency/toolchain upgrades unrelated to Firebase. Initial build/sync attempts exposed:
- SDK Kotlin metadata incompatibility: selected preceding compatible BoM.
- Tool-notification footer appended during a source read/write: removed and source scan repeated.
- Build begun while a new adapter was being added: unresolved adapter reference; rerun after files finalized.
- Initial IDE sync timeout; subsequent Sync finished successfully.
- Kotlin daemon invalid-session fallback during local compilation; compilation subsequently completed; no source compiler errors remain.
- Second emulator startup hit a 10-second function discovery timeout; 60-second local allowance resolved it.
- Windows wrapper resolution required explicit .\\gradlew.bat; PowerShell Gradle -P properties require quoted arguments. Missing-config negative check passed with the documented error.
- Debug network policy required includeSubdomains=false explicitly on all three hosts; all three lint errors corrected without suppressions.
- One instrumentation startup crashed before discovery; direct runner smoke passed and discovery was scoped to the CampusQ package. All 63 tests subsequently executed.
- The first full device pass had 62 passing regressions and one new callable-context failure. Auth and Firestore emulator checks passed. Functions client attempted non-emulated IID/FCM token acquisition with deliberately non-production options; removing IID registrars was tested and reverted because Functions requires the component. Final Android check covers Auth/Firestore; Android callable/FCM verification remains blocked on genuine configuration. Node Functions smoke passed. No synthetic production API key or internal SDK substitute introduced.
- A focused emulator attempt overlapped the previous Suite shutdown and failed on occupied ports; retried only after shutdown.
- Root npm audit initially found 5 high transitive notices from grpc-js1.9.16; explicit1.14.7 override resolved both advisories. Root and functions npm audits now report 0 vulnerabilities. Lockfiles retained.
Host Node 24.19.0 / Java 25.0.2 / Firebase CLI 15.35.0. Functions target runtime 22; local emulator warns and uses host 24. No Node 22 cloud runtime verification.

## Files created/modified
Modified: .gitignore; app/build.gradle.kts; build.gradle.kts; gradle/libs.versions.toml; app/src/main/AndroidManifest.xml; DemoRepository.kt (state contract); CampusQViewModel.kt (state contract observation); CAMPUSQ_UI_PROGRESS.md.
Created: CAMPUSQ_PHASE_3_PLAN.md; this report; firebase.json; firestore.rules; firestore.indexes.json; package.json/package-lock.json; backend/functions/package.json/package-lock.json/index.cjs; backend/tests/foundation.test.mjs and run-all.cjs; docs/firebase/{SETUP,FIRESTORE_SCHEMA,SECURITY_RULES,REPOSITORIES}.md and security-audit.json; main data/repository/{CampusQStateSource,ServiceCatalogRepository}.kt; main data/demo/DemoServiceCatalogRepository.kt; main data/firebase/{FirebaseBackend,FirebaseServiceCatalogRepository,ServiceDocumentMapper}.kt; debug LocalFirebaseBackend.kt, debug manifest/network config; local FirebaseFoundationTest.kt; Android FirebaseEmulatorFoundationTest.kt. Existing app/.gitignore also retained. Local security analysis and generated evidence ignored.

## Manual setup and remaining work before Phase 4
1. Run npx.cmd -y firebase-tools@latest login in Android Studio Terminal and authorize Google locally. Do not share authorization codes.
2. Select/create real CampusQ Project ID on Spark, without Analytics or billing. Provide resulting ID. Creation/organization IAM may require institution administrator.
3. Create Standard(default) database in asia-southeast1, locked/production mode. Stop if billing requested.
4. Register com.example.campusq, retrieve genuine config with apps:sdkconfig --out app/google-services.json; verify IDs/package. Exact commands in setup guide.
5. Review/deploy closed rules and proposed indexes only; verify remotely. No Functions deployment or Blaze approval assumed.
6. Build with explicit config opt-in and verify genuine initialization/permission denial before claiming cloud connection.
7. Agree university identity/email policy, trusted first-admin provisioning, revocation, App Check, data retention and backups before live access.
8. Later phases require reviewed read permissions, full server validation, atomic queue/appointment commands and concurrency tests. These are not implemented by this foundation.
Android callable/FCM client verification awaits genuine project configuration; Node Functions emulator behavior is verified. No live callable exists.\nPhase 2 TalkBack/docked IME/older devices/tablets/foldables/landscape2x acceptance remains open. No release/cloud deployment/production notification verification claimed.
Stop at Phase 3; Phase 4 requires explicit authorization.

## Acceptance decision
Available local Phase 3 foundation: met. Real-project setup/remote verification: not met, blocked by authorization/configuration. All demo screens and workflows remain selected. Complete the documented cloud setup and reviewed live-access prerequisites before claiming a connected backend. Phase 4 is unstarted and requires explicit authorization.
