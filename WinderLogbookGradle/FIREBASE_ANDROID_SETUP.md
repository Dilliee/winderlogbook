# 🔥 Firebase Android Setup Instructions

## Download google-services.json

You need to download the `google-services.json` file for your Android app:

### Steps:

1. **Go to Firebase Console**: [console.firebase.google.com](https://console.firebase.google.com)

2. **Select your "winderlogbook" project**

3. **Click the gear icon (⚙️)** → **"Project settings"**

4. **Scroll down to "Your apps" section**

5. **Add Android app** (if not already added):
   - Click the Android icon (🤖)
   - **Android package name**: `com.winderlogbook.gradle`
   - **App nickname**: "Winder Logbook Android"
   - Click "Register app"

6. **Download google-services.json**:
   - Click "Download google-services.json"
   - Save the file

7. **Replace the existing file**:
   - Copy the downloaded file
   - Replace: `WinderLogbookGradle/app/google-services.json`

### Expected google-services.json content structure:
```json
{
  "project_info": {
    "project_number": "43168247130",
    "project_id": "winderlogbook",
    "storage_bucket": "winderlogbook.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:43168247130:android:xxxxxxxxxxxxxxxx",
        "android_client_info": {
          "package_name": "com.winderlogbook.gradle"
        }
      },
      "oauth_client": [...],
      "api_key": [...],
      "services": {
        "appinvite_service": {...},
        "analytics_service": {...}
      }
    }
  ],
  "configuration_version": "1"
}
```

## After downloading:

1. **Replace the file**: `WinderLogbookGradle/app/google-services.json`
2. **Rebuild the app**: `npm run build`
3. **Test the connection**: Use the Firebase test page

## Verification:

The app should connect to Firebase automatically after replacing the file. You can verify this by:
1. Opening the Firebase test page: `web-dashboard-example/firebase-test.html`
2. Testing the Firebase connection
3. Checking that data appears in your Firebase console

---

**Important**: Make sure the package name in google-services.json matches exactly: `com.winderlogbook.gradle`
