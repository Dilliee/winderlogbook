// Digital Winding Engine Driver Logbook JavaScript

let currentUser = null;
let currentShift = 'Morning';
let tripCounters = {
    persons: 0,
    material: 0,
    mineral: 0,
    explosives: 0
};

// ==================== AUTHENTICATION (SIMPLIFIED) ====================

let isAuthenticated = true; // Always authenticated - no biometric required

function initializeBiometricAuth() {
    // BIOMETRIC AUTHENTICATION REMOVED - Always authenticated
    console.log('✅ Authentication system disabled - always authenticated');
    updateAuthenticationUI();
}

// BIOMETRIC FUNCTIONS REMOVED - No longer needed

// BIOMETRIC AVAILABILITY CHECK REMOVED

// ALL BIOMETRIC FUNCTIONS REMOVED - Authentication disabled

function updateAuthenticationUI() {
    // Update session status - Always authenticated
    const sessionStatusEl = document.getElementById('sessionStatus');
    if (sessionStatusEl) {
        sessionStatusEl.textContent = 'Authenticated ✅';
        sessionStatusEl.style.color = '#4CAF50';
    }
    
    // Get current user and shift from native
    if (typeof WinderLogbook !== 'undefined') {
        const user = WinderLogbook.getCurrentUser();
        const shift = WinderLogbook.getCurrentShift();
        
        const userEl = document.getElementById('currentUserDisplay');
        const shiftEl = document.getElementById('currentShiftDisplay');
        
        if (userEl) userEl.textContent = user;
        if (shiftEl) shiftEl.textContent = shift;
        
        currentUser = user;
        currentShift = shift;
    }
}

