# 📱 APK Distribution Guide - Winder Logbook

## Overview
This guide explains how to build, sign, and distribute the Winder Logbook APK for sideloading installation.

## 🔧 Build Commands

### 1. Build Release APK
```bash
# Clean and build release APK
.\gradlew clean assembleRelease

# APK will be created at:
# app\build\outputs\apk\release\app-release.apk
```

### 2. Build Debug APK (for testing)
```bash
# Build debug APK
.\gradlew assembleDebug

# APK will be created at:
# app\build\outputs\apk\debug\app-debug.apk
```

### 3. Install on Connected Device/Emulator
```bash
# Install release APK
adb install -r app\build\outputs\apk\release\app-release.apk

# Install debug APK
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 🔐 APK Signing Configuration

The app is already configured with a release signing key:
- **Keystore**: `app/winder-release-key.keystore`
- **Store Password**: `winderlogbook`
- **Key Alias**: `winder-key`
- **Key Password**: `winderlogbook`

**Note**: In production, use a secure keystore and passwords. The current setup is for development/testing.

## 📤 Distribution Process

### For Internal Distribution:

1. **Build the signed release APK**:
   ```bash
   .\gradlew assembleRelease
   ```

2. **Locate the APK**:
   ```
   WinderLogbookGradle\app\build\outputs\apk\release\app-release.apk
   ```

3. **Share via**:
   - Email attachment
   - Cloud storage (Google Drive, Dropbox, etc.)
   - USB transfer
   - Network file sharing
   - Company intranet

### For Recipients:

1. **Enable Unknown Sources**:
   - Go to Settings > Security > Unknown Sources
   - Or Settings > Apps > Special Access > Install Unknown Apps
   - Enable for the file manager or browser you're using

2. **Install the APK**:
   - Download/copy the APK file to device
   - Tap the APK file to install
   - Follow the installation prompts

## 🔒 Security Considerations

### App Permissions
The app requests the following permissions:
- **INTERNET**: For Firebase connectivity
- **ACCESS_NETWORK_STATE**: To check network status
- **USE_BIOMETRIC**: For fingerprint/face authentication
- **USE_FINGERPRINT**: Legacy fingerprint support
- **WRITE_EXTERNAL_STORAGE**: For local data backup (Android 10 and below)
- **READ_EXTERNAL_STORAGE**: For accessing local files (Android 12 and below)

### Data Security
- All data is encrypted in transit to Firebase
- Biometric authentication protects app access
- Local data is stored securely using Android's secure storage

### Firebase Security
- Firestore rules control data access
- Only authenticated users can read/write data
- Role-based access control implemented

## 📋 Installation Instructions for End Users

### Android Devices:

1. **Download the APK file** from your organization's distribution method
2. **Enable installation from unknown sources**:
   - Open Settings
   - Go to Security or Privacy
   - Find "Install unknown apps" or "Unknown sources"
   - Enable for your file manager/browser
3. **Install the app**:
   - Locate the downloaded APK file
   - Tap to install
   - Accept permissions when prompted
4. **First-time setup**:
   - Open the Winder Logbook app
   - Allow biometric authentication when prompted
   - The app will sync with the cloud database

### Troubleshooting:

**Installation Blocked**:
- Ensure "Unknown sources" is enabled
- Check available storage space
- Try redownloading the APK

**App Won't Open**:
- Restart the device
- Clear app cache: Settings > Apps > Winder Logbook > Storage > Clear Cache
- Reinstall the app

**No Network Connection**:
- Check Wi-Fi/mobile data
- The app works offline and syncs when connection is restored

## 🔄 Update Process

### For New Versions:
1. Build new APK with incremented version number
2. Distribute new APK file
3. Users install over existing app (data is preserved)
4. Firebase handles data migration automatically

### Version Management:
- Edit `app/build.gradle` to update `versionCode` and `versionName`
- Include release notes with distribution

## 🏢 Enterprise Distribution

### For Large Organizations:

1. **Mobile Device Management (MDM)**:
   - Upload APK to MDM system
   - Deploy to managed devices
   - Automatic updates possible

2. **Internal App Store**:
   - Set up private app repository
   - Control access by user groups
   - Track installation analytics

3. **QR Code Distribution**:
   - Generate QR code linking to APK download
   - Print QR codes for easy sharing
   - Useful for training sessions

### Sample Distribution Email Template:

```
Subject: Winder Logbook Mobile App Installation

Dear Team,

The new Digital Winding Engine Driver Logbook mobile app is ready for installation.

Installation Steps:
1. Download the attached APK file
2. Enable "Unknown Sources" in your Android settings
3. Tap the APK file to install
4. Open the app and follow setup instructions

System Requirements:
- Android 7.0 (API 24) or higher
- 50MB free storage space
- Internet connection for cloud sync

Support:
For technical issues, contact IT Support.

Best regards,
Mining Operations Team
```

## 🔧 Developer Commands

### Quick Build & Install:
```bash
# Clean, build, and install in one command
.\gradlew clean assembleRelease && adb install -r app\build\outputs\apk\release\app-release.apk
```

### Check Connected Devices:
```bash
adb devices
```

### View App Logs:
```bash
adb logcat | findstr "Winder"
```

### Uninstall App:
```bash
adb uninstall com.winderlogbook.gradle
```

---

**Last Updated**: September 2024  
**Project**: Digital Winding Engine Driver Logbook  
**Version**: 1.0.0
