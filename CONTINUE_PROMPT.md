# Nana Android App - Session Context

## Project Overview
Android app "Nana" - personal productivity app with Notes, Schedule, Routines, and Finances modules. Built with Jetpack Compose, Material3, Room database, and Navigation Compose.

## Recent Changes (January 2, 2026)

### Data Layer Improvements (Critical)
1. **Foreign Keys with Cascade Deletes** - Added to:
   - `NoteImage` -> `Note` (CASCADE delete)
   - `ChecklistItem` -> `Note` (CASCADE delete)
   - `RoutineCompletion` -> `Routine` (CASCADE delete)

2. **Database Indexes** - Added for query performance:
   - Notes: `isDeleted/isArchived`, `isPinned`, `updatedAt`
   - Events: `startTime`, `endTime`, `isPinned`, `category`
   - Routines: `isActive`, `isPinned`
   - RoutineCompletions: `routineId`, `date`, unique constraint on `(routineId, date)`
   - Transactions: `date`, `type`, `category`
   - Budgets: unique constraint on `category`

3. **Database Migration** - Version 7 -> 8 with index creation

### ScheduleViewerScreen (NEW)
- Created `ScheduleViewerScreen.kt` - read-only event viewer screen
- Shows: category badge, title, time/date, location, recurrence, reminder, notes
- Navigation: Schedule list -> Viewer (tap card) -> Editor (edit button)
- Route: `Screen.ScheduleViewer` with `eventId` argument

### StateFlow Optimizations
Converted from `Flow` to `StateFlow` to prevent UI flashing:
- `ScheduleViewModel.eventsForSelectedDay`
- `NotesViewModel.notes`
- `FinancesViewModel.filteredTransactions`
- `RoutinesViewModel.routines`, `completedTodayIds`, `todayCompletions`

### Loading States Added
Added loading indicators to all editor screens:
- `ScheduleEditorScreen`
- `NoteEditorScreen`
- `RoutineEditorScreen`
- `TransactionEditorScreen`
- `ChecklistEditorScreen`

### UI/Accessibility Fixes
- Fixed icon-text alignment in ScheduleViewerScreen (added `verticalAlignment`)
- Fixed navigation transitions (ScheduleViewer uses consistent slide pattern)
- Increased touch targets in NoteEditorScreen (HeaderButton, FormattingButton to 40dp with 48dp min)
- BudgetManagerScreen month selector now shows current month first

### BudgetDialog Improvements
- Added `currencySymbol` parameter (was hardcoded $)
- Validation allows 0 amount (category without budget)

## Key File Locations
- Screens: `app/src/main/java/com/allubie/nana/ui/screens/`
- ViewModels: same directory as screens
- Navigation: `app/src/main/java/com/allubie/nana/ui/navigation/`
- Database: `app/src/main/java/com/allubie/nana/data/`
- Theme: `app/src/main/java/com/allubie/nana/ui/theme/`

## Build Command
```powershell
cd D:\nana\android
.\gradlew assembleDebug
.\gradlew installDebug
```

## Known Issues (Non-blocking)
- Deprecation warnings: `Icons.Filled.StickyNote2`, `statusBarColor`, `menuAnchor()`
- Some hardcoded strings should be moved to strings.xml for localization
- Some hardcoded category colors could be centralized

## Architecture Notes
- Uses `viewModel(factory = ViewModel.Factory)` pattern
- Preferences stored in DataStore
- Events use `recurrenceRule` (RRULE format) for repeating events
- `reminderMinutes: List<Int>` for event reminders
- Database version: 8 (includes indexes and foreign keys)
