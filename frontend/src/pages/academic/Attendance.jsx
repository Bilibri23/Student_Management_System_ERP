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
import { useAuthStore } from '../../store/authStore'

const Attendance = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [formData, setFormData] = useState({
    courseId: '',
    studentId: '',
    sessionDate: new Date().toISOString().split('T')[0],
    status: 'PRESENT',
    notes: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['attendance', page, pageSize],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      return academicApi.getAttendance(params).then(res => res.data.data)
    },
  })

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.markAttendance(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['attendance'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const resetForm = () => {
    setFormData({
      courseId: '',
      studentId: '',
      sessionDate: new Date().toISOString().split('T')[0],
      status: 'PRESENT',
      notes: '',
    })
  }

  const handleSubmit = () => {
    createMutation.mutate({
      ...formData,
      courseId: parseInt(formData.courseId),
      studentId: parseInt(formData.studentId),
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'PRESENT':
        return 'success'
      case 'ABSENT':
        return 'error'
      case 'LATE':
        return 'warning'
      case 'EXCUSED':
        return 'info'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'studentName', headerName: 'Student' },
    { field: 'courseName', headerName: 'Course' },
    {
      field: 'sessionDate',
      headerName: 'Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    { field: 'markedByName', headerName: 'Marked By' },
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
    return <Alert severity="error">Failed to load attendance</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Attendance</Typography>
        {user?.role !== 'STUDENT' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              resetForm()
              setOpenDialog(true)
            }}
          >
            Mark Attendance
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
        emptyMessage="No attendance records found"
      />

      <FormDialog
        open={openDialog}
        title="Mark Attendance"
        onClose={() => {
          setOpenDialog(false)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Course ID"
              type="number"
              value={formData.courseId}
              onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Student ID"
              type="number"
              value={formData.studentId}
              onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Session Date"
              type="date"
              value={formData.sessionDate}
              onChange={(e) => setFormData({ ...formData, sessionDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Status"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              required
            >
              <MenuItem value="PRESENT">Present</MenuItem>
              <MenuItem value="ABSENT">Absent</MenuItem>
              <MenuItem value="LATE">Late</MenuItem>
              <MenuItem value="EXCUSED">Excused</MenuItem>
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
    </Box>
  )
}

export default Attendance
