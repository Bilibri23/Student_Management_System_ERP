import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import ProtectedRoute from './components/guards/ProtectedRoute'
import PublicRoute from './components/guards/PublicRoute'

// Auth Pages
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import ForgotPassword from './pages/auth/ForgotPassword'
import VerifyEmail from './pages/auth/VerifyEmail'

// Dashboard
import Dashboard from './pages/dashboard/Dashboard'

// Academic Pages
import Courses from './pages/academic/Courses'
import Enrollments from './pages/academic/Enrollments'
import Attendance from './pages/academic/Attendance'
import Grades from './pages/academic/Grades'
import Exams from './pages/academic/Exams'
import Certificates from './pages/academic/Certificates'

// Finance Pages
import FeeStructures from './pages/finance/FeeStructures'
import Invoices from './pages/finance/Invoices'
import Payments from './pages/finance/Payments'
import Expenses from './pages/finance/Expenses'
import FinanceReports from './pages/finance/Reports'

// Marketing Pages
import Leads from './pages/marketing/Leads'
import Campaigns from './pages/marketing/Campaigns'

// HR Pages
import Employees from './pages/hr/Employees'
import Leaves from './pages/hr/Leaves'

// User Pages
import Profile from './pages/user/Profile'

// Layout
import MainLayout from './components/layout/MainLayout'

function App() {
  const { user } = useAuthStore()

  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/verify-email/:token" element={<VerifyEmail />} />
      </Route>

      {/* Protected Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          {/* Dashboard */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />

          {/* Academic Module */}
          <Route path="/academic/courses" element={<Courses />} />
          <Route path="/academic/enrollments" element={<Enrollments />} />
          <Route path="/academic/attendance" element={<Attendance />} />
          <Route path="/academic/grades" element={<Grades />} />
          <Route path="/academic/exams" element={<Exams />} />
          <Route path="/academic/certificates" element={<Certificates />} />

          {/* Finance Module */}
          <Route path="/finance/fees" element={<FeeStructures />} />
          <Route path="/finance/invoices" element={<Invoices />} />
          <Route path="/finance/payments" element={<Payments />} />
          <Route path="/finance/expenses" element={<Expenses />} />
          <Route path="/finance/reports" element={<FinanceReports />} />

          {/* Marketing Module */}
          <Route path="/marketing/leads" element={<Leads />} />
          <Route path="/marketing/campaigns" element={<Campaigns />} />

          {/* HR Module */}
          <Route path="/hr/employees" element={<Employees />} />
          <Route path="/hr/leaves" element={<Leaves />} />

          {/* User Profile */}
          <Route path="/profile" element={<Profile />} />
        </Route>
      </Route>

      {/* 404 */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App

