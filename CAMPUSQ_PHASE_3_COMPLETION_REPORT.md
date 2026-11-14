# CampusQ Phase 3 — Published deny-all Rules verification
Verified:2026-11-12, Asia/Manila.

**Current result: user-approved Firestore Rules-only deployment succeeded on campusq-e06c7/(default). Published source exactly matches the unchanged, locally tested deny-all file. Actual unauthenticated read and guarded write requests both returned403 PERMISSION_DENIED. Phase4 remains unstarted.**

## Before deployment
- Firebase CLI explicit target check: firebase use --project campusq-e06c7 returned campusq-e06c7.
- Independent project/database metadata confirmed campusq-e06c7, exactly(default), STANDARD, FIRESTORE_NATIVE,asia-southeast1,freeTier=true; billingEnabled=false/no billing account.
- firestore.rules was read and its SHA256 matched the tested baseline:862A1CFA49BCBF014C43AC89DEB6C44D9F217625A7574F9104CDAF61BAD419D0.
- firebase.json parsed successfully and references firestore.rules for(default).
- The policy was not edited before, during or after deployment. It remains Rules v2 with recursive allow read, write: if false.

## Authorized deployment
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase deploy --only firestore:rules --project campusq-e06c7 --non-interactive
```
Exit0. CLI compiled the source, uploaded it and reported released rules to cloud.firestore / Deploy complete.
Only Firestore Rules were selected. The CLI mentions reading firestore.indexes.json during preparation; it did not deploy indexes. No Functions, databases or other Firebase resources were selected/deployed. No billing/IAM or plan change.

## Published release and source equality
Authenticated Rules API GETs confirm:
- Release: projects/campusq-e06c7/releases/cloud.firestore.
- Ruleset: projects/campusq-e06c7/rulesets/a42e813a-233e-4ca2-969b-6592ec55c483.
- Release create/update time:2026-11-12T15:01:25.896399Z (23:01:25 Asia/Manila).
- Published source contains exactly one file, firestore.rules.
- Full content byte equality, including comments and CRLF line endings:PASS.
- Local and remote SHA256 both862A1CFA49BCBF014C43AC89DEB6C44D9F217625A7574F9104CDAF61BAD419D0.
- Post-deployment billing GET still billingEnabled=false/no billing account. Database metadata remains Standard/Native/Singapore/freeTier.

## Safe real-database client checks
Performed using native HTTPS requests without Authorization or a Firebase user token. The registered Android API key was used only for project identification; it was read locally and never printed or written to evidence.
1. GET services/campusq-deny-all-probe:HTTP403, PERMISSION_DENIED, Missing or insufficient permissions.
2. POST documents:commit with one conditional update to that path:HTTP403, PERMISSION_DENIED, Missing or insufficient permissions.

The write request required an existing document whose server updateTime equals9999-12-31T23:59:59.999999Z. This deliberately impossible future precondition prevents both creation and modification even if authorization were unexpectedly granted. No unconditional write was sent; no document was created, updated, deleted or seeded. The API's [updateTime precondition](https://firebase.google.com/docs/firestore/reference/rest/v1/Precondition) requires the target to already exist and match that exact last-update timestamp. This tests an actual write-permission denial without permitting a data change.

Initial probe attempt used a reserved double-underscore document ID and returned400 INVALID_ARGUMENT before policy evaluation. Changed only the ignored verification helper's path, not the Rules or deployed resources; final valid-path run passed both denials. Retained that unsuccessful attempt separately. No redeployment was needed.

Scope: confirms this published catch-all source and those two unauthenticated denials. Earlier local emulator58-case suite covers other actors/get/list/create/update/delete/nested/batch denials. No exhaustive authenticated production policy, App Check or IAM authorization audit is claimed.

## Files, evidence and preservation
Local policy and firebase.json remain unchanged. Updated documentation only: this report, CAMPUSQ_PHASE_3_PLAN.md, CAMPUSQ_UI_PROGRESS.md, docs/firebase/SECURITY_RULES.md and docs/firebase/SETUP.md.
Ignored helper:.firebase/phase3-published-rules-verify.cjs.
Ignored evidence:docs/phase-3/evidence/published-rules-2026-11-12/verification.json contains release/source/hashes/sanitized client outcomes; initial-reserved-path-attempt.json retains the initial invalid-path response. No API keys/credentials in either.
No Android source/build/resource/repository or asset change. All23 screens, Demo Mode, DemoRepository and Git checkpoints49b6315/2f323bf preserved.
No new Gradle/full regression run was necessary for unchanged app/policy source; the new verification is CLI compilation/publication plus remote source comparison and actual denial requests. Earlier passing runs below remain historical.

## Phase boundary
The previously missing published deny-all baseline is now resolved. The approved Phase3 rules-publication task is complete. Role-based permissions, Authentication, live queue commands, notification delivery and Phase4 remain unimplemented/unstarted and require separate authorization. Remaining broader Phase2 accessibility/device limitations stay open.

---

## Historical pre-publication verification record
The following earlier snapshots are retained for evidence. Their statements that no published Rules release existed, that publication was not yet authorized, or that a write probe had not run are historical and superseded by the verified publication above.

# CampusQ Phase 3 — Firebase cloud verification report
Verification date: 2026-11-12, Asia/Manila.

## Local deny-all baseline check — 2026-11-12
Scope: local preparation/verification only; no cloud deployment or metadata requests.

Inspected firestore.rules, firebase.json, package.json, backend/tests/foundation.test.mjs and docs/firebase/SECURITY_RULES.md. Both policy/config files already exist and are correct, so neither was edited:
- Rules version2; recursive /databases/{database}/documents/{document=**}; allow read, write: if false.
- firebase.json selects database(default) and rules file firestore.rules. The relative path resolves to the root file; JSON parses successfully. Existing indexes/functions entries are retained, not executed.
- SHA256 firestore.rules:862A1CFA49BCBF014C43AC89DEB6C44D9F217625A7574F9104CDAF61BAD419D0.
- SHA256 firebase.json:31F41AFC1FC24F87BC082D51A1DA7A48C25D9488A9363CC0D7A1FA93850F0AB1.

Fresh validation: **PASS**, exit0;58 tests passed,0 failures,0 skips on the Firestore emulator only. Loading the actual file through initializeTestEnvironment validates syntax.55 actor/collection cases check get/list/create/update/delete for anonymous, student, staff, administrator and forged/unverified claims across11 collections. Nested/unknown paths, atomic privilege escalation and a positive fixture-presence control also pass. This covers282 denial assertions; the positive control verifies the harness is not merely reading missing data.

The test target is demo-campusq-local at127.0.0.1:8080, not campusq-e06c7. Existing test-harness fixtures are created only in the disposable local emulator with security disabled for setup; client operations are then denied. No real documents or sample cloud data were created, read or modified. The emulator stopped without data import/export. No authentication workflow or Cloud Function was implemented; the unrelated Auth/Functions smoke case was excluded from this focused run rather than reported as tested.

Reproduction (isolated patched Node, global runtime unchanged):
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase emulators:exec --project demo-campusq-local --only firestore "node .firebase/phase3-local-rules-check.cjs"
```
The ignored helper checks the demo project and loopback host, then runs the existing test file with a positive name filter. Initial attempt: all58 Rules cases passed, but a negative filter also included the unrelated Auth/Functions smoke case, which failed because those emulators were intentionally off. Corrected only the local helper filter; the final58-case run passed. No policy, app or test-source fix was needed.

