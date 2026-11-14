# Latest approved Rules publication — 2026-11-12
The approved deploy --only firestore:rules --project campusq-e06c7 completed successfully. A published cloud.firestore release now exists and its single-file source exactly matches the tested local firestore.rules. Actual unauthenticated read and guarded write both returned403 PERMISSION_DENIED. No document changes; billing remains disabled.
Further deployments require separate authorization. Phase4 unstarted. Details/evidence in CAMPUSQ_PHASE_3_COMPLETION_REPORT.md.
The following earlier setup snapshot is retained for context; its no-published-release/publication-not-authorized statements have been superseded by this specific approval and completed deployment.

---

# Firebase setup — CampusQ Phase 3
Updated: 2026-11-12, Asia/Manila.

## Actual cloud verification
Firebase CLI authentication is available in this Android Studio environment. Read-only project and Android-app requests confirmed:
- Project: CampusQ / campusq-e06c7, number 875570165692, ACTIVE.
- Android package: com.example.campusq.
- Firebase App ID: 1:875570165692:android:0252b7d559f6f9dd9f9db9.
- app/google-services.json exists and is Git-ignored. A fresh server download matched its project number/ID, Android package/App ID and API-key value. No key values or account credentials are recorded.
- The opt-in Google Services resource task and genuine-config APK build succeeded. Instrumentation confirmed default FirebaseApp initialization with those IDs, SSL cloud Firestore host and no signed-in user.
- Approved API enablement succeeded: firestore.googleapis.com changed DISABLED → ENABLED, operation done=true, verified by a separate state GET.
- The separately authorized (default) database was created on2026-11-12. Independent metadata confirms exactly one database: STANDARD, FIRESTORE_NATIVE, asia-southeast1, freeTier=true. Billing GET confirms billingEnabled=false and no billing account before/after creation.
- Firebase Rules API was already enabled. The CLI-created database uses closed client access but publishes no Rules release: release list remains empty and cloud.firestore GET returns404. No custom Rules deployed; local deny-all rules remain unchanged.
- Spark/free configuration is preserved: database freeTier=true, billingEnabled=false, empty billingAccountName. No billing upgrade occurred.

Authorized cloud provisioning is limited to Firestore API enablement and exactly the default Standard Native Singapore database. No extra databases, custom Rules/index/function deployments, data/auth writes, callable requests, other API enablement or billing changes were performed. Existing local deny-all rules remain unchanged. Local tests do not establish deployed production rules.

## Database creation and security status
Completed preflight: project-scoped locations includes Singapore; Firebase resources.locationId was unset; no existing database; billing disabled/no billing account. The requested location was used without substitution.
Creation succeeded with the Firebase CLI, --edition standard, --location asia-southeast1 and --point-in-time-recovery DISABLED. Native mode is the CLI create command's type. Post-creation GET confirms actual settings, freeTier=true and unchanged disabled billing.

No published Rules release exists after CLI creation; default closed client access applies. Do not claim local firestore.rules is deployed. No custom rules may be published under the current authorization. If an explicit versioned deny-all release is desired, present the exact policy/release change and obtain separate approval.

Safe metadata checks:
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase firestore:databases:list --project campusq-e06c7 --json
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase firestore:databases:get "(default)" --project campusq-e06c7 --json
```
Do not run database creation again. Do not add sample documents or deploy indexes/Functions. No Phase4 workflows are authorized.

## Android opt-in and safe verification
Normal builds require no private JSON and remain in Demo Mode:
```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
Build with the real local config:
```powershell
.\gradlew.bat '-Pcampusq.firebase.enabled=true' :app:processDebugGoogleServices :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --max-workers=2 --console=plain
```
The flag applies Google Services processing; it does not switch CampusQViewModel/DemoRepository or implement authentication. Default remains false so private config is optional for other developers. Missing config fails early when explicitly enabled. The main manifest disables FCM auto-init and Analytics collection.

FirebaseCloudVerificationTest has two independently reported cases:
- configuredSdkMatchesRegisteredAndroidApp verifies package, default FirebaseApp, expected project/App IDs, cloud host, SSL and no existing signed-in user.
- verifiedDatabaseDeniesUnauthenticatedServerRead additionally requires firebaseCloudDatabaseVerified=true. It performs a bounded Source.SERVER services query and expects PERMISSION_DENIED; explicit API-disabled messages fail. Supply this argument only after independently verifying real database metadata; that prerequisite is now satisfied. The current creation continuation runs it explicitly.

SDK-only check (without database verification argument, the database test skips):
```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pcampusq.firebase.enabled=true' '-Pandroid.testInstrumentationRunnerArguments.class=com.example.campusq.FirebaseCloudVerificationTest' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudVerification=true' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudProjectId=campusq-e06c7' '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudAppId=1:875570165692:android:0252b7d559f6f9dd9f9db9' --max-workers=2 --console=plain
```
These checks do not create users, write documents, invoke functions or request FCM tokens. A later successful denied read would validate only that unauthenticated restriction, not the complete deployed Rules policy.

