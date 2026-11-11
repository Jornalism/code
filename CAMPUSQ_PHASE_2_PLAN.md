# CampusQ — Phase 2: UI/UX Testing and Refinement

Inspection date: 2026-11-09 (Asia/Manila)
Status: Phase 2 implementation and final mobile verification complete. Extended accessibility/device acceptance remains open. See CAMPUSQ_PHASE_2_REPORT.md.
Scope: Existing Kotlin / Jetpack Compose / Material 3 frontend with local demo data.
Phase gate: No Phase 3 or later work without explicit user authorization. No Firebase implementation in Phase 2.

## Preservation contract

- Preserve all 23 screens, the demo role hub, all existing navigation destinations, the CampusQ name, “Queue smarter. Wait less.” tagline, images, launcher branding, green student/staff accents, and specified purple admin headers.
- Refine the existing application; do not regenerate it or replace its architecture wholesale.
- Keep demo actions and their limitations explicit until a separately authorized backend phase.
- Record source findings separately from reproduced runtime failures. A passing build or preview does not verify a user journey.
- The initial preparation was documentation-only. Authorized Phase 2 implementation now refines production code and tests in place; dependencies and all original image assets remain unchanged. Changes and verification are recorded in CAMPUSQ_PHASE_2_REPORT.md.
- No Git repository was detected at the project root during inspection; no commit/diff baseline is available.

## Project and architecture inventory

Only Gradle application module: `:app`; no other project consumers in settings or the IDE's top-level project query.
Package/application ID: `com.example.campusq`; min SDK 26; target SDK 36; compile SDK 36.1.
Toolchain: AGP 9.3.3; Gradle 9.5.0; daemon Java 21; Java source/target 11; Kotlin Compose plugin 2.2.10.
Libraries: Compose BOM 2025.05.00, Material 3, Navigation Compose 2.9.0, lifecycle 2.9.0.
Existing material-icons-extended dependency is used across the UI; do not remove it during preparation.

Data flow:
`MainActivity → CampusQTheme → CampusQApp/NavHost → screens/callbacks → CampusQViewModel → DemoRepository → StateFlow → UI`.

- `MainActivity.kt`: single Compose activity with edge-to-edge enabled.
- `ui/navigation/CampusQRoutes.kt`: 24 destinations (23 screens plus hub), including ticket ID routes.
- `ui/navigation/CampusQAppNavHost.kt`: central navigation, state collection, action wiring, and global snackbar host.
- `viewmodel/CampusQViewModel.kt`: app-level ViewModel directly constructs a demo repository; exposes StateFlows and synchronous actions.
- `data/demo/DemoRepository.kt`: in-memory profiles, services, staff, queue, appointments, transactions, notifications, institution settings, and sample analytics. Reset on a new repository/process; no persistence or remote synchronization.
- `data/model/CampusQModels.kt`: immutable UI data classes and role/status enums. Dates/times are display strings, not authoritative scheduling records.
- `ui/components/`: shared buttons/fields/logo, cards/chips, navigation, calendar, timeline helpers, confirmation/empty states.
- `ui/theme/`: centralized palette/typography/dimensions; light theme only.
- `ui/showcase/CampusQScreenPreviews.kt`: 10 previews, all 393 × 851 dp; not automated screenshot tests.
- `app/src/main/res/`: five PNG brand assets, adaptive launcher XML and density variants, app label, XML theme and backup templates.
- Gradle scripts/catalog/wrapper, manifest, resources, all production Kotlin files, both test sources, existing design/progress documents, and local project instruction locations were inspected. No project AGENTS.md or populated local skill instructions were found.
- Firebase SDKs/plugins/configuration, backend code, repositories backed by Firebase, and remote role enforcement are absent. None was added.

## Screen inventory and Phase 2 checks

“Source inspected” means implementation and route wiring were read. It does not mean visually or behaviorally verified on a device.
All rows below are source inspected and have passed phone and compact/large-text render/capture tests. Primary workflow evidence and remaining unverified interactions are distinguished in CAMPUSQ_PHASE_2_REPORT.md.

