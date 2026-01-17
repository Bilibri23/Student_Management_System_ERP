import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  Container,
  Box,
  Card,
  CardContent,
  Typography,
  Alert,
  CircularProgress,
  Button,
} from '@mui/material'
import { School as SchoolIcon, CheckCircle as CheckCircleIcon } from '@mui/icons-material'
import { authApi } from '../../api/auth'

const VerifyEmail = () => {
  const { token } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const verifyEmail = async () => {
      try {
        await authApi.verifyEmail(token)
        setSuccess(true)
      } catch (err) {
        setError(err.response?.data?.message || 'Email verification failed')
      } finally {
        setLoading(false)
      }
    }

    if (token) {
      verifyEmail()
    } else {
      setError('Invalid verification link')
      setLoading(false)
    }
  }, [token])

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
              Email Verification
            </Typography>

            {loading && (
              <Box display="flex" flexDirection="column" alignItems="center" py={4}>
                <CircularProgress />
                <Typography variant="body2" color="textSecondary" sx={{ mt: 2 }}>
                  Verifying your email...
                </Typography>
              </Box>
            )}

            {success && (
              <Box>
                <Box display="flex" justifyContent="center" mb={2}>
                  <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main' }} />
                </Box>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Your email has been verified successfully!
                </Alert>
                <Button
                  fullWidth
                  variant="contained"
                  onClick={() => navigate('/login')}
                  sx={{ mt: 2 }}
                >
                  Go to Login
                </Button>
              </Box>
            )}

            {error && (
              <Box>
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
                <Box sx={{ textAlign: 'center', mt: 2 }}>
                  <Link to="/login" style={{ textDecoration: 'none' }}>
                    <Typography variant="body2" color="primary">
                      Back to Sign In
                    </Typography>
                  </Link>
                </Box>
              </Box>
            )}
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}

export default VerifyEmail

