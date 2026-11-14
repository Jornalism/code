# CampusQ Student registration fix

Date: 2026-11-13 (Asia/Manila). All project edits are inside CampusQ_Working. This report supersedes the auto-login registration behavior described in the earlier offline implementation report.

## Root cause

LocalSessionViewModel.register() called POST /api/auth/register and then immediately called POST /api/auth/login through establish(). establish() saved the bearer token in LocalSessionStore and assigned the authenticated user. CampusQLocalApp correctly opened the protected dashboard when that user became non-null. Registration was therefore implicitly performing a second authentication operation on the client.

The local backend was already correct: registration returns HTTP 201 with a safe Student account object only. It does not return a token or create a session. No backend endpoint, SQLite schema or migration change was necessary.

## Changes implemented

- app/src/main/java/com/example/campusq/viewmodel/LocalSessionViewModel.kt: removed automatic login/establish from registration. A successful save now emits a separate registrationSucceeded flag and the message: Registration Successful. Your account has been saved. Please log in with your email and password. Registration is rejected while initialization/submission or an existing authenticated/unconfirmed session is active, without replacing or clearing that session. New auth operations clear stale messages.
- app/src/main/java/com/example/campusq/ui/navigation/CampusQLocalApp.kt: consumes the success flag, removes Registration from the back stack and returns to the existing Login screen with the success message. Registration does not populate the authenticated user; protected screens remain in the existing authenticated branch.
- app/src/androidTest/java/com/example/campusq/LocalBackendUiTest.kt: strengthened the existing registration/queue/logout test to require manual login, assert no saved token or protected API access after signup, check empty password fields on reopening Registration, preserve normal session restoration, and check that signing out the UI does not revoke an independent Student session. Added a duplicate-registration UI test that stays on Registration, shows the error, remains signed out and subsequently logs in to the original saved account.
- backend/local/acceptance.test.mjs: added real HTTP/SQLite assertions for one saved Student account, no registration token/session, rejection of protected access, duplicate prevention, explicit login, logout, account retention and an unaffected independent Admin session.
- docs/offline/README.md and docs/offline/PRESENTATION.md: corrected the presentation instructions to require manual login after registration.
- This report and ignored evidence under docs/offline/evidence/registration-fix were added.

RegistrationScreen already clears both transient password fields when submitting and uses non-saveable password state; that behavior was retained. Popping its navigation entry also removes its temporary form state. Failed registration does not emit success or navigate away. No account deletion or global session revocation was introduced. Explicit login, encrypted token saving/restoration, logout and role permissions remain intact.

## Actual verification

- Backend: 21 tests passed, 0 failed/skipped; real HTTP and SQLite, including the new registration/session regression. Exit 0.
- JVM: 70 tests passed, 0 failures/errors/skips in the result XML.
- Default debug build and lint: :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --offline --max-workers=1 --console=plain passed in 5m05s. Lint: 0 errors, 24 existing warnings. No suppressions or failing-test removals.
- Focused Android suite: LocalBackendUiTest, LocalBackendIntegrationTest and LocalMainActivityTest, with localBackend=true. All 9 cases passed on each of Pixel_7/API 37 and Student/API 37: 18 total passes, 0 failures/errors/skips. Gradle exit 0 in 6m55s; archived XML confirms both counts.
- Actual UI assertions passed for Registration Successful → Login → manual email/password entry → Student Dashboard, no pre-login identity/saved token/protected data, password clearing, duplicate errors remaining on Registration, queue/history operations, logout and login-session restoration. Staff and Admin login/workflows also passed, including service/Staff creation, ticket processing and appointment review/rescheduling/cancellation.
- Final default APK rebuilt without the test overrides: exit 0 in 30 seconds. Application ID com.example.campusq, backend http://10.0.2.2:8787. APK path: app/build/outputs/apk/debug/app-debug.apk. SHA256: 4F7CCC1C9196ABB759C9F5155755296C7740EA451A6F65CE260058DD51DEFECB.
- Installed that final normal APK with adb install -r on emulator-5554 and emulator-5558; both reported Success. No app-data clear, uninstall or logout of the normal application was performed.
- Main APK contains no fixture credential asset, SQLite, fixture metadata or backend server entries. Original androidTest credential asset was restored byte-for-byte after the isolated run.
- All 23 screen functions remain. All five original PNG assets match Git HEAD. git diff --check passed, no tracked deletions; HEAD remains 2f323bf. No staging, commit or history rewrite.

## Isolation and preservation

The existing presentation server was already running on loopback 8787 and its SQLite files were in use. It was left running under the same process, and the presentation database was not opened, reset, deleted or used for test registration. Byte-for-byte database hashing was unavailable while that server held the files; no such hash comparison is claimed for this run.

Backend tests used their disposable acceptance database. Android tests used a fresh SQLite fixture under ignored working-copy evidence on loopback 8789. An ignored Gradle init script used application ID com.example.campusq.registrationtest so test session clearing and Keystore actions could not affect the normal app. Final package inventory confirmed the temporary test packages were absent. The isolated server was stopped; only the original presentation listener remained. Test database/evidence files were preserved. The normal debug configuration was restored before the final build/install.

No edits were made to CampusQ or CampusQ_OriginalBackup. No Firebase cloud, billing, firewall, dependency-version, backend-schema, branding or screen-layout changes were made.

## Limits

This was a focused registration/authentication regression run, not a new execution of the entire historical screenshot/Firebase suite. No physical-phone/LAN or WAN-disconnected classroom test was performed. The presentation accounts were not used or inspected; acceptance accounts were isolated. Existing normal-app session contents were not inspected after the update. Full process-death/reboot recovery and exhaustive accessibility remain outside this verification.
