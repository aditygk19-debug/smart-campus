import { Routes, Route, Navigate } from 'react-router-dom';
import { useState } from 'react';
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

  async function handleLogin({ role, userId, password }) {
    if (!userId || !password) {
      throw new Error('Enter your User ID and password.');
    }
    setUser({
      userId,
      role: role.charAt(0).toUpperCase() + role.slice(1),
      status: 'Active',
    });
  }

  function handleLogout() {
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
      {/* Login */}
      <Route
        path="/login"
        element={
          user ? (
            <Navigate
              to={user.role === 'Admin' ? '/admin' : '/dashboard'}
              replace
            />
          ) : (
            <LoginPage onLogin={handleLogin} />
          )
        }
      />

      {/* Student / Faculty routes */}
      <Route
        path="/dashboard"
        element={RequireUser(
          <DashboardPage user={user} onLogout={handleLogout} />,
          ['Student', 'Faculty']
        )}
      />

      <Route
        path="/dashboard/department/:deptId"
        element={RequireUser(
          <DepartmentPage user={user} onLogout={handleLogout} />,
          ['Student', 'Faculty']
        )}
      />

      <Route
        path="/dashboard/department/:deptId/:typeId"
        element={RequireUser(
          <ResourceListPage user={user} onLogout={handleLogout} />,
          ['Student', 'Faculty']
        )}
      />

      <Route
        path="/dashboard/book/:resourceId"
        element={RequireUser(
          <BookingFormPage user={user} onLogout={handleLogout} />,
          ['Student', 'Faculty']
        )}
      />

      <Route
        path="/dashboard/bookings"
        element={RequireUser(
          <BookingStatusPage user={user} onLogout={handleLogout} />,
          ['Student', 'Faculty']
        )}
      />

      {/* Admin routes */}
      <Route
        path="/admin"
        element={RequireUser(
          <AdminDashboardPage user={user} onLogout={handleLogout} />,
          ['Admin']
        )}
      />

      <Route
        path="/admin/reports"
        element={RequireUser(
          <AdminReportsPage user={user} onLogout={handleLogout} />,
          ['Admin']
        )}
      />

      {/* Fallbacks */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}