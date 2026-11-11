# CampusQ Phase 3 — Firebase foundation plan
Date: 2026-11-11, Asia/Manila.

Scope: preserve all 23 screens, demo hub, five PNG assets and DemoRepository. No Phase 4 authentication workflows, live queues, role provisioning, notification delivery or billing activation.

## Execution record
- [x] Review Phase 2 report/progress, MainActivity, ViewModel, DemoRepository, models and Gradle. Git baseline 49b6315 created after exclusion/content screening.
- [x] Check official SDK releases and run compatibility builds. BoM 35.1.0 failed Kotlin metadata compatibility; BoM 34.19.0 selected with Google Services 4.5.0, preserving AGP/Kotlin.
- [x] Add optional genuine-config processing, SDK dependencies and disabled background FCM initialization. Normal demo build requires no JSON.
- [x] Document all 11 collections, stable IDs/ownership/timestamps/statuses, validations, trusted queue ordering/appointment locks and proposed indexes.
- [x] Add closed prototype rules and emulator-only refusing function probe; no mobile permission opened.
- [x] Add Auth/Firestore/Functions emulator configuration,59 rules/backend tests,6 JVM foundation tests, Android emulator connection test.
- [x] Add shared demo-state and asynchronous service-catalog interfaces/adapters. MainActivity/UI/demo commands remain local.
- [x] Gradle Sync successful; debug APK built;33 JVM tests pass; lint 0 errors/20 warnings. npm audits0 vulnerabilities.
- [x] Final combined run: 59 backend tests and 63 connected tests passed; no device failures/skips; debug build, 33 local tests and lint pass.
- [ ] Real Firebase provisioning/connection blocked by account authorization and missing actual Project/App IDs/config.
- [x] Completion report/progress/evidence updated with actual outcomes and remaining blockers.

## Approved cloud target and blocker
User authorized a new project named CampusQ, Android com.example.campusq, Standard edition(default), asia-southeast1 Singapore. No billing or paid services approved. CLI reports no authorized accounts; no project created, remote rules/indexes deployed or provider enabled.
Run npx.cmd -y firebase-tools@latest login locally, then choose/create an actual available Project ID on Spark. Exact Console/CLI setup in docs/firebase/SETUP.md. Do not invent configuration values. Local demo-campusq-local is emulator-only.

## Security and acceptance boundary
All Firestore client reads/writes denied, even trusted-looking custom claims. Later validators/role policies/server transactions are planned, not implemented. Server SDK bypasses rules and needs independent authorization.
Local foundation acceptance: build and meaningful tests pass, Demo Mode retained, all unavailable external checks stated. Full real-project acceptance cannot be claimed until genuine setup and remote verification complete. Phase 2 accessibility/device matrix remains open.
Stop after Phase 3; Phase 4 requires explicit authorization.