// Toast notification function with fallback
function showToast(message) {
    try {
        if (typeof WinderLogbook !== 'undefined' && WinderLogbook.showToast) {
            WinderLogbook.showToast(message);
        } else {
            // Fallback for web mode or when interface is not available
            console.log('📱 Toast:', message);
            
            // Create a simple toast notification for web mode
            const toast = document.createElement('div');
            toast.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: #333;
                color: white;
                padding: 12px 20px;
                border-radius: 8px;
                z-index: 10000;
                font-size: 14px;
                max-width: 300px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                animation: slideIn 0.3s ease-out;
            `;
            toast.textContent = message;
            
            // Add animation styles if not already present
            if (!document.getElementById('toastStyles')) {
                const style = document.createElement('style');
                style.id = 'toastStyles';
                style.textContent = `
                    @keyframes slideIn {
                        from { transform: translateX(100%); opacity: 0; }
                        to { transform: translateX(0); opacity: 1; }
                    }
                `;
                document.head.appendChild(style);
            }
            
            document.body.appendChild(toast);
            
            // Remove toast after 3 seconds
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 3000);
        }
    } catch (error) {
        console.error('❌ Error showing toast:', error);
        console.log('📱 Toast (fallback):', message);
    }
}

// Callback function called from native Android code when user logs out
function onLogout() {
    console.log('🔓 User logged out');
    // No logout required - always authenticated
    showToast('🔓 Logged out successfully');
}

function logout() {
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.logout();
    } else {
        onLogout();
    }
}

// ==================== PAGE NAVIGATION ====================

let currentPage = 'shift-duties';

function showPage(pageId) {
    console.log(`🔄 Switching to page: ${pageId}`);
    
    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.classList.remove('active'));
    
    // Show selected page
    const targetPage = document.getElementById(pageId + '-page');
    if (targetPage) {
        targetPage.classList.add('active');
    }
    
    // Update navigation buttons
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.classList.remove('active');
        if (btn.getAttribute('data-page') === pageId) {
            btn.classList.add('active');
        }
    });
    
    currentPage = pageId;
    
    // Load page-specific content if needed
    switch(pageId) {
        case 'winding-control':
            loadWindingControlPage();
            break;
        case 'logbook-duties':
            loadLogbookDutiesPage();
            break;
        case 'examinations':
            loadExaminationsPage();
            break;
        case 'reports':
            loadReportsPage();
            break;
        case 'shift-duties':
            updateShiftDutiesTitle();
            break;
    }
}

function selectShift(shift) {
    console.log(`🔄 Switching to shift: ${shift}`);
    
    currentShift = shift;
    
    // Update shift buttons
    const shiftButtons = document.querySelectorAll('.shift-btn');
    shiftButtons.forEach(btn => {
        btn.classList.remove('active');
        if (btn.getAttribute('data-shift') === shift) {
            btn.classList.add('active');
        }
    });
    
    // Update shift title
    updateShiftDutiesTitle();
    
    // Update header information based on shift
    updateShiftTimes(shift);
    
    showToast(`📋 Switched to ${shift} Shift`);
}

function updateShiftDutiesTitle() {
    const titleElement = document.getElementById('currentShiftTitle');
    if (titleElement) {
        titleElement.textContent = `${currentShift} Shift:`;
    }
}

function updateShiftTimes(shift) {
    const startTimeInput = document.getElementById('shiftStartTime');
    const endTimeInput = document.getElementById('shiftEndTime');
    
    if (startTimeInput && endTimeInput) {
        switch(shift) {
            case 'Morning':
                startTimeInput.value = '06:00';
                endTimeInput.value = '14:00';
                break;
            case 'Afternoon':
                startTimeInput.value = '14:00';
                endTimeInput.value = '22:00';
                break;
            case 'Night':
                startTimeInput.value = '22:00';
                endTimeInput.value = '06:00';
                break;
        }
    }
}

function loadWindingControlPage() {
    const equipmentChecklistDiv = document.querySelector('#winding-control-page .equipment-checklist');
    if (equipmentChecklistDiv && equipmentChecklistDiv.innerHTML.includes('Equipment checklist will be loaded here')) {
        equipmentChecklistDiv.innerHTML = `
            <div class="equipment-sections">
                <h3>🛡️ Personal Protective Equipment</h3>
                <div class="equipment-grid">
                    <div class="equipment-item">
                        <input type="checkbox" id="ppe-fit">
                        <label for="ppe-fit">Fit for Purpose</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                    <div class="equipment-item">
                        <input type="checkbox" id="ppe-standard">
                        <label for="ppe-standard">To company standard</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                    <div class="equipment-item">
                        <input type="checkbox" id="ppe-loose">
                        <label for="ppe-loose">No loose clothing</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                </div>
                
                <h3>💡 Illumination</h3>
                <div class="equipment-grid">
                    <div class="equipment-item">
                        <input type="checkbox" id="illum-lights">
                        <label for="illum-lights">All lights are On and in working order</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                    <div class="equipment-item">
                        <input type="checkbox" id="illum-moving">
                        <label for="illum-moving">All moving components are illuminated</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                </div>
                
                <h3>🦶 Slippery Conditions</h3>
                <div class="equipment-grid">
                    <div class="equipment-item">
                        <input type="checkbox" id="slip-conditions">
                        <label for="slip-conditions">No slippery conditions</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                </div>
                
                <h3>🛡️ Guards</h3>
                <div class="equipment-grid">
                    <div class="equipment-item">
                        <input type="checkbox" id="guards-place">
                        <label for="guards-place">All guards are in place</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                    <div class="equipment-item">
                        <input type="checkbox" id="guards-limbs">
                        <label for="guards-limbs">Can't put limbs through guards</label>
                        <select class="status-select">
                            <option value="yes">Yes</option>
                            <option value="no">No</option>
                        </select>
                    </div>
                </div>
            </div>
            
            <style>
                .equipment-sections {
                    display: grid;
                    gap: 20px;
                }
                
                .equipment-sections h3 {
                    color: #333;
                    margin-bottom: 10px;
                    padding: 10px;
                    background: #f8f9fa;
                    border-radius: 6px;
                    border-left: 4px solid #4682B4;
                }
                
                .equipment-grid {
                    display: grid;
                    gap: 10px;
                    margin-bottom: 20px;
                }
                
                .equipment-item {
                    display: grid;
                    grid-template-columns: auto 1fr auto;
                    gap: 10px;
                    align-items: center;
                    padding: 10px;
                    background: #fafafa;
                    border-radius: 6px;
                    border: 1px solid #e0e0e0;
                }
                
                .equipment-item input[type="checkbox"] {
                    width: 18px;
                    height: 18px;
                }
                
                .equipment-item label {
                    font-size: 14px;
                    color: #333;
                }
                
                .equipment-item .status-select {
                    padding: 4px 8px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    background: white;
                    min-width: 80px;
                }
            </style>
        `;
    }
}

function loadLogbookDutiesPage() {
    const logbookSectionsDiv = document.querySelector('#logbook-duties-page .logbook-sections');
    if (logbookSectionsDiv && logbookSectionsDiv.innerHTML.includes('Logbook sections will be loaded here')) {
        logbookSectionsDiv.innerHTML = `
            <div class="logbook-duties-content">
                <div class="appointments-section">
                    <h3>📋 Appointments</h3>
                    <div class="appointment-types">
                        <div class="appointment-category">
                            <h4>Engineers</h4>
                            <div class="document-grid">
                                <div class="document-item">
                                    <label>ID Document:</label>
                                    <input type="text" placeholder="Enter ID document number">
                                </div>
                                <div class="document-item">
                                    <label>Letter of appointment:</label>
                                    <input type="text" placeholder="Enter appointment reference">
                                </div>
                            </div>
                        </div>
                        
                        <div class="appointment-category">
                            <h4>Winding Engine Drivers</h4>
                            <div class="document-grid">
                                <div class="document-item">
                                    <label>ID Document:</label>
                                    <input type="text" placeholder="Enter ID document number">
                                </div>
                                <div class="document-item">
                                    <label>Letter of appointment:</label>
                                    <input type="text" placeholder="Enter appointment reference">
                                </div>
                                <div class="document-item">
                                    <label>Winding Engine Drivers Certificate:</label>
                                    <input type="text" placeholder="Enter certificate number">
                                </div>
                            </div>
                        </div>
                        
                        <div class="appointment-category">
                            <h4>Electricians</h4>
                            <div class="document-grid">
                                <div class="document-item">
                                    <label>ID Document:</label>
                                    <input type="text" placeholder="Enter ID document number">
                                </div>
                                <div class="document-item">
                                    <label>Letter of appointment:</label>
                                    <input type="text" placeholder="Enter appointment reference">
                                </div>
                            </div>
                        </div>
                        
                        <div class="appointment-category">
                            <h4>Other Personnel</h4>
                            <div class="personnel-grid">
                                <div class="personnel-type">Fitters</div>
                                <div class="personnel-type">Boilermakers</div>
                                <div class="personnel-type">Riggers</div>
                                <div class="personnel-type">Winder Technicians</div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="logbook-types-section">
                    <h3>📖 Type of Logbook</h3>
                    <div class="logbook-type-grid">
                        <div class="logbook-type">
                            <input type="radio" id="winder-logbook" name="logbook-type" value="winder">
                            <label for="winder-logbook">Winding Engine Drivers Logbook</label>
                        </div>
                        <div class="logbook-type">
                            <input type="radio" id="machinery-logbook" name="logbook-type" value="machinery">
                            <label for="machinery-logbook">Machinery Logbook</label>
                        </div>
                        <div class="logbook-type">
                            <input type="radio" id="rope-logbook" name="logbook-type" value="rope">
                            <label for="rope-logbook">Rope Logbook</label>
                        </div>
                    </div>
                </div>
            </div>
            
            <style>
                .logbook-duties-content {
                    display: grid;
                    gap: 30px;
                }
                
                .appointments-section h3,
                .logbook-types-section h3 {
                    color: #333;
                    margin-bottom: 15px;
                    padding: 10px;
                    background: #f8f9fa;
                    border-radius: 6px;
                    border-left: 4px solid #4682B4;
                }
                
                .appointment-category {
                    margin-bottom: 20px;
                    padding: 15px;
                    border: 1px solid #e0e0e0;
                    border-radius: 8px;
                    background: #fafafa;
                }
                
                .appointment-category h4 {
                    color: #4682B4;
                    margin-bottom: 10px;
                    font-size: 1.1rem;
                }
                
                .document-grid {
                    display: grid;
                    gap: 10px;
                }
                
                .document-item {
                    display: grid;
                    grid-template-columns: 200px 1fr;
                    gap: 10px;
                    align-items: center;
                }
                
                .document-item label {
                    font-weight: 500;
                    color: #333;
                }
                
                .document-item input {
                    padding: 8px 12px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    font-size: 14px;
                }
                
                .personnel-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
                    gap: 10px;
                }
                
                .personnel-type {
                    padding: 10px;
                    background: white;
                    border: 1px solid #ddd;
                    border-radius: 6px;
                    text-align: center;
                    font-weight: 500;
                    color: #333;
                }
                
                .logbook-type-grid {
                    display: grid;
                    gap: 10px;
                }
                
                .logbook-type {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 10px;
                    border: 1px solid #e0e0e0;
                    border-radius: 6px;
                    background: #fafafa;
                }
                
                .logbook-type input[type="radio"] {
                    width: 18px;
                    height: 18px;
                }
                
                .logbook-type label {
                    font-weight: 500;
                    color: #333;
                    cursor: pointer;
                }
                
                @media (max-width: 768px) {
                    .document-item {
                        grid-template-columns: 1fr;
                        gap: 5px;
                    }
                    
                    .personnel-grid {
                        grid-template-columns: 1fr;
                    }
                }
            </style>
        `;
    }
}

function loadExaminationsPage() {
    const examinationsContentDiv = document.querySelector('#examinations-page .examinations-content');
    if (examinationsContentDiv && examinationsContentDiv.innerHTML.includes('Examination schedule will be loaded here')) {
        examinationsContentDiv.innerHTML = `
            <div class="examination-schedule">
                <h3>📅 Examination Schedule & Status</h3>
                
                <div class="schedule-summary">
                    <div class="summary-card overdue">
                        <div class="summary-count">2</div>
                        <div class="summary-label">Overdue</div>
                    </div>
                    <div class="summary-card due-soon">
                        <div class="summary-count">5</div>
                        <div class="summary-label">Due Soon</div>
                    </div>
                    <div class="summary-card completed">
                        <div class="summary-count">8</div>
                        <div class="summary-label">Completed</div>
                    </div>
                </div>

                <div class="examination-categories">
                    <div class="exam-category">
                        <h4 class="daily-header">🌅 Daily Examinations (Reg. 16.74.1)</h4>
                        <p class="regulation-note">Required every day when winding plant makes 50+ trips</p>
                        <div class="exam-items">
                            <div class="exam-row">
                                <input type="checkbox" id="daily-ropes">
                                <label for="daily-ropes">Winding ropes, balance ropes, connections</label>
                                <span class="status-badge pending">Due Today</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="daily-conveyance">
                                <label for="daily-conveyance">Conveyance and suspension members</label>
                                <span class="status-badge pending">Due Today</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="daily-safety">
                                <label for="daily-safety">Safety catches, brakes, depth indicators</label>
                                <span class="status-badge pending">Due Today</span>
                            </div>
                        </div>
                    </div>

                    <div class="exam-category">
                        <h4 class="weekly-header">📅 Weekly Examinations (≤10 days)</h4>
                        <div class="exam-items">
                            <div class="exam-row">
                                <input type="checkbox" id="weekly-shaft">
                                <label for="weekly-shaft">Guides, shaft compartments, doors (Reg. 16.73)</label>
                                <span class="status-badge due-soon">Due in 3 days</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="weekly-signals">
                                <label for="weekly-signals">Signaling arrangements (Reg. 16.74.2)</label>
                                <span class="status-badge due-soon">Due in 3 days</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="weekly-prevention">
                                <label for="weekly-prevention">Overspeed/overwind devices (Reg. 16.75.1)</label>
                                <span class="status-badge completed">Completed</span>
                            </div>
                        </div>
                    </div>

                    <div class="exam-category">
                        <h4 class="monthly-header">🗓️ Monthly Examinations (≤45 days)</h4>
                        <div class="exam-items">
                            <div class="exam-row">
                                <input type="checkbox" id="monthly-rope">
                                <label for="monthly-rope">Rope structure examination (Reg. 16.75.3)</label>
                                <span class="status-badge overdue">Overdue 2 days</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="monthly-connections">
                                <label for="monthly-connections">Rope-drum connections (Reg. 16.75.4)</label>
                                <span class="status-badge due-soon">Due in 5 days</span>
                            </div>
                        </div>
                    </div>

                    <div class="exam-category">
                        <h4 class="annual-header">📅 Annual/Semi-Annual Examinations</h4>
                        <div class="exam-items">
                            <div class="exam-row">
                                <input type="checkbox" id="annual-internal">
                                <label for="annual-internal">Internal mechanical/electrical parts (Reg. 16.75.2)</label>
                                <span class="status-badge scheduled">Next: Dec 2025</span>
                            </div>
                            <div class="exam-row">
                                <input type="checkbox" id="semi-dynamic">
                                <label for="semi-dynamic">Dynamic testing prevention devices (Reg. 16.75.6)</label>
                                <span class="status-badge scheduled">Next: Mar 2025</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="examination-actions">
                    <button class="btn btn-success" onclick="markExaminationComplete()">✅ Mark Examination Complete</button>
                    <button class="btn btn-warning" onclick="reportDefect()">⚠️ Report Defect</button>
                    <button class="btn" onclick="viewRecordBooks()">📚 View Record Books</button>
                </div>
            </div>

            <style>
                .examination-schedule {
                    max-width: 100%;
                }
                
                .schedule-summary {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
                    gap: 15px;
                    margin-bottom: 25px;
                }
                
                .summary-card {
                    padding: 15px;
                    border-radius: 8px;
                    text-align: center;
                    color: white;
                    font-weight: bold;
                }
                
                .summary-card.overdue { background: linear-gradient(135deg, #F44336, #d32f2f); }
                .summary-card.due-soon { background: linear-gradient(135deg, #FF9800, #f57c00); }
                .summary-card.completed { background: linear-gradient(135deg, #4CAF50, #45a049); }
                
                .summary-count {
                    font-size: 1.5rem;
                    margin-bottom: 5px;
                }
                
                .summary-label {
                    font-size: 0.9rem;
                }
                
                .examination-categories {
                    display: grid;
                    gap: 20px;
                }
                
                .exam-category {
                    border: 1px solid #e0e0e0;
                    border-radius: 8px;
                    padding: 15px;
                    background: #fafafa;
                }
                
                .exam-category h4 {
                    margin-bottom: 10px;
                    padding: 8px 12px;
                    border-radius: 6px;
                    color: white;
                }
                
                .daily-header { background: #4CAF50; }
                .weekly-header { background: #2196F3; }
                .monthly-header { background: #FF9800; }
                .annual-header { background: #9C27B0; }
                
                .regulation-note {
                    font-size: 0.85rem;
                    color: #666;
                    font-style: italic;
                    margin-bottom: 10px;
                }
                
                .exam-items {
                    display: grid;
                    gap: 8px;
                }
                
                .exam-row {
                    display: grid;
                    grid-template-columns: auto 1fr auto;
                    gap: 10px;
                    align-items: center;
                    padding: 8px;
                    background: white;
                    border-radius: 4px;
                    border: 1px solid #ddd;
                }
                
                .exam-row input[type="checkbox"] {
                    width: 16px;
                    height: 16px;
                }
                
                .exam-row label {
                    font-size: 14px;
                    color: #333;
                }
                
                .status-badge {
                    padding: 4px 8px;
                    border-radius: 4px;
                    font-size: 11px;
                    font-weight: bold;
                    text-transform: uppercase;
                }
                
                .status-badge.pending { background: #FFC107; color: #000; }
                .status-badge.due-soon { background: #FF9800; color: white; }
                .status-badge.overdue { background: #F44336; color: white; }
                .status-badge.completed { background: #4CAF50; color: white; }
                .status-badge.scheduled { background: #9C27B0; color: white; }
                
                .examination-actions {
                    margin-top: 25px;
                    display: flex;
                    gap: 10px;
                    justify-content: center;
                    flex-wrap: wrap;
                }
                
                @media (max-width: 768px) {
                    .exam-row {
                        grid-template-columns: 1fr;
                        gap: 5px;
                        text-align: left;
                    }
                    
                    .status-badge {
                        justify-self: start;
                    }
                    
                    .examination-actions {
                        flex-direction: column;
                    }
                }
            </style>
        `;
    }
}

function loadReportsPage() {
    const reportsSectionDiv = document.querySelector('#reports-page .reports-section');
    if (reportsSectionDiv && reportsSectionDiv.innerHTML.includes('Reports functionality will be maintained here')) {
        reportsSectionDiv.innerHTML = `
            <div class="reports-content">
                <p>Reports functionality will be integrated here...</p>
                <div class="report-info">
                    <h4>📊 Available Reports</h4>
                    <ul>
                        <li>Daily Shift Reports</li>
                        <li>Weekly Summary Reports</li>
                        <li>Maintenance Reports</li>
                        <li>Equipment Status Reports</li>
                    </ul>
                </div>
            </div>
        `;
    }
}

// Examination helper functions
function markExaminationComplete() {
    showToast('✅ Examination marked as complete');
}

function reportDefect() {
    showToast('⚠️ Defect reporting form opened');
}

function viewRecordBooks() {
    showToast('📚 Record books will be implemented');
}

// End of Shift functionality
function endShift() {
    console.log('🏁 End of shift button clicked');
    
    // Collect all the form data
    const shiftData = {
        // Header information
        shaftName: document.getElementById('shaftName')?.value || '',
        winderName: document.getElementById('winderName')?.value || '',
        compartmentServe: document.getElementById('compartmentServe')?.value || '',
        telNumber: document.getElementById('telNumber')?.value || '001',
        
        // Shift times
        morningTimeIn: document.getElementById('morningTimeIn')?.value || '',
        morningTimeOut: document.getElementById('morningTimeOut')?.value || '',
        afternoonTimeIn: document.getElementById('afternoonTimeIn')?.value || '',
        afternoonTimeOut: document.getElementById('afternoonTimeOut')?.value || '',
        nightTimeIn: document.getElementById('nightTimeIn')?.value || '',
        nightTimeOut: document.getElementById('nightTimeOut')?.value || '',
        
        // Trip counters
        morningTrips: parseInt(document.getElementById('morningTrips')?.value) || 0,
        morningPersons: parseInt(document.getElementById('morningPersons')?.value) || 0,
        morningMaterial: parseInt(document.getElementById('morningMaterial')?.value) || 0,
        morningExplosives: parseInt(document.getElementById('morningExplosives')?.value) || 0,
        morningMineral: parseInt(document.getElementById('morningMineral')?.value) || 0,
        
        afternoonTrips: parseInt(document.getElementById('afternoonTrips')?.value) || 0,
        afternoonPersons: parseInt(document.getElementById('afternoonPersons')?.value) || 0,
        afternoonMaterial: parseInt(document.getElementById('afternoonMaterial')?.value) || 0,
        afternoonExplosives: parseInt(document.getElementById('afternoonExplosives')?.value) || 0,
        afternoonMineral: parseInt(document.getElementById('afternoonMineral')?.value) || 0,
        
        nightTrips: parseInt(document.getElementById('nightTrips')?.value) || 0,
        nightPersons: parseInt(document.getElementById('nightPersons')?.value) || 0,
        nightMaterial: parseInt(document.getElementById('nightMaterial')?.value) || 0,
        nightExplosives: parseInt(document.getElementById('nightExplosives')?.value) || 0,
        nightMineral: parseInt(document.getElementById('nightMineral')?.value) || 0,
        
        // Personnel
        engineer: document.getElementById('engineer')?.checked || false,
        electrician: document.getElementById('electrician')?.checked || false,
        fitter: document.getElementById('fitter')?.checked || false,
        boilermaker: document.getElementById('boilermaker')?.checked || false,
        rigger: document.getElementById('rigger')?.checked || false,
        
        // Maintenance
        maintenanceRequired: document.getElementById('maintenanceRequired')?.checked || false,
        dailyMaintenance: document.getElementById('dailyMaintenance')?.checked || false,
        weeklyMaintenance: document.getElementById('weeklyMaintenance')?.checked || false,
        monthlyMaintenance: document.getElementById('monthlyMaintenance')?.checked || false,
        quarterlyMaintenance: document.getElementById('quarterlyMaintenance')?.checked || false,
        biannualMaintenance: document.getElementById('biannualMaintenance')?.checked || false,
        yearlyMaintenance: document.getElementById('yearlyMaintenance')?.checked || false,
        
        // Inspections
        morningInspection: document.getElementById('morningInspection')?.value || '',
        afternoonInspection: document.getElementById('afternoonInspection')?.value || '',
        nightInspection: document.getElementById('nightInspection')?.value || '',
        
        // Risk Assessment
        riskAssessment: document.getElementById('riskAssessment')?.checked || false,
        
        // Comments
        comments: document.getElementById('dailyComments')?.value || '',
        
        // Metadata
        timestamp: Date.now(),
        date: new Date().toISOString().split('T')[0],
        user: currentUser,
        shift: currentShift,
        entryType: 'end_of_shift'
    };
    
    console.log('📋 Shift data collected:', shiftData);
    
    // Save the data
    saveEntryData(shiftData);
    
    // Show confirmation
    showToast('🏁 End of shift data saved successfully');
    
    // Optional: Clear the form or navigate to a summary
    if (confirm('Shift ended successfully. Would you like to clear the form for the next shift?')) {
        clearShiftForm();
    }
}

function clearShiftForm() {
    // Clear all form fields except for the header info that might stay the same
    const fieldsToKeep = ['shaftName', 'winderName', 'compartmentServe', 'telNumber'];
    
    // Clear time fields
    ['morningTimeIn', 'morningTimeOut', 'afternoonTimeIn', 'afternoonTimeOut', 'nightTimeIn', 'nightTimeOut'].forEach(id => {
        const field = document.getElementById(id);
        if (field) field.value = '';
    });
    
    // Clear trip fields
    ['morningTrips', 'morningPersons', 'morningMaterial', 'morningExplosives', 'morningMineral',
     'afternoonTrips', 'afternoonPersons', 'afternoonMaterial', 'afternoonExplosives', 'afternoonMineral',
     'nightTrips', 'nightPersons', 'nightMaterial', 'nightExplosives', 'nightMineral'].forEach(id => {
        const field = document.getElementById(id);
        if (field) field.value = '';
    });
    
    // Clear personnel checkboxes
    ['engineer', 'electrician', 'fitter', 'boilermaker', 'rigger', 'maintenanceRequired'].forEach(id => {
        const field = document.getElementById(id);
        if (field) field.checked = false;
    });
    
    // Clear maintenance options
    ['dailyMaintenance', 'weeklyMaintenance', 'monthlyMaintenance', 'quarterlyMaintenance', 'biannualMaintenance', 'yearlyMaintenance'].forEach(id => {
        const field = document.getElementById(id);
        if (field) field.checked = false;
    });
    
    // Clear all maintenance sub-tasks
    ['daily', 'weekly', 'monthly', 'quarterly', 'biannual', 'yearly'].forEach(type => {
        for (let i = 1; i <= 6; i++) {
            const taskField = document.getElementById(type + 'Task' + i);
            if (taskField) taskField.checked = false;
        }
    });
    
    // Hide maintenance options and sub-options
    const maintenanceOptions = document.getElementById('maintenanceOptions');
    if (maintenanceOptions) maintenanceOptions.style.display = 'none';
    
    ['daily', 'weekly', 'monthly', 'quarterly', 'biannual', 'yearly'].forEach(type => {
        const subOptions = document.getElementById(type + 'MaintenanceOptions');
        if (subOptions) subOptions.style.display = 'none';
    });
    
    // Clear inspection dropdowns
    ['morningInspection', 'afternoonInspection', 'nightInspection'].forEach(id => {
        const field = document.getElementById(id);
        if (field) field.selectedIndex = 0;
    });
    
    // Clear risk assessment
    const riskAssessment = document.getElementById('riskAssessment');
    if (riskAssessment) riskAssessment.checked = false;
    
    // Hide and clear risk assessment form
    const riskForm = document.getElementById('riskAssessmentForm');
    if (riskForm) riskForm.style.display = 'none';
    
    const riskDetails = document.getElementById('riskAssessmentDetails');
    if (riskDetails) riskDetails.value = '';
    
    // Clear comments
    const commentsField = document.getElementById('dailyComments');
    if (commentsField) commentsField.value = '';
    
    showToast('📝 Form cleared for next shift');
}

// Toggle maintenance options when maintenance checkbox is clicked
function toggleMaintenanceOptions() {
    const maintenanceCheckbox = document.getElementById('maintenanceRequired');
    const maintenanceOptions = document.getElementById('maintenanceOptions');
    
    if (maintenanceCheckbox && maintenanceOptions) {
        if (maintenanceCheckbox.checked) {
            maintenanceOptions.style.display = 'block';
            console.log('🔧 Maintenance options shown');
        } else {
            maintenanceOptions.style.display = 'none';
            // Clear all maintenance sub-options when hiding
            ['dailyMaintenance', 'weeklyMaintenance', 'monthlyMaintenance', 'quarterlyMaintenance', 'biannualMaintenance', 'yearlyMaintenance'].forEach(id => {
                const field = document.getElementById(id);
                if (field) field.checked = false;
            });
            // Hide all sub-option panels
            ['daily', 'weekly', 'monthly', 'quarterly', 'biannual', 'yearly'].forEach(type => {
                const subOptions = document.getElementById(type + 'MaintenanceOptions');
                if (subOptions) subOptions.style.display = 'none';
            });
            console.log('🔧 Maintenance options hidden and cleared');
        }
    }
}

// Toggle maintenance sub-options when individual maintenance types are selected
function toggleMaintenanceSubOptions(type) {
    const checkbox = document.getElementById(type + 'Maintenance');
    const subOptions = document.getElementById(type + 'MaintenanceOptions');
    
    if (checkbox && subOptions) {
        if (checkbox.checked) {
            subOptions.style.display = 'block';
            console.log(`🔧 ${type} maintenance sub-options shown`);
        } else {
            subOptions.style.display = 'none';
            // Clear all sub-option checkboxes for this type
            for (let i = 1; i <= 6; i++) {
                const taskCheckbox = document.getElementById(type + 'Task' + i);
                if (taskCheckbox) taskCheckbox.checked = false;
            }
            console.log(`🔧 ${type} maintenance sub-options hidden and cleared`);
        }
    }
}

// Toggle risk assessment form
function toggleRiskAssessmentForm() {
    const riskCheckbox = document.getElementById('riskAssessment');
    const riskForm = document.getElementById('riskAssessmentForm');
    
    if (riskCheckbox && riskForm) {
        if (riskCheckbox.checked) {
            riskForm.style.display = 'block';
            console.log('🛡️ Risk assessment form opened');
        } else {
            riskForm.style.display = 'none';
            // Clear form data when hiding
            const detailsField = document.getElementById('riskAssessmentDetails');
            if (detailsField) detailsField.value = '';
            console.log('🛡️ Risk assessment form closed and cleared');
        }
    }
}

// Save risk assessment
function saveRiskAssessment() {
    const details = document.getElementById('riskAssessmentDetails')?.value || '';
    
    if (!details.trim()) {
        showToast('⚠️ Please complete the risk assessment details');
        return;
    }
    
    // Save risk assessment data
    const riskData = {
        details: details,
        timestamp: Date.now(),
        user: currentUser,
        shift: currentShift,
        entryType: 'risk_assessment'
    };
    
    saveEntryData(riskData);
    showToast('🛡️ Risk assessment saved successfully');
    console.log('🛡️ Risk assessment saved:', riskData);
}

// Close risk assessment form
function closeRiskAssessmentForm() {
    const riskCheckbox = document.getElementById('riskAssessment');
    const riskForm = document.getElementById('riskAssessmentForm');
    
    if (riskCheckbox) riskCheckbox.checked = false;
    if (riskForm) riskForm.style.display = 'none';
    
    // Clear form data
    const detailsField = document.getElementById('riskAssessmentDetails');
    if (detailsField) detailsField.value = '';
    
    showToast('❌ Risk assessment form closed');
    console.log('🛡️ Risk assessment form closed');
}

// Number scroller functionality
function adjustCounter(fieldId, change) {
    const field = document.getElementById(fieldId);
    if (!field) return;
    
    let currentValue = parseInt(field.value) || 0;
    let newValue = currentValue + change;
    
    // Enforce min/max limits
    const min = parseInt(field.getAttribute('min')) || 0;
    const max = parseInt(field.getAttribute('max')) || 999;
    
    if (newValue < min) newValue = min;
    if (newValue > max) newValue = max;
    
    field.value = newValue;
    
    // Trigger change event for any listeners
    field.dispatchEvent(new Event('change'));
    
    console.log(`📊 Counter ${fieldId} adjusted to ${newValue}`);
}

// User profile management
function loadUserProfile() {
    // Get stored user profile
    const userProfile = JSON.parse(localStorage.getItem('userProfile')) || {};
    
    console.log('👤 Loading user profile:', userProfile);
    
    // Auto-select personnel checkboxes based on user role
    if (userProfile.roles && userProfile.roles.length > 0) {
        userProfile.roles.forEach(role => {
            const checkbox = document.getElementById(role.toLowerCase());
            if (checkbox) {
                checkbox.checked = true;
                console.log(`✅ Auto-selected role: ${role}`);
            }
        });
    }
    
    // Set user info in header if available
    if (userProfile.name) {
        currentUser = userProfile.name;
        const userDisplay = document.getElementById('currentUserDisplay');
        if (userDisplay) {
            userDisplay.textContent = userProfile.name;
        }
    }
}

// Save user profile
function saveUserProfile(name, roles = []) {
    const userProfile = {
        name: name,
        roles: roles,
        lastLogin: Date.now(),
        createdAt: Date.now()
    };
    
    localStorage.setItem('userProfile', JSON.stringify(userProfile));
    console.log('💾 User profile saved:', userProfile);
    
    return userProfile;
}

// Create predefined user profiles for dashboard
function createPredefinedUsers() {
    const predefinedUsers = [
        { name: 'John Doe', roles: ['engineer'] },
        { name: 'Jane Smith', roles: ['electrician'] },
        { name: 'Mike Johnson', roles: ['fitter'] },
        { name: 'Sarah Wilson', roles: ['boilermaker'] },
        { name: 'Tom Brown', roles: ['rigger'] },
        { name: 'Lisa Davis', roles: ['engineer', 'electrician'] }
    ];
    
    localStorage.setItem('predefinedUsers', JSON.stringify(predefinedUsers));
    console.log('👥 Predefined users created:', predefinedUsers);
    
    return predefinedUsers;
}

// Weekly Maintenance Workflow Functions
let weeklyRiskAssessmentCompleted = false;
let electricalChecklistData = {};

// Start Weekly Risk Assessment
function startWeeklyRiskAssessment() {
    const modal = document.getElementById('weeklyRiskAssessmentModal');
    if (modal) {
        modal.style.display = 'block';
        
        // Initialize form with default values
        initializeIndustrialSafetyForm();
        
        console.log('🛡️ Industrial Safety Risk Assessment modal opened');
    }
}

// Initialize Industrial Safety Form
function initializeIndustrialSafetyForm() {
    // Generate unique document serial number
    const serialNumber = generateDocumentSerialNumber();
    document.getElementById('documentSerialNumber').value = serialNumber;
    
    // Set current date
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('documentDate').value = today;
    
    // Initialize signature canvas
    initializeSignatureCanvas();
    
    console.log('📋 Industrial Safety Form initialized with serial:', serialNumber);
}

// Generate unique document serial number
function generateDocumentSerialNumber() {
    const date = new Date();
    const year = date.getFullYear().toString().slice(-2);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const time = Date.now().toString().slice(-4);
    
    return `WL-${year}${month}${day}-${time}`;
}

// Close Weekly Risk Assessment
function closeWeeklyRiskAssessment() {
    const modal = document.getElementById('weeklyRiskAssessmentModal');
    if (modal) {
        modal.style.display = 'none';
        // Clear form data
        clearIndustrialSafetyForm();
        console.log('🛡️ Industrial Safety Risk Assessment modal closed');
    }
}

// Clear Industrial Safety Form
function clearIndustrialSafetyForm() {
    // Clear all form fields
    const form = document.getElementById('weeklyRiskAssessmentModal');
    if (form) {
        const inputs = form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            if (input.type === 'checkbox') {
                input.checked = false;
            } else if (input.type === 'date') {
                input.value = new Date().toISOString().split('T')[0];
            } else if (input.id === 'documentSerialNumber') {
                // Keep serial number - don't clear it
                return;
            } else {
                input.value = '';
            }
        });
    }
    
    // Clear signature
    clearSignature();
    
    // Reset tab to first one
    showRiskTab('lifting');
    
    // Clear all checklist statuses
    const statusButtons = document.querySelectorAll('.status-btn.active');
    statusButtons.forEach(btn => btn.classList.remove('active'));
}

// Show Risk Category Tab
function showRiskTab(tabName) {
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => content.classList.remove('active'));
    
    // Remove active class from all tab buttons
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => btn.classList.remove('active'));
    
    // Show selected tab content
    const selectedTab = document.getElementById(`${tabName}-tab`);
    if (selectedTab) {
        selectedTab.classList.add('active');
    }
    
    // Add active class to selected tab button
    const selectedButton = document.querySelector(`[onclick="showRiskTab('${tabName}')"]`);
    if (selectedButton) {
        selectedButton.classList.add('active');
    }
    
    console.log(`📋 Switched to ${tabName} tab`);
}

// Set Checklist Status
function setChecklistStatus(itemId, status) {
    // Remove active class from all status buttons for this item
    const item = document.querySelector(`[onclick*="${itemId}"]`).closest('.checklist-item');
    if (item) {
        const statusButtons = item.querySelectorAll('.status-btn');
        statusButtons.forEach(btn => btn.classList.remove('active'));
        
        // Add active class to selected status button
        const selectedButton = item.querySelector(`[onclick="setChecklistStatus('${itemId}', '${status}')"]`);
        if (selectedButton) {
            selectedButton.classList.add('active');
        }
        
        // Update item border color based on status
        if (status === 'go') {
            item.style.borderLeftColor = '#28a745';
        } else if (status === 'no-go') {
            item.style.borderLeftColor = '#dc3545';
        }
    }
    
    console.log(`✅ ${itemId} marked as ${status.toUpperCase()}`);
}

// Initialize Signature Canvas
function initializeSignatureCanvas() {
    const canvas = document.getElementById('signatureCanvas');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    let isDrawing = false;
    let lastX = 0;
    let lastY = 0;
    
    // Mouse events
    canvas.addEventListener('mousedown', (e) => {
        isDrawing = true;
        const rect = canvas.getBoundingClientRect();
        lastX = e.clientX - rect.left;
        lastY = e.clientY - rect.top;
    });
    
    canvas.addEventListener('mousemove', (e) => {
        if (!isDrawing) return;
        
        const rect = canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        ctx.beginPath();
        ctx.moveTo(lastX, lastY);
        ctx.lineTo(currentX, currentY);
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        lastX = currentX;
        lastY = currentY;
    });
    
    canvas.addEventListener('mouseup', () => {
        isDrawing = false;
    });
    
    canvas.addEventListener('mouseout', () => {
        isDrawing = false;
    });
    
    // Touch events for mobile
    canvas.addEventListener('touchstart', (e) => {
        e.preventDefault();
        isDrawing = true;
        const rect = canvas.getBoundingClientRect();
        const touch = e.touches[0];
        lastX = touch.clientX - rect.left;
        lastY = touch.clientY - rect.top;
    });
    
    canvas.addEventListener('touchmove', (e) => {
        e.preventDefault();
        if (!isDrawing) return;
        
        const rect = canvas.getBoundingClientRect();
        const touch = e.touches[0];
        const currentX = touch.clientX - rect.left;
        const currentY = touch.clientY - rect.top;
        
        ctx.beginPath();
        ctx.moveTo(lastX, lastY);
        ctx.lineTo(currentX, currentY);
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        lastX = currentX;
        lastY = currentY;
    });
    
    canvas.addEventListener('touchend', (e) => {
        e.preventDefault();
        isDrawing = false;
    });
}

// Clear Signature
function clearSignature() {
    const canvas = document.getElementById('signatureCanvas');
    if (canvas) {
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
}

// Complete Weekly Risk Assessment
function completeWeeklyRiskAssessment() {
    console.log('🛡️ Starting industrial safety risk assessment completion...');
    
    try {
        // Collect all form data
        const formData = collectIndustrialSafetyFormData();
        
        // Validate required fields
        if (!validateIndustrialSafetyForm(formData)) {
            return;
        }
        
        console.log('📋 Industrial safety form data:', formData);
        
        // Save risk assessment data
        const riskAssessmentData = {
            documentSerialNumber: formData.documentSerialNumber,
            date: formData.date,
            personInCharge: formData.personInCharge,
            expectedWork: formData.expectedWork,
            activitiesToPerform: formData.activitiesToPerform,
            sopReference: formData.sopReference,
            jhaReference: formData.jhaReference,
            emergencyContacts: formData.emergencyContacts,
            riskCategories: formData.riskCategories,
            workPlan: formData.workPlan,
            signature: formData.signature,
            completedBy: currentUser,
            completedAt: new Date().toISOString()
        };
        
        console.log('💾 Saving industrial safety risk assessment data:', riskAssessmentData);
        
        // Save to storage and mark as completed
        localStorage.setItem('weeklyRiskAssessment', JSON.stringify(riskAssessmentData));
        weeklyRiskAssessmentCompleted = true;
        
        // Enable Step 2
        const step2 = document.getElementById('weeklyStep2');
        if (step2) {
            step2.style.display = 'block';
            console.log('✅ Step 2 enabled');
        }
        
        // Close modal and show success
        closeWeeklyRiskAssessment();
        showToast('✅ Industrial Safety Risk Assessment completed successfully');
        
        // Save entry data
        const entryData = {
            type: 'industrial_safety_risk_assessment',
            data: riskAssessmentData,
            timestamp: Date.now(),
            date: new Date().toISOString().split('T')[0],
            user: currentUser
        };
        
        console.log('📤 Saving entry data:', entryData);
        saveEntryData(entryData);
        
        console.log('✅ Industrial safety risk assessment completed successfully');
        
    } catch (error) {
        console.error('❌ Error completing industrial safety risk assessment:', error);
        showToast('❌ Error saving risk assessment. Please try again.');
    }
}

// Collect Industrial Safety Form Data
function collectIndustrialSafetyFormData() {
    const formData = {
        documentSerialNumber: document.getElementById('documentSerialNumber').value,
        date: document.getElementById('documentDate').value,
        personInCharge: document.getElementById('personInCharge').value,
        expectedWork: document.getElementById('expectedWork').value,
        activitiesToPerform: document.getElementById('activitiesToPerform').value,
        sopReference: document.getElementById('sopReference').value,
        jhaReference: document.getElementById('jhaReference').value,
        emergencyContacts: {
            safetyContactName: document.getElementById('safetyContactName').value,
            safetyContactPhone: document.getElementById('safetyContactPhone').value,
            emergencyContactName: document.getElementById('emergencyContactName').value,
            emergencyContactPhone: document.getElementById('emergencyContactPhone').value
        },
        riskCategories: {},
        workPlan: {
            preparationPhase: document.getElementById('preparationPhase').value,
            executionPhase: document.getElementById('executionPhase').value,
            completionPhase: document.getElementById('completionPhase').value
        },
        signature: {
            name: document.getElementById('signatureName').value,
            date: document.getElementById('signatureDate').value,
            canvasData: getSignatureCanvasData()
        }
    };
    
    // Collect risk category data
    const riskCategories = ['lifting', 'hotwork', 'energy', 'heights'];
    riskCategories.forEach(category => {
        formData.riskCategories[category] = collectRiskCategoryData(category);
    });
    
    return formData;
}

// Collect Risk Category Data
function collectRiskCategoryData(category) {
    const categoryData = {};
    const checklistItems = document.querySelectorAll(`#${category}-tab .checklist-item`);
    
    checklistItems.forEach(item => {
        const itemText = item.querySelector('.item-text').textContent;
        const activeStatusBtn = item.querySelector('.status-btn.active');
        const notes = item.querySelector('.item-notes').value;
        
        if (activeStatusBtn) {
            const status = activeStatusBtn.classList.contains('go') ? 'go' : 'no-go';
            categoryData[itemText] = {
                status: status,
                notes: notes
            };
        }
    });
    
    return categoryData;
}

// Get Signature Canvas Data
function getSignatureCanvasData() {
    const canvas = document.getElementById('signatureCanvas');
    if (canvas) {
        return canvas.toDataURL();
    }
    return null;
}

// Validate Industrial Safety Form
function validateIndustrialSafetyForm(formData) {
    const requiredFields = [
        { field: 'personInCharge', label: 'Person in Charge' },
        { field: 'expectedWork', label: 'Expected Work' },
        { field: 'activitiesToPerform', label: 'Activities to be Performed' }
    ];
    
    for (const field of requiredFields) {
        if (!formData[field.field] || formData[field.field].trim() === '') {
            showToast(`❌ Please complete the ${field.label} field`);
            console.warn(`❌ Missing required field: ${field.field}`);
            return false;
        }
    }
    
    // Check if at least one risk category has been assessed
    const hasRiskAssessment = Object.values(formData.riskCategories).some(category => 
        Object.keys(category).length > 0
    );
    
    if (!hasRiskAssessment) {
        showToast('❌ Please assess at least one risk category');
        console.warn('❌ No risk categories assessed');
        return false;
    }
    
    // Check if signature is provided
    if (!formData.signature.name || formData.signature.name.trim() === '') {
        showToast('❌ Please provide a signature name');
        console.warn('❌ Missing signature name');
        return false;
    }
    
    return true;
}

// Save Risk Assessment (Draft)
function saveRiskAssessment() {
    try {
        const formData = collectIndustrialSafetyFormData();
        
        // Save as draft
        const draftData = {
            ...formData,
            status: 'draft',
            savedAt: new Date().toISOString(),
            savedBy: currentUser
        };
        
        localStorage.setItem('industrialSafetyDraft', JSON.stringify(draftData));
        showToast('💾 Risk Assessment saved as draft');
        
        console.log('📝 Industrial safety risk assessment saved as draft:', draftData);
        
    } catch (error) {
        console.error('❌ Error saving risk assessment draft:', error);
        showToast('❌ Error saving draft. Please try again.');
    }
}

// Start Electrical Checklist
function startElectricalChecklist() {
    if (!weeklyRiskAssessmentCompleted) {
        showToast('❌ Please complete Risk Assessment first');
        return;
    }
    
    const modal = document.getElementById('electricalChecklistModal');
    if (modal) {
        modal.style.display = 'block';
        console.log('⚡ Electrical checklist modal opened');
    }
}

// Close Electrical Checklist
function closeElectricalChecklist() {
    const modal = document.getElementById('electricalChecklistModal');
    if (modal) {
        modal.style.display = 'none';
        console.log('⚡ Electrical checklist modal closed');
    }
}

// Set status for checklist items
function setStatus(itemId, status) {
    // Remove selected class from all buttons in this row
    const statusButtons = document.querySelectorAll(`[onclick*="${itemId}"]`);
    statusButtons.forEach(btn => btn.classList.remove('selected'));
    
    // Add selected class to clicked button
    const clickedButton = document.querySelector(`[onclick="setStatus('${itemId}', '${status}')"]`);
    if (clickedButton) {
        clickedButton.classList.add('selected');
    }
    
    // Store status in checklist data
    if (!electricalChecklistData[itemId]) {
        electricalChecklistData[itemId] = {};
    }
    electricalChecklistData[itemId].status = status;
    
    // If Yellow or Red, require notes
    const notesField = document.getElementById(`${itemId}_notes`);
    if (notesField && (status === 'yellow' || status === 'red')) {
        notesField.style.borderColor = '#ffc107';
        notesField.placeholder = 'Notes required for ' + status + ' status...';
        notesField.focus();
    }
    
    console.log(`📋 Status set for ${itemId}: ${status}`);
    
    // Trigger notifications for Yellow/Red status
    if (status === 'yellow' || status === 'red') {
        triggerStatusNotification(itemId, status);
    }
}

// Trigger notifications for Yellow/Red status
function triggerStatusNotification(itemId, status) {
    const componentName = getComponentName(itemId);
    const alertLevel = status === 'red' ? 'URGENT' : 'ATTENTION';
    const message = `${alertLevel}: ${componentName} - Status: ${status.toUpperCase()}`;
    
    // Save notification to Firestore for dashboard alerts
    const notificationData = {
        qrId: itemId,
        componentName: componentName,
        status: status,
        inspector: currentUser,
        timestamp: new Date().toISOString(),
        alertLevel: alertLevel,
        escalated: status === 'red'
    };
    
    saveNotificationData(notificationData);
    
    console.log(`🚨 ${alertLevel} notification triggered for ${componentName}`);
    showToast(`🚨 ${alertLevel}: ${componentName} flagged as ${status.toUpperCase()}`);
}

// Get component name from item ID
function getComponentName(itemId) {
    const componentNames = {
        'rm6_isolate': 'RM6 Feeder Breaker',
        'rm6_contactor_disc': 'RM6 Contactor Discolouring',
        'rm6_batt_on': 'RM6 Battery Voltage (ON)',
        'hyd_oil': 'Hydraulic Oil Level',
        'test_ult': 'ULT Wire Test'
    };
    return componentNames[itemId] || itemId;
}

// Save notification data to Firestore
function saveNotificationData(notificationData) {
    try {
        if (typeof WinderLogbook !== 'undefined' && WinderLogbook.saveNotificationData) {
            WinderLogbook.saveNotificationData(JSON.stringify(notificationData));
        } else {
            // Fallback for web testing
            localStorage.setItem(`notification_${Date.now()}`, JSON.stringify(notificationData));
            console.log('📱 Notification saved locally (WinderLogbook interface not available)');
        }
    } catch (error) {
        console.error('❌ Error saving notification data:', error);
    }
}

// Submit Electrical Checklist
function submitElectricalChecklist() {
    console.log('🔄 Starting checklist submission...');
    console.log('📋 Current electricalChecklistData:', electricalChecklistData);
    
    // Check if we have any checklist data
    if (!electricalChecklistData || Object.keys(electricalChecklistData).length === 0) {
        showToast('❌ No checklist data found. Please select status for at least one item.');
        console.error('❌ No electricalChecklistData found');
        return;
    }
    
    try {
        // Collect notes for all items
        Object.keys(electricalChecklistData).forEach(itemId => {
            const notesField = document.getElementById(`${itemId}_notes`);
            if (notesField) {
                electricalChecklistData[itemId].notes = notesField.value;
            }
        });
        
        // Validate that items with Yellow/Red status have notes
        let missingNotes = [];
        Object.keys(electricalChecklistData).forEach(itemId => {
            const item = electricalChecklistData[itemId];
            if ((item.status === 'yellow' || item.status === 'red') && !item.notes) {
                missingNotes.push(getComponentName(itemId));
            }
        });
        
        if (missingNotes.length > 0) {
            showToast(`❌ Please add notes for: ${missingNotes.join(', ')}`);
            return;
        }
        
        // Generate summary with error handling
        let summary;
        try {
            summary = generateChecklistSummary();
            console.log('📊 Generated summary:', summary);
        } catch (summaryError) {
            console.error('❌ Error generating summary:', summaryError);
            summary = {
                totalItems: Object.keys(electricalChecklistData).length,
                greenCount: 0,
                yellowCount: 0,
                redCount: 0,
                overallStatus: 'unknown'
            };
        }
        
        // Create complete checklist report
        const checklistReport = {
            type: 'weekly_electrical_checklist',
            completedBy: currentUser,
            completedAt: new Date().toISOString(),
            shift: currentShift,
            riskAssessment: JSON.parse(localStorage.getItem('weeklyRiskAssessment') || '{}'),
            checklistData: electricalChecklistData,
            summary: summary,
            timestamp: Date.now(),
            date: new Date().toISOString().split('T')[0],
            jobDescription: 'Electrician',
            category: 'Weekly Maintenance',
            status: summary.overallStatus
        };
        
        console.log('📋 Complete checklist report:', checklistReport);
        
        // Save to storage and Firestore
        localStorage.setItem('weeklyElectricalChecklist', JSON.stringify(checklistReport));
        
        // Save entry data with error handling
        try {
            saveEntryData(checklistReport);
            console.log('✅ Entry data saved successfully');
        } catch (entryError) {
            console.error('❌ Error saving entry data:', entryError);
            showToast('⚠️ Warning: Entry data save failed, but continuing...');
        }
        
        // Save checklist data with error handling
        try {
            saveChecklistData(checklistReport);
            console.log('✅ Checklist data saved successfully');
        } catch (checklistError) {
            console.error('❌ Error saving checklist data:', checklistError);
            showToast('⚠️ Warning: Checklist data save failed, but continuing...');
        }
        
        // Generate report if Android interface available
        if (typeof WinderLogbook !== 'undefined' && WinderLogbook.generateWeeklyReport) {
            try {
                WinderLogbook.generateWeeklyReport(JSON.stringify(checklistReport));
                console.log('✅ Weekly report generation requested');
            } catch (reportError) {
                console.error('❌ Error generating weekly report:', reportError);
                showToast('⚠️ Warning: Report generation failed, but checklist saved');
            }
        } else {
            console.log('ℹ️ Android interface not available for report generation');
        }
        
        // Close modal and reset
        closeElectricalChecklist();
        resetWeeklyMaintenance();
        
        showToast('✅ Weekly Electrical Checklist submitted successfully');
        console.log('✅ Weekly electrical checklist completed successfully');
        
    } catch (error) {
        console.error('❌ Critical error in submitElectricalChecklist:', error);
        showToast('❌ Error submitting checklist: ' + error.message);
    }
}

// Save entry data to Android/Firestore
function saveEntryData(entryData) {
    try {
        console.log('💾 Saving entry data:', entryData);
        
        // Save to Android interface if available
        if (typeof WinderLogbook !== 'undefined' && WinderLogbook.saveLogbookEntry) {
            WinderLogbook.saveLogbookEntry(JSON.stringify(entryData));
            console.log('✅ Entry data sent to WinderLogbook interface');
        } else {
            console.warn('⚠️ WinderLogbook interface not available, saving locally only');
        }
        
        // Also save locally as backup
        const key = `entry_${entryData.type}_${Date.now()}`;
        localStorage.setItem(key, JSON.stringify(entryData));
        console.log(`💾 Entry data saved locally with key: ${key}`);
        
    } catch (error) {
        console.error('❌ Error saving entry data:', error);
        throw error;
    }
}

function saveChecklistData(checklistData) {
    try {
        if (typeof WinderLogbook !== 'undefined' && WinderLogbook.saveChecklistData) {
            WinderLogbook.saveChecklistData(JSON.stringify(checklistData));
        } else {
            // Fallback for web testing
            localStorage.setItem(`checklist_${Date.now()}`, JSON.stringify(checklistData));
            console.log('📋 Checklist data saved locally (WinderLogbook interface not available)');
        }
    } catch (error) {
        console.error('❌ Error saving checklist data:', error);
    }
}

// Generate checklist summary
function generateChecklistSummary() {
    let greenCount = 0;
    let yellowCount = 0;
    let redCount = 0;
    
    Object.values(electricalChecklistData).forEach(item => {
        switch (item.status) {
            case 'green': greenCount++; break;
            case 'yellow': yellowCount++; break;
            case 'red': redCount++; break;
        }
    });
    
    return {
        totalItems: Object.keys(electricalChecklistData).length,
        greenCount,
        yellowCount,
        redCount,
        overallStatus: redCount > 0 ? 'red' : (yellowCount > 0 ? 'yellow' : 'green')
    };
}

// Reset weekly maintenance workflow
function resetWeeklyMaintenance() {
    weeklyRiskAssessmentCompleted = false;
    electricalChecklistData = {};
    
    // Hide Step 2
    const step2 = document.getElementById('weeklyStep2');
    if (step2) {
        step2.style.display = 'none';
    }
    
    // Clear stored data
    localStorage.removeItem('weeklyRiskAssessment');
    localStorage.removeItem('weeklyElectricalChecklist');
    
    console.log('🔄 Weekly maintenance workflow reset');
}

// Offline Mode Management
class OfflineManager {
    constructor() {
        this.isOnline = navigator.onLine;
        this.pendingSync = [];
        this.setupNetworkListeners();
        this.loadPendingData();
    }

    setupNetworkListeners() {
        window.addEventListener('online', () => {
            this.isOnline = true;
            this.showConnectionStatus('Online - Syncing data...', 'success');
            this.syncPendingData();
        });

        window.addEventListener('offline', () => {
            this.isOnline = false;
            this.showConnectionStatus('Offline - Data will be cached locally', 'warning');
        });
    }

    showConnectionStatus(message, type) {
        if (typeof WinderLogbook !== 'undefined') {
            WinderLogbook.showToast(message);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }

    addToPendingSync(data) {
        const timestamp = Date.now();
        const pendingItem = {
            id: `pending_${timestamp}`,
            data: data,
            timestamp: timestamp,
            retryCount: 0
        };
        
        this.pendingSync.push(pendingItem);
        this.savePendingData();
        
        if (this.isOnline) {
            this.syncPendingData();
        }
    }

    savePendingData() {
        localStorage.setItem('offlinePendingSync', JSON.stringify(this.pendingSync));
    }

    loadPendingData() {
        const stored = localStorage.getItem('offlinePendingSync');
        if (stored) {
            try {
                this.pendingSync = JSON.parse(stored);
            } catch (e) {
                console.error('Error loading pending sync data:', e);
                this.pendingSync = [];
            }
        }
    }

    async syncPendingData() {
        if (!this.isOnline || this.pendingSync.length === 0) return;

        const itemsToRemove = [];
        
        for (let i = 0; i < this.pendingSync.length; i++) {
            const item = this.pendingSync[i];
            
            try {
                if (typeof WinderLogbook !== 'undefined') {
                    const success = WinderLogbook.saveLogbookEntry(JSON.stringify(item.data));
                    if (success) {
                        itemsToRemove.push(i);
                    } else {
                        item.retryCount++;
                        if (item.retryCount > 3) {
                            console.error('Failed to sync item after 3 retries:', item);
                            itemsToRemove.push(i); // Remove failed items after 3 retries
                        }
                    }
                }
            } catch (e) {
                console.error('Error syncing item:', e);
                item.retryCount++;
                if (item.retryCount > 3) {
                    itemsToRemove.push(i);
                }
            }
        }

        // Remove successfully synced items
        for (let i = itemsToRemove.length - 1; i >= 0; i--) {
            this.pendingSync.splice(itemsToRemove[i], 1);
        }

        this.savePendingData();
        
        if (itemsToRemove.length > 0) {
            this.showConnectionStatus(`Synced ${itemsToRemove.length} cached items`, 'success');
        }
    }

    saveToCache(key, data) {
        try {
            const cacheItem = {
                data: data,
                timestamp: Date.now(),
                version: '1.0'
            };
            localStorage.setItem(`cache_${key}`, JSON.stringify(cacheItem));
        } catch (e) {
            console.error('Error saving to cache:', e);
        }
    }

    loadFromCache(key, maxAge = 24 * 60 * 60 * 1000) { // Default 24 hours
        try {
            const stored = localStorage.getItem(`cache_${key}`);
            if (stored) {
                const cacheItem = JSON.parse(stored);
                const age = Date.now() - cacheItem.timestamp;
                
                if (age < maxAge) {
                    return cacheItem.data;
                } else {
                    // Cache expired, remove it
                    localStorage.removeItem(`cache_${key}`);
                }
            }
        } catch (e) {
            console.error('Error loading from cache:', e);
        }
        return null;
    }

    clearCache() {
        const keys = Object.keys(localStorage);
        keys.forEach(key => {
            if (key.startsWith('cache_')) {
                localStorage.removeItem(key);
            }
        });
    }

    getPendingSyncCount() {
        return this.pendingSync.length;
    }

    getConnectionStatus() {
        return {
            online: this.isOnline,
            pendingItems: this.pendingSync.length,
            lastSync: this.getLastSyncTime()
        };
    }

    getLastSyncTime() {
        const lastSync = localStorage.getItem('lastSyncTime');
        return lastSync ? new Date(parseInt(lastSync)) : null;
    }

    updateLastSyncTime() {
        localStorage.setItem('lastSyncTime', Date.now().toString());
    }
}

// Initialize offline manager
const offlineManager = new OfflineManager();

// Update UI connection status
function updateConnectionStatusUI() {
    const statusIndicator = document.getElementById('statusIndicator');
    const statusText = document.getElementById('statusText');
    const pendingCount = document.getElementById('pendingCount');
    
    if (statusIndicator && statusText && pendingCount) {
        const status = offlineManager.getConnectionStatus();
        
        if (status.online) {
            statusIndicator.textContent = '🟢';
            statusText.textContent = 'Online';
        } else {
            statusIndicator.textContent = '🔴';
            statusText.textContent = 'Offline';
        }
        
        if (status.pendingItems > 0) {
            pendingCount.textContent = `(${status.pendingItems} pending)`;
        } else {
            pendingCount.textContent = '';
        }
    }
}

// Update status every 30 seconds
setInterval(updateConnectionStatusUI, 30000);

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    try {
        console.log('🚀 Digital Winding Engine Driver Logbook Starting...');
        
        if (typeof WinderLogbook !== 'undefined') {
            console.log('✅ Native Android interface detected');
            try {
                WinderLogbook.showToast('Digital Winder Logbook Started');
            } catch (toastError) {
                console.error('❌ Error showing startup toast:', toastError);
            }
        } else {
            console.log('⚠️ Running in web mode - WinderLogbook interface not available');
        }
        
        initializeApp();
        setupEventListeners();
        loadInitialData();
        
        console.log('✅ App startup completed successfully');
        
    } catch (error) {
        console.error('❌ Critical error during app startup:', error);
        if (typeof WinderLogbook !== 'undefined') {
            try {
                WinderLogbook.showToast('App startup error: ' + error.message);
            } catch (toastError) {
                console.error('❌ Could not show error toast:', toastError);
            }
        }
    }
});

function initializeApp() {
    console.log('🔄 Starting app initialization...');
    
    // Debug: Check what methods are available on WinderLogbook
    if (typeof WinderLogbook !== 'undefined') {
        console.log('✅ WinderLogbook interface is available');
        console.log('📋 Available methods:', Object.getOwnPropertyNames(WinderLogbook));
        
        // Test basic method
        try {
            WinderLogbook.showToast('App initialization started');
            console.log('✅ showToast method works');
        } catch (error) {
            console.error('❌ Error calling showToast:', error);
        }
    } else {
        console.warn('⚠️ WinderLogbook interface is NOT available');
    }
    
    const now = new Date();
    const hour = now.getHours();
    
    if (hour >= 6 && hour < 14) {
        currentShift = 'Morning';
        currentUser = 'Bays Draganovic';
    } else if (hour >= 14 && hour < 22) {
        currentShift = 'Afternoon';  
        currentUser = 'Elsa Nitandara';
    } else {
        currentShift = 'Night';
        currentUser = 'Carl Renarafo';
    }
    
    console.log(`Initialized: ${currentShift} shift with driver ${currentUser}`);
    
    // Authentication system disabled - always authenticated
    console.log('✅ Authentication system disabled - always authenticated');
    updateAuthenticationUI();
    
    // Load user profile and auto-select roles with error handling
    setTimeout(() => {
        try {
            loadUserProfile();
            console.log('✅ User profile loaded');
        } catch (error) {
            console.error('❌ Error loading user profile:', error);
        }
    }, 1000);
    
    // Create predefined users if they don't exist
    if (!localStorage.getItem('predefinedUsers')) {
        try {
            createPredefinedUsers();
            console.log('✅ Predefined users created');
        } catch (error) {
            console.error('❌ Error creating predefined users:', error);
        }
    }
    
    console.log('✅ App initialization completed');
}

function setupEventListeners() {
    try {
        console.log('🔄 Setting up event listeners...');
        const statusSelects = document.querySelectorAll('.status-select');
        statusSelects.forEach(select => {
            select.addEventListener('change', function() {
                updateStatusClass(this);
            });
        });
        
        console.log('✅ Event listeners set up successfully');
    } catch (error) {
        console.error('❌ Error setting up event listeners:', error);
    }
}

function loadInitialData() {
    try {
        console.log('🔄 Loading initial data...');
        loadTripCounters();
        updateCounterDisplay();
        loadComponentStatuses();
        updateConnectionStatusUI();
        console.log('✅ Initial data loaded successfully');
    } catch (error) {
        console.error('❌ Error loading initial data:', error);
    }
}

// Trip Counter Functions
function incrementCounter(type) {
    if (tripCounters.hasOwnProperty(type)) {
        tripCounters[type]++;
        updateCounterDisplay();
        saveTripCounters();
        
        if (typeof WinderLogbook !== 'undefined') {
            WinderLogbook.showToast(`${type.charAt(0).toUpperCase() + type.slice(1)} trip recorded: ${tripCounters[type]}`);
        }
    }
}

function decrementCounter(type) {
    if (tripCounters.hasOwnProperty(type) && tripCounters[type] > 0) {
        tripCounters[type]--;
        updateCounterDisplay();
        saveTripCounters();
        
        if (typeof WinderLogbook !== 'undefined') {
            WinderLogbook.showToast(`${type.charAt(0).toUpperCase() + type.slice(1)} trip count decreased: ${tripCounters[type]}`);
        }
    }
}

function updateCounterDisplay() {
    Object.keys(tripCounters).forEach(type => {
        const element = document.getElementById(type + 'Count');
        if (element) {
            element.textContent = tripCounters[type];
        }
    });
}

function saveTripCounters() {
    const data = {
        entryType: 'trip_counters',
        date: new Date().toISOString().split('T')[0], // YYYY-MM-DD format
        shift: currentShift,
        user: 'Bays Draganovic', // Current user
        counters: tripCounters,
        // Also save flat structure for web dashboard compatibility
        persons: tripCounters.persons,
        material: tripCounters.material,
        mineral: tripCounters.mineral,
        explosives: tripCounters.explosives,
        timestamp: Date.now()
    };
    
    // Always try to save to Firestore first, then fallback to offline storage
    if (typeof WinderLogbook !== 'undefined') {
        console.log('🔄 Saving trip counters to Firestore:', data);
        const success = WinderLogbook.saveLogbookEntry(JSON.stringify(data));
        if (success) {
            console.log('✅ Trip counters saved to Firestore successfully');
        } else {
            console.log('❌ Failed to save to Firestore, caching locally');
            offlineManager.addToPendingSync(data);
        }
    } else {
        console.log('📱 WinderLogbook interface not available, caching locally');
        offlineManager.addToPendingSync(data);
    }
    
    // Always cache locally as backup
    localStorage.setItem('winderTripCounters', JSON.stringify(data));
}

function loadTripCounters() {
    const today = new Date().toDateString();
    const stored = localStorage.getItem('winderTripCounters');
    if (stored) {
        try {
            const data = JSON.parse(stored);
            if (data.date === today) {
                tripCounters = data.counters;
            }
        } catch (e) {
            console.error('Error loading trip counters:', e);
        }
    }
}

// Component Status Functions
function updateStatusClass(selectElement) {
    const value = selectElement.value;
    
    selectElement.classList.remove('good', 'attention', 'faulty');
    selectElement.classList.add(value);
    
    saveComponentStatus(selectElement);
    
    const componentName = selectElement.closest('.components-grid').querySelector('.component-label').textContent;
    
    let message = '';
    switch(value) {
        case 'good':
            message = `${componentName}: Status marked as Good ✅`;
            break;
        case 'attention':
            message = `${componentName}: Needs Attention ⚠️`;
            break;
        case 'faulty':
            message = `${componentName}: FAULTY ❌ - Action required!`;
            break;
    }
    
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.showToast(message);
    }
    
    if (value === 'faulty') {
        handleFaultyComponent(componentName);
    }
}

function saveComponentStatus(selectElement) {
    const componentName = selectElement.closest('.components-grid').querySelector('.component-label').textContent;
    const status = selectElement.value;
    
    const statusData = {
        entryType: 'component_status',
        data: {
            component: componentName,
            status: status,
            timestamp: new Date().toISOString(),
            shift: currentShift,
            user: currentUser
        }
    };
    
    // Always try to save to Firestore first, then fallback to offline storage
    if (typeof WinderLogbook !== 'undefined') {
        console.log('🔄 Saving component status to Firestore:', statusData);
        const success = WinderLogbook.saveLogbookEntry(JSON.stringify(statusData));
        if (success) {
            console.log('✅ Component status saved to Firestore successfully');
        } else {
            console.log('❌ Failed to save component status to Firestore, caching locally');
            offlineManager.addToPendingSync(statusData);
        }
    } else {
        console.log('📱 WinderLogbook interface not available, caching locally');
        offlineManager.addToPendingSync(statusData);
    }
    
    // Always cache locally as backup
    const stored = JSON.parse(localStorage.getItem('componentStatuses') || '[]');
    stored.push(statusData);
    localStorage.setItem('componentStatuses', JSON.stringify(stored));
}

function loadComponentStatuses() {
    console.log('Loading component statuses...');
}

function handleFaultyComponent(componentName) {
    console.log(`Faulty component detected: ${componentName}`);
    
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.showToast(`ALERT: ${componentName} marked as faulty. Maintenance required!`);
    }
}

// Voice-to-Text Functions
function startVoiceInput(fieldId) {
    if (typeof WinderLogbook !== 'undefined') {
        const isAvailable = WinderLogbook.isVoiceInputAvailable();
        if (!isAvailable) {
            alert('Voice input not available. Please check microphone permissions.');
            return;
        }
        
        const success = WinderLogbook.startVoiceToText(fieldId);
        if (success) {
            // Show visual feedback
            const field = document.getElementById(fieldId);
            if (field) {
                field.style.border = '2px solid #4CAF50';
                field.placeholder = 'Listening... Speak now';
            }
        }
    } else {
        // Fallback for testing
        simulateVoiceInput(fieldId);
    }
}

function stopVoiceInput() {
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.stopVoiceToText();
    }
    
    // Reset all field styling
    const textareas = document.querySelectorAll('textarea');
    textareas.forEach(textarea => {
        textarea.style.border = '';
        if (textarea.placeholder.includes('Listening')) {
            textarea.placeholder = 'Enter your notes here or use voice input...';
        }
    });
}

function updateVoiceInputField(fieldId, text) {
    const field = document.getElementById(fieldId);
    if (field) {
        // Append to existing text if there's content
        const existingText = field.value.trim();
        if (existingText) {
            field.value = existingText + ' ' + text;
        } else {
            field.value = text;
        }
        
        // Reset styling
        field.style.border = '';
        field.placeholder = 'Enter your notes here or use voice input...';
        
        // Trigger change event for any listeners
        field.dispatchEvent(new Event('change'));
    }
}

function simulateVoiceInput(fieldId) {
    // Voice input simulation not available in production
    showToast('Voice input not available in this mode');
}

// Photo Capture Functions
function capturePhoto(componentName, description = '') {
    if (typeof WinderLogbook !== 'undefined') {
        const isAvailable = WinderLogbook.isCameraAvailable();
        if (!isAvailable) {
            alert('Camera not available. Please check camera permissions.');
            return;
        }
        
        const success = WinderLogbook.capturePhoto(componentName, description);
        if (success) {
            // Show visual feedback
            showNotification(`📸 Photo capture initiated for ${componentName}`, 'info');
        }
    } else {
        // Camera not available
        showToast('Camera not available');
    }
}

// Photo capture simulation removed for production

function openPhotoModal(componentName) {
    // Create a modal for photo capture with description
    const modal = document.createElement('div');
    modal.className = 'photo-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>📸 Capture Photo - ${componentName}</h3>
                <button class="close-btn" onclick="closePhotoModal()">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>Description:</label>
                    <textarea id="photoDescription" rows="3" placeholder="Describe the issue or condition..."></textarea>
                </div>
                <div class="photo-preview" id="photoPreview">
                    <p>📷 Photo will appear here after capture</p>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="takePhoto('${componentName}')">📸 Take Photo</button>
                <button class="btn btn-secondary" onclick="closePhotoModal()">Cancel</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
}

function takePhoto(componentName) {
    const description = document.getElementById('photoDescription').value;
    capturePhoto(componentName, description);
    closePhotoModal();
}

function closePhotoModal() {
    const modal = document.querySelector('.photo-modal');
    if (modal) {
        modal.remove();
    }
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Style the notification
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: bold;
        z-index: 10000;
        max-width: 300px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    `;
    
    // Set background color based on type
    switch(type) {
        case 'success':
            notification.style.backgroundColor = '#4CAF50';
            break;
        case 'error':
            notification.style.backgroundColor = '#F44336';
            break;
        case 'info':
        default:
            notification.style.backgroundColor = '#2196F3';
            break;
    }
    
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// QR Code Scanning Functions
function scanQR(componentType) {
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.showToast(`QR Scanner activated for ${componentType}`);
    } else {
        showToast('QR Scanner not available');
    }
}

// QR scanning simulation removed for production

// Maintenance Toggle Functions
function toggleMaintenance(type, toggleElement) {
    const isActive = toggleElement.classList.contains('active');
    
    if (isActive) {
        toggleElement.classList.remove('active');
    } else {
        toggleElement.classList.add('active');
    }
    
    const status = !isActive;
    const message = status ? `Maintenance ${type} marked as active` : `Maintenance ${type} marked as inactive`;
    
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.showToast(message);
    }
    
    saveMaintenanceStatus(type, status);
}

function saveMaintenanceStatus(type, status) {
    const maintenanceData = {
        type: type,
        status: status,
        timestamp: new Date().toISOString(),
        shift: currentShift,
        user: currentUser
    };
    
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.saveLogbookEntry(JSON.stringify({
            entryType: 'maintenance_status',
            data: maintenanceData
        }));
    } else {
        const stored = JSON.parse(localStorage.getItem('maintenanceStatuses') || '[]');
        stored.push(maintenanceData);
        localStorage.setItem('maintenanceStatuses', JSON.stringify(stored));
    }
}

// Specialist Instructions Functions
function toggleSpecialist(headerElement) {
    const panel = headerElement.parentElement;
    const content = panel.querySelector('.specialist-content');
    const arrow = headerElement.querySelector('span:last-child');
    
    if (content.classList.contains('active')) {
        content.classList.remove('active');
        arrow.textContent = '▼';
    } else {
        content.classList.add('active');
        arrow.textContent = '▲';
    }
}

// Main Action Functions
function saveShiftData() {
    const shiftData = {
        shift: currentShift,
        user: currentUser,
        timestamp: new Date().toISOString(),
        tripCounters: tripCounters,
        morningDriver: document.querySelector('.morning .driver-name')?.textContent,
        afternoonDriver: document.querySelector('.afternoon .driver-name')?.textContent,
        nightDriver: document.querySelector('.night .driver-name')?.textContent
    };
    
    if (typeof WinderLogbook !== 'undefined') {
        const success = WinderLogbook.saveLogbookEntry(JSON.stringify({
            entryType: 'complete_shift_data',
            data: shiftData
        }));
        
        if (success) {
            WinderLogbook.showToast('Shift data saved successfully to Firestore');
        } else {
            WinderLogbook.showToast('Failed to save shift data');
        }
    } else {
        localStorage.setItem('currentShiftData', JSON.stringify(shiftData));
        alert('Shift data saved locally (web mode)');
    }
}

function syncToFirestore() {
    if (typeof WinderLogbook !== 'undefined') {
        const status = offlineManager.getConnectionStatus();
        const message = `Syncing to Firestore... ${status.pendingItems} items pending`;
        WinderLogbook.showToast(message);
        
        // Trigger immediate sync
        offlineManager.syncPendingData();
        offlineManager.updateLastSyncTime();
    } else {
        const pendingCount = offlineManager.getPendingSyncCount();
        alert(`Offline mode: ${pendingCount} items cached locally. Firestore sync available in native Android app.`);
    }
}

function generateReport() {
    if (typeof WinderLogbook !== 'undefined') {
        const reportData = WinderLogbook.generateReport('daily_shift_report');
        WinderLogbook.showToast('Daily shift report generated');
    } else {
        alert('Report generation is only available in the native Android app');
    }
}

function emergencyLog() {
    const emergencyData = {
        type: 'emergency',
        timestamp: new Date().toISOString(),
        shift: currentShift,
        user: currentUser,
        description: prompt('Enter emergency description:') || 'Emergency logged without description'
    };
    
    if (typeof WinderLogbook !== 'undefined') {
        WinderLogbook.saveLogbookEntry(JSON.stringify({
            entryType: 'emergency_log',
            data: emergencyData,
            priority: 'high'
        }));
        WinderLogbook.showToast('🚨 EMERGENCY LOGGED - Alert sent to supervisors');
    } else {
        console.log('EMERGENCY LOG:', emergencyData);
        alert('🚨 Emergency logged (web mode)');
    }
}

// Biometric Signature Functions
function captureShiftSignature(shift, action) {
    if (typeof WinderLogbook !== 'undefined') {
        const signatureData = WinderLogbook.getBiometricSignature();
        const signature = JSON.parse(signatureData);
        
        // Log the biometric action
        WinderLogbook.logBiometricAction(`${shift}_shift_${action}`, `${action} shift signature for ${shift} shift`);
        
        // Update UI to show signature captured
        updateSignatureStatus(shift, action, signature);
        
        // Save signature data
        saveSignatureData(shift, action, signature);
        
        WinderLogbook.showToast(`${action.charAt(0).toUpperCase() + action.slice(1)} shift signature captured for ${shift} shift`);
    } else {
        // Signature capture not available
        showToast('Signature capture not available');
    }
}

function getCurrentShiftDriver(shift) {
    switch(shift) {
        case 'morning': return 'Bays Draganovic';
        case 'afternoon': return 'Elsa Nitandara';
        case 'night': return 'Carl Renarafo';
        default: return 'Unknown Driver';
    }
}

function updateSignatureStatus(shift, action, signature) {
    const statusElement = document.getElementById(`${shift}SignatureStatus`);
    if (statusElement) {
        const timeStr = new Date(signature.timestamp).toLocaleTimeString();
        statusElement.innerHTML = `
            <small><strong>${action.charAt(0).toUpperCase() + action.slice(1)} signature:</strong><br>
            ${signature.user}<br>
            ${timeStr}<br>
            Hash: ${signature.signature_hash}</small>
        `;
        statusElement.className = 'signature-status signed';
    }
}

// BIOMETRIC SIGNATURE FUNCTIONS REMOVED

// BIOMETRIC SIGNATURE LOADING DISABLED

// Auto-save functionality
setInterval(() => {
    saveShiftData();
}, 5 * 60 * 1000);

// Enhanced error handling
window.addEventListener('error', function(event) {
    console.error('Application error:', event.error);
    console.error('Error stack:', event.error?.stack);
    console.error('Error details:', {
        message: event.error?.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno
    });
    
    // Show error toast if interface is available
    if (typeof WinderLogbook !== 'undefined') {
        try {
            WinderLogbook.showToast('An error occurred in the application');
        } catch (toastError) {
            console.error('Could not show error toast:', toastError);
        }
    }
});

// Handle unhandled promise rejections
window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);
    console.error('Promise rejection stack:', event.reason?.stack);
    
    // Show error toast if interface is available
    if (typeof WinderLogbook !== 'undefined') {
        try {
            WinderLogbook.showToast('An unexpected error occurred');
        } catch (toastError) {
            console.error('Could not show error toast:', toastError);
        }
    }
});

console.log('Digital Winding Engine Driver Logbook JavaScript loaded successfully');
