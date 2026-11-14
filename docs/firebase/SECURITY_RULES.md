# Published CampusQ deny-all baseline — 2026-11-12
User-approved Rules-only publication completed on campusq-e06c7/(default). Release projects/campusq-e06c7/releases/cloud.firestore points to rulesets/a42e813a-233e-4ca2-969b-6592ec55c483.
The published single-file source exactly matches local firestore.rules, including line endings. Both SHA256:862A1CFA49BCBF014C43AC89DEB6C44D9F217625A7574F9104CDAF61BAD419D0.
Actual unauthenticated read and safely conditional write both returned403 PERMISSION_DENIED. The write had an impossible year9999 updateTime precondition, so no document could be created/modified. No indexes/Functions/other resources deployed; billing remains disabled.
This publication resolves the previously absent release. Further policy changes/deployments need separate authorization. No Phase4 work. Full evidence/method/limitations in CAMPUSQ_PHASE_3_COMPLETION_REPORT.md.

## Historical local-only preparation record
The no-publication statements below describe the earlier local-check task, before explicit deployment approval. They are superseded by the verified published status above.

# CampusQ security foundation
Phase 3 rules are intentionally **deny all**, including authenticated students and trusted-looking staff/admin claims. This is a closed prototype, not completed role access. UI Demo Mode never requests Firestore. The unused service catalog adapter returns permission errors under current rules.

The existing local baseline uses Rules language v2 and one recursive catch-all: `allow read, write: if false;`. It grants no client access on any collection or nested path. Server/Admin SDK access uses IAM and bypasses these client rules; this is not a complete server authorization policy.

## Intended future permissions (not active)
| Role | Read scope | Allowed commands after authorization |
|---|---|---|
| Student | Own user/profile, tickets, appointments, transactions, notifications; active catalog and sanitized institution policy | Own validated profile edits, join/cancel, book/reschedule/cancel, mark own notification read |
| Registrar Staff | Own staff identity; assigned service/counter work records; minimum necessary student display data | Call/start/complete/skip/no-show within assignment and valid transition |
| Administrator | Operational catalog/staff/settings/reports; audited minimum necessary PII | Provision/disable staff, service/settings management, approved operational corrections |
All queue numbering, locks, appointments/conflicts, role/status/assignment changes, history and audit writes go through trusted backend commands. No direct client administrative writes, even for ADMIN.

## Role trust and bootstrap
Demo UserRole and navigation selection are not authentication. Firebase ID tokens identify uid. A trusted provisioner using least-privilege IAM/Admin SDK assigns custom claims `role` and `authorizationVersion`, and mirrors them in users/uid. Never derive authority from request payload, email alone, profile roleTitle or writable Firestore role data. Account activation, token refresh, revocation and authorization-version checks are required; stale claims must not retain access after deactivation.
First admin bootstrap is a separately reviewed operator command under institution IAM, never a mobile signup flag. No bootstrap script is included in Phase 3. Future student registration sets STUDENT only through trusted validation. Check verified university email and university identity policy before granting services.

Admin SDK bypasses Security Rules. Every future callable must independently validate Auth/App Check as appropriate, active authorization, assignment, input schema, expected version/idempotency and audit output. No production callables exported now. Emulator-only probe refuses unauthenticated requests and refuses operations for authenticated requests; it does not prove future command security.

## Tests and review
Node rules tests seed known fixtures with rules disabled only inside the test harness; clients test denied get/list/create/update/delete for anonymous/student/staff/admin/forged claims across 11 collections, unknown/nested paths and atomic escalation. Positive harness control proves fixtures exist. Auth/Functions emulator smoke checks isolation and closed callable response. These are deny-all regression tests, not authorization/role lifecycle or concurrent queue tests.
No writable validators are present because **zero writes are allowed**. Before granting any create/update, add identical complete post-state validators for required/allowed fields, types, lengths, immutable ownership, timestamp origin and legal transitions; test field deletion, owner/role swapping and create→update bypasses.

## Deployment boundary
This task prepares and checks local files only. Do not publish rules or run firebase deploy without separate explicit approval. The real CampusQ default database was previously verified as Standard/Native/Singapore with billing disabled; CLI creation provides closed client access but did not publish a Rules release. Local test results are not evidence that this file is deployed. No cloud metadata request or mutation is required for this local check.


## Local validation — 2026-11-12
The actual firestore.rules file compiled/loaded successfully in the Firestore emulator. Focused suite:58 tests passed,0 failures/skips, exit0. Anonymous/student/staff/admin/forged claims cannot get/list/create/update/delete across11 collections; unknown/nested paths and escalation batches are denied. A positive fixture-control test passed.

Command:
```powershell
npx.cmd -y --package=node@24.22.3 --package=firebase-tools@latest -- firebase emulators:exec --project demo-campusq-local --only firestore "node .firebase/phase3-local-rules-check.cjs"
```
The ignored helper restricts execution to demo-campusq-local and127.0.0.1:8080, and positively selects the58 existing Firestore cases. Disposable emulator fixtures only; no real project requests/data or deployment. Auth/Functions smoke was excluded. An initial negative filter accidentally included that unrelated smoke case and failed with its emulators off; corrected helper rerun passed all58.
firestore.rules and firebase.json required no edits. Full details and hashes are in CAMPUSQ_PHASE_3_COMPLETION_REPORT.md.
