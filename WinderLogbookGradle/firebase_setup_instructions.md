# Firebase Setup Instructions for Winder Logbook

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" 
3. Project name: `winder-logbook` (or your preferred name)
4. Enable Google Analytics (optional)
5. Create project

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click "Add app" → Android
2. Android package name: `com.winderlogbook.gradle` (must match exactly)
3. App nickname: `Winder Logbook`
4. SHA-1: (Optional for now, can add later for release)
5. Click "Register app"

## Step 3: Download Configuration File

1. Download the `google-services.json` file
2. Place it in `WinderLogbookGradle/app/` directory (same level as build.gradle)

## Step 4: Enable Firestore

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Start in test mode (for development)
4. Choose location closest to your users
5. Click "Done"

## Step 5: Configure Firestore Security Rules (Optional for Development)

Replace default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to all documents for development
    // In production, implement proper authentication rules
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

## Collections that will be created:

- `logbook_entries`: Daily operational entries
- `inspections`: Equipment and safety inspections  
- `maintenance_records`: Maintenance activities
- `users`: User profiles and authentication

## Test Connection

After placing the google-services.json file, run:
```bash
npm run android
```

The app will automatically connect to Firestore and start saving data to the cloud.

## Important Notes

- The current setup uses test mode (no authentication required)
- For production, implement Firebase Authentication
- Consider adding offline support with Firestore's built-in caching
- Monitor usage in Firebase Console for costs and performance
