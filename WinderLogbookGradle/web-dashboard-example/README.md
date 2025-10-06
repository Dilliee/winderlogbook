# 🌐 Winder Logbook Web Dashboard

## 📋 Overview

This web dashboard provides real-time monitoring and analytics for the Digital Winding Engine Driver Logbook mobile application. It connects directly to Firebase Firestore to display live operational data, equipment status, and safety metrics.

## 🚀 Features

### Real-time Monitoring
- ✅ **Live trip counters** - Personnel, material, mineral, explosives
- ✅ **Equipment status** - Real-time component health monitoring  
- ✅ **Emergency alerts** - Instant notifications for safety incidents
- ✅ **Shift tracking** - Current shift status and handovers
- ✅ **Connection status** - Online/offline indicator

### Dashboard Sections
1. **📊 Statistics Overview** - Daily operations summary
2. **🔧 Component Status** - Equipment health monitoring
3. **📈 Maintenance Overview** - Task completion tracking  
4. **🕐 Shift Information** - Current shift assignments

## 🔧 Setup Instructions

### 1. Firebase Configuration

Replace the Firebase configuration in `index.html` with your actual project details:

```javascript
const firebaseConfig = {
    apiKey: "your-actual-api-key",
    authDomain: "your-project.firebaseapp.com", 
    projectId: "your-project-id",
    storageBucket: "your-project.appspot.com",
    messagingSenderId: "123456789",
    appId: "your-app-id"
};
```

### 2. Firestore Security Rules

Ensure your Firestore has the correct security rules for web access:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to dashboard users
    match /{collection}/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.token.role in ['driver', 'supervisor', 'admin'];
    }
  }
}
```

### 3. Authentication Setup

For production use, implement proper authentication:

```javascript
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

const auth = getAuth();

// Add login functionality
function loginToDashboard(email, password) {
    return signInWithEmailAndPassword(auth, email, password);
}
```

### 4. Hosting Options

#### Option A: Firebase Hosting
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
firebase deploy
```

#### Option B: Static Web Server
```bash
# Using Python
python -m http.server 8000

# Using Node.js
npx http-server

# Using Live Server (VS Code extension)
# Right-click index.html → "Open with Live Server"
```

## 📊 Data Sources

The dashboard displays data from these Firestore collections:

| Collection | Purpose | Real-time Updates |
|------------|---------|-------------------|
| `trip_counters` | Daily operation tallies | ✅ Yes |
| `component_status` | Equipment condition | ✅ Yes |
| `emergency_logs` | Safety incidents | ✅ Yes |
| `shifts` | Shift management | ✅ Yes |
| `biometric_signatures` | Authentication logs | ✅ Yes |
| `maintenance_records` | Maintenance tracking | ✅ Yes |

## 🎨 Customization

### Color Themes
Modify the CSS variables to match your company branding:

```css
:root {
    --primary-color: #3498db;
    --secondary-color: #2c3e50; 
    --success-color: #4CAF50;
    --warning-color: #FF9800;
    --danger-color: #F44336;
}
```

### Adding New Metrics
1. Add new stat card in HTML
2. Create Firestore listener in JavaScript
3. Update display function

Example:
```javascript
// Add new metric listener
function initializeNewMetric() {
    const query = collection(db, 'your_collection');
    onSnapshot(query, (snapshot) => {
        // Process data and update display
        updateNewMetricDisplay(snapshot.docs);
    });
}
```

## 🔒 Security Considerations

### Production Checklist:
- [ ] Implement user authentication
- [ ] Configure proper Firestore security rules
- [ ] Use environment variables for sensitive config
- [ ] Enable HTTPS for web hosting
- [ ] Set up user roles and permissions
- [ ] Monitor access logs

### Sample Authentication Flow:
```javascript
// Check authentication status
onAuthStateChanged(auth, (user) => {
    if (user) {
        initializeDashboard();
    } else {
        redirectToLogin();
    }
});
```

## 📱 Mobile Responsiveness

The dashboard is fully responsive and works on:
- ✅ Desktop computers (1920x1080+)
- ✅ Tablets (iPad, Android tablets)
- ✅ Mobile phones (iOS, Android)
- ✅ Large displays (4K monitors)

## 🚨 Emergency Alerts

Emergency notifications appear automatically when:
- High-priority incidents are logged
- Equipment status changes to "Faulty"
- Emergency logs are created from mobile app

Alerts include:
- 🔊 Visual notification (red banner)
- 📝 Incident description and details
- 👤 Reporter name and shift
- ⏰ Auto-dismiss after 30 seconds

## 📊 Analytics & Reporting

### Built-in Metrics:
- Daily operations count
- Personnel transportation numbers
- Material/mineral trip tallies
- Equipment health percentages
- Incident rates and trends
- Shift completion rates

### Export Options:
Add these features for enhanced reporting:
- CSV data export
- PDF report generation
- Email alert notifications
- Historical trend analysis

## 🛠️ Troubleshooting

### Common Issues:

**Dashboard not updating?**
- Check Firebase configuration
- Verify Firestore security rules
- Ensure network connectivity

**No data showing?**
- Confirm mobile app is syncing to Firestore
- Check collection names match exactly
- Verify date formatting consistency

**Connection errors?**
- Check Firebase project status
- Verify API keys and configuration
- Ensure CORS settings allow your domain

### Debug Mode:
Enable browser developer tools console to see:
- Firebase connection status
- Real-time listener events
- Error messages and details

## 📞 Support

For technical support:
1. Check browser console for error messages
2. Verify Firebase project configuration
3. Test with sample data in Firestore console
4. Review security rules and permissions

The web dashboard provides a comprehensive view of your mining operations with real-time updates and professional presentation suitable for management oversight and operational monitoring.
