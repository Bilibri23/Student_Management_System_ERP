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

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return <Alert severity="error">Failed to load enrollments</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Enrollments</Typography>
        {user?.role !== 'STUDENT' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setSelectedEnrollment(null)
              resetForm()
              setOpenDialog(true)
            }}
          >
            Enroll Student
          </Button>
        )}
      </Box>

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
              label="Student ID"
              type="number"
              value={formData.studentId}
              onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Course ID"
              type="number"
              value={formData.courseId}
              onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
              required
            />
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
