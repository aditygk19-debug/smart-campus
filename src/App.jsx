import { Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import DepartmentPage from './pages/DepartmentPage';
import ResourceListPage from './pages/ResourceListPage';
import BookingFormPage from './pages/BookingFormPage';
import BookingStatusPage from './pages/BookingStatusPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminReportsPage from './pages/AdminReportsPage';

export default function App() {
  const [user, setUser] = useState(null);

  // Rehydrate user from localStorage on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('sc_user');
    if (storedUser) {
      try { setUser(JSON.parse(storedUser)); } catch { /* ignore */ }
    }
  }, []);

  async function handleLogin({ role, userId, password }) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
  role: role.charAt(0).toUpperCase() + role.slice(1),
  userId,
  password,
}),
    });

    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.message || 'Login failed');
    }

    // Persist
    localStorage.setItem('sc_token', data.token);
    localStorage.setItem('sc_user', JSON.stringify(data.user));

    // Update state
    setUser(data.user);
    return data.user;
  }

  function handleLogout() {
    localStorage.removeItem('sc_token');
    localStorage.removeItem('sc_user');
    setUser(null);
  }

  function RequireUser(children, roles = []) {
    if (!user) return <Navigate to="/login" replace />;
    if (roles.length && !roles.includes(user.role)) {
      return <Navigate to="/login" replace />;
    }
    return children;
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={
          user ? (
            <Navigate to={user.role === 'Admin' ? '/admin' : '/dashboard'} replace />
          ) : (
            <LoginPage onLogin={handleLogin} />
          )
        }
      />

      <Route
        path="/dashboard"
        element={RequireUser(<DashboardPage user={user} onLogout={handleLogout} />, ['Student', 'Faculty'])}
      />
      <Route
        path="/dashboard/department/:deptId"
        element={RequireUser(<DepartmentPage user={user} onLogout={handleLogout} />, ['Student', 'Faculty'])}
      />
      <Route
        path="/dashboard/department/:deptId/:typeId"
        element={RequireUser(<ResourceListPage user={user} onLogout={handleLogout} />, ['Student', 'Faculty'])}
      />
      <Route
        path="/dashboard/book/:resourceId"
        element={RequireUser(<BookingFormPage user={user} onLogout={handleLogout} />, ['Student', 'Faculty'])}
      />
      <Route
        path="/dashboard/bookings"
        element={RequireUser(<BookingStatusPage user={user} onLogout={handleLogout} />, ['Student', 'Faculty'])}
      />
      <Route
        path="/admin"
        element={RequireUser(<AdminDashboardPage user={user} onLogout={handleLogout} />, ['Admin'])}
      />
      <Route
        path="/admin/reports"
        element={RequireUser(<AdminReportsPage user={user} onLogout={handleLogout} />, ['Admin'])}
      />

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}