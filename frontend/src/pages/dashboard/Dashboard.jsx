import { useQuery } from '@tanstack/react-query'
import { Grid, Card, CardContent, Typography, Box, CircularProgress, Alert } from '@mui/material'
import {
  School as SchoolIcon,
  People as PeopleIcon,
  Book as BookIcon,
  Receipt as InvoiceIcon,
  Assignment as AssignmentIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material'
import { dashboardApi } from '../../api/dashboard'
import { useAuthStore } from '../../store/authStore'

const StatCard = ({ title, value, icon, color = 'primary' }) => (
  <Card>
    <CardContent>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography color="textSecondary" gutterBottom variant="body2">
            {title}
          </Typography>
          <Typography variant="h4" component="div">
            {value}
          </Typography>
        </Box>
        <Box sx={{ color: `${color}.main`, fontSize: 48 }}>{icon}</Box>
      </Box>
    </CardContent>
  </Card>
)

const Dashboard = () => {
  const { user } = useAuthStore()
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => dashboardApi.getStats().then(res => {
      // Backend returns: { success: true, message: "...", data: DashboardStats }
      // DashboardStats has structure: { userId, userName, role, stats: {...} }
      return res.data.data
    }),
    retry: 1,
    onError: (err) => {
      console.error('Dashboard API Error:', err)
      console.error('Error Response:', err.response)
      console.error('Error Details:', err.response?.data)
    },
  })

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    const errorMessage = error.response?.data?.message || error.message || 'Failed to load dashboard statistics'
    return (
      <Alert severity="error">
        {errorMessage}
        {process.env.NODE_ENV === 'development' && (
          <Box component="pre" sx={{ mt: 1, fontSize: '0.75rem', overflow: 'auto' }}>
            {JSON.stringify(error.response?.data || error, null, 2)}
          </Box>
        )}
      </Alert>
    )
  }

  const role = user?.role || 'STUDENT'
  const statsData = stats?.stats || {}

  const getStatCards = () => {
    switch (role) {
      case 'ADMIN':
        return [
          { title: 'Total Students', value: statsData.totalStudents || 0, icon: <PeopleIcon />, color: 'primary' },
          { title: 'Total Staff', value: statsData.totalStaff || 0, icon: <SchoolIcon />, color: 'secondary' },
          { title: 'Total Courses', value: statsData.totalCourses || 0, icon: <BookIcon />, color: 'info' },
          { title: 'Pending Invoices', value: statsData.pendingInvoices || 0, icon: <InvoiceIcon />, color: 'warning' },
          { title: 'Overdue Invoices', value: statsData.overdueInvoices || 0, icon: <InvoiceIcon />, color: 'error' },
          { title: 'Pending Expenses', value: statsData.pendingExpenses || 0, icon: <AssignmentIcon />, color: 'warning' },
        ]
      case 'ACADEMIC_STAFF':
        return [
          { title: 'My Courses', value: statsData.myCourses || 0, icon: <BookIcon />, color: 'primary' },
          { title: 'Total Students', value: statsData.totalStudents || 0, icon: <PeopleIcon />, color: 'info' },
          { title: 'Pending Attendance', value: statsData.pendingAttendance || 0, icon: <AssignmentIcon />, color: 'warning' },
          { title: 'Upcoming Exams', value: statsData.upcomingExams || 0, icon: <BookIcon />, color: 'secondary' },
          { title: 'Pending Grades', value: statsData.pendingGrades || 0, icon: <AssignmentIcon />, color: 'warning' },
        ]
      case 'FINANCE_STAFF':
        return [
          { title: 'Pending Invoices', value: statsData.pendingInvoices || 0, icon: <InvoiceIcon />, color: 'warning' },
          { title: 'Overdue Invoices', value: statsData.overdueInvoices || 0, icon: <InvoiceIcon />, color: 'error' },
          { title: 'Pending Expenses', value: statsData.pendingExpenses || 0, icon: <AssignmentIcon />, color: 'warning' },
          { title: "Today's Payments", value: statsData.todayPayments || 0, icon: <TrendingUpIcon />, color: 'success' },
        ]
      case 'HR_OFFICER':
        return [
          { title: 'Pending Leaves', value: statsData.pendingLeaves || 0, icon: <AssignmentIcon />, color: 'warning' },
          { title: 'Active Employees', value: statsData.activeEmployees || 0, icon: <PeopleIcon />, color: 'primary' },
          { title: 'On Leave', value: statsData.onLeave || 0, icon: <PeopleIcon />, color: 'info' },
          { title: 'New Leads', value: statsData.newLeads || 0, icon: <PeopleIcon />, color: 'secondary' },
        ]
      case 'STUDENT':
      default:
        return [
          { title: 'Enrolled Courses', value: statsData.enrolledCourses || 0, icon: <BookIcon />, color: 'primary' },
          { title: 'Courses with Grades', value: statsData.coursesWithGrades || 0, icon: <BookIcon />, color: 'success' },
          { title: 'Upcoming Exams', value: statsData.upcomingExams || 0, icon: <AssignmentIcon />, color: 'warning' },
          { title: 'Pending Invoices', value: statsData.pendingInvoices || 0, icon: <InvoiceIcon />, color: 'error' },
          { title: 'Average Attendance', value: `${statsData.averageAttendance || 0}%`, icon: <TrendingUpIcon />, color: 'info' },
        ]
    }
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome back, {user?.username}!
      </Typography>
      <Typography variant="body1" color="textSecondary" paragraph>
        Here's an overview of your dashboard
      </Typography>
      <Grid container spacing={3} sx={{ mt: 2 }}>
        {getStatCards().map((stat, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <StatCard {...stat} />
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}

export default Dashboard

