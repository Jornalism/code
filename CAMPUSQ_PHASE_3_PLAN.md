## Latest Rules publication — 2026-11-12
Approved Rules-only deploy completed; exact source equality and actual unauthenticated read/guarded-write denials verified. Billing disabled; no other resources deployed. Earlier no-release observations below are historical. Phase4 unstarted.

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
- [x] 2026-11-12: authenticated CLI confirms campusq-e06c7 and registered Android app; genuine JSON matches server configuration; opt-in build and device SDK initialization verified.
- [x] Explicitly approved Firestore API enablement: single-service operation succeeded; independent state GET confirms ENABLED.
- [x] Historical API-only pre-creation metadata: empty database/Rules-release lists and404 default lookups. Database absence was superseded by the separately authorized creation below.
- [x] Separately authorized default database creation: preflight Singapore available/default resource location unset/billing disabled; create exact Standard Native Singapore(default); independent post-create metadata freeTier=true, billing still disabled.
- [x] Targeted Android cloud verification:2/2 passed, no skips; registered SDK IDs and real unauthenticated server-read denial.
- [x] Explicitly approved Rules-only publication: release cloud.firestore/ruleset a42e813a-233e-4ca2-969b-6592ec55c483 verified; exact tested-source match; unauthenticated read/guarded write denied403; no documents changed.
- [x] Completion report/progress/evidence updated with actual outcomes and remaining blockers.

## Verified cloud target and remaining scope
CampusQ / campusq-e06c7, Android com.example.campusq, App ID1:875570165692:android:0252b7d559f6f9dd9f9db9 verified. Genuine app/google-services.json is ignored and server-matched.
Firestore API enabled under prior approval. New creation approval fulfilled: exactly(default),STANDARD,FIRESTORE_NATIVE,asia-southeast1,freeTier=true. Singapore was available and default resource location unset before creation. Billing GET before/after confirms billingEnabled=false/no billing account. No upgrade.
SDK initialization and real unauthenticated server-read denial passed2/2 device tests with no skips. No published Rules release exists after CLI creation; no custom policy/index/function deployed. Any explicit deployment requires separate authorization. Local demo-campusq-local remains emulator-only.
See docs/firebase/SETUP.md and the current completion report.

## Security and acceptance boundary
Local prototype rules deny all client operations and remain unchanged. Real database retains CLI default closed access with no published Rules release. Actual unauthenticated server-read denial is verified; no write probe or exhaustive role-policy audit was performed. Later validators/role policies/server transactions are planned, not implemented. Server SDK bypasses rules and needs independent authorization.
Local foundation acceptance: build and meaningful tests pass, Demo Mode retained, all unavailable external checks stated. Database configuration/SDK/read-only connection foundation is verified; explicit versioned Rules-source certification remains unavailable until an approved deployment. No broader production security certification is claimed. Phase 2 accessibility/device matrix remains open.
Stop after Phase 3; Phase 4 requires explicit authorization.
