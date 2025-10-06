// Web Dashboard JavaScript for Winder Logbook User Management

// Sample users database (in production, this would be a real database)
let users = [];

// Switch between tabs
function switchTab(tabName) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    // Remove active class from all tabs
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Show selected tab content
    const tabContent = document.getElementById(`${tabName}-tab`);
    if (tabContent) {
        tabContent.classList.add('active');
    }
    
    // Add active class to selected tab button
    const clickedTab = Array.from(document.querySelectorAll('.tab')).find(tab => 
        tab.textContent.toLowerCase().includes(tabName.replace('-', ' '))
    );
    if (clickedTab) {
        clickedTab.classList.add('active');
    }
}

// Load users from localStorage
function loadUsersFromStorage() {
    const storedUsers = localStorage.getItem('winderLogbookUsers');
    if (storedUsers) {
        users = JSON.parse(storedUsers);
    } else {
        // Create some demo users
        users = [
            {
                userId: generateUserId(),
                employeeId: 'EMP001',
                name: 'John Doe',
                username: 'jdoe',
                password: 'password123',
                email: 'john.doe@lucara.com',
                phone: '+267 123 4567',
                jobDescription: 'Winder Driver',
                department: 'Operations',
                shift: 'Day',
                accessLevel: 'user',
                fingerprintData: 'demo_fingerprint_data_1',
                biometricEnrolled: true,
                status: 'active',
                createdAt: Date.now()
            },
            {
                userId: generateUserId(),
                employeeId: 'EMP002',
                name: 'Jane Smith',
                username: 'jsmith',
                password: 'password123',
                email: 'jane.smith@lucara.com',
                phone: '+267 234 5678',
                jobDescription: 'Electrician',
                department: 'Electrical',
                shift: 'Night',
                accessLevel: 'user',
                fingerprintData: 'demo_fingerprint_data_2',
                biometricEnrolled: true,
                status: 'active',
                createdAt: Date.now()
            }
        ];
        saveUsersToStorage();
    }
}

// Save users to localStorage
function saveUsersToStorage() {
    localStorage.setItem('winderLogbookUsers', JSON.stringify(users));
    updateStats();
    renderUsersTable();
}

// Generate unique user ID
function generateUserId() {
    return 'U' + Date.now() + Math.random().toString(36).substr(2, 9);
}

// Update statistics
function updateStats() {
    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status === 'active').length;
    const biometricUsers = users.filter(u => u.biometricEnrolled).length;
    const pendingUsers = users.filter(u => !u.biometricEnrolled).length;
    
    document.getElementById('totalUsers').textContent = totalUsers;
    document.getElementById('activeUsers').textContent = activeUsers;
    document.getElementById('biometricUsers').textContent = biometricUsers;
    document.getElementById('pendingUsers').textContent = pendingUsers;
}

// Render users table
function renderUsersTable() {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';
    
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.employeeId}</td>
            <td>${user.name}</td>
            <td>${user.jobDescription}</td>
            <td>${user.department}</td>
            <td>
                <span class="badge ${user.biometricEnrolled ? 'badge-success' : 'badge-warning'}">
                    ${user.biometricEnrolled ? '✅ Enrolled' : '⏳ Pending'}
                </span>
            </td>
            <td>
                <button class="btn btn-primary" style="padding: 5px 10px; font-size: 12px; margin-right: 5px;" 
                        onclick="editUser('${user.userId}')">✏️ Edit</button>
                <button class="btn btn-danger" style="padding: 5px 10px; font-size: 12px;" 
                        onclick="deleteUser('${user.userId}')">🗑️ Delete</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Filter users
function filterUsers() {
    const searchTerm = document.getElementById('searchUsers').value.toLowerCase();
    const tbody = document.getElementById('usersTableBody');
    const rows = tbody.getElementsByTagName('tr');
    
    Array.from(rows).forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

// Track if we're editing a user
let editingUserId = null;

// Create user form submission
function initializeFormHandler() {
    const form = document.getElementById('createUserForm');
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        console.log('Form submitted, editing user:', editingUserId);
        
        const userData = {
            employeeId: document.getElementById('employeeId').value,
            name: document.getElementById('fullName').value,
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            jobDescription: document.getElementById('jobDescription').value,
            department: document.getElementById('department').value,
            shift: document.getElementById('shift').value,
            accessLevel: document.getElementById('accessLevel').value,
            fingerprintData: document.getElementById('fingerprintData').value,
            biometricEnrolled: !!document.getElementById('fingerprintData').value,
            status: 'active'
        };
        
        if (editingUserId) {
            // Update existing user
            const userIndex = users.findIndex(u => u.userId === editingUserId);
            if (userIndex !== -1) {
                users[userIndex] = {
                    ...users[userIndex],
                    ...userData,
                    updatedAt: Date.now()
                };
                alert(`✅ User ${userData.name} updated successfully!`);
                console.log('User updated:', users[userIndex]);
            }
            editingUserId = null;
        } else {
            // Create new user
            const newUser = {
                ...userData,
                userId: generateUserId(),
                createdAt: Date.now()
            };
            users.push(newUser);
            alert(`✅ User ${newUser.name} created successfully!\n\nUser has been added to the system and is ready to sync to the app.`);
            console.log('User created:', newUser);
        }
        
        saveUsersToStorage();
        resetForm();
        switchTab('users');
    });
}

