# CampusQ security foundation
Phase 3 rules are intentionally **deny all**, including authenticated students and trusted-looking staff/admin claims. This is a closed prototype, not completed role access. UI Demo Mode never requests Firestore. The unused service catalog adapter returns permission errors under current rules.

I've set up prototype Security Rules to keep the data in Firestore safe. They are designed to be secure for this foundation because every mobile read and write is denied, including unknown paths and privileged claims. However, you should review and verify them before broadly sharing your app. If you'd like, I can help you harden these rules.

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

No rules deployed; deny-all local file cannot protect an unconfigured remote database automatically. Manually select locked/production mode on creation, then deploy this reviewed file. Document edition and inspect deployed rules before use. Billing/paid services stay disabled.
