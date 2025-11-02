# Quick Start Guide - PTV Timetable Widget

## 🚀 Get Started in 5 Minutes

> **Note:** The Compose Compiler plugin issue has been fixed! If you encounter any build errors,
> see [Troubleshooting Guide](#-troubleshooting).

### Step 1: Sync Dependencies

```bash
# In Android Studio
1. Open the project
2. Click "Sync Project with Gradle Files" 
   (or File → Sync Project with Gradle Files)
3. Wait for dependencies to download
```

### Step 2: Build the App

```bash
# Via Android Studio
1. Select your device/emulator
2. Click Run (▶️) or press Shift+F10

# Via Command Line
./gradlew installDebug
```

### Step 3: Add Widget to Home Screen

```
1. Long-press on home screen
2. Tap "Widgets"
3. Find "PTV Timetable"
4. Drag to home screen
```

### Step 4: Configure Widget

```
When the configuration screen appears:

1. Route Type: Select (e.g., "Tram")
2. Route Name: Enter route (e.g., "1")
3. From Stop: Enter departure stop
   Example: "Melbourne University/Swanston St #1"
4. To Stop: Enter destination stop
   Example: "Federation Square/Swanston St #13"
5. Tap "Save"
```

### Step 5: Wait for First Update

The widget will show a loading state, then:

- Fetches data from PTV API
- Updates automatically every 15 minutes
- Shows next 3 departures

## 📝 Example Configurations

### Tram Route 1 (Coburg to South Melbourne Beach)

```
Route Type: Tram
Route Name: 1
From Stop: Melbourne University/Swanston St #1
To Stop: Federation Square/Swanston St #13
```

### Train - Alamein Line

```
Route Type: Train
Route Name: Alamein
From Stop: Melbourne Central Railway Station
To Stop: Camberwell Railway Station
```

### Bus Route 200

```
Route Type: Bus
Route Name: 200
From Stop: [Your stop name from PTV app]
To Stop: [Your destination from PTV app]
```

## ⚙️ Important Notes

### Stop Names

- **Use exact names** from the PTV mobile app
- Include **all details**: street names, stop numbers
- Example: ✅ "Melbourne University/Swanston St #1"
- Not: ❌ "Melbourne University"

### Route Names

- **Trains**: Use line name (Alamein, Belgrave, etc.)
- **Trams**: Use number (1, 86, 96, etc.)
- **Buses**: Use number (200, 207, etc.)
- **V/Line**: Use full route name

### Updates

- Widget updates every **15 minutes** automatically
- No manual refresh needed
- Works in background even when app closed
- Requires internet connection

## 🐛 Troubleshooting

### Widget Not Appearing?

```bash
1. Check Gradle sync completed successfully
2. Rebuild: Build → Rebuild Project
3. Reinstall app
```

### Configuration Not Saving?

```bash
1. Check all fields are filled
2. Verify stop names are exact matches
3. Check internet connection
```

### No Departures Showing?

```bash
1. Wait 1-2 minutes for first data fetch
2. Check if service is running at current time
3. Verify route and stop names are correct
4. Check app logs: adb logcat | grep PTV
```

### Linter Errors After Creating Files?

```bash
This is normal! They'll resolve after Gradle sync:
1. File → Sync Project with Gradle Files
2. Build → Clean Project
3. Build → Rebuild Project
```

## 📱 Testing Commands

### View Logs

```bash
adb logcat | grep -i ptv
```

### Clear Widget Data

```bash
adb shell
pm clear com.example.ptvtimetable
```

### Force Widget Update

```bash
# Restart WorkManager
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

## 🎯 Next Steps

1. **Customize Colors**: Edit `res/values/colors.xml`
2. **Add More Routes**: Duplicate widget on home screen
3. **Explore Code**: Check `docs/WIDGET_GUIDE.md`
4. **Contribute**: See `README.md` for guidelines

## 📚 Documentation

- **Widget Guide**: [docs/WIDGET_GUIDE.md](docs/WIDGET_GUIDE.md)
- **Architecture**: [docs/architecture.md](docs/architecture.md)
- **Implementation**: [docs/IMPLEMENTATION_SUMMARY.md](docs/IMPLEMENTATION_SUMMARY.md)

## 🆘 Need Help?

1. Check troubleshooting section above
2. Review [docs/WIDGET_GUIDE.md](docs/WIDGET_GUIDE.md)
3. Open an issue on GitHub

## ✅ Success Checklist

- [ ] Gradle sync completed
- [ ] App builds without errors
- [ ] Widget appears in widget picker
- [ ] Configuration screen opens
- [ ] Widget displays on home screen
- [ ] Data loads after 1-2 minutes
- [ ] Departures show correctly
- [ ] Widget updates every 15 minutes

---

**Ready to go!** 🎉 Your PTV Timetable widget is now set up!
