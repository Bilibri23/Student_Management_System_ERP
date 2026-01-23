import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Divider,
} from '@mui/material'
import {
  School as SchoolIcon,
  Email as EmailIcon,
  Lock as LockIcon,
  Visibility,
  VisibilityOff,
  AutoGraph as AutoGraphIcon,
  Groups as GroupsIcon,
  AccountBalance as AccountBalanceIcon,
} from '@mui/icons-material'
import { useAuthStore } from '../../store/authStore'

const Login = () => {
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const { login, loading } = useAuthStore()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    const result = await login(formData)
    if (result.success) {
      navigate('/dashboard')
    } else {
      setError(result.error || 'Login failed')
    }
  }

  const features = [
    { icon: <SchoolIcon sx={{ fontSize: 28 }} />, title: 'Academic Management', desc: 'Courses, grades, attendance & more' },
    { icon: <AccountBalanceIcon sx={{ fontSize: 28 }} />, title: 'Finance & Marketing', desc: 'Invoices, payments, campaigns' },
    { icon: <GroupsIcon sx={{ fontSize: 28 }} />, title: 'HR & Administration', desc: 'Employees, leaves, payroll' },
  ]

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
        backgroundSize: '400% 400%',
        animation: 'gradientShift 15s ease infinite',
        '@keyframes gradientShift': {
          '0%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
          '100%': { backgroundPosition: '0% 50%' },
        },
      }}
    >
      {/* Left Side - Branding */}
      <Box
        sx={{
          flex: 1,
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          p: 6,
          color: 'white',
        }}
      >
        <Box
          sx={{
            animation: 'fadeIn 1s ease-out',
            '@keyframes fadeIn': {
              from: { opacity: 0, transform: 'translateY(20px)' },
              to: { opacity: 1, transform: 'translateY(0)' },
            },
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
            <Box
              sx={{
                width: 70,
                height: 70,
                borderRadius: '20px',
                background: 'rgba(255,255,255,0.2)',
                backdropFilter: 'blur(10px)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
              }}
            >
              <SchoolIcon sx={{ fontSize: 40 }} />
            </Box>
            <Box>
              <Typography variant="h3" fontWeight={700}>
                EduERP
              </Typography>
              <Typography variant="body1" sx={{ opacity: 0.9 }}>
                School Management System
              </Typography>
            </Box>
          </Box>

          <Typography variant="h4" fontWeight={600} sx={{ mb: 2, maxWidth: 500 }}>
            Streamline Your Institution's Operations
          </Typography>
          <Typography variant="body1" sx={{ mb: 6, opacity: 0.85, maxWidth: 450, lineHeight: 1.8 }}>
            A comprehensive ERP solution designed for educational institutions. 
            Manage academics, finance, marketing, and HR all in one place.
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            {features.map((feature, index) => (
              <Box
                key={index}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  p: 2,
                  borderRadius: 3,
                  background: 'rgba(255,255,255,0.1)',
                  backdropFilter: 'blur(10px)',
                  transition: 'all 0.3s ease',
                  animation: `slideIn 0.5s ease-out ${index * 0.1}s both`,
                  '@keyframes slideIn': {
                    from: { opacity: 0, transform: 'translateX(-20px)' },
                    to: { opacity: 1, transform: 'translateX(0)' },
                  },
                  '&:hover': {
                    background: 'rgba(255,255,255,0.2)',
                    transform: 'translateX(10px)',
                  },
                }}
              >
                <Box
                  sx={{
                    width: 50,
                    height: 50,
                    borderRadius: 2,
                    background: 'rgba(255,255,255,0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {feature.icon}
                </Box>
                <Box>
                  <Typography variant="subtitle1" fontWeight={600}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.8 }}>
                    {feature.desc}
                  </Typography>
                </Box>
              </Box>
            ))}
          </Box>
        </Box>
      </Box>

      {/* Right Side - Login Form */}
      <Box
        sx={{
          flex: { xs: 1, md: '0 0 480px' },
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 4,
        }}
      >
        <Box
          sx={{
            width: '100%',
            maxWidth: 420,
            p: 5,
            borderRadius: 4,
            background: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            animation: 'slideUp 0.6s ease-out',
            '@keyframes slideUp': {
              from: { opacity: 0, transform: 'translateY(30px)' },
              to: { opacity: 1, transform: 'translateY(0)' },
            },
          }}
        >
          {/* Mobile Logo */}
          <Box
            sx={{
              display: { xs: 'flex', md: 'none' },
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1.5,
              mb: 4,
            }}
          >
            <Box
              sx={{
                width: 50,
                height: 50,
                borderRadius: 2,
                background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
              }}
            >
              <SchoolIcon sx={{ fontSize: 28 }} />
            </Box>
            <Typography
              variant="h5"
              fontWeight={700}
              sx={{
                background: 'linear-gradient(135deg, #6366F1 0%, #EC4899 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              EduERP
            </Typography>
          </Box>

          <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
            Welcome back
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Sign in to continue to your dashboard
          </Typography>

          {error && (
            <Alert
              severity="error"
              sx={{
                mb: 3,
                borderRadius: 2,
                animation: 'shake 0.5s ease-in-out',
                '@keyframes shake': {
                  '0%, 100%': { transform: 'translateX(0)' },
                  '25%': { transform: 'translateX(-5px)' },
                  '75%': { transform: 'translateX(5px)' },
                },
              }}
            >
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username or Email"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              required
              autoFocus
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EmailIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              value={formData.password}
              onChange={handleChange}
              required
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                      size="small"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 3 }}>
              <Link to="/forgot-password" style={{ textDecoration: 'none' }}>
                <Typography
                  variant="body2"
                  sx={{
                    color: 'primary.main',
                    fontWeight: 500,
                    '&:hover': { textDecoration: 'underline' },
                  }}
                >
                  Forgot password?
                </Typography>
              </Link>
            </Box>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                fontSize: '1rem',
                fontWeight: 600,
                background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                boxShadow: '0 10px 40px -10px rgba(99, 102, 241, 0.5)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
                  boxShadow: '0 10px 40px -10px rgba(99, 102, 241, 0.7)',
                },
                '&:disabled': {
                  background: 'linear-gradient(135deg, #94A3B8 0%, #CBD5E1 100%)',
                },
              }}
            >
              {loading ? (
                <CircularProgress size={24} sx={{ color: 'white' }} />
              ) : (
                'Sign In'
              )}
            </Button>

            <Divider sx={{ my: 4 }}>
              <Typography variant="body2" color="text.secondary">
                New to EduERP?
              </Typography>
            </Divider>

            <Link to="/register" style={{ textDecoration: 'none' }}>
              <Button
                fullWidth
                variant="outlined"
                size="large"
                sx={{
                  py: 1.5,
                  fontSize: '1rem',
                  fontWeight: 600,
                  borderWidth: 2,
                  '&:hover': {
                    borderWidth: 2,
                    background: 'rgba(99, 102, 241, 0.04)',
                  },
                }}
              >
                Create an Account
              </Button>
            </Link>
          </Box>

          <Typography
            variant="caption"
            color="text.secondary"
            sx={{ display: 'block', textAlign: 'center', mt: 4 }}
          >
            © 2024 EduERP. Built by Group 13 - ICTU
          </Typography>
        </Box>
      </Box>
    </Box>
  )
}

export default Login

