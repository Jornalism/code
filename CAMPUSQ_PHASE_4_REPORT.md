# CampusQ Phase4 initial authentication foundation — implementation report
Work dates:2026-11-12–13, Asia/Manila.
Status:IMPLEMENTED locally. Authentication tests/build checks pass; overall connected verification is NOT CLEAN because existing PixelCopy screenshot captures timed out. Production authentication/provisioning remains BLOCKED/PENDING APPROVAL. Phase4 is NOT fully complete. Phase5 unstarted.

## Implementation and architecture
- Added AuthRepository/FirebaseAuthRepository/UnavailableAuthRepository, AuthViewModel, SessionCoordinator, authenticated identity/claims/session models, UserProfileRepository contract and closed implementation.
- Repository mutations (register/login/reload/verification/reset) are gated in this stage. Only the explicitly configured local demo emulator adapter accepts them. Default production adapter fails safely with PROVIDER_UNAVAILABLE before an SDK mutation call; a configured JSON/build flag cannot bypass this approval boundary.
- FirebaseAuth owns persisted identity/session. No app password/token persistence. Auth and ID-token listeners use callbackFlow and remove both listeners in awaitClose; ViewModel cleanup cancels coordinator observation/operation.
- SessionState distinguishes Initializing, SignedOut, Submitting, EmailVerificationRequired, ProvisioningPending, ProfileUnavailable, Authorized and RecoverableError.
- Verified email is not enrollment/role authorization. Invalid/missing claims remain pending; valid role/version without a verified UID/role/version/active profile remains unavailable. Production ClosedUserProfileRepository makes no Firestore requests and cannot authorize anyone.
- Future consistent trusted profile decisions are tested with fakes, not implemented as a production role grant. No live business destination exists in the authentication graph, even for its future Authorized state.
- Manual construction retained. Existing CampusQViewModel/DemoRepository still own unchanged demo operations and shared state.
- Current production metadata readiness remains based on the inspection's404 CONFIGURATION_NOT_FOUND. No provider initialization/configuration, production account creation, IAM or cloud resource change was performed to probe it.

## Registration — IMPLEMENTED / production BLOCKED
Existing fields/layout and generic validation retained (name>=3 chars, Student ID>=5, generic email, password>=6, confirmation match). No domain/enrollment policy invented.
Valid input calls repository account creation and Auth display-name update, then verification mail. No role argument/claim assignment; no Firestore user/profile write. Student number is input validation only and not represented as persisted data.
Signed-in identity is retained after verification/onboarding failure; UI shows its pending/verification state, safe error and retry path instead of silently creating another account. Duplicate concurrent submissions and repeated registration on a known signed-in identity are blocked.
Production account/email calls remain blocked by the adapter gate. SDK path is exercised only locally.

## Login, recovery, verification and logout — IMPLEMENTED / production BLOCKED
Existing login form submits to AuthViewModel; loading disables duplicate submission. Safe typed error mapping covers invalid credentials, disabled account, network, throttling, unavailable provider, weak password, duplicate registration and generic failure. Raw SDK exception messages/tokens are never shown or logged by app code.
Recovery uses the existing email field/dialog. Generic confirmation avoids asserting whether an account exists; unsuccessful network/provider requests do not falsely report success.
Verification panel offers resend with60-second cooldown and explicit recheck. Recheck reloads Firebase user and forces token refresh once; verification uses actual SDK emailVerified. Verification only confirms inbox control.
Logout clears SDK identity, invalidates auth navigation and signs out again if an earlier in-flight login completes afterward. Listener/token changes trigger fail-closed reconciliation. No custom password/token cache.
Passwords/confirmation use remember (not rememberSaveable) and clear after accepted submission; leaving their composition discards transient state. They are never placed in navigation arguments, preferences, reports or logs.

