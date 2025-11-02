# Troubleshooting Guide

## Build Issues

### ❌ Error: "Compose Compiler Gradle plugin is required when compose is enabled"

**Problem:**

```
com.android.builder.errors.EvalIssueException: Starting in Kotlin 2.0, 
the Compose Compiler Gradle plugin is required when compose is enabled.
```

**Solution:**
This has been fixed! The project now includes the Compose Compiler plugin.

If you still see this error:

1. Ensure `gradle/libs.versions.toml` includes:
   ```toml
   [plugins]
   compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
   ```

2. Ensure `app/build.gradle.kts` includes:
   ```kotlin
   plugins {
       alias(libs.plugins.compose.compiler)
   }
   ```

3. Sync Gradle: `File → Sync Project with Gradle Files`

---

### ❌ Unresolved References (Retrofit, Glance, etc.)

**Problem:**

```
Unresolved reference: retrofit2
Unresolved reference: glance
Unresolved reference: work
```

**Solution:**
These errors appear before Gradle sync completes.

1. **Sync Gradle Dependencies:**
    - `File → Sync Project with Gradle Files`
    - Wait for dependencies to download

2. **If sync fails, check internet connection**

3. **Clear Gradle cache if needed:**
   ```bash
   ./gradlew clean
   rm -rf ~/.gradle/caches/
   ./gradlew build --refresh-dependencies
   ```

4. **Invalidate Android Studio caches:**
    - `File → Invalidate Caches → Invalidate and Restart`

---

### ❌ compileSdk version error

**Problem:**

```
Execution failed for task ':app:checkDebugAarMetadata'.
Could not resolve all files for configuration ':app:debugRuntimeClasspath'.
```

**Solution:**
The issue is with `compileSdk { version = release(36) }`.

**Fix:** Change in `app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.example.ptvtimetable"
    compileSdk = 34  // Change this line
    
    defaultConfig {
        applicationId = "com.example.ptvtimetable"
        minSdk = 28
        targetSdk = 34  // Change this too
        // ...
    }
}
```

---

### ❌ Manifest merger failed

**Problem:**

```
Manifest merger failed : Attribute application@theme value=...
```

**Solution:**
Check `AndroidManifest.xml` for duplicate entries or conflicting attributes.

Ensure only one `<application>` tag exists with all receivers inside it.

---

## Widget Issues

### ❌ Widget Not Appearing in Widget Picker

**Checklist:**

- [ ] App is installed successfully
- [ ] `AndroidManifest.xml` has `PTVWidgetReceiver` registered
- [ ] Widget info XML exists at `res/xml/ptv_widget_info.xml`
- [ ] Receiver has `android:exported="true"`

**Solution:**

1. Uninstall and reinstall the app
2. Reboot device/emulator
3. Check logcat for errors:
   ```bash
   adb logcat | grep -i widget
   ```

---

### ❌ Configuration Activity Not Opening

**Problem:**
Widget added but configuration screen doesn't appear.

**Solution:**

1. Check `AndroidManifest.xml` has:
   ```xml
   <activity
       android:name=".widget.WidgetConfigActivity"
       android:exported="false">
       <intent-filter>
           <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
       </intent-filter>
   </activity>
   ```

2. Check `ptv_widget_info.xml` has:
   ```xml
   android:configure="com.example.ptvtimetable.widget.WidgetConfigActivity"
   ```

---

### ❌ Widget Shows Loading Forever

**Problem:**
Widget displays loading spinner but never shows data.

**Solution:**

1. **Check internet connection**

2. **Verify WorkManager is running:**
   ```bash
   adb shell dumpsys activity service WorkManagerService
   ```

3. **Check logs for API errors:**
   ```bash
   adb logcat | grep -i ptv
   ```

4. **Force an update:**
   ```bash
   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
   ```

5. **Check battery optimization:**
    - Settings → Apps → PTVTimetable
    - Battery → Unrestricted

---

### ❌ No Departures Showing

