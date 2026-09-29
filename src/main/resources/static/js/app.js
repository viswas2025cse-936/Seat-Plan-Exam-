/**
 * SeatPlan Pro — Exam Hall Seating Arrangement Frontend Logic
 */

const API_BASE = '/api';

// State
let allHalls = [];
let allStudents = [];
let allSessions = [];
let currentChartData = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
    loadHalls();
    loadStudents();
    loadSessions();
    loadNotifications();

    // Auto-refresh notifications every 15 seconds
    setInterval(loadNotifications, 15000);
});

// Tab Navigation
function switchTab(tabId) {
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));

    const targetTab = document.getElementById(`tab-${tabId}`);
    const targetBtn = document.querySelector(`.nav-btn[data-tab="${tabId}"]`);

    if (targetTab) targetTab.classList.add('active');
    if (targetBtn) targetBtn.classList.add('active');

    // Refresh specific tab data
    if (tabId === 'dashboard') loadDashboardData();
    if (tabId === 'halls') loadHalls();
    if (tabId === 'students') loadStudents();
    if (tabId === 'sessions') {
        loadSessions();
        populateSessionSelects();
    }
    if (tabId === 'chart') {
        populateChartSessionSelect();
    }
    if (tabId === 'notifications') loadNotifications();
}

// Generic API Helper
async function fetchApi(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
            },
            ...options
        });

        if (response.status === 204) {
            return null;
        }

        const data = await response.json();
        if (!response.ok) {
            const errorMsg = data.message || data.error || 'Request failed';
            throw new Error(errorMsg);
        }
        return data;
    } catch (err) {
        showToast('Error', err.message, 'error');
        throw err;
    }
}

// Toast Notifications
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';
    if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <h4>${escapeHtml(title)}</h4>
            <p>${escapeHtml(message)}</p>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// -------------------------------------------------------------