| # | Screen / route | Source under ui/ | Required UI/state checks |
| --- | --- | --- | --- |
| 01 | Splash / splash | auth/AuthScreens.kt | Artwork crop, system bars, timed/tap navigation without duplicate transitions |
| 02 | Login / login | auth/AuthScreens.kt | Blank/invalid email, short password, visibility, IME, recovery and demo notices |
| 03 | Registration / register | auth/AuthScreens.kt | Required inputs, mismatch errors, enabled state, scrolling, keyboard and Back |
| 04 | Student Home / student_home | student/StudentDashboardServicesQueueScreens.kt | Long name header, active/terminal/empty ticket, badges, quick actions, notifications |
| 05 | Registrar Services / student_services | student/StudentDashboardServicesQueueScreens.kt | Search/clear/no results, requirements sheet, disabled service/queue/booking actions |
| 06 | Queue Status / student_queue_status | student/StudentDashboardServicesQueueScreens.kt | Every queue status, progress copy, serving number, cancel confirmation/dismissal |
| 07 | Book Appointment / student_book_appointment | student/StudentAppointmentHistoryProfileScreens.kt | Available/empty services, calendar, disabled dates/times, summary, restoration |
| 08 | Transaction History / student_history | student/StudentAppointmentHistoryProfileScreens.kt | All/Queue/Appointments filters, empty state, long reference/service, details |
| 09 | Student Profile / student_profile | student/StudentAppointmentHistoryProfileScreens.kt | Edit validation, save/cancel, preferences, help, role switching and exit |
| 10 | Staff Dashboard / staff_dashboard | staff/StaffDashboardQueueScreens.kt | Counts, long Call Next label, no waiting tickets, confirmation and target ticket |
| 11 | Queue Management / staff_queue_management | staff/StaffDashboardQueueScreens.kt | Waiting/Called/Serving, skipped tickets, inline Call versus row click |
| 12 | Ticket Details / staff_ticket_details/{ticketId} | staff/StaffDashboardQueueScreens.kt | Valid/missing ID, state-aware actions, confirmations, metadata, Back |
| 13 | Service in Progress / staff_service_in_progress/{ticketId} | staff/StaffDashboardQueueScreens.kt | Serving/terminal/missing ticket, complete/no-show confirmation, correct return |
| 14 | Staff Appointments / staff_appointments | staff/StaffAppointmentStatusProfileScreens.kt | Today/Upcoming/All, valid actions for every status, dialog button overflow |
| 15 | Service Status / staff_service_status | staff/StaffAppointmentStatusProfileScreens.kt | Ongoing/Completed filters, terminal labels, ticket navigation |
| 16 | Staff Profile / staff_profile | staff/StaffAppointmentStatusProfileScreens.kt | Counter consistency, schedule/account/help notices, switch/exit |
| 17 | Admin Dashboard / admin_dashboard | admin/AdminDashboardServicesStaffScreens.kt | Purple header, drawer, metrics, operational links and long labels |
| 18 | Manage Services / admin_manage_services | admin/AdminDashboardServicesStaffScreens.kt | Add/edit, search/no results, toggles, requirements/duration validation, sheet IME |
| 19 | Staff Management / admin_staff_management | admin/AdminDashboardServicesStaffScreens.kt | Add/edit, name/email/counter validation, active/inactive confirmation, sheet IME |
| 20 | Queue Monitoring / admin_queue_monitoring | admin/AdminQueueAppointmentsReportsSettingsScreens.kt | Every filter, empty state, long row text, ticket detail return to admin |
| 21 | Admin Appointments / admin_appointments | admin/AdminQueueAppointmentsReportsSettingsScreens.kt | Filters, details, schedule settings navigation and return |
| 22 | Reports / admin_reports | admin/AdminQueueAppointmentsReportsSettingsScreens.kt | Period dropdown, chart/count readability, long legends, demo data labeling |
| 23 | System Settings / admin_system_settings | admin/AdminQueueAppointmentsReportsSettingsScreens.kt | Purple header, form save/cancel/validation, schedule/preferences, informational dialogs |
| Hub | Demo Role Preview / demo_preview_hub | auth/AuthScreens.kt | All role and entry links, badges, small-width button labels, scroll reachability |

