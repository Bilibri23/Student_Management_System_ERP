import { useQuery } from '@tanstack/react-query'
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Avatar,
  LinearProgress,
  Chip,
  IconButton,
  Skeleton,
} from '@mui/material'
import {
  School as SchoolIcon,
  People as PeopleIcon,
  Book as BookIcon,
  Receipt as InvoiceIcon,
  Assignment as AssignmentIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  ArrowForward as ArrowForwardIcon,
  CalendarMonth as CalendarIcon,
  AccessTime as TimeIcon,
  Notifications as NotificationsIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  AttachMoney as MoneyIcon,
} from '@mui/icons-material'
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  Legend,
} from 'recharts'
import { dashboardApi } from '../../api/dashboard'
import { useAuthStore } from '../../store/authStore'

const gradients = {
  primary: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
  secondary: 'linear-gradient(135deg, #EC4899 0%, #F472B6 100%)',
  success: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
  warning: 'linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)',
  error: 'linear-gradient(135deg, #EF4444 0%, #F87171 100%)',
  info: 'linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%)',
}

const StatCard = ({ title, value, icon, color = 'primary', trend, trendValue }) => (
  <Card
    sx={{
      position: 'relative',
      overflow: 'hidden',
      transition: 'all 0.3s ease',
      '&:hover': {
        transform: 'translateY(-4px)',
        boxShadow: '0 20px 40px rgba(0,0,0,0.12)',
      },
    }}
  >
    <CardContent sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box sx={{ flex: 1 }}>
          <Typography
            variant="overline"
            sx={{
              color: 'text.secondary',
              fontWeight: 600,
              letterSpacing: 0.5,
              fontSize: '0.7rem',
            }}
          >
            {title}
          </Typography>
          <Typography
            variant="h3"
            sx={{
              fontWeight: 700,
              mt: 1,
              mb: 1,
              background: gradients[color],
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            {value}
          </Typography>
          {trend && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              {trend === 'up' ? (
                <TrendingUpIcon sx={{ fontSize: 16, color: '#10B981' }} />
              ) : (
                <TrendingDownIcon sx={{ fontSize: 16, color: '#EF4444' }} />
              )}
              <Typography
                variant="caption"
                sx={{
                  color: trend === 'up' ? '#10B981' : '#EF4444',
                  fontWeight: 600,
                }}
              >
                {trendValue}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                vs last month
              </Typography>
            </Box>
          )}
        </Box>
        <Avatar
          sx={{
            width: 56,
            height: 56,
            background: gradients[color],
            boxShadow: `0 8px 16px ${color === 'primary' ? 'rgba(99, 102, 241, 0.3)' : 
              color === 'success' ? 'rgba(16, 185, 129, 0.3)' : 
              color === 'warning' ? 'rgba(245, 158, 11, 0.3)' : 
              color === 'error' ? 'rgba(239, 68, 68, 0.3)' : 'rgba(59, 130, 246, 0.3)'}`,
          }}
        >
          {icon}
        </Avatar>
      </Box>
    </CardContent>
    <Box
      sx={{
        position: 'absolute',
        top: -50,
        right: -50,
        width: 150,
        height: 150,
        borderRadius: '50%',
        background: gradients[color],
        opacity: 0.05,
      }}
    />
  </Card>
)

const WelcomeCard = ({ user }) => (
  <Card
    sx={{
      background: gradients.primary,
      color: 'white',
      position: 'relative',
      overflow: 'hidden',
    }}
  >
    <CardContent sx={{ p: 4, position: 'relative', zIndex: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Welcome back, {user?.username}! 👋
          </Typography>
          <Typography variant="body1" sx={{ opacity: 0.9, mb: 2 }}>
            Here's what's happening with your institution today.
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <Chip
              icon={<CalendarIcon sx={{ color: 'white !important' }} />}
              label={new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
              sx={{
                bgcolor: 'rgba(255,255,255,0.2)',
                color: 'white',
                fontWeight: 500,
                '& .MuiChip-icon': { color: 'white' },
              }}
            />
            <Chip
              icon={<TimeIcon sx={{ color: 'white !important' }} />}
              label={new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
              sx={{
                bgcolor: 'rgba(255,255,255,0.2)',
                color: 'white',
                fontWeight: 500,
                '& .MuiChip-icon': { color: 'white' },
              }}
            />
          </Box>
        </Box>
        <Box
          sx={{
            display: { xs: 'none', md: 'block' },
            width: 150,
            height: 150,
            position: 'relative',
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              width: '100%',
              height: '100%',
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.1)',
              animation: 'pulse 2s infinite',
              '@keyframes pulse': {
                '0%, 100%': { transform: 'scale(1)', opacity: 0.5 },
                '50%': { transform: 'scale(1.1)', opacity: 0.3 },
              },
            }}
          />
          <Avatar
            sx={{
              width: 120,
              height: 120,
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              bgcolor: 'rgba(255,255,255,0.2)',
              fontSize: '3rem',
              fontWeight: 700,
            }}
          >
            {user?.username?.charAt(0).toUpperCase()}
          </Avatar>
        </Box>
      </Box>
    </CardContent>
    <Box
      sx={{
        position: 'absolute',
        bottom: -100,
        right: -100,
        width: 300,
        height: 300,
        borderRadius: '50%',
        background: 'rgba(255,255,255,0.1)',
      }}
    />
    <Box
      sx={{
        position: 'absolute',
        top: -50,
        left: -50,
        width: 200,
        height: 200,
        borderRadius: '50%',
        background: 'rgba(255,255,255,0.05)',
      }}
    />
  </Card>
)

const QuickActionCard = ({ title, description, icon, color, onClick }) => (
  <Card
    onClick={onClick}
    sx={{
      cursor: 'pointer',
      transition: 'all 0.3s ease',
      border: '2px solid transparent',
      '&:hover': {
        transform: 'translateY(-4px)',
        borderColor: color,
        boxShadow: `0 12px 24px ${color}20`,
      },
    }}
  >
    <CardContent sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Avatar
          sx={{
            width: 48,
            height: 48,
            background: `${color}15`,
            color: color,
          }}
        >
          {icon}
        </Avatar>
        <Box sx={{ flex: 1 }}>
          <Typography variant="subtitle1" fontWeight={600}>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {description}
          </Typography>
        </Box>
        <ArrowForwardIcon sx={{ color: 'text.secondary' }} />
      </Box>
    </CardContent>
  </Card>
)

const ActivityItem = ({ title, time, type, status }) => {
  const getStatusColor = () => {
    switch (status) {
      case 'success': return '#10B981'
      case 'warning': return '#F59E0B'
      case 'error': return '#EF4444'
      default: return '#6366F1'
    }
  }

  const getIcon = () => {
    switch (type) {
      case 'payment': return <MoneyIcon />
      case 'notification': return <NotificationsIcon />
      case 'check': return <CheckCircleIcon />
      case 'warning': return <WarningIcon />
      default: return <AssignmentIcon />
    }
  }

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        p: 2,
        borderRadius: 2,
        transition: 'all 0.2s ease',
        '&:hover': {
          bgcolor: 'rgba(99, 102, 241, 0.04)',
        },
      }}
    >
      <Avatar
        sx={{
          width: 40,
          height: 40,
          bgcolor: `${getStatusColor()}15`,
          color: getStatusColor(),
        }}
      >
        {getIcon()}
      </Avatar>
      <Box sx={{ flex: 1 }}>
        <Typography variant="body2" fontWeight={500}>
          {title}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {time}
        </Typography>
      </Box>
      <Box
        sx={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          bgcolor: getStatusColor(),
        }}
      />
    </Box>
  )
}

