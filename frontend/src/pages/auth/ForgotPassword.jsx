import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Container,
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material'
import { School as SchoolIcon } from '@mui/icons-material'
import { authApi } from '../../api/auth'

const ForgotPassword = () => {
  const [step, setStep] = useState(1) // 1 = email input, 2 = OTP and password reset
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleRequestOtp = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await authApi.forgotPassword({ email })
      setSuccess(true)
      setStep(2)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send OTP code')
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e) => {
    e.preventDefault()
    setError('')

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }

    setLoading(true)

    try {
      await authApi.resetPassword({
        token: otp,
        newPassword: newPassword,
      })
      setSuccess(true)
      setTimeout(() => {
        navigate('/login')
      }, 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
          <SchoolIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Typography component="h1" variant="h4">
            ERP System
          </Typography>
        </Box>
        <Card sx={{ width: '100%', mt: 2 }}>
          <CardContent>
            <Typography component="h2" variant="h5" align="center" gutterBottom>
              {step === 1 ? 'Forgot Password' : 'Reset Password'}
            </Typography>
            {error && (
              <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
                {error}
              </Alert>
            )}
            {success && step === 1 && (
              <Alert severity="success" sx={{ mt: 2, mb: 2 }}>
                OTP code has been sent to your email. Please check your inbox.
              </Alert>
            )}
            {success && step === 2 && (
              <Alert severity="success" sx={{ mt: 2, mb: 2 }}>
                Password reset successfully! Redirecting to login...
              </Alert>
            )}

            {step === 1 ? (
              <Box component="form" onSubmit={handleRequestOtp} sx={{ mt: 1 }}>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Enter your email address and we'll send you an OTP code to reset your password.
                </Typography>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="email"
                  label="Email Address"
                  name="email"
                  type="email"
                  autoFocus
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 3, mb: 2 }}
                  disabled={loading || success}
                >
                  {loading ? <CircularProgress size={24} /> : 'Send OTP Code'}
                </Button>
                <Box sx={{ textAlign: 'center' }}>
                  <Link to="/login" style={{ textDecoration: 'none' }}>
                    <Typography variant="body2" color="primary">
                      Back to Sign In
                    </Typography>
                  </Link>
                </Box>
              </Box>
            ) : (
              <Box component="form" onSubmit={handleResetPassword} sx={{ mt: 1 }}>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Enter the OTP code sent to {email} and your new password.
                </Typography>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="otp"
                  label="OTP Code"
                  name="otp"
                  autoFocus
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  placeholder="Enter 6-digit OTP"
                  inputProps={{ maxLength: 6 }}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="newPassword"
                  label="New Password"
                  type="password"
                  id="newPassword"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="confirmPassword"
                  label="Confirm New Password"
                  type="password"
                  id="confirmPassword"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 3, mb: 2 }}
                  disabled={loading || success}
                >
                  {loading ? <CircularProgress size={24} /> : 'Reset Password'}
                </Button>
                <Divider sx={{ my: 2 }} />
                <Button
                  fullWidth
                  variant="text"
                  onClick={() => {
                    setStep(1)
                    setOtp('')
                    setNewPassword('')
                    setConfirmPassword('')
                    setSuccess(false)
                    setError('')
                  }}
                  disabled={loading}
                >
                  Use Different Email
                </Button>
              </Box>
            )}
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}

export default ForgotPassword
