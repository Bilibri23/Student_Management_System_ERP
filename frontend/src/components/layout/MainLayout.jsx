import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Avatar,
  Menu,
  MenuItem,
} from '@mui/material'
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  School as SchoolIcon,
  AccountBalance as FinanceIcon,
  Campaign as MarketingIcon,
  People as HrIcon,
  Person as PersonIcon,
  Logout as LogoutIcon,
  MenuBook as CourseIcon,
  Assignment as EnrollmentIcon,
  CheckCircle as AttendanceIcon,
  Grade as GradeIcon,
  Quiz as ExamIcon,
  Description as CertificateIcon,
  Receipt as InvoiceIcon,
  Payment as PaymentIcon,
  AttachMoney as ExpenseIcon,
  Assessment as ReportIcon,
  AccountTree as FeeIcon,
  PersonAdd as LeadIcon,
  Analytics as CampaignIcon2,
  Work as EmployeeIcon,
  EventAvailable as LeaveIcon,
} from '@mui/icons-material'
import { useAuthStore } from '../../store/authStore'
import GlobalSearch from '../common/GlobalSearch'
import NotificationBell from '../common/NotificationBell'

const drawerWidth = 260

const menuItems = [
  { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon />, roles: ['ALL'] },
  {
    label: 'Academic',
    icon: <SchoolIcon />,
    roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'],
    children: [
      { label: 'Courses', path: '/academic/courses', icon: <CourseIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF'] },
      { label: 'Enrollments', path: '/academic/enrollments', icon: <EnrollmentIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'Attendance', path: '/academic/attendance', icon: <AttendanceIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'Grades', path: '/academic/grades', icon: <GradeIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'Exams', path: '/academic/exams', icon: <ExamIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'Certificates', path: '/academic/certificates', icon: <CertificateIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
    ],
  },
  {
    label: 'Finance',
    icon: <FinanceIcon />,
    roles: ['ADMIN', 'FINANCE_STAFF', 'STUDENT'],
    children: [
      { label: 'Fee Structures', path: '/finance/fees', icon: <FeeIcon />, roles: ['ADMIN', 'FINANCE_STAFF'] },
      { label: 'Invoices', path: '/finance/invoices', icon: <InvoiceIcon />, roles: ['ADMIN', 'FINANCE_STAFF', 'STUDENT'] },
      { label: 'Payments', path: '/finance/payments', icon: <PaymentIcon />, roles: ['ADMIN', 'FINANCE_STAFF', 'STUDENT'] },
      { label: 'Expenses', path: '/finance/expenses', icon: <ExpenseIcon />, roles: ['ADMIN', 'FINANCE_STAFF'] },
      { label: 'Reports', path: '/finance/reports', icon: <ReportIcon />, roles: ['ADMIN', 'FINANCE_STAFF'] },
    ],
  },
  {
    label: 'Marketing',
    icon: <MarketingIcon />,
    roles: ['ADMIN', 'HR_OFFICER'],
    children: [
      { label: 'Leads', path: '/marketing/leads', icon: <LeadIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
      { label: 'Campaigns', path: '/marketing/campaigns', icon: <CampaignIcon2 />, roles: ['ADMIN', 'HR_OFFICER'] },
    ],
  },
  {
    label: 'HR',
    icon: <HrIcon />,
    roles: ['ADMIN', 'HR_OFFICER'],
    children: [
      { label: 'Employees', path: '/hr/employees', icon: <EmployeeIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
      { label: 'Leaves', path: '/hr/leaves', icon: <LeaveIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
    ],
  },
]

const MainLayout = () => {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [anchorEl, setAnchorEl] = useState(null)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen)
  }

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget)
  }

  const handleMenuClose = () => {
    setAnchorEl(null)
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const handleNavigate = (path) => {
    navigate(path)
    setMobileOpen(false)
  }

  const hasAccess = (itemRoles) => {
    if (itemRoles.includes('ALL')) return true
    return itemRoles.includes(user?.role)
  }

  const hasAccessToAnyChild = (children) => {
    if (!children) return false
    return children.some((child) => hasAccess(child.roles))
  }

  const drawer = (
    <div>
      <Toolbar sx={{ bgcolor: 'primary.main', color: 'white' }}>
        <Typography variant="h6" noWrap component="div">
          ERP System
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => {
          // For parent items with children, only show if user has access to at least one child
          if (item.children) {
            if (!hasAccessToAnyChild(item.children)) return null
          } else {
            // For items without children, check parent role access
            if (!hasAccess(item.roles)) return null
          }

          if (item.children) {
            const accessibleChildren = item.children.filter((child) => hasAccess(child.roles))
            if (accessibleChildren.length === 0) return null

            return (
              <div key={item.label}>
                <ListItem disablePadding>
                  <ListItemButton>
                    <ListItemIcon>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.label} />
                  </ListItemButton>
                </ListItem>
                {accessibleChildren.map((child) => (
                  <ListItem key={child.path} disablePadding sx={{ pl: 4 }}>
                    <ListItemButton
                      selected={location.pathname === child.path}
                      onClick={() => handleNavigate(child.path)}
                    >
                      <ListItemIcon>{child.icon}</ListItemIcon>
                      <ListItemText primary={child.label} />
                    </ListItemButton>
                  </ListItem>
                ))}
              </div>
            )
          }

          return (
            <ListItem key={item.path} disablePadding>
              <ListItemButton
                selected={location.pathname === item.path}
                onClick={() => handleNavigate(item.path)}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          )
        })}
      </List>
    </div>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 0, mr: 2 }}>
            {menuItems
              .flatMap((item) => (item.children ? item.children : [item]))
              .find((item) => item.path === location.pathname)?.label || 'Dashboard'}
          </Typography>
          <GlobalSearch />
          <NotificationBell />
          <IconButton onClick={handleMenuOpen} sx={{ p: 0 }}>
            <Avatar sx={{ bgcolor: 'secondary.main' }}>
              {user?.username?.charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={() => { handleNavigate('/profile'); handleMenuClose(); }}>
              <ListItemIcon>
                <PersonIcon fontSize="small" />
              </ListItemIcon>
              Profile
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          mt: 8,
          minHeight: '100vh',
          bgcolor: 'background.default',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  )
}

export default MainLayout