const Dashboard = () => {
  const { user } = useAuthStore()
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => dashboardApi.getStats().then(res => {
      return res.data.data
    }),
    retry: 1,
    onError: (err) => {
      console.error('Dashboard API Error:', err)
    },
  })

  const chartData = [
    { name: 'Jan', students: 400, revenue: 2400 },
    { name: 'Feb', students: 300, revenue: 1398 },
    { name: 'Mar', students: 520, revenue: 9800 },
    { name: 'Apr', students: 478, revenue: 3908 },
    { name: 'May', students: 589, revenue: 4800 },
    { name: 'Jun', students: 639, revenue: 3800 },
  ]

  const pieData = [
    { name: 'Academic', value: 400, color: '#6366F1' },
    { name: 'Finance', value: 300, color: '#10B981' },
    { name: 'Marketing', value: 200, color: '#F59E0B' },
    { name: 'HR', value: 100, color: '#EC4899' },
  ]

  const recentActivities = [
    { title: 'New student enrollment completed', time: '2 minutes ago', type: 'check', status: 'success' },
    { title: 'Payment received - Invoice #1234', time: '15 minutes ago', type: 'payment', status: 'success' },
    { title: 'Leave request pending approval', time: '1 hour ago', type: 'warning', status: 'warning' },
    { title: 'New course added to catalog', time: '3 hours ago', type: 'notification', status: 'info' },
  ]

  if (isLoading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={200} sx={{ mb: 3, borderRadius: 4 }} />
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} md={3} key={i}>
              <Skeleton variant="rounded" height={140} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
      </Box>
    )
  }

  if (error) {
    const errorMessage = error.response?.data?.message || error.message || 'Failed to load dashboard statistics'
    return (
      <Alert
        severity="error"
        sx={{
          borderRadius: 3,
          '& .MuiAlert-icon': { alignItems: 'center' },
        }}
      >
        {errorMessage}
      </Alert>
    )
  }

  const role = user?.role || 'STUDENT'
  const statsData = stats?.stats || {}

  const getStatCards = () => {
    switch (role) {
      case 'ADMIN':
        return [
          { title: 'Total Students', value: statsData.totalStudents || 0, icon: <PeopleIcon />, color: 'primary', trend: 'up', trendValue: '+12%' },
          { title: 'Total Staff', value: statsData.totalStaff || 0, icon: <SchoolIcon />, color: 'secondary', trend: 'up', trendValue: '+5%' },
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
        ]
    }
  }

  return (
    <Box>
      {/* Welcome Card */}
      <Box sx={{ mb: 4 }}>
        <WelcomeCard user={user} />
      </Box>

      {/* Stats Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {getStatCards().map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <StatCard {...stat} />
          </Grid>
        ))}
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Box>
                  <Typography variant="h6" fontWeight={600}>
                    Overview Analytics
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Student enrollment & revenue trends
                  </Typography>
                </Box>
                <Chip label="Last 6 months" size="small" sx={{ bgcolor: '#6366F115', color: '#6366F1', fontWeight: 500 }} />
              </Box>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="colorStudents" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366F1" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#6366F1" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                  <XAxis dataKey="name" stroke="#94A3B8" fontSize={12} />
                  <YAxis stroke="#94A3B8" fontSize={12} />
                  <Tooltip
                    contentStyle={{
                      background: 'white',
                      border: 'none',
                      borderRadius: 12,
                      boxShadow: '0 10px 40px rgba(0,0,0,0.1)',
                    }}
                  />
                  <Area type="monotone" dataKey="students" stroke="#6366F1" strokeWidth={2} fillOpacity={1} fill="url(#colorStudents)" />
                  <Area type="monotone" dataKey="revenue" stroke="#10B981" strokeWidth={2} fillOpacity={1} fill="url(#colorRevenue)" />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Module Distribution
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Activity across modules
              </Typography>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'center' }}>
                {pieData.map((item) => (
                  <Chip
                    key={item.name}
                    label={item.name}
                    size="small"
                    sx={{
                      bgcolor: `${item.color}15`,
                      color: item.color,
                      fontWeight: 500,
                      fontSize: '0.7rem',
                    }}
                  />
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Activity */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" fontWeight={600}>
                  Recent Activity
                </Typography>
                <Chip label="Today" size="small" sx={{ bgcolor: '#6366F115', color: '#6366F1', fontWeight: 500 }} />
              </Box>
              <Box>
                {recentActivities.map((activity, index) => (
                  <ActivityItem key={index} {...activity} />
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Quick Actions
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                Frequently used actions
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <QuickActionCard
                  title="Add New Student"
                  description="Register a new student"
                  icon={<PeopleIcon />}
                  color="#6366F1"
                />
                <QuickActionCard
                  title="Create Invoice"
                  description="Generate new invoice"
                  icon={<InvoiceIcon />}
                  color="#10B981"
                />
                <QuickActionCard
                  title="View Reports"
                  description="Financial reports & analytics"
                  icon={<AssignmentIcon />}
                  color="#F59E0B"
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

export default Dashboard

