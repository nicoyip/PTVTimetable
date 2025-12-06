# Security Guidelines

## API Credentials

### Overview

This project uses the PTV Timetable API which requires authentication via a Developer ID and API
Key. These credentials **must not** be committed to version control.

### Setup

1. **Get Credentials**: Register
   at [PTV Timetable API](https://www.ptv.vic.gov.au/footer/data-and-reporting/datasets/ptv-timetable-api/)

2. **Configure Local Properties**: Create `local.properties` in the project root:

```properties
PTV_DEV_ID=your_dev_id
PTV_API_KEY=your_api_key
```

3. **Build Configuration**: The Secrets Gradle Plugin automatically:
    - Reads credentials from `local.properties`
    - Generates `BuildConfig` constants
    - Prevents accidental exposure

### How It Works

```
local.properties (git ignored)
    ↓
Secrets Gradle Plugin
    ↓
BuildConfig.PTV_DEV_ID
BuildConfig.PTV_API_KEY
    ↓
PTVApiClient.kt
```

### Files

| File | Purpose | Committed |
|------|---------|-----------|
| `local.properties` | Your actual credentials | ❌ No (in .gitignore) |
| `local.properties.example` | Template for developers | ✅ Yes |
| `local.defaults.properties` | Placeholder values for CI | ✅ Yes |
| `PTVApiClient.kt` | Uses BuildConfig | ✅ Yes (no secrets) |

### Best Practices

✅ **DO**:

- Keep `local.properties` in `.gitignore`
- Use `BuildConfig` to access credentials
- Provide example files for other developers
- Rotate credentials if accidentally exposed

❌ **DON'T**:

- Hardcode credentials in source files
- Commit `local.properties` to git
- Share credentials publicly
- Store credentials in screenshots or logs

### CI/CD

For continuous integration:

1. Use environment variables in CI system
2. Generate `local.properties` in build script:

```bash
echo "PTV_DEV_ID=$PTV_DEV_ID" >> local.properties
echo "PTV_API_KEY=$PTV_API_KEY" >> local.properties
```

### Credential Exposure Response

If credentials are accidentally exposed:

1. **Immediately** request new credentials from PTV
2. Update `local.properties` with new values
3. Review git history for exposure:
   ```bash
   git log -p -- local.properties
   ```
4. If exposed in git history, consider rewriting history or creating new repository

### Verification

Ensure credentials are not exposed:

```bash
# Should return empty
grep -r "3002528\|d754e449" --exclude-dir=.git --exclude=local.properties .

# Should show BuildConfig usage only
grep -r "PTV_DEV_ID\|PTV_API_KEY" app/src/
```

## Additional Security

### ProGuard/R8

The release build uses code obfuscation. The `proguard-rules.pro` includes:

```proguard
-keep class com.example.ptvtimetable.BuildConfig { *; }
```

This keeps BuildConfig accessible while obfuscating other code.

### HTTPS

All API calls use HTTPS (enforced by OkHttp and Android's Network Security Config).

### No Root Requirement

This app does not require root access and follows Android security best practices.

## Reporting Security Issues

If you discover a security vulnerability:

1. **Do not** open a public issue
2. Email the maintainers privately
3. Include detailed information about the vulnerability
4. Allow reasonable time for a fix before public disclosure

---

Last updated: December 2024
