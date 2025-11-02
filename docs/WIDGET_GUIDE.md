# PTV Timetable Android Widget Guide

## Overview

The PTV Timetable Android Widget displays real-time departure information for Public Transport
Victoria (PTV) routes directly on your Android home screen. This widget is based on the iOS
Scriptable widget design and provides similar functionality.

## Features

- **Real-time Departures**: Shows upcoming departures with estimated times
- **Multiple Transport Types**: Supports Train, Tram, Bus, V/Line, and Night Bus
- **Express Service Indicators**: Highlights express services
- **Disruption Alerts**: Displays warning when service disruptions are active
- **Automatic Updates**: Refreshes every 15 minutes using WorkManager
- **Material Design 3**: Modern, beautiful UI following Android design guidelines
- **Route-Specific Colors**: Each transport type has its own distinctive color:
    - Train: Blue (#3070C7)
    - Tram: Green (#88BC41)
    - Bus: Orange (#EF8933)
    - V/Line: Purple (#832690)
    - Night Bus: White

## Architecture

### Components

1. **PTVTimetableWidget** (Glance Widget)
    - Main widget UI using Jetpack Glance
    - Displays departure information from SharedPreferences
    - Responsive Material 3 design

2. **PTVWidgetReceiver** (AppWidget Receiver)
    - Handles widget lifecycle events
    - Updates widget when system triggers refresh

3. **PTVWidgetWorker** (WorkManager Worker)
    - Fetches data from PTV API every 15 minutes
    - Updates SharedPreferences with latest departures
    - Triggers widget UI refresh

4. **WidgetConfigActivity** (Configuration Activity)
    - User interface for configuring widget settings
    - Input fields for route type, route name, and stops
    - Material Design 3 components

5. **PTVRepository** (Data Layer)
    - Handles all API calls to PTV
    - Implements business logic for data processing
    - Error handling and data transformation

6. **PTVApiService** (Retrofit Interface)
    - Defines API endpoints
    - Uses Kotlin coroutines for async operations

## Setup Instructions

### 1. Add Widget to Home Screen

1. Long-press on your Android home screen
2. Tap "Widgets"
3. Find "PTVTimetable" in the widget list
4. Drag the "PTV Timetable" widget to your home screen

### 2. Configure Widget

When you place the widget, a configuration screen will appear:

1. **Route Type**: Select from:
    - Train
    - Tram
    - Bus
    - V/Line
    - Night Bus

2. **Route Name**: Enter the route identifier
    - For trains: Use line name (e.g., "Alamein", "Belgrave")
    - For trams: Use route number (e.g., "1", "86", "96")
    - For buses: Use route number (e.g., "200", "207")
    - For V/Line: Use full route name

3. **From Stop**: Enter your departure stop name
    - Use the exact name as it appears in the PTV app
    - Include all details (e.g., "Melbourne University/Swanston St #1")

4. **To Stop**: Enter your destination stop name
    - Use the exact name as it appears in the PTV app
    - Include all details (e.g., "Federation Square/Swanston St #13")

5. Tap **Save** to create the widget

### 3. Widget Updates

- The widget automatically updates every 15 minutes
- Data is fetched in the background using WorkManager
- No manual refresh required
- Updates work even when the app is closed

## Technical Details

### Dependencies

```kotlin
// Glance for modern widgets
implementation("androidx.glance:glance-appwidget:1.1.1")
implementation("androidx.glance:glance-material3:1.1.1")

// WorkManager for periodic updates
implementation("androidx.work:work-runtime-ktx:2.10.0")

// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// OkHttp for networking
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
```

### API Integration

The widget uses the PTV API at `https://www.ptv.vic.gov.au/lithe/` with the following endpoints:

- `/routes` - Get route information
- `/search` - Search for stops
- `/stop-services` - Get departures for a stop
- `/disruptions` - Get service disruptions
- `/route-stops` - Get stops pattern for a route

### Data Storage

Widget configuration and departure data is stored in SharedPreferences:

- **Configuration**: `route_type`, `route_name`, `from_stop`, `to_stop`
- **Departure Data**: Scheduled times, platforms, express status, minutes until departure
- **Metadata**: Last update time, disruption status

### Widget Display Information

The widget displays:

- Transport type icon (🚆/🚊/🚌)
- Last update time
- Departure and destination stops
- Route number/name
- Up to 3 upcoming departures with:
    - Platform number (if applicable)
    - Scheduled departure time
    - Minutes until departure
    - Express service indicator
    - Real-time tracking icon

## Comparison with iOS Version

### Similarities

- Same API endpoints and data source
- Matching color scheme for transport types
- Similar layout and information hierarchy
- Disruption warnings
- Express service indicators

### Differences

- **Technology**: Uses Jetpack Glance instead of Scriptable
- **Updates**: WorkManager for background updates vs on-demand refresh
- **Configuration**: Dedicated configuration activity vs script parameters
- **Persistence**: SharedPreferences vs widget parameters
- **Styling**: Material Design 3 vs iOS widget styling

## Troubleshooting

### Widget Not Updating

1. Check internet connection
2. Verify WorkManager is not restricted in battery settings
3. Check app permissions for network access
4. Reinstall widget if necessary

### Configuration Errors

1. Ensure stop names match exactly as in PTV app
2. Verify route name is correct for the selected route type
3. Check that both departure and destination stops are on the same route

### No Departures Showing

1. Verify stops are correct
2. Check if service is running at current time
3. Look for disruption notices
4. Reconfigure widget with correct information

## Future Enhancements

Potential improvements:

- Multiple widget instances with different routes
- Widget size variants (small, medium, large)
- Dark/light theme support
- Tap actions to open PTV app or refresh manually
- Favorite routes management in main app
- Historical departure tracking

## Credits

Based on the iOS Scriptable widget by Ricky Li

- Original repository: https://github.com/imchlorine/PTVTimetable.git

Android implementation uses modern Android development practices:

- Kotlin coroutines for async operations
- Jetpack Glance for widget UI
- Material Design 3 guidelines
- Repository pattern for data management
- WorkManager for reliable background work

## Disclaimer

This widget is not affiliated with PTV or Myki. For personal use only. Official PTV information
should be verified through official PTV channels.