**Problem:**
Widget shows "0 departures" or empty list.

**Possible Causes:**

1. **Incorrect stop names** - Use exact names from PTV app
2. **No service at current time** - Check PTV website
3. **Wrong route type/name** - Verify configuration
4. **API token expired** - Check GitHub repository for token

**Solution:**

1. Reconfigure widget with exact stop names
2. Test with a known working route (e.g., Tram 1)
3. Check PTV service status
4. Review logs for API errors

---

## Runtime Issues

### ❌ WorkManager Not Scheduling Updates

**Problem:**
Widget doesn't update after initial load.

**Solution:**

1. **Check WorkManager constraints:**
   ```kotlin
   val constraints = Constraints.Builder()
       .setRequiredNetworkType(NetworkType.CONNECTED)
       .build()
   ```

2. **Verify periodic work is scheduled:**
   ```bash
   adb shell dumpsys jobscheduler | grep -i ptv
   ```

3. **Re-save configuration** to reschedule work

---

### ❌ API Token Issues

**Problem:**

```
HTTP 401 Unauthorized
Authentication required
```

**Solution:**
The token is fetched from:

```
https://raw.githubusercontent.com/imchlorine/PTVTimetable/refs/heads/main/token
```

If this fails:

1. Check GitHub repository is accessible
2. Check token file exists in repository
3. Implement local token storage as fallback

---

### ❌ ProGuard/R8 Issues in Release Build

**Problem:**
App crashes in release build but works in debug.

**Solution:**
Add to `proguard-rules.pro`:

```proguard
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.ptvtimetable.data.models.** { *; }

# Glance
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.work.** { *; }
```

---

## Development Issues

### ❌ ViewBinding Not Working

**Problem:**

```
Unresolved reference: ActivityWidgetConfigBinding
```

**Solution:**

1. Ensure `buildFeatures { viewBinding = true }` in `build.gradle.kts`
2. Sync Gradle
3. Rebuild project: `Build → Rebuild Project`
4. Check layout file name matches binding class

---

### ❌ Kotlin Version Conflicts

**Problem:**

```
Kotlin version mismatch
Expected 2.0.21, found 1.9.x
```

**Solution:**

1. Update all Kotlin plugins to same version
2. Check `gradle/libs.versions.toml`:
   ```toml
   kotlin = "2.0.21"
   ```
3. Update Android Studio to latest version

---

## Testing Commands

### View All Logs

```bash
adb logcat | grep -i ptv
```

### Clear App Data

```bash
adb shell pm clear com.example.ptvtimetable
```

### Force Widget Update

```bash
adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE
```

### Check WorkManager Status

```bash
adb shell dumpsys jobscheduler | grep ptv
```

### View SharedPreferences

```bash
adb shell run-as com.example.ptvtimetable cat shared_prefs/widget_prefs.xml
```

### Restart App

```bash
adb shell am force-stop com.example.ptvtimetable
adb shell am start -n com.example.ptvtimetable/.MainActivity
```

---

## Still Having Issues?

1. **Check documentation:**
    - [WIDGET_GUIDE.md](WIDGET_GUIDE.md)
    - [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
    - [README.md](../README.md)

2. **Review logs carefully:**
   ```bash
   adb logcat *:E | grep ptv  # Errors only
   ```

3. **Clean and rebuild:**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

4. **Create an issue** on GitHub with:
    - Full error message
    - Android version
    - Device/emulator details
    - Steps to reproduce
    - Relevant logs

---

## Quick Fixes Summary

| Issue | Quick Fix |
|-------|-----------|
| Unresolved references | Sync Gradle |
| Widget not appearing | Reinstall app |
| Configuration not opening | Check manifest |
| No data loading | Check internet & logs |
| Build fails | Clean & rebuild |
| Release crash | Add ProGuard rules |
| WorkManager not running | Check battery settings |
| API errors | Check token & connectivity |

---

**Last Updated:** Based on Kotlin 2.0.21 and AGP 8.13.0