## Navigation and Demo Mode — IMPLEMENTED
Public CampusQApp now coordinates two separate roots:
- Authentication graph: existing splash/login/register plus session status panel; no student/staff/admin business routes or demo repository data.
- Demo graph: original routes and screen callbacks retained in CampusQDemoApp. Existing Student/Staff/Admin preview choices explicitly enter this mode.
A root mode key and authentication navigationEpoch prevent auth callbacks from changing application mode or restoring old auth history. Demo preview grants no Firebase role. Returning to authentication displays the actual session; incomplete live features display honest unavailable/onboarding UI.
All23 screens+hub remain in the inventory. Five PNG assets unchanged. No Student/Staff/Admin screen file, DemoRepository or CampusQViewModel change.

## New source/test files
- app/src/main/java/com/example/campusq/data/auth/AuthModels.kt
- app/src/main/java/com/example/campusq/data/auth/AuthRepository.kt
- app/src/main/java/com/example/campusq/data/auth/FirebaseAuthRepository.kt
- app/src/main/java/com/example/campusq/data/auth/UserProfileRepository.kt
- app/src/main/java/com/example/campusq/data/auth/SessionCoordinator.kt
- app/src/main/java/com/example/campusq/viewmodel/AuthViewModel.kt
- app/src/main/java/com/example/campusq/ui/navigation/CampusQRoot.kt
- app/src/main/res/values/auth_strings.xml
- app/src/test/java/com/example/campusq/AuthSessionTest.kt
- app/src/androidTest/java/com/example/campusq/Phase4AuthUiTest.kt
- app/src/androidTest/java/com/example/campusq/Phase4AuthEmulatorTest.kt

Modified:src/debug/.../LocalFirebaseBackend.kt (isolated named apps/dummy keys for emulator tests); AuthScreens.kt (callbacks, secret lifetime, loading/recovery feedback, previews); CampusQAppNavHost.kt (original demo graph retained behind mode boundary); app/build.gradle.kts and gradle/libs.versions.toml (lifecycle Compose); .gitignore (local Phase4 evidence exclusion); CAMPUSQ_UI_PROGRESS.md (final progress entry).
Documentation added:this report and docs/firebase/PHASE_4_PROVISIONING.md.
Deleted files:NONE.
Pre-existing pending Phase3 reports/setup/security docs and FirebaseCloudVerificationTest were preserved; they are not new Phase4 changes. No Git reset/staging/commit performed.

## Dependencies and configuration
No existing version changed. Added androidx.lifecycle:lifecycle-runtime-compose at the existing2.9.0 lifecycle version for collectAsStateWithLifecycle. No Hilt, coroutines-test or Task-await dependency added; SDK tasks bridged using cancellable continuations.
Google Services4.5.0/Firebase BoM34.19.0/Kotlin2.2.10/AGP9.3.3 retained. campusq.firebase.enabled remains optional and defaultfalse.
firestore.rules, firebase.json, indexes, Functions sources, manifest settings, private Google config and both JS package locks unchanged. No rule/provider/resource deployment or billing change.

