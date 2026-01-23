import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  MenuItem,
  Grid,
  Skeleton,
  Card,
  CardContent,
  Typography,
  Avatar,
} from '@mui/material'
import {
  MenuBook as CourseIcon,
  School as SchoolIcon,
  People as PeopleIcon,
  CheckCircle as ActiveIcon,
} from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import { hrApi } from '../../api/hr'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import PageHeader from '../../components/common/PageHeader'
import { useAuthStore } from '../../store/authStore'

const Courses = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedCourse, setSelectedCourse] = useState(null)
  const [formData, setFormData] = useState({
    courseCode: '',
    courseName: '',
    credits: '',
    department: '',
    semester: '',
    academicYear: '',
    description: '',
    instructorId: '',
    maxCapacity: '',
    scheduleDay: '',
    startTime: '',
    endTime: '',
    room: '',
    status: 'ACTIVE',
    prerequisites: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['courses', page, pageSize],
    queryFn: () => academicApi.getCourses({ page, size: pageSize }).then(res => res.data.data),
  })

  // Fetch employees for instructor dropdown
  const { data: employeesData } = useQuery({
    queryKey: ['employees-for-courses'],
    queryFn: () => hrApi.getEmployees({ page: 0, size: 100 }).then(res => res.data.data),
  })

  const instructors = employeesData?.content || []

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.createCourse(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['courses'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => academicApi.updateCourse(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['courses'])
      setOpenDialog(false)
      setSelectedCourse(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => academicApi.deleteCourse(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['courses'])
      setOpenDeleteDialog(false)
      setSelectedCourse(null)
    },
  })

  const resetForm = () => {
    setFormData({
      courseCode: '',
      courseName: '',
      credits: '',
      department: '',
      semester: '',
      academicYear: '',
      description: '',
      instructorId: '',
      maxCapacity: '',
      scheduleDay: '',
      startTime: '',
      endTime: '',
      room: '',
      status: 'ACTIVE',
      prerequisites: '',
    })
    setSelectedCourse(null)
  }

  const handleEdit = (course) => {
    setSelectedCourse(course)
    setFormData({
      courseCode: course.courseCode || '',
      courseName: course.courseName || '',
      credits: course.credits || '',
      department: course.department || '',
      semester: course.semester || '',
      academicYear: course.academicYear || '',
      description: course.description || '',
      instructorId: course.instructorId || '',
      maxCapacity: course.maxCapacity || '',
      scheduleDay: course.scheduleDay || '',
      startTime: course.startTime || '',
      endTime: course.endTime || '',
      room: course.room || '',
      status: course.status || 'ACTIVE',
      prerequisites: course.prerequisites || '',
    })
    setOpenDialog(true)
  }

  const handleDelete = (course) => {
    setSelectedCourse(course)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      credits: parseInt(formData.credits) || 0,
      maxCapacity: parseInt(formData.maxCapacity) || 0,
      instructorId: formData.instructorId ? parseInt(formData.instructorId) : null,
    }

    if (selectedCourse) {
      updateMutation.mutate({ id: selectedCourse.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const columns = [
    { field: 'courseCode', headerName: 'Course Code' },
    { field: 'courseName', headerName: 'Course Name' },
    { field: 'credits', headerName: 'Credits' },
    { field: 'department', headerName: 'Department' },
    { field: 'semester', headerName: 'Semester' },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip
          label={value || 'N/A'}
          color={value === 'ACTIVE' ? 'success' : value === 'INACTIVE' ? 'default' : 'warning'}
          size="small"
        />
      ),
    },
    { field: 'currentEnrollment', headerName: 'Enrolled' },
    { field: 'maxCapacity', headerName: 'Capacity' },
  ]

  const canEdit = user?.role === 'ADMIN' || user?.role === 'ACADEMIC_STAFF'
  const canDelete = user?.role === 'ADMIN'

  const statsCards = [
    {
      title: 'Total Courses',
      value: data?.totalElements || 0,
      icon: <CourseIcon />,
      color: '#6366F1',
    },
    {
      title: 'Active Courses',
      value: data?.content?.filter(c => c.status === 'ACTIVE').length || 0,
      icon: <ActiveIcon />,
      color: '#10B981',
    },
    {
      title: 'Total Enrollment',
      value: data?.content?.reduce((acc, c) => acc + (c.currentEnrollment || 0), 0) || 0,
      icon: <PeopleIcon />,
      color: '#F59E0B',
    },
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
    return (
      <Alert
        severity="error"
        sx={{ borderRadius: 3 }}
      >
        Failed to load courses. Please try again later.
      </Alert>
    )
  }

  return (
    <Box>
      <PageHeader
        title="Courses"
        subtitle="Manage academic courses and programs"
        action={canEdit ? () => { resetForm(); setOpenDialog(true) } : undefined}
        actionLabel="Add Course"
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
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: `0 8px 24px ${stat.color}20`,
                },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar
                  sx={{
                    width: 48,
                    height: 48,
                    bgcolor: `${stat.color}20`,
                    color: stat.color,
                  }}
                >
                  {stat.icon}
                </Avatar>
                <Box>
                  <Typography variant="h4" fontWeight={700} color={stat.color}>
                    {stat.value}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stat.title}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <DataTable
        columns={columns}
        rows={data?.content || []}
        page={page}
        pageSize={pageSize}
        total={data?.totalElements || 0}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        actions={{
          edit: canEdit ? handleEdit : undefined,
          delete: canDelete ? handleDelete : undefined,
        }}
        emptyMessage="No courses found"
      />

      <FormDialog
        open={openDialog}
        onClose={() => {
          setOpenDialog(false)
          resetForm()
        }}
        title={selectedCourse ? 'Edit Course' : 'Add Course'}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Course Code"
              required
              value={formData.courseCode}
              onChange={(e) => setFormData({ ...formData, courseCode: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Course Name"
              required
              value={formData.courseName}
              onChange={(e) => setFormData({ ...formData, courseName: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Credits"
              type="number"
              required
              value={formData.credits}
              onChange={(e) => setFormData({ ...formData, credits: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Department"
              required
              value={formData.department}
              onChange={(e) => setFormData({ ...formData, department: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Semester"
              required
              value={formData.semester}
              onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Academic Year"
              required
              value={formData.academicYear}
              onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Max Capacity"
              type="number"
              required
              value={formData.maxCapacity}
              onChange={(e) => setFormData({ ...formData, maxCapacity: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Status"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            >
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="INACTIVE">Inactive</MenuItem>
              <MenuItem value="ARCHIVED">Archived</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Schedule Day"
              value={formData.scheduleDay}
              onChange={(e) => setFormData({ ...formData, scheduleDay: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Start Time"
              type="time"
              value={formData.startTime}
              onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="End Time"
              type="time"
              value={formData.endTime}
              onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Room"
              value={formData.room}
              onChange={(e) => setFormData({ ...formData, room: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Instructor"
              value={formData.instructorId}
              onChange={(e) => setFormData({ ...formData, instructorId: e.target.value })}
              helperText={instructors.length === 0 ? "No instructors available. Add employees in HR first." : ""}
            >
              <MenuItem value="">
                <em>None (Optional)</em>
              </MenuItem>
              {instructors.map((emp) => (
                <MenuItem key={emp.id} value={emp.id}>
                  {emp.firstName} {emp.lastName} - {emp.position || emp.department || 'Staff'}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Description"
              multiline
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Prerequisites"
              value={formData.prerequisites}
              onChange={(e) => setFormData({ ...formData, prerequisites: e.target.value })}
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedCourse(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedCourse?.id)}
        title="Delete Course"
        message={`Are you sure you want to delete course "${selectedCourse?.courseName}"? This action cannot be undone.`}
        loading={deleteMutation.isLoading}
      />
    </Box>
  )
}

export default Courses
