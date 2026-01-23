import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Avatar,
  Menu,
  MenuItem,
  Collapse,
  Tooltip,
  Badge,
  Chip,
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
  ExpandLess,
  ExpandMore,
  Settings as SettingsIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  KeyboardArrowRight,
} from '@mui/icons-material'
import { useAuthStore } from '../../store/authStore'
import GlobalSearch from '../common/GlobalSearch'
import NotificationBell from '../common/NotificationBell'

const drawerWidth = 280

const menuItems = [
  { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon />, roles: ['ALL'], color: '#6366F1' },
  {
    label: 'Academic',
    icon: <SchoolIcon />,
    roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'],
    color: '#8B5CF6',
    children: [
      { label: 'Courses', path: '/academic/courses', icon: <CourseIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF'] },
      { label: 'Course Catalog', path: '/academic/catalog', icon: <CourseIcon />, roles: ['STUDENT'] },
      { label: 'My Enrollments', path: '/academic/enrollments', icon: <EnrollmentIcon />, roles: ['STUDENT'] },
      { label: 'Enrollments', path: '/academic/enrollments', icon: <EnrollmentIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF'] },
      { label: 'Attendance', path: '/academic/attendance', icon: <AttendanceIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'My Grades', path: '/academic/grades', icon: <GradeIcon />, roles: ['STUDENT'] },
      { label: 'Grades', path: '/academic/grades', icon: <GradeIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF'] },
      { label: 'Exams', path: '/academic/exams', icon: <ExamIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
      { label: 'Certificates', path: '/academic/certificates', icon: <CertificateIcon />, roles: ['ADMIN', 'ACADEMIC_STAFF', 'STUDENT'] },
    ],
  },
  {
    label: 'Finance',
    icon: <FinanceIcon />,
    roles: ['ADMIN', 'FINANCE_STAFF', 'STUDENT'],
    color: '#10B981',
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
    color: '#F59E0B',
    children: [
      { label: 'Leads', path: '/marketing/leads', icon: <LeadIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
      { label: 'Campaigns', path: '/marketing/campaigns', icon: <CampaignIcon2 />, roles: ['ADMIN', 'HR_OFFICER'] },
    ],
  },
  {
    label: 'HR',
    icon: <HrIcon />,
    roles: ['ADMIN', 'HR_OFFICER'],
    color: '#EC4899',
    children: [
      { label: 'Employees', path: '/hr/employees', icon: <EmployeeIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
      { label: 'Leaves', path: '/hr/leaves', icon: <LeaveIcon />, roles: ['ADMIN', 'HR_OFFICER'] },
    ],
  },
]

const MainLayout = () => {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [anchorEl, setAnchorEl] = useState(null)
  const [expandedMenus, setExpandedMenus] = useState({})
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

  const toggleExpand = (label) => {
    setExpandedMenus(prev => ({
      ...prev,
      [label]: !prev[label]
    }))
  }

  const hasAccess = (itemRoles) => {
    if (itemRoles.includes('ALL')) return true
    return itemRoles.includes(user?.role)
  }

  const hasAccessToAnyChild = (children) => {
    if (!children) return false
    return children.some((child) => hasAccess(child.roles))
  }

  const isMenuActive = (item) => {
    if (item.path) return location.pathname === item.path
    if (item.children) {
      return item.children.some(child => location.pathname === child.path)
    }
    return false
  }

  const getRoleColor = (role) => {
    const colors = {
      ADMIN: '#EF4444',
      ACADEMIC_STAFF: '#8B5CF6',
      FINANCE_STAFF: '#10B981',
      HR_OFFICER: '#F59E0B',
      STUDENT: '#3B82F6',
    }
    return colors[role] || '#6366F1'
  }

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Logo Section */}
      <Box
        sx={{
          p: 3,
          background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
          color: 'white',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              background: 'rgba(255,255,255,0.2)',
              backdropFilter: 'blur(10px)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <SchoolIcon sx={{ fontSize: 28 }} />
          </Box>
          <Box>
            <Typography variant="h6" fontWeight={700} sx={{ lineHeight: 1.2 }}>
              EduERP
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.8 }}>
              School Management
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* User Info Card */}
      <Box sx={{ p: 2 }}>
        <Box
          sx={{
            p: 2,
            borderRadius: 3,
            background: 'linear-gradient(135deg, #F8FAFC 0%, #EEF2FF 100%)',
            border: '1px solid #E2E8F0',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Avatar
              sx={{
                width: 44,
                height: 44,
                background: `linear-gradient(135deg, ${getRoleColor(user?.role)} 0%, ${getRoleColor(user?.role)}99 100%)`,
                fontWeight: 600,
                fontSize: '1.1rem',
              }}
            >
              {user?.username?.charAt(0).toUpperCase()}
            </Avatar>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography variant="subtitle2" fontWeight={600} noWrap>
                {user?.username}
              </Typography>
              <Chip
                label={user?.role?.replace('_', ' ')}
                size="small"
                sx={{
                  height: 20,
                  fontSize: '0.65rem',
                  fontWeight: 600,
                  background: `${getRoleColor(user?.role)}20`,
                  color: getRoleColor(user?.role),
                  border: `1px solid ${getRoleColor(user?.role)}40`,
                }}
              />
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Navigation */}
      <Box sx={{ flex: 1, overflow: 'auto', px: 2, py: 1 }}>
        <Typography
          variant="overline"
          sx={{ px: 2, color: 'text.secondary', fontSize: '0.65rem', letterSpacing: 1 }}
        >
          Main Menu
        </Typography>
        <List sx={{ py: 1 }}>
          {menuItems.map((item) => {
            if (item.children) {
              if (!hasAccessToAnyChild(item.children)) return null
            } else {
              if (!hasAccess(item.roles)) return null
            }

            const isActive = isMenuActive(item)
            const isExpanded = expandedMenus[item.label] || isActive

            if (item.children) {
              const accessibleChildren = item.children.filter((child) => hasAccess(child.roles))
              if (accessibleChildren.length === 0) return null

              return (
                <Box key={item.label} sx={{ mb: 0.5 }}>
                  <ListItemButton
                    onClick={() => toggleExpand(item.label)}
                    sx={{
                      borderRadius: 2,
                      mb: 0.5,
                      py: 1.2,
                      transition: 'all 0.2s ease',
                      ...(isActive && {
                        background: `${item.color}10`,
                        '& .MuiListItemIcon-root': { color: item.color },
                        '& .MuiListItemText-primary': { color: item.color, fontWeight: 600 },
                      }),
                      '&:hover': {
                        background: `${item.color}15`,
                      },
                    }}
                  >
                    <ListItemIcon
                      sx={{
                        minWidth: 40,
                        color: isActive ? item.color : 'text.secondary',
                      }}
                    >
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText
                      primary={item.label}
                      primaryTypographyProps={{
                        fontWeight: isActive ? 600 : 500,
                        fontSize: '0.9rem',
                      }}
                    />
                    <Box
                      sx={{
                        transition: 'transform 0.2s ease',
                        transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)',
                        color: 'text.secondary',
                      }}
                    >
                      <ExpandMore fontSize="small" />
                    </Box>
                  </ListItemButton>
                  <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                    <List disablePadding sx={{ pl: 2 }}>
                      {accessibleChildren.map((child) => {
                        const isChildActive = location.pathname === child.path
                        return (
                          <ListItemButton
                            key={child.path}
                            onClick={() => handleNavigate(child.path)}
                            sx={{
                              borderRadius: 2,
                              py: 1,
                              mb: 0.5,
                              position: 'relative',
                              transition: 'all 0.2s ease',
                              ...(isChildActive && {
                                background: `linear-gradient(135deg, ${item.color} 0%, ${item.color}CC 100%)`,
                                color: 'white',
                                boxShadow: `0 4px 12px ${item.color}40`,
                                '& .MuiListItemIcon-root': { color: 'white' },
                              }),
                              '&:hover': {
                                background: isChildActive
                                  ? `linear-gradient(135deg, ${item.color} 0%, ${item.color}CC 100%)`
                                  : `${item.color}10`,
                              },
                            }}
                          >
                            <ListItemIcon
                              sx={{
                                minWidth: 36,
                                color: isChildActive ? 'white' : 'text.secondary',
                              }}
                            >
                              {child.icon}
                            </ListItemIcon>
                            <ListItemText
                              primary={child.label}
                              primaryTypographyProps={{
                                fontWeight: isChildActive ? 600 : 400,
                                fontSize: '0.85rem',
                              }}
                            />
                            {isChildActive && (
                              <KeyboardArrowRight sx={{ fontSize: 18 }} />
                            )}
                          </ListItemButton>
                        )
                      })}
                    </List>
                  </Collapse>
                </Box>
              )
            }

            return (
              <ListItemButton
                key={item.path}
                onClick={() => handleNavigate(item.path)}
                sx={{
                  borderRadius: 2,
                  mb: 0.5,
                  py: 1.2,
                  transition: 'all 0.2s ease',
                  ...(isActive && {
                    background: `linear-gradient(135deg, ${item.color} 0%, ${item.color}CC 100%)`,
                    color: 'white',
                    boxShadow: `0 4px 12px ${item.color}40`,
                    '& .MuiListItemIcon-root': { color: 'white' },
                  }),
                  '&:hover': {
                    background: isActive
                      ? `linear-gradient(135deg, ${item.color} 0%, ${item.color}CC 100%)`
                      : `${item.color}10`,
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 40,
                    color: isActive ? 'white' : 'text.secondary',
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.label}
                  primaryTypographyProps={{
                    fontWeight: isActive ? 600 : 500,
                    fontSize: '0.9rem',
                  }}
                />
              </ListItemButton>
            )
          })}
        </List>
      </Box>

      {/* Footer */}
      <Box sx={{ p: 2, borderTop: '1px solid #E2E8F0' }}>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textAlign: 'center' }}>
          © 2024 EduERP v1.0
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textAlign: 'center', fontSize: '0.65rem' }}>
          Group 13 - ICTU
        </Typography>
      </Box>
    </Box>
  )

  const getPageTitle = () => {
    const allItems = menuItems.flatMap((item) => (item.children ? item.children : [item]))
    return allItems.find((item) => item.path === location.pathname)?.label || 'Dashboard'
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: '#F8FAFC' }}>
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
          bgcolor: 'rgba(255, 255, 255, 0.8)',
          backdropFilter: 'blur(20px)',
          borderBottom: '1px solid #E2E8F0',
        }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2, display: { sm: 'none' }, color: 'text.primary' }}
            >
              <MenuIcon />
            </IconButton>
            <Box>
              <Typography
                variant="h5"
                noWrap
                component="div"
                sx={{
                  fontWeight: 700,
                  color: 'text.primary',
                  background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              >
                {getPageTitle()}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
              </Typography>
            </Box>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GlobalSearch />
            <NotificationBell />
            <Tooltip title="Account settings">
              <IconButton
                onClick={handleMenuOpen}
                sx={{
                  p: 0.5,
                  border: '2px solid transparent',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    border: '2px solid #6366F1',
                  },
                }}
              >
                <Avatar
                  sx={{
                    width: 38,
                    height: 38,
                    background: `linear-gradient(135deg, ${getRoleColor(user?.role)} 0%, ${getRoleColor(user?.role)}99 100%)`,
                    fontWeight: 600,
                  }}
                >
                  {user?.username?.charAt(0).toUpperCase()}
                </Avatar>
              </IconButton>
            </Tooltip>
          </Box>
          
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            PaperProps={{
              sx: {
                mt: 1.5,
                minWidth: 200,
                borderRadius: 3,
                boxShadow: '0 10px 40px rgba(0,0,0,0.1)',
                border: '1px solid #E2E8F0',
              },
            }}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <Box sx={{ px: 2, py: 1.5, borderBottom: '1px solid #E2E8F0' }}>
              <Typography variant="subtitle2" fontWeight={600}>
                {user?.username}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {user?.email}
              </Typography>
            </Box>
            <MenuItem
              onClick={() => { handleNavigate('/profile'); handleMenuClose(); }}
              sx={{ py: 1.5, mx: 1, my: 0.5, borderRadius: 2 }}
            >
              <ListItemIcon>
                <PersonIcon fontSize="small" sx={{ color: '#6366F1' }} />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Profile Settings</Typography>
            </MenuItem>
            <MenuItem
              onClick={handleLogout}
              sx={{
                py: 1.5,
                mx: 1,
                my: 0.5,
                borderRadius: 2,
                color: '#EF4444',
                '&:hover': { bgcolor: '#FEF2F2' },
              }}
            >
              <ListItemIcon>
                <LogoutIcon fontSize="small" sx={{ color: '#EF4444' }} />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Sign Out</Typography>
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
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              border: 'none',
              boxShadow: '4px 0 24px rgba(0,0,0,0.08)',
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              border: 'none',
              boxShadow: '4px 0 24px rgba(0,0,0,0.05)',
            },
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
          p: { xs: 2, sm: 3 },
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          mt: 8,
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #F8FAFC 0%, #EEF2FF 100%)',
        }}
      >
        <Box
          sx={{
            animation: 'fadeIn 0.4s ease-out',
            '@keyframes fadeIn': {
              from: { opacity: 0, transform: 'translateY(10px)' },
              to: { opacity: 1, transform: 'translateY(0)' },
            },
          }}
        >
          <Outlet />
        </Box>
      </Box>
    </Box>
  )
}

export default MainLayout