## Tests and checks — actual results
- Gradle Sync:PASS.
- :app:compileDebugKotlin:PASS, BUILD SUCCESSFUL in1m27s.
- :app:testDebugUnitTest :app:assembleDebug :app:lintDebug initial combined check:PASS, exit0, BUILD SUCCESSFUL in6m47s.
- JVM XML:64 tests passed (31 new AuthSessionTest +26 existing domain +1 inventory +6 foundation),0 failures/errors/skips.
- Initial lint:0 errors/23 warnings. Fixed new plural-count warning with a proper plurals resource. Updated lint now0 errors/22 warnings; no suppression/baseline added.
- Remaining22 warnings:1 OldTargetApi,1 AndroidGradlePluginVersion,10 GradleDependency,1 NewerVersionAvailable,7 UnusedResources (existing colors),2 IconXmlAndPng. The lifecycle artifact update notice is new; Kotlin's repository update notice also refreshed. Versions intentionally preserved.
- IDE analysis:AuthScreens no errors (warning analysis timed out); CampusQRoot no errors, remaining style suggestions recorded, not claimed warning-free.
- Existing backend suite in the combined local run:59 passed,0 failures/skips.
- Initial full connected run:73 cases,69 passed,2 SDK-cache setup failures,2 production-cloud skips. All6 new Compose tests and existing demo/navigation/screen cases passed.
- SDK setup repair: debug factory now accepts isolated test instance IDs/dummy API keys, avoiding reuse of a deleted Firebase SDK cached context. No production configuration change. The test now reconstructs only the repository over SDK-owned identity, not a deleted SDK app.
- Targeted repaired SDK run:2/2 passed,0 failures/errors/skips; BUILD SUCCESSFUL in1m55s, exit0. Actual local email verification/reset/login/logout and the mutation gate exercised.
- Full combined rerun on Pixel_7/API37:73 XML cases,70 passed,1 failed,2 deliberate real-cloud skips; exit1, BUILD FAILED in10m56s. All8 new Android cases and existing functional/navigation cases passed. The sole failure was AllScreensTest.compactLargeTextScreenRendersAndCaptures[1: Campus], an existing PixelCopy capture timeout.
- Standalone unchanged AllScreensTest rerun:48 cases,46 passed,2 failed,0 skips; exit1, BUILD FAILED in8m46s. Failures:compact Create Account and phone Quick Actions, both Failed waiting for PixelCopy. The previously failed compact splash passed this rerun. Screen-content assertions preceded capture and did not fail. The first full run had48/48 screen captures passing; later runs are explicitly NOT clean screenshot passes.
- Environment investigation:device awake, CampusQ test activity focused, no ANR since boot, no graphics error in the filtered sample. Host free physical memory was about368MB/15.8GB and Android background Play Store dex compilation was active. Resource pressure is a possible contributor; root cause is NOT conclusively verified. No unchanged UI or screenshot test was rewritten/suppressed to hide these failures.
- Genuine-config opt-in check:PASS, exit0, BUILD SUCCESSFUL in1m49s. processDebugGoogleServices, assembleDebug, testDebugUnitTest, lintDebug and the SDK-identity-only connected test ran with campusq.firebase.enabled=true. Identity test XML:1 passed,0 failures/errors/skips; expected package, project campusq-e06c7, app registration, default FirebaseApp, SSL endpoint and no signed-in identity were verified. This is configuration/SDK initialization verification, not production Auth-provider readiness or account/mail testing.
- Genuine-config APK installation and MainActivity cold launch:PASS. adb install reported Success; am start reported Status:ok and the CampusQ MainActivity held focus. No credentials entered. Optional UI-hierarchy export did not execute because automatic permission review timed out; no visual assertion is based on that attempt.
- Final APK SHA256:B0435B8D9F9E08AA1824FB4C58D5034973AF7C3739FC1FE820574B262C20586C.
- Compose previews:LoginAuthPreview,RegistrationAuthPreview and VerificationPanelPreview rendered successfully and visually reviewed. Existing forms retain layout/branding and scrolling; the new panel is separate from business screens.
- No production Auth workflow tests or account/email requests. No fresh cloud mutation/Rules check was needed; deny-all source hash is preserved locally.
XML results from failed and passing runs are retained separately under ignored docs/phase-4/evidence/2026-11-13/. Raw Auth-emulator action-link logs are not copied into project evidence.

## Reproducible checks
Run commands from the project root in PowerShell. These commands use only the local demo emulator project; no deployment command is part of this stage.
```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --max-workers=2 --console=plain
$env:FUNCTIONS_DISCOVERY_TIMEOUT="60"
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase emulators:exec --project demo-campusq-local --only auth,firestore,functions "node backend/tests/run-all.cjs"
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.campusq.AllScreensTest' --max-workers=2 --console=plain
```
The genuine-config check additionally uses -Pcampusq.firebase.enabled=true and processDebugGoogleServices. Its single configuredSdkMatchesRegisteredAndroidApp case opts in with firebaseCloudVerification=true, firebaseCloudProjectId=campusq-e06c7 and the existing registered app ID; it does not perform a document operation or Auth workflow.

The existing all-in-one runner runs59 backend cases then JVM/build/lint/connected checks with firebaseEmulators=true. Production cloud instrumentation is excluded by default. Node24.22.3 is an isolated invocation workaround for the previously observed Windows assertion; no system Node version was changed.