Student and staff each have five bottom destinations. Admin has five bottom destinations plus seven drawer destinations.
Admin queue monitoring intentionally links into staff ticket details in the current implementation; retain access while testing return behavior.

## Phase 2 execution record — 2026-11-09–10

See [the completion report](CAMPUSQ_PHASE_2_REPORT.md) for the complete fix/file inventory, results and remaining limitations.

- Latest completed combined pass: debug APK PASS, 27 local tests PASS, 62 connected tests PASS, Gradle lint PASS with 0 errors / 19 warnings. The final header/label contrast rerun passed the same complete check set.
- Runtime: Pixel_7 AVD, Android 17 / API 37, emulator-5554. All 23 screens plus hub captured at native phone size and constrained 320 × 640 dp / 1.5× fonts.
- Brand PNG hashes remain identical to the inspection baseline. Existing palette retained, with darker accessible control/header roles and an added dark error-text token.
- IDE preview rendering and real application deployment succeeded. Visual lint still reports scroll/overlap diagnostics; full accessibility certification is not claimed.
- Core mobile gate is implemented and tested; the wider manual/device/accessibility matrix remains open. No Firebase or Phase 3 work started.

| Finding IDs | Execution status |
| --- | --- |
| UX-01, UX-03 | Fixed: current service validation/empty booking state and safe missing-ticket behavior |
| UX-02 | Fixed: root tab navigation, role exit and Back; runtime restored-detail regression also corrected |
| UX-04, UX-06 | Refined from reviewed compact/large-text captures |
| UX-05 | Improved semantics, contrast and minimum date/control targets; broader visual-lint/manual audit remains open |
| UX-07 | Long sheets/dialogs/drawer scroll; full docked IME and all configurations not certified |
| UX-08 | Booking dates/month use saveable ISO values; login restoration tested; exhaustive recreation matrix remains open |
| UX-09 | Local form validation implemented and tested in domain and key UI flows |
| UX-10 | Shared transition rules and terminal-state presentation implemented/tested |
| UX-11 | Per-route system-bar icon treatment implemented; actual login legibility observed |
| UX-12, UX-13 | FIFO/position updates, unique numbers, ownership, fixture references and profile/counter consistency corrected |
| UX-14 | Fictitious metrics removed; settings remain explicitly demo descriptions/preferences |
| QA-01 | 27 meaningful local tests and 62 instrumented tests pass |
| QA-02 | All screen previews/captures added; no golden-image comparison harness claimed |
| QA-03 | All 14 original preview StateFlow lint errors fixed; no suppression/baseline |

## Original findings and prioritized refinement backlog

These are static source findings or explicit coverage gaps, not device reproductions. Priorities guide Phase 2 work; they are not claims of production readiness.

