import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  MenuItem,
  Grid,
  Card,
  CardContent,
  Avatar,
  Skeleton,
} from '@mui/material'
import {
  Add as AddIcon,
  Assignment as EnrollmentIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
  HourglassEmpty as PendingIcon,
  ThumbUp as ApproveIcon,
} from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import { userApi } from '../../api/user'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import PageHeader from '../../components/common/PageHeader'
import { useAuthStore } from '../../store/authStore'

const Enrollments = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedEnrollment, setSelectedEnrollment] = useState(null)
  const [formData, setFormData] = useState({
    studentId: '',
    courseId: '',
    notes: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['enrollments', page, pageSize],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      return academicApi.getEnrollments(params).then(res => res.data.data)
    },
  })

  // Fetch students for dropdown
  const { data: studentsData } = useQuery({
    queryKey: ['students-list'],
    queryFn: () => userApi.getStudents({ page: 0, size: 100 }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  // Fetch courses for dropdown
  const { data: coursesData } = useQuery({
    queryKey: ['courses-list'],
    queryFn: () => academicApi.getCourses({ page: 0, size: 100 }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  const students = studentsData?.content || []
  const courses = coursesData?.content || []

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.enrollStudent(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['enrollments'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => academicApi.dropEnrollment(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['enrollments'])
      setOpenDeleteDialog(false)
      setSelectedEnrollment(null)
    },
  })

  const approveMutation = useMutation({
    mutationFn: (id) => academicApi.approveEnrollment(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['enrollments'])
    },
  })

  const handleApprove = (enrollment) => {
    if (!enrollment.approved) {
      approveMutation.mutate(enrollment.id)
    }
  }

  const resetForm = () => {
    setFormData({
      studentId: '',
      courseId: '',
      notes: '',
    })
  }

  const handleDelete = (enrollment) => {
    setSelectedEnrollment(enrollment)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    createMutation.mutate({
      ...formData,
      studentId: parseInt(formData.studentId),
      courseId: parseInt(formData.courseId),
    })
  }

  const columns = [
    {
      field: 'student',
      headerName: 'Student',
      render: (value) => value?.fullName || '-',
    },
    {
      field: 'course',
      headerName: 'Course',
      render: (value) => value ? `${value.courseCode} - ${value.courseName}` : '-',
    },
    {
      field: 'enrollmentDate',
      headerName: 'Enrollment Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      field: 'active',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value ? 'Active' : 'Inactive'} color={value ? 'success' : 'default'} size="small" />
      ),
    },
    {
      field: 'approved',
      headerName: 'Approved',
      render: (value) => (
        <Chip label={value ? 'Yes' : 'No'} color={value ? 'success' : 'warning'} size="small" />
      ),
    },
  ]

  const rows = data?.content || []
  const isStudent = user?.role === 'STUDENT'

  const statsCards = [
    { title: 'Total Enrollments', value: data?.totalElements || 0, icon: <EnrollmentIcon />, color: '#6366F1' },
    { title: 'Active', value: rows.filter(e => e.active).length, icon: <ActiveIcon />, color: '#10B981' },
    { title: 'Pending Approval', value: rows.filter(e => !e.approved).length, icon: <PendingIcon />, color: '#F59E0B' },
  ]

  if (isLoading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={80} sx={{ mb: 3, borderRadius: 3 }} />
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {[1, 2, 3].map((i) => (
            <Grid item xs={12} sm={4} key={i}>
              <Skeleton variant="rounded" height={100} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
        <Skeleton variant="rounded" height={400} sx={{ borderRadius: 3 }} />
      </Box>
    )
  }

  if (error) {
    return <Alert severity="error" sx={{ borderRadius: 3 }}>Failed to load enrollments</Alert>
  }

  return (
    <Box>
      <PageHeader
        title={isStudent ? "My Enrollments" : "Enrollments"}
        subtitle={isStudent ? "View your enrolled courses" : "Manage student enrollments"}
        action={!isStudent ? () => { setSelectedEnrollment(null); resetForm(); setOpenDialog(true) } : undefined}
        actionLabel="Enroll Student"
        badge={`${data?.totalElements || 0} total`}
      />

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statsCards.map((stat, index) => (
          <Grid item xs={12} sm={4} key={index}>
            <Card
              sx={{
                background: `linear-gradient(135deg, ${stat.color}10 0%, ${stat.color}05 100%)`,
                border: `1px solid ${stat.color}20`,
                transition: 'all 0.3s ease',
                '&:hover': { transform: 'translateY(-2px)', boxShadow: `0 8px 24px ${stat.color}20` },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ width: 48, height: 48, bgcolor: `${stat.color}20`, color: stat.color }}>
                  {stat.icon}
                </Avatar>
                <Box>
                  <Typography variant="h4" fontWeight={700} color={stat.color}>{stat.value}</Typography>
                  <Typography variant="body2" color="text.secondary">{stat.title}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Student hint to browse courses */}
      {isStudent && rows.length === 0 && (
        <Alert 
          severity="info" 
          sx={{ mb: 3, borderRadius: 3 }}
          action={
            <Button color="inherit" size="small" href="/academic/catalog">
              Browse Courses
            </Button>
          }
        >
          You are not enrolled in any courses yet. Visit the Course Catalog to browse and enroll in available courses.
        </Alert>
      )}

      <DataTable
        columns={columns}
        rows={rows}
        page={page}
        pageSize={pageSize}
        total={data?.totalElements || 0}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        actions={{
          delete: handleDelete,
        }}
        emptyMessage="No enrollments found"
      />

      {/* Pending Enrollments Section for Admin */}
      {!isStudent && rows.filter(e => !e.approved).length > 0 && (
        <Box sx={{ mt: 4 }}>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>Pending Approvals</Typography>
          <Grid container spacing={2}>
            {rows.filter(e => !e.approved).map((enrollment) => (
              <Grid item xs={12} sm={6} md={4} key={enrollment.id}>
                <Card sx={{ border: '1px solid #F59E0B40', bgcolor: '#FFFBEB' }}>
                  <CardContent>
                    <Typography variant="subtitle1" fontWeight={600}>
                      {enrollment.student?.fullName || 'Unknown Student'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {enrollment.course?.courseCode} - {enrollment.course?.courseName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Enrolled: {enrollment.enrollmentDate ? new Date(enrollment.enrollmentDate).toLocaleDateString() : 'N/A'}
                    </Typography>
                    <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                      <Button
                        size="small"
                        variant="contained"
                        color="success"
                        startIcon={<ApproveIcon />}
                        onClick={() => handleApprove(enrollment)}
                        disabled={approveMutation.isLoading}
                      >
                        Approve
                      </Button>
                      <Button
                        size="small"
                        variant="outlined"
                        color="error"
                        onClick={() => handleDelete(enrollment)}
                      >
                        Reject
                      </Button>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}

      <FormDialog
        open={openDialog}
        title="Enroll Student"
        onClose={() => {
          setOpenDialog(false)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              select
              label="Student"
              value={formData.studentId}
              onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
              required
              helperText={students.length === 0 ? "No students available" : ""}
            >
              <MenuItem value="">
                <em>Select a student</em>
              </MenuItem>
              {students.map((student) => (
                <MenuItem key={student.id} value={student.id}>
                  {student.firstName} {student.lastName} ({student.email})
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              select
              label="Course"
              value={formData.courseId}
              onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
              required
              helperText={courses.length === 0 ? "No courses available. Create courses first." : ""}
            >
              <MenuItem value="">
                <em>Select a course</em>
              </MenuItem>
              {courses.map((course) => (
                <MenuItem key={course.id} value={course.id}>
                  {course.courseCode} - {course.courseName}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Notes"
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Drop Enrollment"
        description="Are you sure you want to drop this enrollment?"
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedEnrollment(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedEnrollment?.id)}
        confirmText="Drop"
      />
    </Box>
  )
}

export default Enrollments