// DASHBOARD LOGIC
// -------------------------------------------------------------
async function loadDashboardData() {
    try {
        const [halls, students, sessions] = await Promise.all([
            fetchApi('/halls'),
            fetchApi('/students'),
            fetchApi('/sessions')
        ]);

        allHalls = halls || [];
        allStudents = students || [];
        allSessions = sessions || [];

        // Update Stat Cards
        document.getElementById('statHallsCount').textContent = allHalls.length;
        document.getElementById('statStudentsCount').textContent = allStudents.length;
        document.getElementById('statSessionsCount').textContent = allSessions.length;

        let totalAllocated = 0;
        allSessions.forEach(s => totalAllocated += (s.allocatedCount || 0));
        document.getElementById('statAllocatedCount').textContent = totalAllocated;

        // Render Sessions in Dashboard
        const list = document.getElementById('dashboardSessionsList');
        if (allSessions.length === 0) {
            list.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No sessions scheduled yet.</td></tr>';
            return;
        }

        list.innerHTML = allSessions.map(s => `
            <tr>
                <td><strong>${escapeHtml(s.sessionName)}</strong><br><small style="color: var(--text-muted);">${escapeHtml(s.courseCode || 'N/A')}</small></td>
                <td>${s.examDate || '-'}</td>
                <td><span class="badge badge-neutral">${s.hallsCount} Halls</span></td>
                <td>${s.totalDeclaredCapacity} Seats</td>
                <td>
                    <span class="badge ${s.status === 'ALLOCATED' ? 'badge-success' : 'badge-warning'}">
                        ${s.status}
                    </span>
                </td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="viewSessionChartFromDashboard(${s.id})">Chart 🖨️</button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        console.error('Failed to load dashboard data:', err);
    }
}

function viewSessionChartFromDashboard(sessionId) {
    switchTab('chart');
    setTimeout(() => {
        const select = document.getElementById('chartSessionSelect');
        if (select) {
            select.value = sessionId;
            loadSessionHallsForChart();
        }
    }, 150);
}

// -------------------------------------------------------------
// HALL MANAGEMENT
// -------------------------------------------------------------
function updateHallCapacityHint() {
    const rows = parseInt(document.getElementById('hallRows').value) || 0;
    const cols = parseInt(document.getElementById('hallCols').value) || 0;
    const physical = rows * cols;
    const hint = document.getElementById('hallCapacityHint');
    const capInput = document.getElementById('hallCapacity');

    hint.textContent = `Physical Seats: ${physical} (${rows} rows × ${cols} cols). Declared capacity cannot exceed ${physical}.`;
    if (parseInt(capInput.value) > physical || !capInput.value) {
        capInput.value = physical;
    }
}

async function loadHalls() {
    try {
        allHalls = await fetchApi('/halls');
        const list = document.getElementById('hallsList');
        document.getElementById('hallsBadgeCount').textContent = `${allHalls.length} Halls`;

        if (!allHalls || allHalls.length === 0) {
            list.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No halls registered yet.</td></tr>';
            return;
        }

        list.innerHTML = allHalls.map(h => `
            <tr>
                <td><strong>${escapeHtml(h.name)}</strong></td>
                <td>${h.totalRows} × ${h.totalColumns}</td>
                <td>${h.totalSeats}</td>
                <td><span class="badge badge-primary">${h.capacity} Students</span></td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="handleDeleteHall(${h.id}, '${escapeHtml(h.name)}')">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Failed to load halls:', err);
    }
}

async function handleCreateHall(e) {
    e.preventDefault();
    const name = document.getElementById('hallName').value.trim();
    const totalRows = parseInt(document.getElementById('hallRows').value);
    const totalColumns = parseInt(document.getElementById('hallCols').value);
    const capacity = parseInt(document.getElementById('hallCapacity').value);

    try {
        await fetchApi('/halls', {
            method: 'POST',
            body: JSON.stringify({ name, totalRows, totalColumns, capacity })
        });
        showToast('Success', `Hall '${name}' registered successfully!`, 'success');
        document.getElementById('hallForm').reset();
        document.getElementById('hallRows').value = 5;
        document.getElementById('hallCols').value = 6;
        document.getElementById('hallCapacity').value = 28;
        updateHallCapacityHint();
        loadHalls();
        loadDashboardData();
    } catch (err) {
        // Handled by fetchApi toast
    }
}

async function handleDeleteHall(id, name) {
    if (!confirm(`Are you sure you want to delete exam hall '${name}'?`)) return;
    try {
        await fetchApi(`/halls/${id}`, { method: 'DELETE' });
        showToast('Deleted', `Hall '${name}' was deleted.`, 'warning');
        loadHalls();
        loadDashboardData();
    } catch (err) {}
}

// -------------------------------------------------------------
// STUDENT MANAGEMENT
// -------------------------------------------------------------
async function loadStudents() {
    try {
        allStudents = await fetchApi('/students');
        renderStudentsTable(allStudents);
    } catch (err) {
        console.error('Failed to load students:', err);
    }
}

function renderStudentsTable(students) {
    const list = document.getElementById('studentsList');
    document.getElementById('studentsBadgeCount').textContent = `${students.length} Students`;

    if (!students || students.length === 0) {
        list.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No students registered yet.</td></tr>';
        return;
    }

    list.innerHTML = students.map(s => `
        <tr>
            <td><strong style="color: #fff; font-family: 'JetBrains Mono', monospace;">${escapeHtml(s.rollNumber)}</strong></td>
            <td>${escapeHtml(s.name)}</td>
            <td>
                <span class="badge ${getSectionBadgeClass(s.sectionKey)}">
                    ${escapeHtml(s.className)}-${escapeHtml(s.section)}
                </span>
            </td>
            <td>${escapeHtml(s.department || '-')}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="quickLookupStudent('${escapeHtml(s.rollNumber)}')">Lookup 🔍</button>
                <button class="btn btn-danger btn-sm" onclick="handleDeleteStudent(${s.id}, '${escapeHtml(s.name)}')">Delete</button>
            </td>
        </tr>
    `).join('');
}

function handleStudentSearch() {
    const query = document.getElementById('studentSearchInput').value.toLowerCase().trim();
    if (!query) {
        renderStudentsTable(allStudents);
        return;
    }
    const filtered = allStudents.filter(s =>
        s.rollNumber.toLowerCase().includes(query) ||
        s.name.toLowerCase().includes(query) ||
        s.className.toLowerCase().includes(query) ||
        s.section.toLowerCase().includes(query) ||
        (s.department && s.department.toLowerCase().includes(query))
    );
    renderStudentsTable(filtered);
}

async function handleCreateStudent(e) {
    e.preventDefault();
    const rollNumber = document.getElementById('studentRoll').value.trim();
    const name = document.getElementById('studentName').value.trim();
    const department = document.getElementById('studentDept').value.trim();
    const className = document.getElementById('studentClass').value.trim();
    const section = document.getElementById('studentSec').value.trim();

    try {
        await fetchApi('/students', {
            method: 'POST',
            body: JSON.stringify({ rollNumber, name, department, className, section })
        });
        showToast('Success', `Student ${name} (${rollNumber}) registered!`, 'success');
        document.getElementById('studentForm').reset();
        loadStudents();
        loadDashboardData();
    } catch (err) {}
}

async function generateSampleStudents() {
    const sections = [
        { className: 'CSE', section: 'A', dept: 'Computer Science' },
        { className: 'CSE', section: 'B', dept: 'Computer Science' },
        { className: 'ECE', section: 'A', dept: 'Electronics' },
        { className: 'MECH', section: 'A', dept: 'Mechanical' }
    ];

    const count = 10;
    const batch = [];
    const timestamp = Date.now().toString().slice(-4);

    for (let i = 1; i <= count; i++) {
        const secInfo = sections[i % sections.length];
        const roll = `SAMPLE-${secInfo.className}-${timestamp}${i}`;
        batch.push({
            rollNumber: roll,
            name: `Examinee ${roll}`,
            department: secInfo.dept,
            className: secInfo.className,
            section: secInfo.section
        });
    }

    try {
        await fetchApi('/students/bulk', {
            method: 'POST',
            body: JSON.stringify({ students: batch })
        });
        showToast('Success', `Added 10 sample students across ${sections.length} classes/sections.`, 'success');
        loadStudents();
        loadDashboardData();
    } catch (err) {}
}

async function handleDeleteStudent(id, name) {
    if (!confirm(`Delete student record for '${name}'?`)) return;
    try {
        await fetchApi(`/students/${id}`, { method: 'DELETE' });
        showToast('Deleted', `Student '${name}' was deleted.`, 'warning');
        loadStudents();
        loadDashboardData();
    } catch (err) {}
}

function quickLookupStudent(rollNumber) {
    switchTab('lookup');
    setTimeout(() => {
        document.getElementById('lookupRollInput').value = rollNumber;
        document.getElementById('lookupForm').dispatchEvent(new Event('submit'));
    }, 150);
}

// -------------------------------------------------------------
// EXAM SESSIONS & AUTO-ALLOCATOR
// -------------------------------------------------------------
async function loadSessions() {
    try {
        allSessions = await fetchApi('/sessions');
        document.getElementById('sessionsBadgeCount').textContent = `${allSessions.length} Sessions`;
        const list = document.getElementById('sessionsList');

        if (!allSessions || allSessions.length === 0) {
            list.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No exam sessions scheduled.</td></tr>';
            return;
        }

        list.innerHTML = allSessions.map(s => `
            <tr>
                <td><strong>${escapeHtml(s.sessionName)}</strong><br><small style="color: var(--text-muted);">${escapeHtml(s.courseCode || '')}</small></td>
                <td>${s.examDate || '-'}</td>
                <td><span class="badge badge-neutral">${s.hallsCount} Halls</span></td>
                <td>${s.totalDeclaredCapacity} Seats</td>
                <td><strong style="color: var(--primary-light);">${s.allocatedCount}</strong></td>
                <td>
                    <span class="badge ${s.status === 'ALLOCATED' ? 'badge-success' : 'badge-warning'}">
                        ${s.status}
                    </span>
                </td>
                <td>
                    <button class="btn btn-primary btn-sm" onclick="quickAllocateSession(${s.id})">⚡ Allocate</button>
                    <button class="btn btn-secondary btn-sm" onclick="viewSessionChartFromDashboard(${s.id})">Chart</button>
                    <button class="btn btn-danger btn-sm" onclick="handleDeleteSession(${s.id}, '${escapeHtml(s.sessionName)}')">Delete</button>
                </td>
            </tr>
        `).join('');

        populateSessionSelects();
    } catch (err) {
        console.error('Failed to load sessions:', err);
    }
}

function populateSessionSelects() {
    const autoSelect = document.getElementById('autoAllocateSessionSelect');
    if (!autoSelect) return;

    if (allSessions.length === 0) {
        autoSelect.innerHTML = '<option value="">No sessions available</option>';
        return;
    }

    autoSelect.innerHTML = allSessions.map(s => `
        <option value="${s.id}">${escapeHtml(s.sessionName)} (${s.examDate || ''})</option>
    `).join('');
}

async function handleCreateSession(e) {
    e.preventDefault();
    const sessionName = document.getElementById('sessionName').value.trim();
    const courseCode = document.getElementById('courseCode').value.trim();
    const examDate = document.getElementById('sessionDate').value;
    const startTime = document.getElementById('sessionStart').value.trim();
    const endTime = document.getElementById('sessionEnd').value.trim();

    // Default to assigning all existing halls to this session
    const hallIds = allHalls.map(h => h.id);

    try {
        await fetchApi('/sessions', {
            method: 'POST',
            body: JSON.stringify({ sessionName, courseCode, examDate, startTime, endTime, hallIds })
        });
        showToast('Success', `Exam Session '${sessionName}' created!`, 'success');
        document.getElementById('sessionForm').reset();
        loadSessions();
        loadDashboardData();
    } catch (err) {}
}

async function quickAllocateSession(sessionId) {
    document.getElementById('autoAllocateSessionSelect').value = sessionId;
    await runAutoAllocation();
}

async function runAutoAllocation() {
    const sessionSelect = document.getElementById('autoAllocateSessionSelect');
    const sessionId = sessionSelect.value;

    if (!sessionId) {
        showToast('Warning', 'Please select an exam session first.', 'warning');
        return;
    }

    try {
        const response = await fetchApi('/allocations/auto-allocate', {
            method: 'POST',
            body: JSON.stringify({ examSessionId: parseInt(sessionId) })
        });

        showToast('Allocation Complete', response.message, 'success');

        // Display results banner
        const banner = document.getElementById('allocationResultBanner');
        const title = document.getElementById('allocationResultTitle');
        const message = document.getElementById('allocationResultMessage');
        const breakdown = document.getElementById('allocationHallBreakdown');

        title.textContent = `⚡ Allocation Succeeded (${response.totalAllocated} Students Seated)`;
        message.textContent = `${response.message} Unavoidable adjacent conflicts: ${response.unavoidableConflictsCount}.`;

        if (response.hallSummary && response.hallSummary.length > 0) {
            breakdown.innerHTML = response.hallSummary.map(item => `<li>${escapeHtml(item)}</li>`).join('');
        } else {
            breakdown.innerHTML = '';
        }

        banner.style.display = 'block';

        loadSessions();
        loadDashboardData();
    } catch (err) {
        // Handled by fetchApi error toast
    }
}

async function handleDeleteSession(id, name) {
    if (!confirm(`Delete exam session '${name}' and all its allocations?`)) return;
    try {
        await fetchApi(`/sessions/${id}`, { method: 'DELETE' });
        showToast('Deleted', `Session '${name}' was deleted.`, 'warning');
        loadSessions();
        loadDashboardData();
    } catch (err) {}
}

// -------------------------------------------------------------
// SEATING CHART & PRINT PREVIEW
// -------------------------------------------------------------
async function populateChartSessionSelect() {
    const sessionSelect = document.getElementById('chartSessionSelect');
    if (!sessionSelect) return;

    if (allSessions.length === 0) {
        allSessions = await fetchApi('/sessions');
    }

    if (allSessions.length === 0) {
        sessionSelect.innerHTML = '<option value="">No sessions available</option>';
        return;
    }

    sessionSelect.innerHTML = allSessions.map(s => `
        <option value="${s.id}">${escapeHtml(s.sessionName)}</option>
    `).join('');

    loadSessionHallsForChart();
}

async function loadSessionHallsForChart() {
    const sessionId = document.getElementById('chartSessionSelect').value;
    const hallSelect = document.getElementById('chartHallSelect');
    if (!sessionId || !hallSelect) return;

    try {
        const session = await fetchApi(`/sessions/${sessionId}`);
        if (!session.halls || session.halls.length === 0) {
            hallSelect.innerHTML = '<option value="">No halls assigned to session</option>';
            document.getElementById('seatingGridContainer').innerHTML = '<p style="color: var(--text-muted); padding: 2rem;">No halls assigned to this session.</p>';
            return;
        }

        hallSelect.innerHTML = session.halls.map(h => `
            <option value="${h.id}">${escapeHtml(h.name)} (${h.capacity} seats)</option>
        `).join('');

        renderSeatingChart();
    } catch (err) {
        console.error('Failed to load session halls for chart:', err);
    }
}

async function renderSeatingChart() {
    const sessionId = document.getElementById('chartSessionSelect').value;
    const hallId = document.getElementById('chartHallSelect').value;
    const container = document.getElementById('seatingGridContainer');

    if (!sessionId || !hallId) {
        container.innerHTML = '<p style="color: var(--text-muted); padding: 2rem;">Please select a session and hall above.</p>';
        return;
    }

    try {
        const chart = await fetchApi(`/allocations/sessions/${sessionId}/halls/${hallId}/chart`);
        currentChartData = chart;

        // Update headers
        document.getElementById('chartHallTitle').textContent = `${chart.hallName} — Seating Layout`;
        document.getElementById('chartHallMeta').textContent = `Session: ${chart.sessionName} | Exam Date: ${chart.examDate || '-'} | Time: ${chart.startTime || '-'} to ${chart.endTime || '-'}`;
        document.getElementById('chartTotalSeatedBadge').textContent = `${chart.allocatedCount} Seated`;
        document.getElementById('chartCapacityBadge').textContent = `Capacity: ${chart.capacity}`;

        const conflictBadge = document.getElementById('chartConflictStatusBadge');
        if (chart.unavoidableConflictsCount > 0) {
            conflictBadge.className = 'badge badge-warning';
            conflictBadge.textContent = `⚠️ ${chart.unavoidableConflictsCount} Unavoidable Conflicts`;
        } else {
            conflictBadge.className = 'badge badge-success';
            conflictBadge.textContent = `✓ 0 Adjacent Conflicts`;
        }

        // Render 2D grid
        let html = '';
        if (chart.grid && chart.grid.length > 0) {
            chart.grid.forEach((row, rowIdx) => {
                html += `<div class="seating-row">`;
                html += `<div class="row-label">Row ${rowIdx + 1}</div>`;

                row.forEach(cell => {
                    let cellClass = 'seat-cell';
                    if (!cell.occupied) {
                        cellClass += ' vacant';
                    } else {
                        cellClass += ' occupied';
                        if (cell.sectionKey) {
                            const normalized = cell.sectionKey.replace(/[^a-zA-Z0-9]/g, '-');
                            cellClass += ` section-${normalized}`;
                        }
                        if (cell.hasAdjacentConflict) {
                            cellClass += ' conflict';
                        }
                    }

                    html += `
                        <div class="${cellClass}" id="seat-${cell.rowNumber}-${cell.colNumber}" onclick='openSeatModal(${JSON.stringify(cell)})'>
                            <div class="seat-code-tag">
                                <span>${escapeHtml(cell.seatCode)}</span>
                                ${cell.occupied ? '<span>👤</span>' : '<span>⚪</span>'}
                            </div>
                            ${cell.occupied ? `
                                <div class="seat-student-roll" title="${escapeHtml(cell.studentRollNumber)}">${escapeHtml(cell.studentRollNumber)}</div>
                                <div class="seat-student-name" title="${escapeHtml(cell.studentName)}">${escapeHtml(cell.studentName)}</div>
                                <div class="seat-class-badge">${escapeHtml(cell.sectionKey)}</div>
                            ` : `
                                <div style="color: var(--text-muted); font-size: 0.72rem; text-align: center; margin: auto;">VACANT</div>
                            `}
                        </div>
                    `;
                });

                html += `</div>`;
            });
        }

        container.innerHTML = html;
    } catch (err) {
        container.innerHTML = `<p style="color: var(--danger); padding: 2rem;">Failed to load seating chart: ${escapeHtml(err.message)}</p>`;
    }
}

function openPrintableView() {
    const sessionId = document.getElementById('chartSessionSelect').value;
    const hallId = document.getElementById('chartHallSelect').value;
    if (!sessionId || !hallId) {
        showToast('Warning', 'Select session and hall to print.', 'warning');
        return;
    }
    window.open(`/api/allocations/sessions/${sessionId}/halls/${hallId}/chart/print`, '_blank');
}

// Seat Modal
function openSeatModal(cell) {
    const modal = document.getElementById('seatModal');
    const title = document.getElementById('seatModalTitle');
    const body = document.getElementById('seatModalBody');

    title.textContent = `Seat Details — ${cell.seatCode}`;

    if (!cell.occupied) {
        body.innerHTML = `
            <div style="text-align: center; padding: 1.5rem;">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🪑</div>
                <h4 style="color: var(--text-secondary);">Vacant Seat</h4>
                <p style="color: var(--text-muted); font-size: 0.88rem; margin-top: 0.25rem;">
                    Row ${cell.rowNumber}, Column ${cell.colNumber} is currently unoccupied.
                </p>
            </div>
        `;
    } else {
        body.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span class="badge ${getSectionBadgeClass(cell.sectionKey)}" style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
                        ${escapeHtml(cell.sectionKey)}
                    </span>
                    <span class="badge ${cell.hasAdjacentConflict ? 'badge-danger' : 'badge-success'}">
                        ${cell.hasAdjacentConflict ? 'Adjacent Conflict' : 'Non-Adjacent Verified'}
                    </span>
                </div>
                <div class="stat-card" style="background: rgba(11, 15, 25, 0.8);">
                    <div class="stat-icon students">🎓</div>
                    <div class="stat-info">
                        <div class="stat-value" style="font-size: 1.35rem;">${escapeHtml(cell.studentName)}</div>
                        <div class="stat-label">Roll Number: <strong style="color: var(--accent);">${escapeHtml(cell.studentRollNumber)}</strong></div>
                    </div>
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; font-size: 0.88rem;">
                    <div><span style="color: var(--text-muted);">Department:</span><br><strong>${escapeHtml(cell.department || 'N/A')}</strong></div>
                    <div><span style="color: var(--text-muted);">Class & Section:</span><br><strong>${escapeHtml(cell.className)} - ${escapeHtml(cell.section)}</strong></div>
                    <div><span style="color: var(--text-muted);">Row Number:</span><br><strong>Row ${cell.rowNumber}</strong></div>
                    <div><span style="color: var(--text-muted);">Column Number:</span><br><strong>Column ${cell.colNumber}</strong></div>
                </div>
            </div>
        `;
    }

    modal.classList.add('active');
}

function closeSeatModal() {
    const modal = document.getElementById('seatModal');
    if (modal) modal.classList.remove('active');
}

// -------------------------------------------------------------
// STUDENT LOOKUP PORTAL
// -------------------------------------------------------------
async function handleStudentLookup(e) {
    e.preventDefault();
    const roll = document.getElementById('lookupRollInput').value.trim();
    const container = document.getElementById('lookupResultContainer');

    if (!roll) return;

    try {
        const result = await fetchApi(`/allocations/student/${encodeURIComponent(roll)}`);

        if (!result.allocations || result.allocations.length === 0) {
            container.innerHTML = `
                <div class="lookup-result-box" style="border-color: var(--warning);">
                    <h3 style="color: var(--warning); margin-bottom: 0.5rem;">⚠️ No Active Allocations</h3>
                    <p style="color: var(--text-secondary);">Student <strong>${escapeHtml(result.studentName)} (${escapeHtml(result.rollNumber)})</strong> is registered, but no exam seating has been generated yet.</p>
                </div>
            `;
            container.style.display = 'block';
            return;
        }

        let allocHtml = result.allocations.map(a => `
            <div style="border-top: 1px solid var(--border-color); padding-top: 1.25rem; margin-top: 1.25rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                    <div>
                        <h4 style="font-size: 1.15rem; color: #fff;">${escapeHtml(a.sessionName)}</h4>
                        <div style="color: var(--text-secondary); font-size: 0.82rem; margin-top: 2px;">
                            Exam Date: <strong>${a.examDate || '-'}</strong> | Time: <strong>${a.startTime || '-'} - ${a.endTime || '-'}</strong>
                        </div>
                    </div>
                    <div class="seat-highlight-badge">${escapeHtml(a.seatCode)}</div>
                </div>

                <div class="lookup-meta-grid">
                    <div>
                        <div class="lookup-item-label">Exam Hall</div>
                        <div class="lookup-item-value" style="color: var(--accent);">${escapeHtml(a.hallName)}</div>
                    </div>
                    <div>
                        <div class="lookup-item-label">Row Number</div>
                        <div class="lookup-item-value">Row ${a.rowNumber}</div>
                    </div>
                    <div>
                        <div class="lookup-item-label">Column / Desk</div>
                        <div class="lookup-item-value">Column ${a.colNumber}</div>
                    </div>
                    <div>
                        <div class="lookup-item-label">Seat Identifier</div>
                        <div class="lookup-item-value" style="color: #f59e0b;">${escapeHtml(a.seatCode)}</div>
                    </div>
                </div>
            </div>
        `).join('');

        container.innerHTML = `
            <div class="lookup-result-box">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap;">
                    <div>
                        <span class="badge ${getSectionBadgeClass(result.className + '-' + result.section)}">
                            ${escapeHtml(result.className)} - ${escapeHtml(result.section)}
                        </span>
                        <h3 style="font-size: 1.6rem; color: #fff; margin-top: 0.4rem;">${escapeHtml(result.studentName)}</h3>
                        <div style="color: var(--accent); font-family: 'JetBrains Mono', monospace; font-size: 0.95rem;">
                            Roll Number: <strong>${escapeHtml(result.rollNumber)}</strong>
                        </div>
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); text-align: right;">
                        Department: ${escapeHtml(result.department || 'N/A')}
                    </div>
                </div>
                ${allocHtml}
            </div>
        `;
        container.style.display = 'block';

    } catch (err) {
        container.innerHTML = `
            <div class="lookup-result-box" style="border-color: var(--danger);">
                <h3 style="color: var(--danger); margin-bottom: 0.5rem;">❌ Student Not Found</h3>
                <p style="color: var(--text-secondary);">No student found with roll number '<strong>${escapeHtml(roll)}</strong>'. Please check the roll number and try again.</p>
            </div>
        `;
        container.style.display = 'block';
    }
}

// -------------------------------------------------------------
// NOTIFICATION & AUDIT LOGS
// -------------------------------------------------------------
async function loadNotifications() {
    try {
        const notifications = await fetchApi('/notifications');
        const badge = document.getElementById('notificationBadgeCount');
        if (badge) badge.textContent = notifications ? notifications.length : 0;

        const list = document.getElementById('notificationsList');
        if (!list) return;

        if (!notifications || notifications.length === 0) {
            list.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No notification logs found.</td></tr>';
            return;
        }

        list.innerHTML = notifications.map(n => {
            let badgeClass = 'badge-neutral';
            if (n.severity === 'SUCCESS') badgeClass = 'badge-success';
            if (n.severity === 'WARNING') badgeClass = 'badge-warning';
            if (n.severity === 'ERROR') badgeClass = 'badge-danger';
            if (n.severity === 'INFO') badgeClass = 'badge-primary';

            const timeStr = n.timestamp ? new Date(n.timestamp).toLocaleTimeString() : '-';

            return `
                <tr>
                    <td style="font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: var(--text-muted);">${timeStr}</td>
                    <td><span class="badge ${badgeClass}">${escapeHtml(n.severity)}</span></td>
                    <td><strong style="color: #cbd5e1; font-size: 0.82rem;">${escapeHtml(n.eventType)}</strong></td>
                    <td><strong>${escapeHtml(n.title)}</strong></td>
                    <td style="color: var(--text-secondary);">${escapeHtml(n.message)}</td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load notifications:', err);
    }
}

// Helpers
function getSectionBadgeClass(sectionKey) {
    if (!sectionKey) return 'badge-neutral';
    if (sectionKey.includes('CSE-A')) return 'badge-primary';
    if (sectionKey.includes('CSE-B')) return 'badge-accent';
    if (sectionKey.includes('ECE')) return 'badge-warning';
    if (sectionKey.includes('MECH')) return 'badge-danger';
    return 'badge-primary';
}

function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
