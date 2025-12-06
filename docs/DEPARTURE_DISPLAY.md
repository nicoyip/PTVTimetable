# Displaying PTV Departures on MainActivity

## Overview

The MainActivity now displays real-time PTV timetable data through the Transform fragment. This
guide explains how the feature works and how to use it.

## Architecture

### Components

1. **TransformViewModel** (`ui/transform/TransformViewModel.kt`)
    - Manages departure data state using Kotlin Flow
    - Fetches data from PTVRepository
    - Handles loading, success, and error states
    - Formats times and calculates minutes until departure

2. **TransformFragment** (`ui/transform/TransformFragment.kt`)
    - Displays departures in a RecyclerView
    - Shows loading indicators and error states
    - Provides configuration dialog for route selection
    - Supports both phone and tablet layouts

3. **PTVRepository** (`data/repository/PTVRepository.kt`)
    - Handles API calls to PTV service
    - Provides route, stop, and departure data
    - Returns Result<T> for error handling

### Data Flow

```
User Input → ViewModel → Repository → API Service → ViewModel → UI
```

1. User configures route via dialog
2. ViewModel calls repository methods
3. Repository fetches from PTV API
4. Data flows back through StateFlow
5. Fragment observes and updates UI

## Usage

### Configuring Departures

When you navigate to the Transform screen, a configuration dialog appears:

**Fields:**

- **Route Type**: 0=tram, 1=train, 2=bus, 3=v/line, 4=night bus
- **Route Name**: Route number or name (e.g., "86")
- **From Stop**: Departure stop name (e.g., "Bourke St/Spencer St")
- **To Stop**: Destination stop name (e.g., "Bundoora RMIT")

**Example Configuration:**

```
Route Type: 0
Route Name: 86
From Stop: Bourke St/Spencer St
To Stop: Bundoora RMIT
```

### Viewing Departures

Once configured, the screen displays:

- **Header**: Route and stop information
- **Departure Cards**: Each showing:
    - Route label and direction
    - Platform number (if available)
    - Express badge (if express service)
    - Scheduled departure time
    - Minutes until departure (large, on right)
    - Color-coded route indicator bar

### Color Coding

Route indicators use PTV's official colors:

- **Tram**: Green (#88BC41)
- **Train**: Blue (#3070C7)
- **Bus**: Orange (#EF8933)
- **V/Line**: Purple (#832690)
- **Night Bus**: White (#FFFFFF)

### States

1. **Loading**: Progress indicator shown
2. **Success**: Departures displayed in cards
3. **Empty**: "No upcoming departures" message
4. **Error**: Error message with retry button

## UI Layouts

### Phone (Default)

- Linear list of departure cards
- Full-width cards with comfortable spacing
- Header at top showing route info

### Tablet (w600dp+)

- Grid layout (2 columns for w600dp, 4 columns for very large screens)
- Cards arranged in grid for better space utilization
- Same card design as phone

## Data Models

### DepartureUiState

```kotlin
data class DepartureUiState(
    val isLoading: Boolean = false,
    val departures: List<DepartureItem> = emptyList(),
    val error: String? = null,
    val routeName: String = "",
    val fromStop: String = "",
    val toStop: String = ""
)
```

### DepartureItem

```kotlin
data class DepartureItem(
    val scheduledTime: String,      // e.g., "3:45 PM"
    val minutesUntil: String,        // e.g., "5 mins", "Now", "Departed"
    val platform: String?,           // e.g., "1", "2"
    val isExpress: Boolean,          // Express service indicator
    val routeLabel: String,          // e.g., "86 - Bundoora RMIT"
    val routeType: Int              // 0-4 for color coding
)
```

## Material Design 3

The UI follows Material Design 3 guidelines:

- **MaterialCardView** for departure items
- **Chips** for platform and express badges
- **Typography** using Material theme text appearances
- **Colors** from app theme and PTV brand colors
- **Elevation** and corner radius for depth
- **Spacing** following 8dp grid system

## Error Handling

The app handles various error scenarios:

1. **Route Not Found**: Displays specific error message
2. **Stop Not Found**: Displays specific error message
3. **API Failure**: Shows network error with retry option
4. **Empty Results**: Shows "No departures" message

Users can retry failed requests via:

- Snackbar retry button
- Re-opening configuration dialog (tap FAB)

## Future Enhancements

Potential improvements:

1. **Persistent Configuration**: Save last used route
2. **Multiple Routes**: Support multiple departure boards
3. **Auto-Refresh**: Periodically update departures
4. **Disruption Alerts**: Display service disruptions
5. **Favorites**: Quick access to saved routes
6. **Notifications**: Alert before departure
7. **Offline Cache**: Show last known data when offline
8. **Search**: Better stop/route search UX

## Testing

### Manual Testing Checklist

- [ ] Dialog appears on first launch
- [ ] Valid route/stop loads departures
- [ ] Invalid input shows error
- [ ] Retry button works
- [ ] Loading indicator displays
- [ ] Empty state shows when no departures
- [ ] Express badge shows for express services
- [ ] Platform numbers display correctly
- [ ] Colors match route types
- [ ] Times update correctly
- [ ] Grid layout works on tablets

### Test Data

**Working Examples:**

```
Tram 86:
- Route Type: 0
- Route Name: 86
- From: Bourke St/Spencer St
- To: Bundoora RMIT

Train (Frankston Line):
- Route Type: 1  
- Route Name: Frankston
- From: Flinders Street
- To: Frankston
```

## Troubleshooting

**Problem**: No departures shown

- Check route name matches exactly
- Verify stop name is correct
- Ensure internet connection
- Check API token is valid

**Problem**: Wrong departures

- Verify route type number
- Check stop name spelling
- Ensure direction is correct

**Problem**: App crashes

- Check logs for exceptions
- Verify all layouts have required views
- Ensure API response format matches models

## Technical Notes

- Uses Kotlin Coroutines for async operations
- StateFlow for reactive UI updates
- Material Design 3 components throughout
- ViewBinding for type-safe view access
- Repository pattern for data layer
- No runBlocking usage (follows project guidelines)
- @SerializedName for ProGuard compatibility
- Supports multiple screen sizes and orientations
