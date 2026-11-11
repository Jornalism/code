# Firebase setup — CampusQ Phase 3
Status: SDK foundation implemented; **no real Firebase project is connected**. Approved target: display name CampusQ, Android package com.example.campusq, Standard edition (default) database, asia-southeast1 Singapore. Billing must remain off. CLI has no authorized accounts; Firebase MCP/Developer Knowledge tools are not available in this session. Only cloud setup is blocked; local work proceeds as requested.

## Unblock account/project setup
Run these commands in Android Studio Terminal (PowerShell uses .cmd because .ps1 execution is restricted):
```powershell
npx.cmd -y firebase-tools@latest login
npx.cmd -y firebase-tools@latest login:list
```
Complete Google authorization locally; do not paste auth codes, refresh tokens, service-account keys or credentials into chat.
Create a project in Firebase Console: Add project → name CampusQ → choose/record the actual globally unique Project ID → disable Google Analytics for now → create on Spark/free plan. Do not attach a billing account. If institution policy requires an organization administrator, request project creation/IAM from that administrator. Tell the agent the resulting Project ID; it must not guess one from the display name.

Alternatively, once an actual available Project ID has been selected, use:
```powershell
npx.cmd -y firebase-tools@latest projects:create YOUR_ACTUAL_PROJECT_ID --display-name CampusQ
```
Placeholders in this document are instructions, not configured values. No .firebaserc with invented remote aliases is included.

## Provision the approved database and Android app
After project authorization/ID is available:
```powershell
npx.cmd -y firebase-tools@latest firestore:databases:list --project YOUR_ACTUAL_PROJECT_ID
npx.cmd -y firebase-tools@latest firestore:databases:create "(default)" --edition standard --location asia-southeast1 --project YOUR_ACTUAL_PROJECT_ID
npx.cmd -y firebase-tools@latest firestore:databases:get "(default)" --project YOUR_ACTUAL_PROJECT_ID
npx.cmd -y firebase-tools@latest apps:create ANDROID CampusQ --package-name com.example.campusq --project YOUR_ACTUAL_PROJECT_ID
npx.cmd -y firebase-tools@latest apps:list ANDROID --project YOUR_ACTUAL_PROJECT_ID
```
If database creation via CLI asks for billing, **stop** and report the exact requirement; do not enable billing. Console alternative: Firestore → Create database → Standard → (default) → Singapore asia-southeast1 → production/locked rules. Do not choose test/open mode. Verify actual location/edition, not just these intended values.

Retrieve the real Android configuration with CLI using the App ID returned above:
```powershell
npx.cmd -y firebase-tools@latest apps:sdkconfig ANDROID YOUR_ACTUAL_FIREBASE_APP_ID --project YOUR_ACTUAL_PROJECT_ID --out app/google-services.json
```
Ensure package_name is com.example.campusq and project_id is the intended real project. Keep this file local; .gitignore excludes it as requested. It contains app identifiers, not server authority; security still depends on Rules/IAM. Never place Admin SDK keys in Android.

Review and deploy **only** the closed rules and proposed indexes once actual project/edition are verified:
```powershell
npx.cmd -y firebase-tools@latest deploy --only firestore:rules,firestore:indexes --project YOUR_ACTUAL_PROJECT_ID
```
No deployment has been performed in this phase. Do not deploy functions or enable billing. Cloud Functions production generally requires Blaze; approval is a separate decision. Leave sign-in provider/workflow implementation for Phase 4. No Auth provider was remotely enabled. FCM dependency is available, but token registration/notification permission/delivery are deferred.

