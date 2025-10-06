# 🔥 Firestore Database Integration for Web Dashboard

## 📊 Database Structure

The Digital Winding Engine Driver Logbook uses Firebase Firestore as the primary database for real-time synchronization between the mobile app and web dashboard.

### 🗂️ Collections Structure

#### 1. **logbook_entries** - General logbook entries
```json
{
  "id": "auto-generated",
  "entryType": "operation|incident|maintenance|shift_start|shift_end",
  "description": "Entry description",
  "user": "Driver name",
  "shift": "Morning|Afternoon|Night",
  "timestamp": 1726589123456,
  "date": "2024-09-17",
  "personnelCount": 8,
  "materialWeight": 150.5
}
```

#### 2. **trip_counters** - Daily trip tallies
```json
{
  "id": "auto-generated",
  "entryType": "trip_counters",
  "date": "2024-09-17",
  "shift": "Morning|Afternoon|Night",
  "user": "Driver name",
  "counters": {
    "persons": 25,
    "material": 10,
    "mineral": 8,
    "explosives": 2
  },
  "timestamp": 1726589123456
}
```

#### 3. **component_status** - Equipment condition logs
```json
{
  "id": "auto-generated",
  "entryType": "component_status",
  "component": "Engine|Electrician|Rigger|Boilermaker|Fitter",
  "status": "good|attention|faulty",
  "user": "Driver name",
  "shift": "Morning|Afternoon|Night",
  "timestamp": 1726589123456,
  "date": "2024-09-17",
  "notes": "Optional maintenance notes"
}
```

#### 4. **biometric_signatures** - Digital signatures
```json
{
  "id": "auto-generated",
  "entryType": "biometric_signature",
  "action": "morning_shift_start|afternoon_shift_end",
  "user": "Driver name",
  "shift": "Morning|Afternoon|Night",
  "signature_hash": "abc123def456",
  "authenticated": true,
  "timestamp": 1726589123456,
  "date": "2024-09-17"
}
```

#### 5. **shifts** - Complete shift data
```json
{
  "id": "auto-generated",
  "entryType": "complete_shift_data",
  "shift": "Morning|Afternoon|Night",
  "user": "Driver name",
  "startTime": "06:00",
  "endTime": "14:00",
  "winderName": "Main Shaft Winder",
  "tripCounters": {
    "persons": 25,
    "material": 10,
    "mineral": 8,
    "explosives": 2
  },
  "timestamp": 1726589123456,
  "date": "2024-09-17"
}
```

#### 6. **emergency_logs** - Emergency incidents
```json
{
  "id": "auto-generated",
  "entryType": "emergency_log",
  "type": "emergency",
  "description": "Emergency description",
  "user": "Driver name",
  "shift": "Morning|Afternoon|Night",
  "priority": "high",
  "timestamp": 1726589123456,
  "date": "2024-09-17"
}
```

#### 7. **maintenance_records** - Maintenance activities
```json
{
  "id": "auto-generated",
  "type": "toBeDone|completed",
  "status": true,
  "component": "Motor|Brakes|Ropes",
  "user": "Technician name",
  "shift": "Morning|Afternoon|Night",
  "timestamp": 1726589123456,
  "date": "2024-09-17",
  "notes": "Maintenance details"
}
```

#### 8. **users** - User management
```json
{
  "employeeNumber": "WED001",
  "name": "Bays Draganovic",
  "role": "Winding Engine Driver",
  "shift": "Morning",
  "active": true,
  "lastLogin": 1726589123456
}
```

---

## 🌐 Web Dashboard Integration

### API Endpoints for Web Dashboard

The web dashboard can access Firestore data through these patterns:

#### 1. **Real-time Dashboard Stats**
```javascript
// Web dashboard JavaScript
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, query, where, orderBy, limit, onSnapshot } from 'firebase/firestore';

const firebaseConfig = {
  // Your Firebase config
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Get real-time dashboard stats
function getDashboardStats() {
  const today = new Date().toISOString().split('T')[0];
  
  // Listen to today's trip counters
  const tripCountersQuery = query(
    collection(db, 'trip_counters'),
    where('date', '==', today),
    orderBy('timestamp', 'desc'),
    limit(1)
  );
  
  onSnapshot(tripCountersQuery, (snapshot) => {
    snapshot.forEach((doc) => {
      const data = doc.data();
      updateDashboardStats(data.counters);
    });
  });
}
```