To redownload config if required, use Firebase Console → Project settings → Your apps → registered Android app → google-services.json. Place it at app/google-services.json. Do not create another app registration or overwrite with a different package/project. Keep private local configuration and Admin SDK credentials out of Git.

To include the real server-denial test, add '-Pandroid.testInstrumentationRunnerArguments.firebaseCloudDatabaseVerified=true' to the device command above. It performs one unauthenticated Source.SERVER services query, expects PERMISSION_DENIED, and never writes. Actual post-creation result:2/2 passed,0 failures/errors/skips; BUILD SUCCESSFUL in1m43s on Pixel_7/API37. The Phase3 report retains the JUnit XML and limitations; a denied read alone is not an exhaustive role/write-policy audit.

## Windows Node assertion
Global Node24.19.0 reproduced a native Windows abort after apps:sdkconfig finished its output: UV_HANDLE_CLOSING in src/win/async.c, exit -1073740791 (0xC0000409). Other CLI metadata commands exited normally. This can make apparently successful output unreliable; always inspect the exit code and resulting config.

An isolated Node24.22.3 invocation successfully retrieved and compared the same server config with exit0:
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase --version
```
Use this wrapper in place of npx.cmd -y firebase-tools@latest for affected CLI commands. Global Node remains24.19.0; no global install or package-lock changes were made. This is a tested workaround, not proof of a unique root cause. The official Node24 changelog records a Windows libuv assertion fix in24.20.0; Firebase also has a matching Windows CLI issue:
- [Node24 changelog](https://raw.githubusercontent.com/nodejs/node/main/doc/changelogs/CHANGELOG_V24.md)
- [Windows assertion fix](https://github.com/nodejs/node/pull/61999)
- [Firebase CLI issue](https://github.com/firebase/firebase-tools/issues/11099)

## SDK compatibility and repository boundary
Firebase BoM34.19.0 and Google Services4.5.0 compile with this project's existing Kotlin2.2 toolchain. The newer BoM35.1.0 was previously tested and rejected because Auth25.0.0 uses Kotlin2.4 metadata. No metadata-check suppression or unrelated toolchain upgrade was made. Main Firebase modules Auth/Firestore/Functions/Messaging are used, without deprecated -ktx modules.

CampusQStateSource preserves the demo ViewModel boundary. ServiceCatalogRepository has demo and explicit one-shot Firebase adapters. FirebaseBackend.fromConfiguration is explicit and not selected by the UI. No authentication workflow, realtime queue or remote mutations are introduced.

## Local emulators
Node>=24.12 and Java21+ are needed locally. Functions deployment runtime is Node22; host Node24 produces an expected emulator runtime mismatch warning. No production function is deployed.

```powershell
npm.cmd ci --ignore-scripts
npm.cmd --prefix backend/functions ci --ignore-scripts
$env:FUNCTIONS_DISCOVERY_TIMEOUT='60'
npm.cmd run test:rules
npm.cmd run emulators
```
All local commands explicitly target demo-campusq-local, never campusq-e06c7. Auth127.0.0.1:9099, Firestore127.0.0.1:8080 and Functions127.0.0.1:5001 use disposable fixtures. The Functions probe is exported only in its emulator and rejects protected operations.

LocalFirebaseBackend exists only in src/debug, uses a separate named app, demo project, in-memory cache and 10.0.2.2 on Android Studio AVD. Release has no emulator factory or debug HTTP exception. Genuine cloud SDK tests use the separate default Firebase app.

Combined baseline suite, with booted AVD:
```powershell
$env:FUNCTIONS_DISCOVERY_TIMEOUT='60'
npm.cmd run test:all
```
This runs59 backend tests and then build, unit tests, lint and device tests. For genuine-config verification, use the same commands with the opt-in and cloud-test ID arguments above, and firebaseEmulators=true. Do not pass firebaseCloudDatabaseVerified=true without independently verified database metadata. Local emulator arguments alone never authorize real-cloud data writes.

Locked JS dependencies retained. Root @grpc/grpc-js1.14.7 override addresses previously identified transitive advisories. CLI npx dependencies are separate and can emit their own deprecation notices. Re-run both npm audits after dependency changes; do not use audit fix --force.

Android callable/FCM delivery is still unverified. Earlier dummy local options could not satisfy non-emulated IID/FCM token prerequisites. Standard SDK component registration is preserved; Node tests verify the local Functions probe. No production callable or notification workflow exists to exercise in Phase3.

## Reference documents
- [Schema](FIRESTORE_SCHEMA.md)
- [Rules and intended permissions](SECURITY_RULES.md)
- [Phase3 completion report](../../CAMPUSQ_PHASE_3_COMPLETION_REPORT.md)
- [Official Android setup](https://firebase.google.com/docs/android/setup)
- [Android SDK releases](https://firebase.google.com/support/release-notes/android)
- [Emulator Suite](https://firebase.google.com/docs/emulator-suite/install_and_configure)
- [Rules tests](https://firebase.google.com/docs/rules/unit-tests)