// Initialize form handler after DOM is loaded
window.addEventListener('DOMContentLoaded', () => {
    loadUsersFromStorage();
    updateStats();
    renderUsersTable();
    updateSyncInfo();
    initializeFormHandler();
});

// Capture fingerprint (simulated)
function captureFingerprint() {
    const statusDiv = document.getElementById('fingerprintStatus');
    statusDiv.innerHTML = '<p style="color: #ffc107;">⏳ Capturing fingerprint...</p>';
    
    // Simulate fingerprint capture
    setTimeout(() => {
        const fingerprintData = 'FP_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        document.getElementById('fingerprintData').value = fingerprintData;
        
        statusDiv.innerHTML = `
            <p style="color: #28a745; font-weight: bold;">✅ Fingerprint captured successfully!</p>
            <p style="font-size: 12px; margin-top: 10px;">Fingerprint ID: ${fingerprintData}</p>
        `;
    }, 2000);
}

// Reset form
function resetForm() {
    document.getElementById('createUserForm').reset();
    document.getElementById('fingerprintData').value = '';
    document.getElementById('fingerprintStatus').innerHTML = '';
    editingUserId = null; // Clear editing flag
    console.log('Form reset');
}

// Edit user
function editUser(userId) {
    console.log('Editing user:', userId);
    const user = users.find(u => u.userId === userId);
    if (!user) {
        console.error('User not found:', userId);
        return;
    }
    
    console.log('User found:', user);
    
    // Set editing flag
    editingUserId = userId;
    
    // Switch to create tab and populate form
    switchTab('create');
    
    // Wait for tab to switch, then populate form
    setTimeout(() => {
        document.getElementById('employeeId').value = user.employeeId || '';
        document.getElementById('fullName').value = user.name || '';
        document.getElementById('username').value = user.username || '';
        document.getElementById('password').value = user.password || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phone').value = user.phone || '';
        document.getElementById('jobDescription').value = user.jobDescription || '';
        document.getElementById('department').value = user.department || '';
        document.getElementById('shift').value = user.shift || 'Day';
        document.getElementById('accessLevel').value = user.accessLevel || 'user';
        document.getElementById('fingerprintData').value = user.fingerprintData || '';
        
        if (user.biometricEnrolled && user.fingerprintData) {
            document.getElementById('fingerprintStatus').innerHTML = `
                <p style="color: #28a745; font-weight: bold;">✅ Fingerprint enrolled</p>
                <p style="font-size: 12px; margin-top: 10px;">Fingerprint ID: ${user.fingerprintData}</p>
            `;
        }
        
        console.log('Form populated for editing');
    }, 100);
}

// Delete user
function deleteUser(userId) {
    const user = users.find(u => u.userId === userId);
    if (!user) return;
    
    if (confirm(`Are you sure you want to delete ${user.name}?`)) {
        users = users.filter(u => u.userId !== userId);
        saveUsersToStorage();
        alert(`✅ User ${user.name} deleted successfully`);
    }
}

// Update sync info
function updateSyncInfo() {
    const lastSync = localStorage.getItem('lastSyncTime');
    if (lastSync) {
        document.getElementById('lastSyncTime').textContent = new Date(parseInt(lastSync)).toLocaleString();
    } else {
        document.getElementById('lastSyncTime').textContent = 'Never';
    }
    
    document.getElementById('dbUserCount').textContent = users.length;
    document.getElementById('deviceCount').textContent = '1'; // Simulated
}

// Sync to devices
function syncToDevices() {
    const syncData = users.map(u => ({
        userId: u.userId,
        employeeId: u.employeeId,
        name: u.name,
        username: u.username,
        password: u.password,
        email: u.email,
        jobDescription: u.jobDescription,
        department: u.department,
        shift: u.shift,
        accessLevel: u.accessLevel,
        fingerprintData: u.fingerprintData,
        biometricEnrolled: u.biometricEnrolled
    }));
    
    // Save sync data
    localStorage.setItem('syncData', JSON.stringify(syncData));
    localStorage.setItem('lastSyncTime', Date.now().toString());
    
    // Generate download file
    const dataStr = JSON.stringify(syncData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'winder_logbook_users.json';
    link.click();
    
    updateSyncInfo();
    alert(`✅ ${syncData.length} users synced successfully!\n\nA JSON file has been downloaded. Upload this file to the Android app to sync users.`);
}

// Generate sync code
function generateSyncCode() {
    const code = Math.random().toString(36).substr(2, 6).toUpperCase();
    localStorage.setItem('syncCode', code);
    localStorage.setItem('syncCodeData', JSON.stringify(users));
    
    document.getElementById('syncCode').value = code;
    document.getElementById('syncCodeSection').style.display = 'block';
    
    alert(`Sync code generated: ${code}\n\nEnter this code in the Android app to sync users.`);
}

// Copy sync code
function copySyncCode() {
    const codeInput = document.getElementById('syncCode');
    codeInput.select();
    document.execCommand('copy');
    alert('✅ Sync code copied to clipboard!');
}

