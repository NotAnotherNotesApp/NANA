# NANA - Student Management App

NANA is a minimal Android application designed specifically for students to manage their academic and personal lives through four main functionalities: Notes, Routines, Schedules, and Expense tracking.

## Features

### Notes
- Create, edit, and organize notes
- Pin important notes
- Rich text editing support
- Search functionality
- Categories and labels
- Archive and recycle bin

### Routines
- Create daily/weekly routines
- Track completion progress
- Habit tracking integration
- Configurable reminders
- Progress statistics

### Schedules
- Time-based schedule management
- Calendar views (daily, weekly, monthly)
- Class schedule templates
- Conflict detection
- Recurring events support

### Expenses
- Track expenses with categories
- Budget management
- Monthly summaries
- Customizable expense categories
- Visual spending analytics

### Screenshots

<div align="center">
<div>
<img src="fastlane/metadata/metadata/images/phoneScreenshots/1.png" width="30%" />
<img src="fastlane/metadata/metadata/images/phoneScreenshots/2.png" width="30%" />
<img src="fastlane/metadata/metadata/images/phoneScreenshots/4.png" width="30%" />
<img src="fastlane/metadata/metadata/images/phoneScreenshots/5.png" width="30%" />
<img src="fastlane/metadata/metadata/images/phoneScreenshots/6.png" width="30%" />
<img src="fastlane/metadata/metadata/images/phoneScreenshots/7.png" width="30%" />
</div>
</div>

## Technical Specifications

- **Platform**: Android
- **Minimum API Level**: 24 (Android 7.0 Nougat)
- **Target API Level**: 34
- **Development**: Kotlin + Jetpack Compose
- **Architecture**: MVVM with Room database
- **UI Framework**: Material You design system

## UI Features

- Material You adaptive theming
- AMOLED dark theme support
- Smooth animations and transitions
- Bottom navigation with 4 main sections
- Overflow menu for settings and additional features

## Project Structure

```
app/
├── src/main/java/com/allubie/nana/
│   ├── MainActivity.kt
│   └── ui/
│       ├── navigation/
│       │   └── MainNavigation.kt
│       ├── screens/
│       │   ├── notes/
│       │   ├── routines/
│       │   ├── schedules/
│       │   ├── expenses/
│       │   └── settings/
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── src/main/res/
    ├── values/
    ├── drawable/
    └── mipmap/
```

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Run on device or emulator

```bash
./gradlew build
```

## Development Status

This project contains the complete UI implementation with:
-  Modern Material You design
-  Bottom navigation
-  All four main screens (Notes, Routines, Schedules, Expenses)
-  Settings screen
-  Responsive layout components
-  Sample data for demonstration

## Next Steps

- Implement Room database for data persistence
- Add view models and state management
- Implement actual functionality for CRUD operations
- Add notification system
- Implement backup/restore features
- Add user preferences and settings persistence

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## Developer

Created by allubie as part of the NANA student management application project.