#### 2. **Component Status Monitoring**
```javascript
// Monitor component status in real-time
function monitorComponentStatus() {
  const componentQuery = query(
    collection(db, 'component_status'),
    orderBy('timestamp', 'desc'),
    limit(20)
  );
  
  onSnapshot(componentQuery, (snapshot) => {
    const components = [];
    snapshot.forEach((doc) => {
      components.push({ id: doc.id, ...doc.data() });
    });
    updateComponentStatusDisplay(components);
  });
}
```

#### 3. **Emergency Alerts**
```javascript
// Monitor emergency logs for alerts
function monitorEmergencyLogs() {
  const emergencyQuery = query(
    collection(db, 'emergency_logs'),
    where('priority', '==', 'high'),
    orderBy('timestamp', 'desc'),
    limit(10)
  );
  
  onSnapshot(emergencyQuery, (snapshot) => {
    snapshot.forEach((doc) => {
      const emergency = doc.data();
      if (isNewEmergency(emergency)) {
        showEmergencyAlert(emergency);
      }
    });
  });
}
```

### 📊 Dashboard Visualizations

#### Sample Dashboard Metrics:

1. **Operations Today**
   - Total trips (persons + material + mineral + explosives)
   - Personnel transported
   - Material/mineral loads
   - Explosives handled

2. **Equipment Health**
   - Components in good condition
   - Components needing attention
   - Faulty components requiring immediate action

3. **Shift Performance**
   - Operations per shift
   - Biometric signature compliance
   - Maintenance completed vs. pending

4. **Safety Metrics**
   - Emergency incidents
   - Response times
   - Compliance rates

---

## 🔧 Implementation Steps

### 1. Firebase Project Setup
1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable Firestore Database
3. Configure security rules for your domain
4. Add web app configuration

### 2. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to dashboard users
    match /logbook_entries/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.token.role == 'driver';
    }
    
    match /trip_counters/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.token.role == 'driver';
    }
    
    match /component_status/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    match /emergency_logs/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Admin access for users collection
    match /users/{document} {
      allow read, write: if request.auth != null && request.auth.token.role == 'admin';
    }
  }
}
```

### 3. Web Dashboard Authentication
```javascript
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

const auth = getAuth();

// Dashboard login
async function loginToDashboard(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    console.log('Dashboard user logged in:', user.uid);
    initializeDashboard();
  } catch (error) {
    console.error('Login error:', error);
  }
}
```

---

## 📱 Mobile App Configuration

The mobile app is already configured to sync with Firestore. Ensure your `google-services.json` file is properly configured:

### Key Configuration Files:
- `app/google-services.json` - Firebase configuration
- `app/build.gradle` - Firebase dependencies included
- `FirestoreService.kt` - Database service layer
- `WinderLogbookInterface.kt` - JavaScript bridge

---

## 🚀 Real-time Sync Features

### Automatic Data Sync:
✅ **Trip counters** sync immediately when updated  
✅ **Component status** changes sync in real-time  
✅ **Emergency logs** trigger immediate alerts  
✅ **Biometric signatures** provide audit trail  
✅ **Offline support** with automatic sync when online  

### Dashboard Benefits:
📊 **Real-time monitoring** of all winder operations  
📈 **Historical data analysis** and trending  
🚨 **Instant emergency notifications**  
📋 **Compliance tracking** and reporting  
👥 **Multi-shift coordination** and handovers  

---

## 📞 Support & Maintenance

For technical support or questions about the Firestore integration:

1. **Database Queries** - Use Firestore console for debugging
2. **Real-time Issues** - Check network connectivity and security rules
3. **Performance** - Monitor Firestore usage and optimize queries
4. **Security** - Regularly review and update security rules

The integration provides a robust, scalable foundation for mining operations management with full audit trails and real-time monitoring capabilities.
