# Phase 4 trusted provisioning design — NOT IMPLEMENTED
Date:2026-11-13, Asia/Manila.
Status:PENDING APPROVAL. No operator code, IAM changes, credentials, production accounts, claims, or profile documents are created by this stage.

## Current boundary
The production Auth adapter blocks account/login/email operations pending explicit approval. Emulator mutations require the exact demo-campusq-local project and the debug-only local backend. Firebase resource processing remains behind campusq.firebase.enabled; this flag does not authorize production Auth operations.
Published firestore.rules remains the tested deny-all file. ClosedUserProfileRepository performs no Firestore reads/writes and cannot return an authorized profile. Missing/invalid claims produce ProvisioningPending; valid claims without a verified profile produce ProfileUnavailable. No live business routes exist in the authentication graph; even a future Authorized state currently shows an honest unavailable panel.

## Operator and credentials
Use a trusted workstation or existing institution-controlled server with least-privilege Firebase Admin SDK access through a separately approved ADC/short-lived impersonation setup. The current workspace does not have gcloud installed; do not assume its CLI login is an approved Admin SDK credential configuration.
No service-account key, refresh token, operator password or private credential belongs in Android, Git, resources, reports, screenshots or chat. Do not ask anyone to paste credentials into chat. Agree who operates the tool, its IAM permissions, audit destination and recovery process before implementation. Never use a mobile admin screen as an IAM principal.

## Canonical identity and records
All document IDs are Firebase Auth UID, never student number/demo IDs. Proposed:
- users/{uid}: uid, displayName, Auth-authoritative email mirror, role(STUDENT/STAFF/ADMIN), isActive, authorizationVersion(integer>=1), schemaVersion, server createdAt/updatedAt.
- studentProfiles/{uid}: userUid, studentNumber, program/yearLevel (nullable until approved data obtained), schemaVersion and timestamps. No role authority. Approved student-number normalization/uniqueness enforced through trusted reservation/transaction design.
- staffProfiles/{uid}: userUid, roleTitle, counterId, assignedServiceIds, isActive mirror, schemaVersion and timestamps. Assignment cannot grant authorization.
Auth controls email and verification. A stored emailVerified mirror must not authorize anything. No password/token fields. Required UI full name/student number must be verified/completed rather than fabricated; current registration validates these as input but saves no student profile to Firestore.

## Eligibility and role assignment
Public sign-up does not assign any role, including STUDENT. Email verification proves mailbox control only.
An approved operator verifies institutional eligibility against the approved roster/process, links that record to the exact UID, and then assigns STUDENT. Staff appointment/assignment requires registrar approval. ADMIN requires explicit institution authorization; an institutional Google account or client email suffix is insufficient.
First admin bootstrap is a separate, audited operator action after the UID is independently confirmed. No hardcoded admin email/password or self-service promotion.
Custom claims contain only access-control data: role and authorizationVersion. Never accept a requested role from Android, profile payload or Demo Mode.

## Consistency and partial failures
Auth claims and Firestore cannot be changed in one atomic transaction:
1. Validate operator authority and request; record an idempotent operation keyed by target UID/request ID.
2. Mark the canonical record inactive/pending and advance authorizationVersion, so existing permissions fail closed.
3. Create/validate the UID-based role profile and protected references.
4. Set trusted Auth claims using Admin SDK, preserving only approved unrelated claims.
5. Verify the matching role/version/profile, then activate the canonical record at that version.
6. Complete audit entry without credentials or unnecessary PII.
On failure, leave inactive/pending. Retry resumes the same operation; never create another Auth account, reuse an unrelated profile, or temporarily authorize inconsistent records.

For role changes, deny access during the transition and require a refreshed token matching the canonical record/version. For disablement, make the canonical record inactive first, disable the Auth user and revoke refresh tokens. Existing ID tokens may remain valid until expiration; eventual rules/backend checks must require active status and matching authorizationVersion. Trusted backend token validation must account for revocation where required. The app's navigation is convenience, not the enforcement boundary.

## Future profile-access rules — design only
Keep deny-all deployed until a separately approved design is implemented/tested.
Potential minimal first policy: verified owner may get a narrowly scoped own-user/profile record, with no list or cross-user access. Server-owned role/status/version/ownership/assignment fields remain client-immutable. Prefer server-only profile writes initially; any later allowed profile editing requires exact field allowlists and complete post-state validators for both create/update. Email and student ID changes use their own validated workflows.
Future operational reads/writes additionally require verified active identity, trusted role/version and appropriate owner/assignment scope. Sensitive administrative operations stay behind trusted controls. Deny every unspecified path. No broad authenticated-user access.
No rules, Functions or indexes are deployed in this stage. Admin SDK bypasses Rules, so all operator/server code must validate independently.

## Billing and future automation
Email/password client foundation and local emulator testing do not require a plan upgrade. Do not upgrade Identity Platform or enable paid features incidentally. Auth email quotas and production delivery need separate real-world validation.
Production Cloud Functions deployment requires Blaze. If automatic provisioning/queues require Functions or another paid host, stop and request approval before any change. No Functions deployment is proposed as part of this initial stage.

## Pending approvals
1. Initialize/enable production Email/Password provider on campusq-e06c7 and approve production account/email operations. Current readiness remains based on the prior404 CONFIGURATION_NOT_FOUND result.
2. Approve any real test accounts and verification/recovery mail delivery checks.
3. Define institution domains, enrollment/student-number policy, program/year data and duplicate-number handling. Until then generic format/length checks only.
4. Approve operator ownership, first-admin identity and credential/IAM approach.
5. Review profile rules separately and explicitly authorize any later publication.
6. Approve live operational features in their proper later phase. Phase5 is unstarted.
