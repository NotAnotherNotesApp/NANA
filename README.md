# Nana

A beautiful, all-in-one personal productivity app for Android built with Jetpack Compose and Material Design 3.

![Android](https://img.shields.io/badge/Android-26%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

### 📝 Notes
- Rich text editor with formatting (bold, italic, underline, strikethrough)
- Headers (H1, H2, H3), lists (ordered/unordered), and code blocks
- Note labels/tags with customizable colors
- Image attachments
- Pin important notes
- Archive and trash management

### ✅ Checklists
- Create and manage task lists
- Check off completed items
- Organize with labels

### 💰 Finance Tracker
- Track income and expenses
- Customizable expense/income categories with unique colors
- Budget management with weekly/monthly/yearly periods
- Visual spending breakdown with donut charts
- Cash flow overview and insights

### 📅 Schedule
- Event management with categories
- Visual calendar integration
- Event viewer with details

### 🔄 Routines
- Create daily/weekly routines
- Track routine completion
- Statistics and progress tracking

### ⚙️ Settings
- Multiple theme options (Light, Dark, AMOLED Black, System)
- Dynamic color support (Material You)
- Customizable labels and categories
- Currency symbol configuration

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM with ViewModels
- **Database:** Room (SQLite)
- **Dependency Injection:** Manual with ViewModelProvider.Factory
- **Async:** Kotlin Coroutines & Flow
- **Rich Text:** Compose Rich Editor

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog or newer

## Building

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/nana.git
   cd nana/android
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Run on an emulator or physical device

## Project Structure

```
app/src/main/java/com/allubie/nana/
├── data/
│   ├── dao/          # Room DAOs
│   ├── model/        # Data models & entities
│   └── repository/   # Repository layer
├── ui/
│   ├── screens/      # Feature screens
│   │   ├── finances/
│   │   ├── notes/
│   │   ├── routines/
│   │   ├── schedule/
│   │   └── settings/
│   └── theme/        # App theming
├── util/             # Utility classes
└── NanaApplication.kt
```

## Screenshots

*Coming soon*

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Compose Rich Editor](https://github.com/MohamedRejworksolutions/compose-rich-editor)
