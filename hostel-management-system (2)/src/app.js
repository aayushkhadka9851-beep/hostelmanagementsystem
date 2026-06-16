/**
 * Hostel Management System Master Client Controller
 * Built using Vanilla ES6 JavaScript for clean structural performance
 */

// Global State
let authState = {
  token: localStorage.getItem("hostel_token") || null,
  user: JSON.parse(localStorage.getItem("hostel_user")) || null
};

// State caches for dynamically populating selects
let roomTypesCache = [];
let roomsCache = [];
let studentsCache = [];

// API Helper - Includes Authorizations
async function apiFetch(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  
  if (authState.token) {
    headers["Authorization"] = `Bearer ${authState.token}`;
  }

  const response = await fetch(url, { ...options, headers });
  
  if (response.status === 401) {
    // Session token expired or invalid, drop to login
    handleLogout();
    throw new Error("Unauthorized access. Access Token expired.");
  }

  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.error || `HTTP error! Status: ${response.status}`);
  }

  return response.json();
}

// 1. SESSION MANAGEMENT & SCREEN DECORATIONS
function updateAuthUI() {
  const loginSection = document.getElementById("login-container");
  const appSection = document.getElementById("app-container");

  if (authState.token) {
    loginSection.classList.add("d-none");
    appSection.classList.remove("d-none");
    
    // Default load - dashboard screen
    navigateView("dashboard");
  } else {
    appSection.classList.add("d-none");
    loginSection.classList.remove("d-none");
    document.getElementById("auth-error").classList.add("d-none");
  }
}

