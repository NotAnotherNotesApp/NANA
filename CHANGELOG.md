# Changelog

## v0.9.1 (Build 5)

### Highlights
- Widgets: off-main-thread DB/DataStore work with snapshot rendering for faster updates
- Checklist widget: single-row toggle with immediate refresh to avoid stale state
- Recent Notes widget: top-three non-checklist query and shared Glance color providers
- Scheduling: periodic polling disabled; daily midnight WorkManager refresh plus boot-time update
- Stability: tightened widget error handling paths and consistent theming across widgets

## v0.8.9 (Build 4)

### Features
- **Notes**: Create, edit, and organize notes with rich text editing and checklists
- **Schedule**: Calendar-based event management with recurrence, reminders, and location support
- **Routines**: Daily routine tracking with completion history and trend statistics
- **Finances**: Expense/income logging with category budgets, multi-currency support, and spending overview
- **Widgets**: Quick Actions, Recent Notes, Checklist, and Budget Status home screen widgets

### Recent Improvements
- Timezone picker now displays UTC offset alongside timezone IDs
- Expanded category icons from 80 to 110+ with new groups (Daily Habits, Bills & Utilities)
- Icon picker redesigned with scrollable grid and category grouping
- Color palette expanded from 12 to 18 colors (Fuchsia, Sky, Emerald, Rose, Stone, Royal Blue)
- Inline category creation in transaction editor
- Snackbar feedback for trash operations (restore, delete, empty)
- Notes archive and trash management with restore/delete support
- Cascade delete support for data integrity
- Query performance indexes on dates, pins, and activity status
- StateFlow conversions to prevent UI flashing
- Loading states on all editor screens
- Labels and categories management screen
- Widgets moved DB/DataStore work off the main thread with snapshot rendering for faster updates
- Checklist widget now toggles items with single-row queries and immediate refresh
- Recent Notes widget limits to top non-checklist notes; shared Glance color providers across widgets
- Periodic widget polling disabled; daily midnight WorkManager refresh plus boot-time update added
