import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  MenuItem,
  InputAdornment,
  IconButton,
  Grid,
  Stepper,
  Step,
  StepLabel,
  Divider,
} from '@mui/material'
import {
  School as SchoolIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Lock as LockIcon,
  Visibility,
  VisibilityOff,
  Badge as BadgeIcon,
  CheckCircle as CheckCircleIcon,
  ArrowBack as ArrowBackIcon,
  ArrowForward as ArrowForwardIcon,
} from '@mui/icons-material'
import { authApi } from '../../api/auth'

const roles = [
  { value: 'STUDENT', label: 'Student', color: '#3B82F6' },
  { value: 'ACADEMIC_STAFF', label: 'Academic Staff', color: '#8B5CF6' },
  { value: 'FINANCE_STAFF', label: 'Finance Staff', color: '#10B981' },
  { value: 'HR_OFFICER', label: 'HR Officer', color: '#F59E0B' },
]

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    role: 'STUDENT',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const [activeStep, setActiveStep] = useState(0)
  const navigate = useNavigate()

  const steps = ['Personal Info', 'Account Details', 'Complete']

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
    setError('')
  }

  const handleNext = () => {
    if (activeStep === 0) {
      if (!formData.firstName || !formData.lastName) {
        setError('Please fill in all fields')
        return
      }
    }
    if (activeStep === 1) {
      if (!formData.username || !formData.email || !formData.password || !formData.confirmPassword) {
        setError('Please fill in all fields')
        return
      }
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match')
        return
      }
      if (formData.password.length < 6) {
        setError('Password must be at least 6 characters')
        return
      }
    }
    setError('')
    setActiveStep((prev) => prev + 1)
  }

  const handleBack = () => {
    setActiveStep((prev) => prev - 1)
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await authApi.register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
        role: formData.role,
      })
      setSuccess(true)
      setTimeout(() => {
        navigate('/login')
      }, 3000)
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Let's start with your basic information
            </Typography>
            <TextField
              fullWidth
              label="First Name"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              required
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PersonIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              fullWidth
              label="Last Name"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              required
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PersonIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              fullWidth
              select
              label="Select Your Role"
              name="role"
              value={formData.role}
              onChange={handleChange}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <BadgeIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            >
              {roles.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Box
                      sx={{
                        width: 8,
                        height: 8,
                        borderRadius: '50%',
                        bgcolor: option.color,
                      }}
                    />
                    {option.label}
                  </Box>
                </MenuItem>
              ))}
            </TextField>
          </Box>
        )
      case 1:
        return (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Create your account credentials
            </Typography>
            <TextField
              fullWidth
              label="Username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PersonIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              fullWidth
              label="Email Address"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              required
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
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              fullWidth
              label="Confirm Password"
              name="confirmPassword"
              type={showConfirmPassword ? 'text' : 'password'}
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowConfirmPassword(!showConfirmPassword)} edge="end" size="small">
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </Box>
        )
      case 2:
        return (
          <Box sx={{ textAlign: 'center', py: 2 }}>
            <Box
              sx={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                mx: 'auto',
                mb: 3,
                boxShadow: '0 10px 40px rgba(16, 185, 129, 0.3)',
              }}
            >
              <CheckCircleIcon sx={{ fontSize: 40, color: 'white' }} />
            </Box>
            <Typography variant="h5" fontWeight={600} gutterBottom>
              Almost there!
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Review your information and click "Create Account" to complete registration.
            </Typography>
            <Box
              sx={{
                p: 3,
                borderRadius: 3,
                bgcolor: '#F8FAFC',
                border: '1px solid #E2E8F0',
                textAlign: 'left',
              }}
            >
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">Name</Typography>
                  <Typography variant="body2" fontWeight={500}>{formData.firstName} {formData.lastName}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">Role</Typography>
                  <Typography variant="body2" fontWeight={500}>{roles.find(r => r.value === formData.role)?.label}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">Username</Typography>
                  <Typography variant="body2" fontWeight={500}>{formData.username}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">Email</Typography>
                  <Typography variant="body2" fontWeight={500}>{formData.email}</Typography>
                </Grid>
              </Grid>
            </Box>
          </Box>
        )
      default:
        return null
    }
  }

  if (success) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
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
        <Box
          sx={{
            p: 6,
            borderRadius: 4,
            background: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            textAlign: 'center',
            maxWidth: 400,
          }}
        >
          <Box
            sx={{
              width: 100,
              height: 100,
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              mx: 'auto',
              mb: 3,
              animation: 'pulse 2s infinite',
              '@keyframes pulse': {
                '0%, 100%': { transform: 'scale(1)' },
                '50%': { transform: 'scale(1.05)' },
              },
            }}
          >
            <CheckCircleIcon sx={{ fontSize: 50, color: 'white' }} />
          </Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Registration Successful!
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Please check your email to verify your account. Redirecting to login...
          </Typography>
          <CircularProgress size={24} sx={{ color: '#6366F1' }} />
        </Box>
      </Box>
    )
  }

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
        <Box sx={{ maxWidth: 500 }}>
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

          <Typography variant="h4" fontWeight={600} sx={{ mb: 2 }}>
            Join Our Platform
          </Typography>
          <Typography variant="body1" sx={{ opacity: 0.85, lineHeight: 1.8 }}>
            Create your account to access the comprehensive ERP system. 
            Manage academics, finance, marketing, and HR all in one place.
          </Typography>
        </Box>
      </Box>

      {/* Right Side - Registration Form */}
      <Box
        sx={{
          flex: { xs: 1, md: '0 0 520px' },
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 4,
        }}
      >
        <Box
          sx={{
            width: '100%',
            maxWidth: 460,
            p: 5,
            borderRadius: 4,
            background: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
          }}
        >
          {/* Mobile Logo */}
          <Box
            sx={{
              display: { xs: 'flex', md: 'none' },
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1.5,
              mb: 3,
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
            Create Account
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Fill in your details to get started
          </Typography>

          {/* Stepper */}
          <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel
                  sx={{
                    '& .MuiStepLabel-label': {
                      fontSize: '0.75rem',
                      fontWeight: 500,
                    },
                  }}
                >
                  {label}
                </StepLabel>
              </Step>
            ))}
          </Stepper>

          {error && (
            <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            {renderStepContent()}

            <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
              {activeStep > 0 && (
                <Button
                  onClick={handleBack}
                  variant="outlined"
                  startIcon={<ArrowBackIcon />}
                  sx={{
                    flex: 1,
                    py: 1.5,
                    borderColor: '#E2E8F0',
                    color: '#64748B',
                    '&:hover': {
                      borderColor: '#CBD5E1',
                      bgcolor: '#F8FAFC',
                    },
                  }}
                >
                  Back
                </Button>
              )}
              {activeStep < 2 ? (
                <Button
                  onClick={handleNext}
                  variant="contained"
                  endIcon={<ArrowForwardIcon />}
                  sx={{
                    flex: 1,
                    py: 1.5,
                    background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                    boxShadow: '0 10px 40px -10px rgba(99, 102, 241, 0.5)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
                    },
                  }}
                >
                  Continue
                </Button>
              ) : (
                <Button
                  type="submit"
                  variant="contained"
                  disabled={loading}
                  sx={{
                    flex: 1,
                    py: 1.5,
                    background: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
                    boxShadow: '0 10px 40px -10px rgba(16, 185, 129, 0.5)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #059669 0%, #10B981 100%)',
                    },
                  }}
                >
                  {loading ? <CircularProgress size={24} sx={{ color: 'white' }} /> : 'Create Account'}
                </Button>
              )}
            </Box>
          </Box>

          <Divider sx={{ my: 3 }}>
            <Typography variant="body2" color="text.secondary">
              Already registered?
            </Typography>
          </Divider>

          <Link to="/login" style={{ textDecoration: 'none' }}>
            <Button
              fullWidth
              variant="outlined"
              sx={{
                py: 1.5,
                fontWeight: 600,
                borderWidth: 2,
                '&:hover': {
                  borderWidth: 2,
                  background: 'rgba(99, 102, 241, 0.04)',
                },
              }}
            >
              Sign In Instead
            </Button>
          </Link>

          <Typography
            variant="caption"
            color="text.secondary"
            sx={{ display: 'block', textAlign: 'center', mt: 3 }}
          >
            © 2024 EduERP. Built by Group 13 - ICTU
          </Typography>
        </Box>
      </Box>
    </Box>
  )
}

export default Register