// Handle Session login
async function handleLogin(e) {
  e.preventDefault();
  const username = e.target.elements["login-username"].value;
  const password = e.target.elements["login-password"].value;
  const errorAlert = document.getElementById("auth-error");

  try {
    const data = await apiFetch("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });

    if (data.success) {
      authState.token = data.token;
      authState.user = data.user;
      localStorage.setItem("hostel_token", data.token);
      localStorage.setItem("hostel_user", JSON.stringify(data.user));
      updateAuthUI();
    }
  } catch (error) {
    console.error("Auth Fail:", error);
    errorAlert.textContent = error.message || "Invalid credentials.";
    errorAlert.classList.remove("d-none");
  }
}

// Clear Session
function handleLogout() {
  localStorage.removeItem("hostel_token");
  localStorage.removeItem("hostel_user");
  authState.token = null;
  authState.user = null;
  updateAuthUI();
}

// Custom View Routing
function navigateView(target) {
  // Hide all screens
  document.querySelectorAll(".screen-view").forEach(screen => {
    screen.classList.add("d-none");
  });

  // Deactivate all sidebar items
  document.querySelectorAll("#sidebar-nav button").forEach(btn => {
    btn.classList.remove("active");
  });

  // Activate matching screen & button
  const matchingScreen = document.getElementById(`view-${target}`);
  if (matchingScreen) {
    matchingScreen.classList.remove("d-none");
  }
  
  const matchingBtn = document.querySelector(`#sidebar-nav button[data-target="${target}"]`);
  if (matchingBtn) {
    matchingBtn.classList.add("active");
  }

  // Set navbar title based on navigation target
  const titleMap = {
    dashboard: "Operations Dashboard Controls",
    students: "Admitted Students Registry",
    rooms: "Manage Room Inventory Units",
    payments: "Track Outstanding Outstanding Revenue",
    settings: "System Configuration Preferences"
  };
  document.getElementById("header-title").textContent = titleMap[target] || "Manage Portal";

  // Invoke view specific loader hooks
  switch (target) {
    case "dashboard":
      loadDashboardStats();
      break;
    case "students":
      loadStudentsView();
      break;
    case "rooms":
      loadRoomsView();
      break;
    case "payments":
      loadPaymentsView();
      break;
    case "settings":
      loadSettingsView();
      break;
  }
}

// UTC live clock emulator
function startUTCClock() {
  const clockEl = document.getElementById("clock-display");
  function tick() {
    const now = new Date();
    const utcHours = String(now.getUTCHours()).padStart(2, "0");
    const utcMinutes = String(now.getUTCMinutes()).padStart(2, "0");
    const utcSeconds = String(now.getUTCSeconds()).padStart(2, "0");
    clockEl.textContent = `UTC ${utcHours}:${utcMinutes}:${utcSeconds}`;
  }
  setInterval(tick, 1000);
  tick();
}


// 2. DASHBOARD VIEW CONTROLLER
async function loadDashboardStats() {
  try {
    const stats = await apiFetch("/api/dashboard/stats");
    
    // Paint simple counts
    document.getElementById("stat-students").textContent = stats.totalStudents;
    document.getElementById("stat-rooms").textContent = stats.totalRooms;
    document.getElementById("stat-occupied-rooms").textContent = stats.occupiedRooms;
    document.getElementById("stat-available-rooms").textContent = stats.availableRooms;
    document.getElementById("stat-pending").textContent = stats.pendingPaymentsCount;
    document.getElementById("stat-revenue").textContent = `$${stats.monthlyRevenue}`;

    // Bed Distribution percentage
    const bd = stats.bedDistribution;
    const bedPct = bd.totalBeds > 0 ? Math.round((bd.occupiedBeds / bd.totalBeds) * 100) : 0;
    
    document.getElementById("cap-beds-pct").textContent = `${bedPct}%`;
    document.getElementById("cap-occupied-beds").textContent = bd.occupiedBeds;
    document.getElementById("cap-total-beds").textContent = bd.totalBeds;
    document.getElementById("dashboard-progress-beds").style.width = `${bedPct}%`;

    // Render Timeline activity registry
    const timelineContainer = document.getElementById("activity-timeline");
    timelineContainer.innerHTML = "";
    
    if (stats.recentActivities.length === 0) {
      timelineContainer.innerHTML = '<div class="text-center text-slate-400 py-3 fs-8">No logged activities.</div>';
    } else {
      stats.recentActivities.forEach(act => {
        const item = document.createElement("div");
        item.className = "timeline-item text-slate-700 fs-8";
        
        // Define color category
        let color = "bg-blue-600";
        if (act.category === "room") color = "bg-teal-500";
        if (act.category === "payment") color = "bg-emerald-500";
        if (act.category === "settings") color = "bg-amber-500";

        item.innerHTML = `
          <span class="timeline-badge ${color}"></span>
          <div class="d-flex justify-content-between">
            <span class="fw-semibold text-slate-800">${act.action}</span>
            <span class="text-slate-400 font-mono text-end" style="font-size: 0.725rem">${act.time}</span>
          </div>
        `;
        timelineContainer.appendChild(item);
      });
    }

    // Paint Occupancy details Table
    const tableBody = document.getElementById("dashboard-occupancy-table").querySelector("tbody");
    tableBody.innerHTML = "";

    stats.roomsOccupancy.forEach(room => {
      const tr = document.createElement("tr");
      
      const occPct = room.capacity > 0 ? Math.round((room.occupied / room.capacity) * 100) : 0;
      let badgeClass = "badge-available";
      if (room.status === "Full") badgeClass = "badge-full";
      if (room.status === "Maintenance") badgeClass = "badge-maintenance";

      tr.innerHTML = `
        <td class="font-mono fw-bold text-slate-800">${room.room_number}</td>
        <td>
          <div class="d-flex align-items-center gap-2">
            <div class="progress rounded-pill bg-slate-100 flex-grow-1" style="height: 6px; width: 80px;">
              <div class="progress-bar rounded-pill bg-[#2563eb]" style="width: ${occPct}%"></div>
            </div>
            <span class="text-slate-500 text-end font-mono" style="min-width: 60px;">${room.occupied}/${room.capacity} beds</span>
          </div>
        </td>
        <td><span class="badge ${badgeClass === "badge-full" ? "badge bg-danger/10 text-danger" : badgeClass === "badge-maintenance" ? "badge bg-secondary/10 text-secondary" : "badge bg-success/10 text-success"} fs-9.5">${room.status}</span></td>
        <td><span class="text-slate-800 fw-bold font-mono">${occPct}%</span></td>
      `;
      tableBody.appendChild(tr);
    });

  } catch (error) {
    console.error("Failed loading stats:", error);
  }
}


// 3. STUDENT SECTION VIEWS
async function loadStudentsView(q = "") {
  try {
    const students = await apiFetch(`/api/students?q=${encodeURIComponent(q)}`);
    studentsCache = students; // local cache
    
    const tableBody = document.getElementById("students-table").querySelector("tbody");
    tableBody.innerHTML = "";

    if (students.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-slate-400 py-4 fs-7.5">No students found matching your parameters.</td></tr>`;
      return;
    }

    students.forEach(s => {
      const tr = document.createElement("tr");
      tr.id = `row-student-${s.student_id}`;
      
      const badgeClass = s.status === "Active" ? "badge bg-success/10 text-success" : "badge bg-secondary/10 text-secondary";
      
      tr.innerHTML = `
        <td class="font-mono text-slate-400">${s.student_id}</td>
        <td>
          <span class="fw-semibold text-slate-800 d-block">${s.student_name}</span>
          <span class="text-slate-400 fs-8 d-block font-mono">Mail: ${s.email}</span>
        </td>
        <td>
          <span class="text-slate-700 font-mono d-block">${s.contact}</span>
          <span class="text-slate-400 fs-8 d-block font-mono">Emergency: ${s.emergency_contact}</span>
        </td>
        <td>
          <span class="badge bg-blue-50 text-blue-700 fs-8.5 font-mono fw-bold border border-blue-100 px-2.5 py-1">Room ${s.room_number}</span>
        </td>
        <td class="font-mono text-slate-600">${s.admission_date}</td>
        <td><span class="${badgeClass} fs-9.5">${s.status}</span></td>
        <td class="text-end">
          <div class="d-inline-flex gap-1.5">
            <button class="btn btn-outline-secondary btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="viewStudentDetails(${s.student_id})" title="View Profile">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
            <button class="btn btn-outline-primary btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="editStudentPrep(${s.student_id})" title="Edit Details">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
            </button>
            <button class="btn btn-outline-danger btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="deleteStudent(${s.student_id})" title="Remove Student">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            </button>
          </div>
        </td>
      `;
      tableBody.appendChild(tr);
    });

  } catch (error) {
    console.error("Failed load students:", error);
  }
}

// Read dropdown list rooms cache helper
async function refreshRoomsDropdown(selectElementId, activeRoomIdToKeep = null) {
  try {
    const list = await apiFetch("/api/rooms");
    roomsCache = list;

    const select = document.getElementById(selectElementId);
    select.innerHTML = '<option value="" disabled selected>Select Room Unit</option>';

    list.forEach(r => {
      // Allow if there are spare beds, or if it is already assigned to this edited student
      const availableBeds = r.capacity - r.occupied_beds;
      const isMaint = r.status === "Maintenance";

      if ((availableBeds > 0 && !isMaint) || r.room_id === activeRoomIdToKeep) {
        const option = document.createElement("option");
        option.value = r.room_id;
        option.textContent = `Room ${r.room_number} (${r.room_type} — rent $${r.monthly_rent}/mo) [${availableBeds} beds available]`;
        if (r.room_id === activeRoomIdToKeep) {
          option.textContent = `Room ${r.room_number} (${r.room_type} — rent $${r.monthly_rent}/mo) [CURRENT ASSIGNMENT]`;
        }
        select.appendChild(option);
      }
    });
  } catch (error) {
    console.error("Option loading error:", error);
  }
}

// Open modal and pre-register
window.prepAddStudentModal = async function() {
  await refreshRoomsDropdown("add-student-room");
  document.getElementById("add-student-date").value = new Date().toISOString().split("T")[0];
};

// Form Post registered Student
async function handleAddStudentSubmit(e) {
  e.preventDefault();
  const bodyData = {
    student_name: document.getElementById("add-student-name").value,
    email: document.getElementById("add-student-email").value,
    contact: document.getElementById("add-student-contact").value,
    emergency_contact: document.getElementById("add-student-emergency").value,
    address: document.getElementById("add-student-address").value,
    room_id: document.getElementById("add-student-room").value,
    admission_date: document.getElementById("add-student-date").value,
    status: document.getElementById("add-student-status").value
  };

  try {
    await apiFetch("/api/students", {
      method: "POST",
      body: JSON.stringify(bodyData)
    });

    // Close Modal trigger
    const modalEl = document.getElementById("modal-add-student");
    const modalInst = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
    modalInst.hide();
    
    // Clear Form reset
    document.getElementById("form-add-student").reset();
    
    // Reload parameters
    loadStudentsView();
  } catch (error) {
    alert(error.message);
  }
}

// Preparations for Student Edit Forms
window.editStudentPrep = async function(id) {
  try {
    const s = await apiFetch(`/api/students/${id}`);
    
    document.getElementById("edit-student-id").value = s.student_id;
    document.getElementById("edit-student-name").value = s.student_name;
    document.getElementById("edit-student-email").value = s.email;
    document.getElementById("edit-student-contact").value = s.contact;
    document.getElementById("edit-student-emergency").value = s.emergency_contact;
    document.getElementById("edit-student-address").value = s.address;
    document.getElementById("edit-student-date").value = s.admission_date;
    document.getElementById("edit-student-status").value = s.status;

    // Load available rooms cache
    await refreshRoomsDropdown("edit-student-room", s.room_id);
    document.getElementById("edit-student-room").value = s.room_id || "";

    // Open Modal
    const modal = new bootstrap.Modal(document.getElementById("modal-edit-student"));
    modal.show();
  } catch (error) {
    alert(error.message);
  }
};

// Post Revision of Student Records
async function handleEditStudentSubmit(e) {
  e.preventDefault();
  const id = document.getElementById("edit-student-id").value;
  const bodyData = {
    student_name: document.getElementById("edit-student-name").value,
    email: document.getElementById("edit-student-email").value,
    contact: document.getElementById("edit-student-contact").value,
    emergency_contact: document.getElementById("edit-student-emergency").value,
    address: document.getElementById("edit-student-address").value,
    room_id: document.getElementById("edit-student-room").value,
    admission_date: document.getElementById("edit-student-date").value,
    status: document.getElementById("edit-student-status").value
  };

  try {
    await apiFetch(`/api/students/${id}`, {
      method: "PUT",
      body: JSON.stringify(bodyData)
    });

    // Close
    const modalEl = document.getElementById("modal-edit-student");
    const modalInst = bootstrap.Modal.getInstance(modalEl);
    modalInst.hide();

    loadStudentsView();
  } catch (error) {
    alert(error.message);
  }
}

// Inspect details of a target hosteller
window.viewStudentDetails = async function(id) {
  try {
    const s = await apiFetch(`/api/students/${id}`);
    
    // Fetch initials as avatar placeholder
    const initials = s.student_name.split(" ").map(n => n[0]).join("").toUpperCase();
    document.getElementById("view-student-avatar").textContent = initials;
    document.getElementById("view-student-name").textContent = s.student_name;
    
    const badge = document.getElementById("view-student-badge");
    badge.textContent = `${s.status} Checked`;
    badge.className = `badge ${s.status === "Active" ? "bg-success/10 text-success" : "bg-secondary/10 text-slate-500"}`;

    document.getElementById("view-student-id").textContent = `#${s.student_id}`;
    document.getElementById("view-student-email").textContent = s.email;
    document.getElementById("view-student-contact").textContent = s.contact;
    document.getElementById("view-student-emergency").textContent = s.emergency_contact;
    document.getElementById("view-student-address").textContent = s.address;
    document.getElementById("view-student-room").textContent = s.room_number ? `Unit ${s.room_number}` : "Not Allocated";
    document.getElementById("view-student-date").textContent = s.admission_date;

    const modal = new bootstrap.Modal(document.getElementById("modal-view-student"));
    modal.show();
  } catch (error) {
    alert(error.message);
  }
};

// Delete operations Student
window.deleteStudent = async function(id) {
  const check = confirm("Warning: Deleting a student deletes their payment logs too. Are you sure you want to delete this student?");
  if (!check) return;

  try {
    const res = await apiFetch(`/api/students/${id}`, { method: "DELETE" });
    alert(res.message);
    loadStudentsView();
  } catch (error) {
    alert(error.message);
  }
};


// 4. ROOMS TAB WORKFLOWS
async function loadRoomsView(q = "") {
  try {
    const list = await apiFetch(`/api/rooms?q=${encodeURIComponent(q)}`);
    roomsCache = list;

    const tableBody = document.getElementById("rooms-table").querySelector("tbody");
    tableBody.innerHTML = "";

    if (list.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-slate-400 py-4 fs-7.5">No matching room records.</td></tr>`;
      return;
    }

    list.forEach(r => {
      const tr = document.createElement("tr");
      tr.id = `row-room-${r.room_id}`;
      
      let badgeClass = "badge bg-success/10 text-success";
      if (r.status === "Full") badgeClass = "badge bg-danger/10 text-danger";
      if (r.status === "Maintenance") badgeClass = "badge bg-secondary/10 text-secondary";

      tr.innerHTML = `
        <td class="font-mono text-slate-400">${r.room_id}</td>
        <td class="font-mono fw-bold text-slate-800">Unit ${r.room_number}</td>
        <td><span class="text-slate-700 fw-medium">${r.room_type}</span></td>
        <td class="text-center font-mono text-slate-800">${r.capacity} beds</td>
        <td class="font-mono text-slate-800 fw-semibold">$${r.monthly_rent}</td>
        <td>
          <div class="d-flex align-items-center gap-1.5 font-mono">
            <span class="fw-bold text-slate-800">${r.occupied_beds}</span>
            <span class="text-slate-400">/ ${r.capacity} allocated</span>
          </div>
        </td>
        <td><span class="${badgeClass} fs-9.5">${r.status}</span></td>
        <td class="text-end">
          <div class="d-inline-flex gap-1.5">
            <button class="btn btn-outline-primary btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="editRoomPrep(${r.room_id})" title="Edit Room Properties">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
            </button>
            <button class="btn btn-outline-danger btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="deleteRoom(${r.room_id})" title="Remove Unit">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            </button>
          </div>
        </td>
      `;
      tableBody.appendChild(tr);
    });
  } catch (error) {
    console.error("Room list load fail:", error);
  }
}

// Prefill Option lists config for type Select elements
async function fillCategoriesOptions(selectorId) {
  try {
    const types = await apiFetch("/api/settings/room-types");
    const select = document.getElementById(selectorId);
    select.innerHTML = '<option value="" disabled selected>Select Class Type</option>';
    
    types.forEach(t => {
      const opt = document.createElement("option");
      opt.value = t.type_name;
      opt.textContent = t.type_name;
      select.appendChild(opt);
    });
  } catch (error) {
    console.error("Failed load room types for options:", error);
  }
}

// Action triggers
window.prepAddRoomModal = async function() {
  await fillCategoriesOptions("add-room-type");
};

async function handleAddRoomSubmit(e) {
  e.preventDefault();
  const bodyData = {
    room_number: document.getElementById("add-room-number").value,
    room_type: document.getElementById("add-room-type").value,
    capacity: document.getElementById("add-room-capacity").value,
    monthly_rent: document.getElementById("add-room-rent").value,
    status: document.getElementById("add-room-status").value
  };

  try {
    await apiFetch("/api/rooms", {
      method: "POST",
      body: JSON.stringify(bodyData)
    });

    const modalEl = document.getElementById("modal-add-room");
    const modalInst = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
    modalInst.hide();

    document.getElementById("form-add-room").reset();
    loadRoomsView();
  } catch (error) {
    alert(error.message);
  }
}

window.editRoomPrep = async function(id) {
  try {
    const r = await apiFetch(`/api/rooms/${id}`);
    
    document.getElementById("edit-room-id").value = r.room_id;
    document.getElementById("edit-room-number").value = r.room_number;
    document.getElementById("edit-room-capacity").value = r.capacity;
    document.getElementById("edit-room-rent").value = r.monthly_rent;
    document.getElementById("edit-room-status").value = r.status;

    await fillCategoriesOptions("edit-room-type");
    document.getElementById("edit-room-type").value = r.room_type;

    const modal = new bootstrap.Modal(document.getElementById("modal-edit-room"));
    modal.show();
  } catch (error) {
    alert(error.message);
  }
};

async function handleEditRoomSubmit(e) {
  e.preventDefault();
  const id = document.getElementById("edit-room-id").value;
  const bodyData = {
    room_number: document.getElementById("edit-room-number").value,
    room_type: document.getElementById("edit-room-type").value,
    capacity: document.getElementById("edit-room-capacity").value,
    monthly_rent: document.getElementById("edit-room-rent").value,
    status: document.getElementById("edit-room-status").value
  };

  try {
    await apiFetch(`/api/rooms/${id}`, {
      method: "PUT",
      body: JSON.stringify(bodyData)
    });

    const modalEl = document.getElementById("modal-edit-room");
    const modalInst = bootstrap.Modal.getInstance(modalEl);
    modalInst.hide();

    loadRoomsView();
  } catch (error) {
    alert(error.message);
  }
}

// Delete room
window.deleteRoom = async function(id) {
  const check = confirm("Are you sure you want to delete this room?");
  if (!check) return;

  try {
    const res = await apiFetch(`/api/rooms/${id}`, { method: "DELETE" });
    alert(res.message);
    loadRoomsView();
  } catch (error) {
    alert(error.message);
  }
};


// 5. TRANSACTIONS / PAYMENTS CONTROLLERS
async function loadPaymentsView(q = "") {
  try {
    const list = await apiFetch(`/api/payments?q=${encodeURIComponent(q)}`);
    
    const tableBody = document.getElementById("payments-table").querySelector("tbody");
    tableBody.innerHTML = "";

    if (list.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-slate-400 py-4 fs-7.5">No transaction vouchers logged.</td></tr>`;
      return;
    }

    list.forEach(p => {
      const tr = document.createElement("tr");
      tr.id = `row-payment-${p.payment_id}`;
      
      const badgeClass = p.payment_status === "Paid" ? "badge bg-success/10 text-success" : "badge bg-warning/10 text-warning";

      tr.innerHTML = `
        <td class="font-mono text-slate-400">TXN-${String(p.payment_id).padStart(6, "0")}</td>
        <td><span class="fw-semibold text-slate-800">${p.student_name}</span></td>
        <td><b class="text-slate-700 font-mono">Room ${p.room_number}</b></td>
        <td class="font-mono text-slate-800 fw-bold">$${p.amount}</td>
        <td class="font-mono text-slate-600">${p.payment_date}</td>
        <td><span class="${badgeClass} fs-9.5">${p.payment_status}</span></td>
        <td class="text-end">
          <div class="d-inline-flex gap-1.5">
            <button class="btn btn-outline-primary btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="editPaymentPrep(${p.payment_id})" title="Alter status">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
            </button>
            <button class="btn btn-outline-danger btn-sm p-1.5 rounded-3 d-flex align-items-center justify-content-center" onclick="deletePayment(${p.payment_id})" title="Void Log">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            </button>
          </div>
        </td>
      `;
      tableBody.appendChild(tr);
    });

  } catch (error) {
    console.error("Failed loading payment ledgers:", error);
  }
}

// Populators for Payments dropdown Student list
async function refreshPaymentStudentsDropdown() {
  try {
    const list = await apiFetch("/api/students");
    const select = document.getElementById("add-payment-student");
    select.innerHTML = '<option value="" disabled selected>Select Hosteller student</option>';
    
    list.forEach(s => {
      if (s.status === "Active") {
        const option = document.createElement("option");
        option.value = s.student_id;
        option.dataset.rent = s.room_id ? roomsCache.find(r => r.room_id === s.room_id)?.monthly_rent || 0 : 0;
        option.textContent = `${s.student_name} (Room ${s.room_number})`;
        select.appendChild(option);
      }
    });

    // Auto calculate rent on select change
    select.addEventListener("change", function() {
      const selectedOption = select.options[select.selectedIndex];
      const rentValue = selectedOption.dataset.rent || "0";
      document.getElementById("add-payment-amount").value = rentValue;
    });

  } catch (error) {
    console.error("dropdown fill fail:", error);
  }
}

window.prepAddPaymentModal = async function() {
  await refreshPaymentStudentsDropdown();
  document.getElementById("add-payment-date").value = new Date().toISOString().split("T")[0];
};

async function handleAddPaymentSubmit(e) {
  e.preventDefault();
  const bodyData = {
    student_id: document.getElementById("add-payment-student").value,
    amount: document.getElementById("add-payment-amount").value,
    payment_date: document.getElementById("add-payment-date").value,
    payment_status: document.getElementById("add-payment-status").value
  };

  try {
    await apiFetch("/api/payments", {
      method: "POST",
      body: JSON.stringify(bodyData)
    });

    const modalEl = document.getElementById("modal-add-payment");
    const modalInst = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
    modalInst.hide();

    document.getElementById("form-add-payment").reset();
    loadPaymentsView();
  } catch (error) {
    alert(error.message);
  }
}

window.editPaymentPrep = async function(id) {
  try {
    // Get full catalog formats
    const payments = await apiFetch("/api/payments");
    const p = payments.find(pay => pay.payment_id === id);
    if (!p) return;

    document.getElementById("edit-payment-id").value = p.payment_id;
    document.getElementById("edit-payment-student-label").textContent = `${p.student_name} (Room ${p.room_number})`;
    document.getElementById("edit-payment-amount").value = p.amount;
    document.getElementById("edit-payment-date").value = p.payment_date;
    document.getElementById("edit-payment-status").value = p.payment_status;

    const modal = new bootstrap.Modal(document.getElementById("modal-edit-payment"));
    modal.show();
  } catch (error) {
    alert(error.message);
  }
};

async function handleEditPaymentSubmit(e) {
  e.preventDefault();
  const id = document.getElementById("edit-payment-id").value;
  const bodyData = {
    amount: document.getElementById("edit-payment-amount").value,
    payment_date: document.getElementById("edit-payment-date").value,
    payment_status: document.getElementById("edit-payment-status").value
  };

  try {
    await apiFetch(`/api/payments/${id}`, {
      method: "PUT",
      body: JSON.stringify(bodyData)
    });

    const modalEl = document.getElementById("modal-edit-payment");
    const modalInst = bootstrap.Modal.getInstance(modalEl);
    modalInst.hide();

    loadPaymentsView();
  } catch (error) {
    alert(error.message);
  }
}

// Void Payment
window.deletePayment = async function(id) {
  const check = confirm("Warning: Voiding statement logs is destructive. Proceed anyway?");
  if (!check) return;

  try {
    const res = await apiFetch(`/api/payments/${id}`, { method: "DELETE" });
    alert(res.message);
    loadPaymentsView();
  } catch (error) {
    alert(error.message);
  }
};


// 6. SETTINGS WORKFLOWS
async function loadSettingsView() {
  try {
    // Load hostel info
    const info = await apiFetch("/api/settings/hostel-info");
    document.getElementById("set-hostel-name").value = info.hostelName;
    document.getElementById("set-hostel-date").value = info.establishedDate;
    document.getElementById("set-hostel-contact").value = info.contactNo;
    document.getElementById("set-hostel-email").value = info.email;
    document.getElementById("set-hostel-address").value = info.address;

    document.getElementById("active-hostel-pill-name").textContent = info.hostelName;

    // Load available configurations
    await loadSettingsRoomTypes();
  } catch (err) {
    console.error("fail settings:", err);
  }
}

async function handleHostelInfoSubmit(e) {
  e.preventDefault();
  const bodyData = {
    hostelName: document.getElementById("set-hostel-name").value,
    establishedDate: document.getElementById("set-hostel-date").value,
    contactNo: document.getElementById("set-hostel-contact").value,
    email: document.getElementById("set-hostel-email").value,
    address: document.getElementById("set-hostel-address").value
  };

  try {
    const res = await apiFetch("/api/settings/hostel-info", {
      method: "PUT",
      body: JSON.stringify(bodyData)
    });
    document.getElementById("active-hostel-pill-name").textContent = res.hostelName;
    alert("Hostel config profiles successfully updated.");
  } catch (error) {
    alert(error.message);
  }
}

async function loadSettingsRoomTypes() {
  try {
    const list = await apiFetch("/api/settings/room-types");
    roomTypesCache = list;
    const ul = document.getElementById("room-types-list");
    ul.innerHTML = "";

    list.forEach(t => {
      const li = document.createElement("li");
      li.className = "list-group-item bg-transparent border-0 d-flex align-items-center justify-content-between px-0 py-2 fs-7.5 border-bottom border-slate-100";
      li.innerHTML = `
        <span class="fw-semibold text-slate-800 d-flex align-items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 12V8H4v4"/><path d="M2 12h20"/><path d="M12 12V4"/><polyline points="8 4 12 4 16 4"/></svg>
          ${t.type_name}
        </span>
        <button class="btn btn-outline-danger btn-sm px-2 py-0.5" onclick="deleteRoomType(${t.type_id})">
          Remove
        </button>
      `;
      ul.appendChild(li);
    });
  } catch (error) {
    console.error(error);
  }
}

async function handleAddRoomTypeSubmit(e) {
  e.preventDefault();
  const type_name = document.getElementById("new-room-type-name").value.trim();
  
  try {
    await apiFetch("/api/settings/room-types", {
      method: "POST",
      body: JSON.stringify({ type_name })
    });
    
    document.getElementById("new-room-type-name").value = "";
    await loadSettingsRoomTypes();
  } catch (error) {
    alert(error.message);
  }
}

window.deleteRoomType = async function(id) {
  try {
    await apiFetch(`/api/settings/room-types/${id}`, { method: "DELETE" });
    await loadSettingsRoomTypes();
  } catch (error) {
    alert(error.message);
  }
};


// 7. INITIALIZER ENTRYPOINT BINDINGS & SELECTORS
document.addEventListener("DOMContentLoaded", () => {
  // Start emulating clock
  startUTCClock();
  
  // Login workflow hook
  document.getElementById("login-form").addEventListener("submit", handleLogin);
  document.getElementById("btn-logout").addEventListener("click", handleLogout);

  // Active validation check
  updateAuthUI();

  // Sidebar link navigators
  document.querySelectorAll("#sidebar-nav button").forEach(button => {
    button.addEventListener("click", () => {
      const target = button.dataset.target;
      if (target) {
        navigateView(target);
      }
    });
  });

  // Attach search parameters query filters
  document.getElementById("search-student").addEventListener("input", (e) => {
    loadStudentsView(e.target.value);
  });
  
  document.getElementById("search-room").addEventListener("input", (e) => {
    loadRoomsView(e.target.value);
  });

  document.getElementById("search-payment").addEventListener("input", (e) => {
    loadPaymentsView(e.target.value);
  });

  // Modal loading setups hooks - bootstrap events integration
  document.getElementById("modal-add-student").addEventListener("show.bs.modal", () => {
    window.prepAddStudentModal();
  });

  document.getElementById("modal-add-room").addEventListener("show.bs.modal", () => {
    window.prepAddRoomModal();
  });

  document.getElementById("modal-add-payment").addEventListener("show.bs.modal", () => {
    window.prepAddPaymentModal();
  });

  // Core Form events bindings
  document.getElementById("form-add-student").addEventListener("submit", handleAddStudentSubmit);
  document.getElementById("form-edit-student").addEventListener("submit", handleEditStudentSubmit);
  document.getElementById("form-add-room").addEventListener("submit", handleAddRoomSubmit);
  document.getElementById("form-edit-room").addEventListener("submit", handleEditRoomSubmit);
  document.getElementById("form-add-payment").addEventListener("submit", handleAddPaymentSubmit);
  document.getElementById("form-edit-payment").addEventListener("submit", handleEditPaymentSubmit);
  document.getElementById("hostel-info-form").addEventListener("submit", handleHostelInfoSubmit);
  document.getElementById("add-room-type-form").addEventListener("submit", handleAddRoomTypeSubmit);
});