## Test scope
JVM fakes cover registration validation/mismatch, duplicate submits, partial verification failure preserving identity, login/error cases, verification refresh/cooldown, generic reset, logout/in-flight completion, initial restoration, profile mismatch/unavailable/inactive/stale version, all role boundaries, mode isolation and listener cleanup.
Compose tests cover login password saved-state clearing, registration verification UI, verified/no-role pending UI without demo data, logout/back graph behavior, late callbacks while in Demo Mode and recovery confirmation. Compose tests use fakes; SDK tests exercise the adapter directly. A full UI-to-Auth-emulator end-to-end journey is not separately verified.
SDK emulator tests cover actual email/password create/update-name/verification action code/reload, reset/old-password rejection/new-password login, repository reconstruction over the SDK-owned session and signout, plus default adapter mutation gate.
Repository reconstruction checks reuse of SDK-owned identity without supplying credentials. Full process-death/disk-restoration/physical-device persistence is NOT separately verified. Emulator action-code handling does not prove real mail delivery.

## Trusted provisioning / future rules — PENDING APPROVAL / NOT IMPLEMENTED
See docs/firebase/PHASE_4_PROVISIONING.md for UID schemas, eligibility, student/staff/admin and first-admin provisioning, trusted custom claims, version/active checks, role transitions/revocation and idempotent partial-failure recovery.
No operator credentials or production provisioning code added. No users/{uid},studentProfiles/{uid},staffProfiles/{uid} production documents created. No new Firestore permissions implemented/published.
Institution domains/enrollment/student-number uniqueness policy, required program/year data, operator ownership/IAM and first-admin identity remain undecided.
Production Cloud Functions/Identity Platform paid upgrades are not introduced. Any proposed paid feature must stop for approval.

## Pending production steps
1. Separately approve initializing/enabling Email/Password in Firebase Console → campusq-e06c7 → Authentication → Get started → Sign-in method → Email/Password.
2. Separately approve production account/email operations and any real test accounts; only then review lifting the adapter's production gate. The Gradle Firebase flag alone is insufficient.
3. Approve institutional eligibility and provisioning/IAM process.
4. Review and separately authorize minimal profile-access Rules; preserve deny-all until that happens.
5. Verify real verification/recovery delivery, production session/role behavior and eventual backend enforcement after those approvals.
No action above was performed in this initial implementation stage.

## Verification limitations / BLOCKED
- Overall connected suite is not a clean pass because of intermittent existing PixelCopy capture timeouts. Revalidate captures on a stable emulator/host before claiming complete visual acceptance. Failed XML is retained; no assertion is bypassed.
- Full TalkBack/keyboard/physical-device/cross-device matrix and full process-death session persistence are not verified by this stage.
- Real production registration/login/mail delivery/provider readiness/trusted profile access/claims provisioning remain untested and unapproved. Successful fake authorization tests are not production authorization verification.

## Git checkpoint recommendation
Keep49b6315/2f323bf intact. After review, create a new checkpoint containing reviewed Phase3 pending work plus Phase4 sources/docs/tests as appropriate. Screen staged content first: exclude google-services.json, local.properties, .firebase state, dependencies/build/evidence, credentials and emulator action links. No checkpoint is created automatically in this stage.

## Acceptance
IMPLEMENTED:local authentication/session/UI foundation and fail-closed role boundary.
TESTED:64 JVM,59 backend emulator,8 new Android auth/UI cases,1 genuine-config SDK identity case, sync/build/lint/previews and launch; exact individual runs above.
BLOCKED/NOT CLEAN:intermittent PixelCopy captures; full visual/cross-device/process-death acceptance.
PENDING APPROVAL:production provider/account/email operations, trusted provisioning/IAM and future profile rules.
NOT IMPLEMENTED:production profiles/claims provisioning, live role business features, new Firestore permissions, Phase5.

Safe local foundation implementation is available for review. Full Phase4 acceptance is NOT met while trusted production provisioning/profile access/authorization and provider setup remain unapproved or unavailable. Phase5 remains unstarted.
