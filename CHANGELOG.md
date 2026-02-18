# Changelog

All notable changes to the NANA app will be documented in this file.

---

## [0.8.5] - 2026-02-18

### Added
- **Labels & Categories system** -- unified label management across Notes, Events, and Finances
  - Preset labels seeded on first launch (4 note labels, 8 expense categories, 6 income categories, 7 event categories)
  - Custom label creation with icon and color selection
  - Soft-delete for preset labels (hide/unhide)
  - Dedicated Labels & Categories screen accessible from Settings
- **Database migration 8 -> 9** -- new `labels` table with unique type/name index

### Changed
- Version bumped from 0.8.1 to 0.8.5

### Fixed
- Database performance improvements via indexes on all tables (migration 7 -> 8)

---

## [0.8.1] - Initial Tracked Release

### Core Features

#### Notes
- Rich text note editor with HTML content support
- Checklist notes with reorderable, checkable items
- Image attachments with position ordering
- Pin, archive, and soft-delete (trash) support
- Per-note color coding (9 accent colors)
- Note Viewer (read-only) and Note Editor screens
- Archive and Trash screens with restore/permanent delete

#### Schedule
- Event creation with title, description, location, and category
- All-day event support
- Multiple reminders per event (configurable minute offsets)
- Recurring events (Daily, Weekly, Monthly, Yearly, Custom RRULE)
- Event color coding and pinning
- Predefined categories: Meeting, Class, Personal, Work, Health, Social, Other
- Schedule Viewer and Schedule Editor screens

#### Routines
- Three routine types: Simple (mark done), Counter (target count), Timer (duration-based)
- Per-day-of-week scheduling with reminder times
- Streak tracking with completion history
- Routine Statistics screen with progress visualization
- Custom icon and color per routine
- Pinning support

#### Finances
- Income and Expense transaction tracking
- Predefined expense categories (Food, Transport, Shopping, Entertainment, Bills, Health, Education, Other)
- Predefined income categories (Allowance, Part-time Job, Scholarship, Gift, Other)
- Budget Manager with per-category and overall budgets (Weekly, Monthly, Yearly periods)
- Finances Overview screen with charts (Vico library)
- Transaction Editor with date, amount, category, and note fields
- Currency support for 10 currencies

### Notifications & Reminders
- AlarmManager-based exact alarm scheduling (with inexact fallback on Android 12+)
- Three notification channels: Events (high), Routines (default), General (default)
- Event reminders at configurable offsets before start time
- Routine reminders with weekly repeating alarms per scheduled day
- "Mark Done" action button on routine notifications
- Deep-link intents from notifications to relevant screens
- Alarm persistence across device reboots via BootReceiver
- Runtime notification permission handling for Android 13+

### Backup & Restore
- Full data export to JSON (backup format v2)
- Exports all 9 entity types plus user preferences
- Backup saved to Downloads as `nana_backup_YYYYMMDD_HHmmss.json`
- Import from file with full data replacement (FK-safe ordering)
- Preferences restored on import (theme, currency, timezone, 24h format)
- Reminder rescheduling after restore

### Settings
- **General:** Currency selector, Timezone selector, 24-hour format toggle
- **Appearance:** Theme picker (Light, Dark, AMOLED, System default)
- **Data Management:** Backup, Restore, Empty Trash
- **About:** Version display, Developer link, Latest Release link, Open Source Licenses

### Theme & UI
- Material 3 with dynamic color support (Material You on Android 12+)
- AMOLED true-black theme
- Jetpack Compose UI with animated navigation transitions
- Edge-to-edge layout with status/nav bar color sync
- Animated bottom navigation bar (hide on sub-screens)
- Manrope font family with full M3 type scale
- Standardized spacing, corners, icons, and button dimensions
- Custom pin icon vectors (Keep/KeepFilled)

### Technical
- Room database (version 9, 9 entities, 6 DAOs)
- Kotlin with Jetpack Compose, KSP for annotation processing
- DataStore for user preferences
- Coil for image loading
- Gson for JSON serialization
- Min SDK 26, Target SDK 35
- ProGuard minification and resource shrinking for release builds