## Android opt-in
Normal commands need no JSON and stay in Demo Mode:
```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
With genuine app/google-services.json:
```powershell
.\gradlew.bat '-Pcampusq.firebase.enabled=true' :app:assembleDebug
```
Missing config fails early with a clear message. This flag applies Google Services resource processing; it **does not switch the UI repository**. MainActivity/ViewModel still use DemoRepository. FirebaseBackend.fromConfiguration is explicit and unused by the app. Never infer “connected” from adding dependencies or generating resources; verify real project options and a controlled server request later. Closed rules must deny all access.

Versions: officially documented BoM 35.1.0 was tested but Auth 25.0.0 carries Kotlin 2.4 metadata incompatible with existing compiler 2.2. The compatible preceding BoM 34.19.0 is selected without skipping metadata checks or changing AGP/Kotlin. Main Firebase modules (no deprecated -ktx artifacts); Google Services 4.5.0. Final build evidence determines compatibility, not docs alone.

## Local emulators (no account/billing required)
Requirements: Node>=24.12 for rules-unit-testing6, Java 21+ for Firestore; available host Node 24.19.0/Java 25.0.2. Functions deployment runtime is Node 22 (supported); host emulator uses Node 24 and warns about mismatch. No production runtime certification claimed.
```powershell
npm.cmd ci --ignore-scripts
npm.cmd --prefix backend/functions ci --ignore-scripts
$env:FUNCTIONS_DISCOVERY_TIMEOUT='60' # Allow cold local Functions discovery on this workstation.
npm.cmd run test:rules
npm.cmd run emulators
```
All commands use explicit `demo-campusq-local`, a Firebase demo identifier that is **not** a provisioned project. Auth127.0.0.1:9099, Firestore127.0.0.1:8080, Functions127.0.0.1:5001. UI disabled; no emulator data import/export; shutdown destroys test fixtures. Functions exports an emulator-only rejecting probe and no production functions.

Android Studio AVD uses 10.0.2.2 to reach host ports. LocalFirebaseBackend exists in src/debug only; explicit named app, demo project, in-memory Firestore cache and emulator endpoints. Debug HTTP exception is limited to loopback/10.0.2.2; release has no emulator factory or cleartext exception. Do not use these endpoints on physical devices without a separately reviewed host/adb routing setup.

For the combined backend and full Android verification (requires a booted AVD):
```powershell
$env:FUNCTIONS_DISCOVERY_TIMEOUT='60'
npm.cmd run test:all
```
This runs 59 backend tests before invoking the Gradle build, local tests, lint and connected tests inside the same local emulator session. Windows runner uses an explicit Gradle wrapper path.

While all emulators run:
```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.firebaseEmulators=true' --max-workers=2
```
Without that argument, the one emulator-specific Android check is skipped, not passed. Existing demo tests can run without Firebase emulators. The rules suite includes anonymous Auth smoke solely locally; it does not enable anonymous production auth.

Reproducibility: package locks committed. @grpc/grpc-js override1.14.7 fixes two upstream advisories in the JS test SDK's pinned1.9.16 dependency. Re-run npm audits and all emulator tests after upgrades; never use audit fix --force to downgrade Firebase9. Both project package audits were clean after override. Firebase CLI itself comes from npx latest and has independent transient dependencies.

## References checked
- [Android setup and plugin](https://firebase.google.com/docs/android/setup)
- [Android releases and BoM 34.19.0 mapping](https://firebase.google.com/support/release-notes/android)
- [Local Emulator Suite](https://firebase.google.com/docs/emulator-suite/install_and_configure)
- [Rules unit tests](https://firebase.google.com/docs/rules/unit-tests)
- [Functions supported runtimes](https://firebase.google.com/docs/functions/manage-functions)

Android callable limitation: the Functions SDK requires IID and attempts token acquisition from non-emulated FCM/Installations. With deliberately non-production local options, the call fails its API-key precondition. Removing IID registration also breaks Functions component dependencies, so standard SDK registration is retained. Android tests cover local Auth and Firestore; Node tests verify the Functions emulator probe. Android callable/FCM verification awaits genuine configuration and reviewed notification/consent behavior. No fake production key or substitute IID implementation is used.
