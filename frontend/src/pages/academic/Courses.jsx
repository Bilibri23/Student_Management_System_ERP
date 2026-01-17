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
} from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
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
    { id: 'courseCode', label: 'Course Code', minWidth: 120 },
    { id: 'courseName', label: 'Course Name', minWidth: 200 },
    { id: 'credits', label: 'Credits', minWidth: 80 },
    { id: 'department', label: 'Department', minWidth: 120 },
    { id: 'semester', label: 'Semester', minWidth: 100 },
    {
      id: 'status',
      label: 'Status',
      minWidth: 100,
      format: (value) => (
        <Chip
          label={value}
          color={value === 'ACTIVE' ? 'success' : value === 'INACTIVE' ? 'default' : 'warning'}
          size="small"
        />
      ),
    },
    { id: 'currentEnrollment', label: 'Enrolled', minWidth: 80 },
    { id: 'maxCapacity', label: 'Capacity', minWidth: 80 },
  ]

  const canEdit = user?.role === 'ADMIN' || user?.role === 'ACADEMIC_STAFF'
  const canDelete = user?.role === 'ADMIN'

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return <Alert severity="error">Failed to load courses</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Courses</Typography>
        {canEdit && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              resetForm()
              setOpenDialog(true)
            }}
          >
            Add Course
          </Button>
        )}
      </Box>

      <DataTable
        columns={columns}
        rows={data?.content || []}
        page={page}
        pageSize={pageSize}
        totalElements={data?.totalElements || 0}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        onEdit={canEdit ? handleEdit : undefined}
        onDelete={canDelete ? handleDelete : undefined}
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
              label="Instructor ID"
              type="number"
              value={formData.instructorId}
              onChange={(e) => setFormData({ ...formData, instructorId: e.target.value })}
            />
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