Files changed in this local-check task:
- docs/firebase/SECURITY_RULES.md: clarify local policy, server/IAM distinction and explicit no-deployment boundary; record focused results.
- CAMPUSQ_PHASE_3_COMPLETION_REPORT.md and CAMPUSQ_UI_PROGRESS.md: record actual checks and scope.
- Ignored local helper .firebase/phase3-local-rules-check.cjs and evidence under docs/phase-3/evidence/local-rules-baseline-2026-11-12/.
No policy/configuration/Android source, resources, repository, indexes, package locks or Git checkpoints changed. Gradle builds/tests/lint were not rerun because no Android/build/policy code changed; the meaningful new verification is the actual local Rules compiler and denial suite.

No firebase deploy/publish, billing/plan changes, database creation, index/function deployment or Phase4 work occurred. The real project's last verified state still had no published Rules release; this task does not publish one or re-query it. Wait for explicit user approval before any cloud deployment.


**Current result: approved (default) database created and independently verified as Standard / Native / asia-southeast1, with freeTier=true and billing disabled. Android SDK identity and real server-denied read verification passed (2 tests, no skips). No published Firestore Rules release exists; no custom Rules, indexes or Functions were deployed. Phase 4 is unstarted.**

## Verified cloud configuration
- Authenticated Firebase CLI 15.36.0 is accessible in this Android Studio workspace.
- Project display name CampusQ; ID campusq-e06c7; number 875570165692; ACTIVE. projects:list exited 0.
- Registered Android package com.example.campusq; App ID 1:875570165692:android:0252b7d559f6f9dd9f9db9; ACTIVE. apps:list ANDROID exited 0.
- Genuine app/google-services.json exists and is Git-ignored. Sanitized inspection and a fresh server download matched project ID/number, package, App ID and API-key value. No API-key value, account identity, OAuth token or private config is included in this report/evidence. Unique ignored comparison file was deleted.
- Existing opt-in -Pcampusq.firebase.enabled=true successfully processed Google Services resources and built the APK. Default opt-in behavior is unchanged; the UI still uses DemoRepository.
- Instrumentation configuredSdkMatchesRegisteredAndroidApp passed against the default FirebaseApp with those independently confirmed IDs, cloud Firestore host, SSL enabled and no signed-in user.
- Free configuration independently checked before and after database creation: billingEnabled=false, billingAccountName empty, database freeTier=true. No billing upgrade/account attachment occurred; Spark is preserved.