| ID | Priority | Evidence | Impact / proposed acceptance check |
| --- | --- | --- | --- |
| UX-01 | High | StudentAppointmentHistoryProfileScreens.kt:102–103 falls back to all services when the available booking list is empty | Disabling every service still exposes booking choices. Empty availability must show a clear state and disable confirmation. Recheck stale preselection after admin changes. |
| UX-02 | High | CampusQAppNavHost.kt:52–57 uses launchSingleTop/restoreState without popUpTo/saveState; exit callbacks navigate to hub | Repeated tab switching can grow the stack; Back can return into an exited role. Define and test tab, detail, role-switch, and exit behavior while preserving destinations. |
| UX-03 | High | CampusQAppNavHost.kt resolves an unknown ticket ID to queueTickets.firstOrNull() | Missing ID can show another ticket with actionable controls. Require the existing not-found UI and safe Back. |
| UX-04 | Medium | CampusQButtonsInputs.kt fixes shared button height at 50 dp; long Call Next text and three hub buttons share narrow rows | Check label clipping and vertical growth at 1.5×/2× font scale before altering components. Preserve visual style. |
| UX-05 | Medium | CampusQNavigationWidgets.kt custom clickable filters/calendar/slots lack explicit selected semantics; calendar cells 38 dp, slots 44 dp; auth role chips 40 dp | Audit actual hit areas and focus behavior. Provide clear selection/date announcements and nonoverlapping minimum 48 dp touch targets where practical. Material automatic touch expansion must be measured rather than assumed absent. |
| UX-06 | Medium | CampusQAppointmentCard uses an unweighted leading row beside a status chip; multiple top bars/header rows have unconstrained titles/actions | Long names/services and large fonts may crowd status/actions. Reproduce at compact widths, refine constraints, verify all chips/actions remain readable. |
| UX-07 | Medium | Staff/add-staff/service/notification sheets and several edit dialogs use non-scrollable Columns | Check short windows, many notifications/requirements, landscape and keyboard reachability. Admin service edit already scrolls; verify other sheets individually. |
| UX-08 | Medium | Booking date/month use remember, while time/service use rememberSaveable | Rotation/recreation can reset a chosen date while retaining other selections. Test and preserve coherent form state. Some selected detail dialogs also use remember. |
| UX-09 | Medium | Student profile saves without validation; staff email accepts arbitrary text/generated fallback; service duration accepts zero and falls back on invalid overflow; settings accept blanks | Add consistent inline feedback for editable demo forms; cancel must not save and reopening should have defined draft behavior. No real account creation implied. |
| UX-10 | Medium | Home excludes CANCELLED/EXPIRED but still treats COMPLETED/NO_SHOW as an active ticket; service-in-progress enables actions for every state except COMPLETED | Align terminal-state presentation/action availability. Cover all queue and appointment enum values. Staff appointment NO_SHOW is also available for CANCELLED. |
| UX-11 | Medium | Theme.kt forces light-colored status icons; auth/splash/hub backgrounds are light | Potential system-bar legibility problem. Verify on-device, including scrolling headers and navigation modes, before targeted inset/theme fixes. |
| UX-12 | Medium | DemoRepository prepends new tickets; callNextWaitingTicket selects first WAITING; peopleAhead/position are not recalculated for other tickets | Newly joined students may be called before older tickets and positions remain stale. Track as local demo behavior; agree/demonstrate coherent ordering for UI walkthroughs without implementing a backend. |
| UX-13 | Medium | Student history receives all shared transactions; initial A-001 and R-001 fixtures disagree across lists; current staff counter differs from directory/dashboard | Cross-role walkthroughs can show inconsistent records, names, counters, and counts. Reconcile demo fixtures and view filtering as needed for reliable UI tests. Backend authorization remains a later concern. |
| UX-14 | Low | Admin metrics use sample offsets; report ranges are October 2026 fixtures; settings schedule does not drive calendar/home copy | Keep demo limitations visible; avoid presenting fixtures or local preferences as live operational behavior. |
| QA-01 | High | Only addition_isCorrect and useAppContext exist | No repository/ViewModel/navigation/UI behavior coverage. Build a focused regression suite as part of refinement. |
| QA-02 | Medium | 10 of 23 screens have fixed phone previews; no screenshot test task/goldens configured | Establish complete visual coverage and approved baselines across sizes/states before claiming visual parity. |
| QA-03 | High | Baseline :app:lintDebug failed with 14 StateFlowValueCalledInComposition errors in CampusQScreenPreviews.kt | Replace direct composition reads with observed or stable preview fixture state during Phase 2; rerun lint without suppressions or a lint baseline. |

Existing snackbar handling uses a single StateFlow message and clears after display; rapid repeated actions and snackbar positioning should also be checked during interactions.
Most strings are inline English; preserve copy/branding while checking line wrapping and accessibility. Localization is not introduced during preparation.

