# PTV Timetable - Android

A modern Android application for viewing Public Transport Victoria (PTV) timetables with a beautiful
home screen widget. Built with Kotlin, Jetpack Compose, and Material Design 3.

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=flat&logo=kotlin&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design%203-757575?style=flat&logo=material-design&logoColor=white)

## Features

### 📱 Home Screen Widget

- **Real-time Departures**: View upcoming train, tram, bus, V/Line, and night bus departures
- **Auto-refresh**: Updates every 15 minutes automatically
- **Beautiful UI**: Material Design 3 with transport-specific colors
- **Express Indicators**: Highlights express services
- **Disruption Alerts**: Shows service disruption warnings
- **Easy Configuration**: Simple setup through configuration activity

### 🎨 Design

- Material Design 3 guidelines
- Dynamic color theming
- Transport-specific color coding:
    - 🚆 Train: Blue
    - 🚊 Tram: Green
    - 🚌 Bus: Orange
    - 🚆 V/Line: Purple
    - 🌙 Night Bus: White

## Screenshots

[Widget screenshots would go here]

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 28 or higher
- Kotlin 2.0.21

### Installation

1. Clone the repository

```bash
git clone https://github.com/yourusername/PTVTimetable.git
cd PTVTimetable
```

2. Open in Android Studio

```bash
open -a "Android Studio" .
```

3. Sync Gradle dependencies
4. Build and run on device/emulator

### Setting Up the Widget

1. Long-press on your Android home screen
2. Tap "Widgets"
3. Find "PTVTimetable" and drag to home screen
4. Configure with your route details:
    - Select route type (Train/Tram/Bus/V-Line/Night Bus)
    - Enter route name/number
    - Enter departure stop name
    - Enter destination stop name
5. Tap "Save"

For detailed widget setup instructions, see [Widget Guide](docs/WIDGET_GUIDE.md).

## Architecture

This app follows Clean Architecture principles and Android best practices:

### Layers

```
app/
├── data/
│   ├── api/          # Retrofit API service
│   ├── models/       # Data models with @SerializedName
│   └── repository/   # Repository pattern implementation
├── ui/               # UI components (Fragments/ViewModels)
└── widget/           # Widget implementation
    ├── PTVTimetableWidget.kt  # Glance widget UI
    ├── PTVWidgetReceiver.kt   # Widget receiver
    ├── PTVWidgetWorker.kt     # WorkManager worker
    └── WidgetConfigActivity.kt # Configuration UI
```

### Technologies Used

- **Kotlin**: 100% Kotlin codebase
- **Jetpack Glance**: Modern widget framework
- **Jetpack Compose**: UI toolkit (for future features)
- **Material Design 3**: Design system
- **Coroutines & Flow**: Asynchronous programming
- **Retrofit**: Network requests
- **OkHttp**: HTTP client
- **Gson**: JSON parsing
- **WorkManager**: Background task scheduling
- **Navigation Component**: Fragment navigation
- **ViewModel & LiveData**: MVVM architecture

## Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.17.0")
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("com.google.android.material:material:1.13.0")

// Glance for Widgets
implementation("androidx.glance:glance-appwidget:1.1.1")
implementation("androidx.glance:glance-material3:1.1.1")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.10.0")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// OkHttp
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
```

## API

This app uses the PTV API at `https://www.ptv.vic.gov.au/lithe/`. Key endpoints:

- `GET /routes` - List routes by type
- `GET /search` - Search for stops
- `GET /stop-services` - Get departures for a stop
- `GET /disruptions` - Get service disruptions
- `GET /route-stops` - Get stop patterns

## Project Structure

```
PTVTimetable/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/ptvtimetable/
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── PTVApiService.kt
│   │   │   │   │   └── PTVApiClient.kt
│   │   │   │   ├── models/
│   │   │   │   │   └── PTVModels.kt
│   │   │   │   └── repository/
│   │   │   │       └── PTVRepository.kt
│   │   │   ├── ui/
│   │   │   │   ├── transform/
│   │   │   │   ├── reflow/
│   │   │   │   ├── slideshow/
│   │   │   │   └── settings/
│   │   │   ├── widget/
│   │   │   │   ├── PTVTimetableWidget.kt
│   │   │   │   ├── PTVWidgetReceiver.kt
│   │   │   │   ├── PTVWidgetWorker.kt
│   │   │   │   └── WidgetConfigActivity.kt
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── xml/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── docs/
│   ├── WIDGET_GUIDE.md
│   └── architecture.md
└── README.md
```

## Development Guidelines

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- No `runBlocking` - use coroutines properly

### Best Practices

- Never hardcode DPI values
- Use `@SerializedName` for data classes (ProGuard compatibility)
- Follow Material Design 3 guidelines
- Use Kotlin coroutines and Flow for async operations
- Implement proper error handling

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

## Troubleshooting

### Widget Not Showing

1. Check if widget is properly registered in AndroidManifest.xml
2. Verify permissions are granted
3. Check WorkManager is not restricted in battery settings

### API Errors

1. Verify internet connection
2. Check API token is valid
3. Review logs for detailed error messages

### Build Issues

1. Clean and rebuild: `./gradlew clean build`
2. Invalidate caches in Android Studio
3. Update Android Studio and Gradle plugin

## Roadmap

- [ ] Multiple widget instances support
- [ ] Different widget sizes (small, medium, large)
- [ ] Dark theme support
- [ ] In-app timetable viewer
- [ ] Favorite routes management
- [ ] Notifications for departures
- [ ] Trip planning feature
- [ ] Offline mode with cached data

## License

This project is for personal use only. Not affiliated with PTV or Myki.

## Credits

- **Original iOS Widget**: [Ricky Li](https://github.com/imchlorine/PTVTimetable)
- **PTV API**: Public Transport Victoria
- **Android Implementation**: Built with modern Android development practices

## Disclaimer

This app is not affiliated with, endorsed by, or officially connected to Public Transport Victoria (
PTV) or Myki. All product and company names are trademarks™ or registered® trademarks of their
respective holders.

For official PTV information, visit [ptv.vic.gov.au](https://www.ptv.vic.gov.au)

## Contact

For issues and questions, please open an issue on GitHub.

---

Made with ❤️ for Melbourne commuters