## Approved default database creation — 2026-11-12
The user explicitly authorized exactly (default), Standard edition, Native mode, asia-southeast1 (Singapore), initial closed client access and no billing upgrade. No other database or custom policy/index/function deployment was authorized.

Preflight, completed before creation:
- Project-scoped Firestore locations list includes asia-southeast1 / Singapore.
- Firebase project resources.locationId is unset; no fixed default Google Cloud resource location was reported. The requested location was not substituted.
- Cloud Billing GET returned billingEnabled=false and billingAccountName=""; database list was empty.
- Official [free quota documentation](https://firebase.google.com/docs/firestore/quotas) allows one free database. Paid recovery/backup/TTL features were not enabled. Creation succeeded without a billing request or permission change.

Creation command (isolated patched Node; no credentials in command):
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase firestore:databases:create "(default)" --project campusq-e06c7 --location asia-southeast1 --edition standard --point-in-time-recovery DISABLED --non-interactive --json
```
Result: exit 0 / status=success. Created at 2026-11-12T14:17:37.091638Z (22:17:37 Asia/Manila).

Independent authenticated post-creation GET/list checks:
- Exactly one database: projects/campusq-e06c7/databases/(default).
- databaseEdition=STANDARD; type=FIRESTORE_NATIVE; locationId=asia-southeast1.
- freeTier=true; pointInTimeRecoveryEnablement=POINT_IN_TIME_RECOVERY_DISABLED.
- billingEnabled=false; billingAccountName remains empty.
- appEngineIntegrationMode=DISABLED. No App Engine app or other service was explicitly provisioned.
- Firebase project resources.locationId remains unset; the database's own location is authoritative and immutable.

## Initial security and deployed release
The Firebase CLI database-create implementation uses closed rules by default, blocking incoming third-party traffic. No test/open rules were selected. [Firebase production mode](https://firebase.google.com/docs/firestore/manage-databases) describes denial of mobile/web reads and writes; IAM-authorized server access remains separate.

Actual Rules metadata after creation: release list empty; GET projects/campusq-e06c7/releases/cloud.firestore returns404 NOT_FOUND. **The CLI did not publish a Rules release.** There is no deployed source to download/compare, and local firestore.rules is not claimed to be deployed. We retained default closed access rather than publishing custom rules under the user's explicit prohibition.
The real Android Source.SERVER services query completed with the expected PERMISSION_DENIED; the SDK identity case also passed. Authoritative JUnit XML:2 tests,0 failures/errors/skips. No write probe is permitted/performed, and no claim of exhaustive authenticated-role/write-policy verification is made. An explicit versioned deny-all release, if desired, needs separate authorization; it is not silently deployed.

No document or sample data writes, extra databases, billing/IAM/API changes, custom Rules/index/function deployment, Auth workflow or live queue implementation occurred in this creation continuation.

## Previous API-only step (historical, superseded database absence)
The separately approved firestore.googleapis.com enable operation changed DISABLED to ENABLED and was verified by a service-state GET. Before database creation, database lists and Rules release lists were empty and default lookups returned404. Those observations are preserved in api-*.json and must not be treated as the current database state.

## Current verification and remaining scope
The prior cloud-connection blockers (missing database and skipped read check) are resolved. The Phase3 database/SDK/read-only connection foundation is verified. No explicit Rules release is published, so versioned deployed Rules-source verification remains unavailable without a separately approved deployment. Phase2 accessibility/device limits and deferred callable/FCM delivery remain open; Phase4 has not started.

- Metadata/billing preflight and independent post-creation checks: PASS.
- After instrumentation cleanup, reinstalled the genuine-config APK: Success; normal MainActivity cold launch returned Status:ok. No source change. Final protected-file comparison and git diff --check passed; existing checkpoints remain intact.
- Android SDK and real Firestore server-denial check: PASS, exit0, BUILD SUCCESSFUL in1m43s; Pixel_7 Android17/API37;2 passed,0 failures/errors/skips. This is a targeted new cloud check, not a fresh full65-case run.
- No application code/resources/build changes in this creation continuation. Full regression tests/lint are not rerun solely for remote provisioning; preceding build/unit/backend/65-case results below remain separately labeled.
- Local ignored evidence: docs/phase-3/evidence/actual-cloud-2026-11-12/database-preflight.json, database-postcreate.json, database-final-summary.json and database-device/ JUnit XML. Earlier API-only, SDK-only and full-suite evidence is retained separately without overwriting it.
- Phase4 remains unstarted and requires explicit authorization. Custom deployed Rules, indexes, Functions, role assignment, auth workflows and realtime queues remain deferred.
- No deployed Rules source exists to certify. Default closed access and the particular client-denial result have a narrower scope than a full production Rules audit.

Reproduce the targeted check with the existing genuine local JSON:
```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pcampusq.firebase.enabled=true' '-Pandroid.testInstrumentationRunnerArguments.class=com.example.campusq.FirebaseCloudVerificationTest' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudVerification=true' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudProjectId=campusq-e06c7' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudAppId=1:875570165692:android:0252b7d559f6f9dd9f9db9' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudDatabaseVerified=true' --max-workers=2 --console=plain
```

## Verification test correctness
FirebaseCloudVerificationTest now separates SDK initialization from database denial:
1. SDK identity/host/SSL/no-user check, explicitly opted in with known project/App IDs.
2. Server-read denial check, additionally guarded by firebaseCloudDatabaseVerified=true, which is supplied only after the real database metadata and API are independently verified.

Earlier runs correctly skipped the database case while provisioning was blocked; the current explicitly enabled run is recorded above. A disabled API can also surface as PERMISSION_DENIED; the new guard and explicit message rejection prevent treating that response as successful Rules connectivity. Future denied reads establish only the tested unauthenticated restriction, not the whole deployed policy.

## Preceding Android/backend verification results (before API-only continuation)
- Genuine-config Gradle command :app:processDebugGoogleServices :app:testDebugUnitTest :app:assembleDebug :app:lintDebug: exit 0, BUILD SUCCESSFUL in 3m 9s. 33 JVM tests have passing XML (up-to-date result reuse); lint 0 errors/20 warnings.
- Targeted real-config device run: exit 0, BUILD SUCCESSFUL in 1m 38s; authoritative JUnit XML 2 cases, 1 SDK pass, 0 failures/errors, 1 database skip. Console progress count is not used instead of XML.
- Root and backend/functions npm audit rerun: both exit 0, 0 vulnerabilities.
- After test cleanup removed the package, reinstalled the verified APK: adb install returned Success; normal MainActivity cold launch returned Status: ok and held window focus. A preceding launch attempt correctly reported the package absent. No application source change was required.
- Final combined genuine-config run: exit 0; Gradle BUILD SUCCESSFUL in 7m 5s. Backend 59/59 passed, no failures/skips. Authoritative Android JUnit XML: 65 total, **64 passed, 0 failures/errors, 1 expected database-read skip** on Pixel_7 Android 17/API 37. Includes all existing demo behavior/restoration and 48 screen-capture cases, local Firebase emulator check and real-config SDK check. Console progress displayed 66/65; XML contains exactly 65 cases and is the source of totals.
- The combined runner executed :app:testDebugUnitTest, :app:assembleDebug, :app:lintDebug and :app:connectedDebugAndroidTest with genuine-config opt-in, explicit cloud IDs and firebaseEmulators=true. It deliberately omitted firebaseCloudDatabaseVerified. Local Auth/Firestore/Functions emulators used demo-campusq-local and stopped cleanly. JVM passing outputs were reused up-to-date.
- Final lint XML: 0 errors, 20 warnings (11 SDK/dependency notices, 7 preserved unused colors, 2 launcher-resource notices). No suppression or new lint baseline.
- Evidence: docs/phase-3/evidence/actual-cloud-2026-11-12/ retains sanitized metadata, targeted SDK XML, full device XML, JVM XML and lint text/XML. APK SHA256: A1E92848406B10E1B56B04B6414A16EA3498AAA74108714EADE14FEB93D7C929.

No Gradle source/dependency changes were necessary in this continuation; the existing opt-in was exercised with actual config. Prior successful IDE Sync is retained; current Gradle configuration and resource processing passed.

## Node assertion investigation
Global Node 24.19.0 reproduced UV_HANDLE_CLOSING in src/win/async.c after Firebase apps:sdkconfig output, with native exit -1073740791 (0xC0000409). Therefore printed JSON/download output alone is insufficient evidence of command success.
An isolated npx Node 24.22.3 + firebase-tools 15.36.0 invocation retrieved the same config and all comparisons passed with exit 0. Global Node remains 24.19.0; no global install or lockfile change. This verified workaround avoids the observed abort, without claiming a unique root cause.
Official [Node24 changelog](https://raw.githubusercontent.com/nodejs/node/main/doc/changelogs/CHANGELOG_V24.md) records a Windows libuv assertion fix in 24.20.0; see [fix](https://github.com/nodejs/node/pull/61999) and [matching Firebase CLI issue](https://github.com/firebase/firebase-tools/issues/11099). The setup guide documents the tested wrapper.

## Files and preservation
The database-creation continuation updates this report, CAMPUSQ_PHASE_3_PLAN.md, CAMPUSQ_UI_PROGRESS.md and docs/firebase/SETUP.md, plus ignored read-only preflight/post-creation helpers and evidence. No production/test source or local policy/index file changed.

Preceding cloud-SDK continuation changed:
- app/src/androidTest/java/com/example/campusq/FirebaseCloudVerificationTest.kt (new explicit verification tests).
- CAMPUSQ_PHASE_3_COMPLETION_REPORT.md, CAMPUSQ_PHASE_3_PLAN.md, CAMPUSQ_UI_PROGRESS.md and docs/firebase/SETUP.md.
- Local ignored app/google-services.json was supplied by the user, verified and left intact; it is not staged/committed.
- Local evidence under docs/phase-3/evidence/actual-cloud-2026-11-12/; ignored one-off combined runner under .firebase/.

Git checkpoints 49b6315 and 2f323bf remain intact. Diff checks confirm all 23 screen sources, MainActivity and drawable assets equal the Phase 2 baseline. DemoRepository, existing Gradle opt-in, deny-all rules/indexes, .gitignore and both package locks are unchanged from HEAD. No auth workflow/realtime queue/UI repository switch. Existing schema/security/repository architecture described in the historical foundation report remains applicable.

## Historical same-day environment recovery
Before the genuine JSON/authenticated session was available, a full 64-case device run had 58 passes, 5 Espresso focus failures and 1 expected cloud skip. dumpsys window identified an ANR dialog from com.android.systemui holding focus. After AVD reboot, all 11 cases in the affected interaction class passed without app changes. These results remain separately retained under docs/phase-3/evidence/cloud-attempt-2026-11-12/ and are not substituted for the latest full-suite result.

## Acceptance boundary
Android registration/configuration/SDK initialization: VERIFIED. Local foundation remains preserved.
Firestore API: ENABLED. Exactly one default database: VERIFIED, Standard/Native/Singapore, freeTier=true, billing disabled. Rules release: none published by CLI creation. Read-only mobile server-denial check: VERIFIED, plus SDK identity,2/2 passed. No exhaustive authenticated-role/write-policy or explicit deployed Rules-source audit is claimed.
Android callable/FCM delivery, production role/auth/queue workflows, release/physical-device and broader accessibility matrix remain outside verified scope. A complete production Rules/role/write audit is not claimed. Phase 4 requires explicit user authorization.

---

## Historical local-foundation report (2026-11-11)
The following results are retained as the earlier completed run, not substituted for current cloud verification.

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
