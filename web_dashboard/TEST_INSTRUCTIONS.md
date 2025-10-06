# Web Dashboard Testing Instructions

## Fixed Issues
1. ✅ User creation now saves properly to localStorage
2. ✅ Edit button now works correctly
3. ✅ Form properly switches between create and edit mode
4. ✅ Tab switching fixed

## How to Test

### Test 1: Create New User
1. Open `index.html` in your web browser
2. Click "➕ Create User" tab
3. Fill in the form:
   - Employee ID: EMP003
   - Full Name: Test User
   - Email: test@example.com
   - Username: testuser
   - Password: test123
   - Job Description: Select from dropdown (e.g., "Winder Driver")
   - Department: Select from dropdown (e.g., "Operations")
4. Click "Capture Fingerprint" (wait 2 seconds)
5. Click "✅ Create User & Sync to App"
6. **Expected Result**: Alert appears confirming user creation
7. Navigate to "👥 User Management" tab
8. **Expected Result**: New user appears in the table

### Test 2: Edit Existing User
1. Go to "👥 User Management" tab
2. Find a user in the table
3. Click "✏️ Edit" button next to any user
4. **Expected Result**: 
   - Switches to "Create User" tab
   - Form is populated with user's data
5. Change the Full Name to "Updated Name"
6. Click "✅ Create User & Sync to App"
7. **Expected Result**: Alert appears confirming update
8. Navigate back to "User Management" tab
9. **Expected Result**: User's name is updated in the table

### Test 3: Delete User
1. Go to "👥 User Management" tab
2. Click "🗑️ Delete" button next to any user
3. Confirm the deletion
4. **Expected Result**: 
   - Alert confirms deletion
   - User removed from table
   - Statistics updated

### Test 4: Fingerprint Capture
1. Go to "Create User" tab
2. Fill in basic info (at minimum: Employee ID, Name, Email, Username, Password, Job Description, Department)
3. Click "Capture Fingerprint"
4. **Expected Result**: 
   - Shows "⏳ Capturing fingerprint..."
   - After 2 seconds: "✅ Fingerprint captured successfully!"
   - Displays Fingerprint ID

### Test 5: Search Users
1. Go to "User Management" tab
2. Type in the search box (e.g., "John")
3. **Expected Result**: Table filters to show only matching users

### Test 6: Sync to App
1. Go to "🔄 Sync Status" tab
2. Click "🔄 Sync All Users to Devices"
3. **Expected Result**:
   - JSON file downloads (`winder_logbook_users.json`)
   - Alert confirms sync
   - Last sync time updates

### Test 7: Sync Code Generation
1. Go to "Sync Status" tab
2. Click "🔑 Generate Sync Code"
3. **Expected Result**:
   - 6-character code appears
   - Can copy code to clipboard
   - Alert confirms generation

## Browser Console Testing

Open browser console (F12) to see debug messages:
- "User created:" when creating new user
- "User updated:" when editing user
- "Editing user:" when clicking edit button
- "Form populated for editing" when edit form loads

## localStorage Verification

In browser console, check stored data:
```javascript
// View all users
JSON.parse(localStorage.getItem('winderLogbookUsers'))

// View last sync time
localStorage.getItem('lastSyncTime')

// Clear all data (if needed)
localStorage.clear()
```

## Common Issues & Solutions

### Issue: User not appearing after creation
**Solution**: Check browser console for errors, ensure all required fields are filled

### Issue: Edit button does nothing
**Solution**: Refresh the page, check console for JavaScript errors

### Issue: Form doesn't clear after submission
**Solution**: Click "❌ Clear Form" button manually, or refresh page

### Issue: Statistics not updating
**Solution**: Refresh the page - statistics update on page load and after save

## Demo Users

The dashboard includes 2 demo users by default:
- **EMP001** - John Doe (Winder Driver)
- **EMP002** - Jane Smith (Electrician)

You can edit or delete these, or create new ones.

## Integration with Mobile App

After creating users:
1. Go to "Sync Status" tab
2. Download user data (JSON file or sync code)
3. Open mobile app
4. Use credentials to log in:
   - Username: testuser
   - Password: test123
5. Job description should auto-fill in forms!

