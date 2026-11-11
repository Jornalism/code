# CampusQ — Official Material Design 3 UI/UX Design System

**Application Name:** CampusQ — Smart Queue & Appointment Management System  
**Official Tagline:** Queue smarter. Wait less.  
**Scope:** Registrar Office Digital Queue & Appointment Management (Student, Registrar Staff, Administrator)

---

## 1. Brand Color Palette

All color tokens are centralized in [`Color.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/theme/Color.kt) and mapped to semantic Material 3 color roles in [`Theme.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/theme/Theme.kt).

| Token | Hex | Role / Usage |
| :--- | :--- | :--- |
| `CampusQGreenPrimary` | `#1B8F4D` | Brand accents, icons and queue progress ring; see Phase 2 accessible header/control roles below |
| `CampusQGreenDark` | `#126A38` | Brand wordmark, accessible green headers/controls, deep accents and text on green containers |
| `CampusQGreenLight` | `#E8F5E9` | Soft green tile backgrounds, unselected available time slots, icon containers |
| `CampusQBackground` | `#F7F9F8` | Primary screen canvas background |
| `CampusQSurface` | `#FFFFFF` | Cards, input fields, bottom navigation, dialogs, sheets |
| `CampusQTextPrimary` | `#17221C` | Headings, card titles, primary body text |
| `CampusQTextSecondary` | `#66736B` | Subtitles, supporting labels, timestamps |
| `CampusQSuccess` | `#2E7D32` | `COMPLETED` status chips, active badges |
| `CampusQWarning` | `#F9A825` | `WAITING`, `BOOKED`, and `SKIPPED` status accents |
| `CampusQError` | `#D32F2F` | `CANCELLED`, `NO_SHOW`, destructive buttons, validation errors |
| `CampusQInfo` | `#1565C0` | `CALLED`, `SERVING`, `CHECKED_IN` status chips |
| `CampusQAdminPurple` | `#7B3FC4` | Admin Dashboard (Screen 17) & System Settings (Screen 23) headers and accents |
| `CampusQAdminPurpleLight` | `#F1EAF9` | Admin drawer selection container and light purple badges |

---

## 2. Typography Scale

Defined in [`Type.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/theme/Type.kt) using scalable `sp` units for Android accessibility font-scaling compliance:

- **Brand Name (`displaySmall`)**: `30sp`, Bold
- **Primary Screen Heading (`headlineSmall`)**: `23sp`, SemiBold
- **Section Heading (`titleLarge`)**: `19sp`, SemiBold
- **Card Heading (`titleMedium`)**: `16sp`, SemiBold
- **Primary Body (`bodyLarge`)**: `16sp`, Regular
- **Secondary Body (`bodyMedium`)**: `14sp`, Regular
- **Supporting Text (`bodySmall`)**: `12sp`, Regular
- **Buttons (`labelLarge`)**: `15sp`, SemiBold
- **Status Chips (`labelMedium`)**: `12sp`, Medium
- **Navigation Labels (`labelSmall`)**: `11sp`, Medium

---

## 3. Spacing, Shapes & Dimensions

Defined in `CampusQDimens` inside [`Theme.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/theme/Theme.kt):

- **4dp Grid**: `4dp` (XS), `8dp` (S), `12dp` (M), `16dp` (Standard), `24dp` (L), `32dp` (XL)
- **Screen Horizontal Padding**: `16dp`
- **Standard Card Radius**: `16dp`
- **Large Feature Card Radius**: `20dp`
- **Button & Input Radius**: `12dp`
- **Status Chip & Filter Pill Radius**: `50%` (fully rounded)
- **Standard Button Height**: `50dp` (exceeds `48dp` minimum accessibility touch target)

---

## 4. Reusable Component Library

Located in `com.example.campusq.ui.components`:

1. **[`CampusQButtonsInputs.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/components/CampusQButtonsInputs.kt)**:
   - `CampusQFullLogo` (Graduation-cap Q icon + `CampusQ` wordmark + `Queue smarter. Wait less.`)
   - `CampusQPrimaryButton`, `CampusQSecondaryButton`, `CampusQDestructiveButton`, `CampusQTextButton`
   - `CampusQTextField`, `CampusQPasswordField`, `CampusQSearchField`
2. **[`CampusQCardsStatus.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/components/CampusQCardsStatus.kt)**:
   - `CampusQCard`, `CampusQTicketCard`, `CampusQServiceCard`, `CampusQAppointmentCard`, `CampusQStatCard`
   - `CampusQStatusChip`, `CampusQQueueStatusChip`, `CampusQAppointmentStatusChip`
3. **[`CampusQNavigationWidgets.kt`](file:///C:/Users/nico/AndroidStudioProjects/CampusQ/app/src/main/java/com/example/campusq/ui/components/CampusQNavigationWidgets.kt)**:
   - `CampusQTopAppBar`, `CampusQBottomNavigation`, `CampusQDemoLabel`
   - `CampusQSectionHeading`, `CampusQFilterTabs`, `CampusQQueueProgressIndicator`
   - `CampusQProfileHeader`, `CampusQProfileMenuItem`, `CampusQEmptyState`, `CampusQConfirmationDialog`
   - `CampusQDateSelector` (7-column monthly calendar grid), `CampusQTimeSlotChip`

---

## 5. Role Accents & Navigation Conventions

- **Student Interface**: 5 bottom navigation destinations (`Home`, `Queue`, `Appointments`, `History`, `Profile`) with primary green active state.
- **Registrar Staff Interface**: 5 bottom navigation destinations (`Dashboard`, `Queue`, `Appointments`, `History`, `Profile`) with compact operational cards and state-aware ticket transitions.
- **Administrator Interface**: Coordinated mixed treatment matching the reference board—purple headers on `Admin Dashboard` (Screen 17) and `System Settings` (Screen 23), and green headers on `Manage Services`, `Staff Management`, `Queue Monitoring`, `Appointments`, and `Reports`. Includes both a 5-item bottom bar and a full 7-item `ModalNavigationDrawer`.

---

## 6. Official Image Assets

Imported into `app/src/main/res/drawable-nodpi` and adaptive launcher drawables:

- `R.drawable.img_campusq_icon`: Graduation-cap Q emblem
- `R.drawable.img_campus_welcome_bg`: Curved green campus welcome illustration (Splash screen)
- `R.drawable.img_campus_building`: Campus Registrar building illustration
- `R.drawable.img_avatar_student`: Default student profile avatar (`Juan Dela Cruz`)
- `R.drawable.img_avatar_staff`: Default Registrar staff profile avatar (`Maria Santos`)

## Phase 2 accessibility refinements — 2026-11-09–10

Original color values and image assets remain defined and preserved. Readability fixes use the existing #126A38 dark green for small green text, selected controls, filled actions and white-text green headers. White on #1B8F4D measured 4.13:1; white on #126A38 measures 6.68:1. Purple admin headers remain purple.

A dark error-text token, CampusQErrorText (#B71C1C), was added for status text on pale red. Neutral status labels use the existing primary text color. Disabled controls remain distinguishable and expose disabled semantics.

Buttons now use minimum heights so text can grow. Calendar cells expose full dates and selected/disabled state, with 48 × 52 dp targets and horizontal panning on narrow widths. Larger text uses stacked metadata where side-by-side rows crowded content; normal phone layouts retain their existing structure.

The app remains a light CampusQ theme. The remaining visual-lint/manual accessibility limits are documented in CAMPUSQ_PHASE_2_REPORT.md; no universal accessibility compliance claim is made.
