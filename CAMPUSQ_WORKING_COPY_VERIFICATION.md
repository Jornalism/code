# CampusQ working-copy verification

Date: 2026-11-13 (Asia/Manila). Scope: only C:\Users\nico\AndroidStudioProjects\CampusQ_Working.

## Verified project state

Android Studio's project index resolves settings.gradle.kts inside CampusQ_Working; the active editor and .idea Gradle configuration agree. Git's worktree and .git directory also resolve inside CampusQ_Working. No linked source/backend/Gradle/documentation directories were found. Required Gradle wrapper, settings, module build file, manifest, launcher, backend and migration files exist. Every Git-tracked file is present; no tracked deletions.

This is an existing implemented application, not a new frontend. Inspected MainActivity → CampusQLocalApp, local HTTP API/repository, server routes, SQLite initialization/migration, fixture and existing tests. Normal launch uses the local authentication/workflow root. All 23 screen functions remain, plus the historical Demo hub. All five original PNG files match Git HEAD; theme and branding resources have no pending changes against HEAD. Database files and their WAL/SHM companions are present.

Git already contained substantial modified/untracked Phase 3, Phase 4 and offline work at entry. HEAD remains 2f323bf; checkpoints 49b6315 and 2f323bf exist. git fsck --connectivity-only exited 0 (existing dangling blobs were left intact). No staging, commit, reset, checkout, pruning or history rewrite was performed. Reviewed offline completion/progress reports, README/presentation runbook and Phase 3/4 reports. Historical Android XML separately confirms 81 cases/5 skips/0 failures; fresh results below are independent.

## Fresh checks performed in this working copy

- Backend: node --test --test-concurrency=1 backend/local/acceptance.test.mjs — 20 passed, no failures/skips, exit 0.
- Gradle: :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --offline --max-workers=1 --console=plain — exit 0, 6m44s. JVM XML: 70 passed, no failures/errors/skips. Lint: 0 errors, 24 warnings. No suppressions or dependency upgrades added.
- Full emulator: :app:connectedDebugAndroidTest with localBackend=true, --offline, --max-workers=1 — exit 0, 14m19s, Pixel_7 AVD/API 37. Final XML: 81 unique cases, 76 passed, 0 failures/errors, 5 intentional inactive Firebase skips. Console reports 86 callback events; the XML count is used here.
- All eight live-server Android cases passed: independent role sessions/permissions/shared state; Student registration and queue polling; Staff ticket processing; Student booking/rescheduling/cancellation; Staff appointment review; Admin service/Staff creation; unavailable-server login; actual launcher activity close/relaunch and encrypted-session/ticket restoration.
- All 48 component screen captures passed: 23 screens plus historical hub at normal phone and 320dp/1.5 font scale. These are render checks, not exhaustive live-control or accessibility acceptance.
- Windows Start-CampusQ.cmd launched successfully on loopback port 8788 using a new isolated database; /health returned ok/schema 1. Test launcher stopped afterward.
- Fresh Android acceptance fixture on loopback 8787 passed /health. After the live tests, stopping/restarting it with --resume preserved every recorded table count: 9 users, 8 services, 4 queue tickets, 3 appointments, 3 transactions, 38 activity rows. SQLite integrity_check returned ok. Fixture stopped at handoff.
- Presentation SQLite readback: copied its SQLite/WAL/SHM into isolated evidence before opening it. Schema 1, integrity ok, zero foreign-key violations, zero users/services/tickets/appointments/transactions. Source presentation SQLite/WAL/SHM hashes remained unchanged.
- Debug APK inspected: no local-acceptance credential asset, fixture metadata, SQLite or backend server entries. SHA256: FEC469278AC24B86969051249DE8D94E2E1BB04946FE407A2953BC865A5196A4.
- git diff --check passed; private fixtures and evidence are ignored.

Backend coverage includes real authentication/permissions, shared queue and appointment transitions, conflict/concurrency protection, restart persistence and online-backup readback. Android tests confirm emulator-to-laptop HTTP connectivity and core role workflows. No Firebase cloud, billing, firewall, execution-policy or network configuration changes were made.

## Changes made during this verification

1. backend/local/fixture.mjs: copied acceptance-fixture.json contained an absolute database path into the original CampusQ project. Added a guard that rejects fixture databases outside this checkout's backend/local/data directory before opening SQLite. Tested that the stale path fails closed, then verified a valid working-copy --resume succeeds.
2. Preserved old metadata as backend/local/data/acceptance-fixture-before-working-verification.json (ignored). Created a fresh random acceptance database, metadata and androidTest-only credential asset inside CampusQ_Working. Existing copied databases were retained; presentation data was not populated or reset.
3. docs/offline/README.md: documented copied-checkout fixture handling.
4. Added this report and ignored evidence under docs/offline/evidence/working-copy-verification (fresh test logs/XML, lint, database readback and restart counts).

No Android application source/design changes were needed. No command targeted the original CampusQ or CampusQ_OriginalBackup for modification. Neither directory requires changes for these checks.

## Remaining unverified acceptance and setup

- No physical Android device was connected. Phone LAN connectivity, simultaneous Android devices, Wi-Fi/client isolation and classroom network behavior remain unverified. No firewall change was attempted.
- WAN-disconnected classroom rehearsal was not performed. Gradle used --offline and runtime tests used local HTTP, which does not prove the physical network scenario.
- Full OS process-death/reboot restoration remains unverified; the actual activity close/relaunch test passed.
- HTTPS certificate provisioning/trust, exhaustive TalkBack/accessibility and every live profile/settings/admin edge interaction remain unverified.
- Five Firebase-only cases were intentionally skipped; they are not offline acceptance passes.
- Presentation database is empty. The laptop operator still needs to provision the first Administrator privately with node backend/local/operator.mjs bootstrap-admin, then create real services and Staff. No default credentials were created. Interactive hidden-password operator entry was not exercised in this run.

Use docs/offline/PRESENTATION.md for the remaining rehearsal. Available automated offline checks passed; complete classroom acceptance is not claimed.
