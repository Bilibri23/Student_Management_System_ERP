import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  CardActions,
  Grid,
  Chip,
  TextField,
  InputAdornment,
  Avatar,
  Skeleton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Divider,
  LinearProgress,
} from '@mui/material'
import {
  Search as SearchIcon,
  MenuBook as CourseIcon,
  Schedule as ScheduleIcon,
  Person as InstructorIcon,
  Room as RoomIcon,
  People as CapacityIcon,
  CheckCircle as EnrolledIcon,
  Add as EnrollIcon,
  Close as CloseIcon,
  Star as StarIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import { useAuthStore } from '../../store/authStore'
import PageHeader from '../../components/common/PageHeader'

const CourseCatalog = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCourse, setSelectedCourse] = useState(null)
  const [enrollDialogOpen, setEnrollDialogOpen] = useState(false)

  const { data: coursesData, isLoading: coursesLoading } = useQuery({
    queryKey: ['courses-catalog'],
    queryFn: () => academicApi.getCourses({ page: 0, size: 100 }).then(res => res.data.data),
  })

  const { data: myEnrollments } = useQuery({
    queryKey: ['my-enrollments'],
    queryFn: () => academicApi.getEnrollments({ studentId: user?.id, page: 0, size: 100 }).then(res => res.data.data),
    enabled: !!user?.id,
  })

  const enrollMutation = useMutation({
    mutationFn: (courseId) => academicApi.enrollStudent({ 
      studentId: user?.id, 
      courseId: courseId,
      notes: 'Self-enrolled via course catalog'
    }),
    onSuccess: () => {
      queryClient.invalidateQueries(['my-enrollments'])
      queryClient.invalidateQueries(['courses-catalog'])
      setEnrollDialogOpen(false)
      setSelectedCourse(null)
    },
  })

  const enrolledCourseIds = myEnrollments?.content?.map(e => e.course?.id) || []

  const filteredCourses = coursesData?.content?.filter(course => {
    const searchLower = searchTerm.toLowerCase()
    return (
      course.courseName?.toLowerCase().includes(searchLower) ||
      course.courseCode?.toLowerCase().includes(searchLower) ||
      course.department?.toLowerCase().includes(searchLower)
    )
  }) || []

  const handleEnrollClick = (course) => {
    setSelectedCourse(course)
    setEnrollDialogOpen(true)
  }

  const handleConfirmEnroll = () => {
    if (selectedCourse) {
      enrollMutation.mutate(selectedCourse.id)
    }
  }

  const isEnrolled = (courseId) => enrolledCourseIds.includes(courseId)

  const getCapacityPercentage = (current, max) => {
    if (!max) return 0
    return Math.min((current / max) * 100, 100)
  }

  const getCapacityColor = (percentage) => {
    if (percentage >= 90) return '#EF4444'
    if (percentage >= 70) return '#F59E0B'
    return '#10B981'
  }

  if (coursesLoading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={80} sx={{ mb: 3, borderRadius: 3 }} />
        <Skeleton variant="rounded" height={56} sx={{ mb: 4, borderRadius: 2 }} />
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid item xs={12} sm={6} md={4} key={i}>
              <Skeleton variant="rounded" height={280} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
      </Box>
    )
  }

  return (
    <Box>
      <PageHeader
        title="Course Catalog"
        subtitle="Browse and enroll in available courses"
        badge={`${filteredCourses.length} courses available`}
      />

      {/* Search Bar */}
      <Card sx={{ mb: 4, border: '1px solid #E2E8F0' }}>
        <CardContent sx={{ py: 2 }}>
          <TextField
            fullWidth
            placeholder="Search courses by name, code, or department..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: '#94A3B8' }} />
                </InputAdornment>
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: 2,
                bgcolor: '#F8FAFC',
              },
            }}
          />
        </CardContent>
      </Card>

      {/* My Enrollments Summary */}
      {myEnrollments?.content?.length > 0 && (
        <Card
          sx={{
            mb: 4,
            background: 'linear-gradient(135deg, #6366F110 0%, #8B5CF610 100%)',
            border: '1px solid #6366F130',
          }}
        >
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <Avatar sx={{ bgcolor: '#6366F1', width: 40, height: 40 }}>
                <EnrolledIcon />
              </Avatar>
              <Box>
                <Typography variant="h6" fontWeight={600}>
                  My Enrolled Courses
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  You are currently enrolled in {myEnrollments.content.length} course(s)
                </Typography>
              </Box>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {myEnrollments.content.slice(0, 5).map((enrollment) => (
                <Chip
                  key={enrollment.id}
                  label={enrollment.course?.courseCode || 'Course'}
                  size="small"
                  sx={{
                    bgcolor: '#6366F120',
                    color: '#6366F1',
                    fontWeight: 500,
                  }}
                />
              ))}
              {myEnrollments.content.length > 5 && (
                <Chip
                  label={`+${myEnrollments.content.length - 5} more`}
                  size="small"
                  sx={{ bgcolor: '#E2E8F0', color: '#64748B' }}
                />
              )}
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Course Grid */}
      <Grid container spacing={3}>
        {filteredCourses.map((course) => {
          const enrolled = isEnrolled(course.id)
          const capacityPercent = getCapacityPercentage(course.currentEnrollment, course.maxCapacity)
          const isFull = capacityPercent >= 100

          return (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'all 0.3s ease',
                  border: enrolled ? '2px solid #10B981' : '1px solid #E2E8F0',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 12px 24px rgba(0,0,0,0.1)',
                  },
                }}
              >
                {/* Course Header */}
                <Box
                  sx={{
                    background: enrolled
                      ? 'linear-gradient(135deg, #10B981 0%, #34D399 100%)'
                      : 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                    p: 2,
                    color: 'white',
                  }}
                >
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Typography variant="h6" fontWeight={700} sx={{ flex: 1 }}>
                      {course.courseCode}
                    </Typography>
                    {enrolled && (
                      <Chip
                        icon={<EnrolledIcon sx={{ color: 'white !important', fontSize: 16 }} />}
                        label="Enrolled"
                        size="small"
                        sx={{
                          bgcolor: 'rgba(255,255,255,0.2)',
                          color: 'white',
                          fontWeight: 600,
                        }}
                      />
                    )}
                  </Box>
                  <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.5 }}>
                    {course.courseName}
                  </Typography>
                </Box>

                <CardContent sx={{ flex: 1 }}>
                  {/* Course Details */}
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CourseIcon sx={{ fontSize: 18, color: '#64748B' }} />
                      <Typography variant="body2" color="text.secondary">
                        {course.credits} Credits • {course.department}
                      </Typography>
                    </Box>

                    {course.scheduleDay && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <ScheduleIcon sx={{ fontSize: 18, color: '#64748B' }} />
                        <Typography variant="body2" color="text.secondary">
                          {course.scheduleDay} {course.startTime && `• ${course.startTime} - ${course.endTime}`}
                        </Typography>
                      </Box>
                    )}

                    {course.room && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <RoomIcon sx={{ fontSize: 18, color: '#64748B' }} />
                        <Typography variant="body2" color="text.secondary">
                          Room {course.room}
                        </Typography>
                      </Box>
                    )}

                    {/* Capacity Bar */}
                    <Box sx={{ mt: 1 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="caption" color="text.secondary">
                          Enrollment
                        </Typography>
                        <Typography variant="caption" fontWeight={600} color={getCapacityColor(capacityPercent)}>
                          {course.currentEnrollment || 0} / {course.maxCapacity || '∞'}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={capacityPercent}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          bgcolor: '#E2E8F0',
                          '& .MuiLinearProgress-bar': {
                            bgcolor: getCapacityColor(capacityPercent),
                            borderRadius: 3,
                          },
                        }}
                      />
                    </Box>
                  </Box>
                </CardContent>

                <Divider />

                <CardActions sx={{ p: 2 }}>
                  {enrolled ? (
                    <Button
                      fullWidth
                      variant="outlined"
                      disabled
                      startIcon={<EnrolledIcon />}
                      sx={{
                        borderColor: '#10B981',
                        color: '#10B981',
                        '&.Mui-disabled': {
                          borderColor: '#10B981',
                          color: '#10B981',
                        },
                      }}
                    >
                      Already Enrolled
                    </Button>
                  ) : isFull ? (
                    <Button
                      fullWidth
                      variant="outlined"
                      disabled
                      sx={{ color: '#EF4444', borderColor: '#EF4444' }}
                    >
                      Course Full
                    </Button>
                  ) : (
                    <Button
                      fullWidth
                      variant="contained"
                      startIcon={<EnrollIcon />}
                      onClick={() => handleEnrollClick(course)}
                      sx={{
                        background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
                        '&:hover': {
                          background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
                        },
                      }}
                    >
                      Enroll Now
                    </Button>
                  )}
                </CardActions>
              </Card>
            </Grid>
          )
        })}
      </Grid>

      {filteredCourses.length === 0 && (
        <Card sx={{ p: 6, textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <CourseIcon sx={{ fontSize: 64, color: '#CBD5E1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            No courses found
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Try adjusting your search criteria
          </Typography>
        </Card>
      )}

      {/* Enrollment Confirmation Dialog */}
      <Dialog
        open={enrollDialogOpen}
        onClose={() => setEnrollDialogOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Avatar sx={{ bgcolor: '#6366F120', color: '#6366F1' }}>
              <EnrollIcon />
            </Avatar>
            <Typography variant="h6" fontWeight={600}>
              Confirm Enrollment
            </Typography>
          </Box>
          <IconButton onClick={() => setEnrollDialogOpen(false)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          {selectedCourse && (
            <Box>
              <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
                You are about to enroll in this course. Please review the details below.
              </Alert>
              <Card sx={{ bgcolor: '#F8FAFC', border: '1px solid #E2E8F0' }}>
                <CardContent>
                  <Typography variant="h6" fontWeight={700} color="primary">
                    {selectedCourse.courseCode}
                  </Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedCourse.courseName}
                  </Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Typography variant="caption" color="text.secondary">Credits</Typography>
                      <Typography variant="body2" fontWeight={600}>{selectedCourse.credits}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="caption" color="text.secondary">Department</Typography>
                      <Typography variant="body2" fontWeight={600}>{selectedCourse.department}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="caption" color="text.secondary">Schedule</Typography>
                      <Typography variant="body2" fontWeight={600}>
                        {selectedCourse.scheduleDay || 'TBD'}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="caption" color="text.secondary">Room</Typography>
                      <Typography variant="body2" fontWeight={600}>
                        {selectedCourse.room || 'TBD'}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 0 }}>
          <Button
            onClick={() => setEnrollDialogOpen(false)}
            variant="outlined"
            sx={{ borderColor: '#E2E8F0', color: '#64748B' }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirmEnroll}
            variant="contained"
            disabled={enrollMutation.isLoading}
            sx={{
              background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
              minWidth: 120,
            }}
          >
            {enrollMutation.isLoading ? <CircularProgress size={20} sx={{ color: 'white' }} /> : 'Confirm Enrollment'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default CourseCatalog
