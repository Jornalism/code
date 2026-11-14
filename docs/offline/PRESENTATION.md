# CampusQ classroom presentation runbook

Read README.md first. Use an isolated trusted network and presentation-only credentials. The initial Administrator chooses their password interactively; there are no published/default passwords. Existing acceptance fixture data is for tests, not the presentation database.

## Prepare
1. Open Android Studio Terminal at the CampusQ project root. Confirm node --version is24.12+.
2. Stop any acceptance fixture server using port8787. Keep its database; do not delete it.
3. Run node backend/local/operator.mjs bootstrap-admin and enter name/email/password locally. This succeeds only when no Administrator exists in the chosen database. If already provisioned, sign in with that account; use the authorized operator recovery command if necessary.
4. Run .\backend\local\Start-CampusQ.cmd and leave it running. It uses backend/local/data/campusq.sqlite by default. Health: http://127.0.0.1:8787/health.
5. For an emulator, build/run the default debug app (http://10.0.2.2:8787). For phones, use the LAN startup/build commands in README.md with the actual laptop IP; approve any required firewall rule yourself. No internet is needed by these runtime requests.
6. Install the same configured APK on each participating device. Use separate Student, Staff and Admin sessions where possible. A single device can demonstrate sequential sign-out/sign-in, but is less convincing for simultaneous updates.

## Administrator
1. Sign in with the operator-provisioned account.
2. Add an available Registrar service in Manage Services. Enable its queue and appointment features and set its estimate.
3. Create an active Staff account in Staff Management with name/email/counter and an initial password. Hand the credentials to the presenter privately. Students cannot create Staff/Admin accounts.
4. Open Accounts and system activity from the drawer to inspect real registered accounts/actions. Dashboard counts initially reflect an empty database, not sample data.

## Student queue and Staff processing
1. Register a Student with a new email, unique Student number and password. Registration saves the account, displays Registration Successful and returns to Login. Manually enter the new email/password and log in before continuing; registration alone does not create a session or verify enrollment.
2. Open Services and select the service just created. Request a ticket once.
3. Show the real queue number/status; the server, not Android, allocated and saved it.
4. On the Staff session, open the queue. Call that ticket (or Call Next), start service, then complete the transaction.
5. Observe the Student status update within the polling interval plus network latency. Open Student History to show the completion.
6. Observe Admin queue/dashboard/reports reflect actual records.

## Appointment
1. Student opens Book Appointment, selects the service, a future weekday and an available slot, then submits.
2. Staff opens appointments and confirms/reviews that actual booking.
3. Student sees the changed status. Demonstrate reschedule or cancellation while BOOKED/CONFIRMED if desired; a changed booking requires review again.
4. Trying an occupied service/slot from another account must fail or show unavailable; do not bypass the server conflict response.

## Persistence
1. Close/relaunch the Student app. The encrypted session is revalidated against the server; saved records remain.
2. Stop the server with Ctrl+C. Show the app's connection warning; do not claim new operations work while the laptop is unavailable.
3. Restart the same server with the same database path. Reconnect and show saved users/services/tickets/appointments/transactions.
4. Optional read-only record counts: node backend/local/status.mjs. Do not show private credential/fixture files on the projector.

## Backup and finish
Run .\backend\local\Backup-CampusQ.cmd C:\PrivateBackups\campusq.sqlite-backup after creating that private directory. Each destination must be new; backups are never overwritten. Sign out clients and Ctrl+C the server. Keep database/backups private.

## What to explain honestly
- This is a shared laptop-server system that operates without internet, not a standalone phone database.
- Laptop/network availability is required. HTTP is an isolated presentation limitation; HTTPS certificate setup is optional and not automatically completed.
- Password recovery is a trusted local operator workflow; no email/SMS is sent.
- Push notifications, enrollment validation, dynamic schedules and institutional role-assignment policies are not implemented.
- Check CAMPUSQ_OFFLINE_COMPLETION_REPORT.md for actual emulator, physical-device and end-to-end verification coverage. Do not present untested physical-device or accessibility acceptance as passed.