## Phase 2 execution sequence

1. Capture the unchanged 23-screen device/preview baseline and shared components. Record configuration and images; do not silently approve screenshots that contain defects.
2. Add targeted behavior coverage using the existing JUnit4 and Compose test dependencies. Test repo behavior directly; introduce only the smallest injection/fake/clock seam justified by a test.
3. Reproduce high-priority UX-01–03 and shared layout/accessibility issues. Refine existing components first, then affected screens. Preserve all routes and assets.
4. Validate student, staff, admin, and cross-role demo journeys; exercise empty/disabled/terminal/invalid states, confirmations and cancellation.
5. Repeat affected visual/behavior checks after fixes, then run local tests, debug app assembly, lint, and device tests when a device is available.
6. Record evidence and remaining limitations in CAMPUSQ_UI_PROGRESS.md. Stop at the Phase 2 gate; request explicit authorization before any next phase.

This preparation uses the analysis/report portion of the testing-setup skill. Framework installation, Hilt, Robolectric, coverage plugins and screenshot tooling are deferred: the current request is inspection/preparation, not a testing-stack migration. No dependency upgrade is needed to document the baseline.

## Test strategy and reproducible commands

Run from the project root in PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
# Only with an available device/emulator:
.\gradlew.bat :app:connectedDebugAndroidTest --console=plain
```

For repeated targeted tests once implemented:
` .\gradlew.bat :app:testDebugUnitTest --tests "fully.qualified.TestClass" --console=plain `.
Do not describe the existing arithmetic test as queue/booking coverage.

Current test setup:
- JUnit4 local tests; AndroidJUnit4 instrumentation; Compose UI test and Espresso dependencies already declared.
- No DI framework, mocking framework, Robolectric, screenshot harness/goldens, end-to-end journeys, or coverage reporting configured.
- Composables already accept values/callbacks, so many screen tests can supply fixtures without a dependency injection framework.
- ViewModel constructs its repository directly; consider a small constructor seam when needed rather than a broad rewrite.
- DemoRepository reads the real date/time; deterministic date-boundary tests will need a clock seam or controlled fixtures.
- Unit targets: queue/appointment updates and histories, profile/settings saves, service availability, selection and message behavior.
- Compose behavior targets: validation, no-results states, disabled actions, confirmations, status-driven controls, date/service selection, Back/tab/drawer navigation and state restoration.
- Screenshot targets: all 23 screens plus hub, shared controls, empty/long/error/terminal states. No screenshot baseline exists yet; proposed evidence folder is `docs/phase-2/evidence/` when capture begins.

Manual/device journeys to record:
- Entry → login invalid/valid-demo notice → register mismatch/valid-demo notice → hub.
- Student → services/search/details → join → queue → cancel/dismiss/restore → appointment summary/dismiss/confirm → filtered history → edit profile → exit.
- Staff → call next → ticket → skip/recall → start → complete/no-show → service status; appointments → confirm/check-in/complete; profile → exit.
- Admin → drawer and bottom tabs → service/staff add/edit/disable/enable → queue details → appointments/settings → report periods → exit.
- Cross-role within one app process: student joins/books → staff sees and updates → student/admin reflect the same changes. Separately document reset on process restart.
- Repeated tab switching, system Back, modal Back, drawer Back, and role exit/re-entry.

Visual/configuration matrix:
- Compact phones including 320 dp width, existing 393 × 851 dp reference, medium/tablet widths and landscape/short windows.
- Screenshot sweep: widths 400/610/900 dp × heights 400/500/1000 dp, plus phone font scale 1.5× and 2× accessibility review.
- Light CampusQ theme under both system light/dark settings (no dark theme redesign assumed).
- Gesture and three-button system navigation; keyboard visible/hidden; API 26 and API 36+ where available.
- TalkBack reading order, action labels, selected state, focus, contrast, touch bounds, scroll reachability, and text clipping.
- Rotation/activity recreation and selected form/filter/scroll state; process restart expectations documented separately.
- Record unavailable configurations as NOT RUN, not PASS.

## Historical baseline verification record — 2026-11-09

Executed on the existing source, before any documentation edits:
` .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain `

| Check | Result | Evidence / limitation |
| --- | --- | --- |
| Debug local unit tests | PASS | 1 test, 0 failures/errors/skips; ExampleUnitTest.addition_isCorrect only. XML timestamp 2026-11-09T14:41:30.369Z. |
| Full debug application assembly | PASS | :app:assembleDebug completed; app/build/outputs/apk/debug/app-debug.apk |
| Android lint | FAIL | 14 errors, 25 warnings; combined Gradle command exit 1 after 3m 10s |
| Connected device discovery | Completed, none found | SDK adb devices -l returned an empty device list |
| Instrumented tests / runtime journeys | NOT RUN | No connected device/emulator |
| Compose render / visual lint / screenshots / TalkBack | NOT RUN | Source inspection is not visual verification |
| Release build / minification | NOT RUN | Debug baseline only |

All 14 lint errors are StateFlowValueCalledInComposition in `ui/showcase/CampusQScreenPreviews.kt` at lines 49, 50, 51, 70, 86, 87, 101, 102, 120, 135, 136, 137, 138, and 164.
Warning breakdown: 1 OldTargetApi, 1 RedundantLabel, 1 AndroidGradlePluginVersion, 7 GradleDependency, 1 NewerVersionAvailable, 5 ModifierParameter, 7 UnusedResources, 2 IconXmlAndPng.
Dependency-update warnings are lint observations, not a request or decision to upgrade.

Reports:
- [Local test XML](app/build/test-results/testDebugUnitTest/TEST-com.example.campusq.ExampleUnitTest.xml)
- [Local test HTML](app/build/reports/tests/testDebugUnitTest/index.html)
- [Lint HTML](app/build/reports/lint-results-debug.html)
- [Lint text](app/build/reports/lint-results-debug.txt)
- [Lint SARIF](app/build/reports/lint-results-debug.sarif)

Build outputs/reports may be regenerated or removed by clean. The results above are the durable record; they do not certify workflow correctness.

### Preserved brand asset baseline

All five PNGs were visually inspected as source assets; this does not validate their layout on any screen.
Paths are relative to `app/src/main/res/drawable-nodpi/`. SHA-256 recorded before refinement.

| Asset | Bytes | SHA-256 |
| --- | --- | --- |
| img_avatar_staff.png | 301673 | 1EB94459DA4F4C9986E0AB5D301E43A7E2BA923267AEF18E66AD4A63CDA4AEF3 |
| img_avatar_student.png | 295920 | D67EAB5E82C9381856432894F16412E7B4DE3DBB198DC45A0FFADF0EEC195EE3 |
| img_campusq_icon.png | 216452 | A12C2A673440AFB690154F5135411F58556EEB989017ADC6A4F0A3AD8453042E |
| img_campus_building.png | 2626398 | 269832F0D0339008D0202D430413F1C125A665535B0CA3FF9A267444884F4982 |
| img_campus_welcome_bg.png | 1140647 | 45ADD3A88D3F9EACC9DC0390504FF0419653B54322F089DB8F80FA9DB544DAAE |

## Phase 2 acceptance gate

- All 23 screens plus hub remain implemented and reachable through their existing workflows.
- Five brand PNGs and launcher identity preserved; before/after visual review recorded.
- Blocking/high UI issues fixed and tested; remaining issues carry explicit severity and evidence.
- Primary demo journeys and critical negative/empty/disabled/terminal states tested; state restoration reviewed.
- Local tests, full debug app build, lint and available device checks have recorded results; failures fixed or explicitly unresolved.
- No runtime or accessibility verification claimed without corresponding test/capture evidence.
- Development progress updated; Phase 2 completion clearly distinguished from this preparation.
- Phase 3–12 remain unstarted until explicitly authorized. Their detailed scope is not supplied in this request and is not invented here.
