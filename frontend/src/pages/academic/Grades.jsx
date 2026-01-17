import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  TextField,
  MenuItem,
  Grid,
} from '@mui/material'
import { Add as AddIcon, Download as DownloadIcon } from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import { useAuthStore } from '../../store/authStore'

const Grades = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [formData, setFormData] = useState({
    studentId: '',
    courseId: '',
    componentId: '',
    marksObtained: '',
    comments: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['grades', page, pageSize],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      return academicApi.getGrades(params).then(res => res.data.data)
    },
  })

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.enterGrade(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['grades'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const downloadMutation = useMutation({
    mutationFn: (studentId) => academicApi.downloadTranscript(studentId),
    onSuccess: (response, studentId) => {
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `transcript-${studentId}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
  })

  const resetForm = () => {
    setFormData({
      studentId: '',
      courseId: '',
      componentId: '',
      marksObtained: '',
      comments: '',
    })
  }

  const handleSubmit = () => {
    createMutation.mutate({
      ...formData,
      studentId: parseInt(formData.studentId),
      courseId: parseInt(formData.courseId),
      componentId: parseInt(formData.componentId),
      marksObtained: parseFloat(formData.marksObtained),
    })
  }

  const handleDownloadTranscript = (studentId) => {
    downloadMutation.mutate(studentId)
  }

  const columns = [
    { field: 'studentName', headerName: 'Student' },
    { field: 'courseName', headerName: 'Course' },
    { field: 'componentName', headerName: 'Component' },
    {
      field: 'marksObtained',
      headerName: 'Marks',
      render: (value, row) => `${value || 0} / ${row.maxMarks || 0}`,
    },
    { field: 'grade', headerName: 'Grade' },
    { field: 'comments', headerName: 'Comments' },
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
    return <Alert severity="error">Failed to load grades</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Grades</Typography>
        <Box display="flex" gap={2}>
          {user?.role !== 'STUDENT' && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                resetForm()
                setOpenDialog(true)
              }}
            >
              Enter Grade
            </Button>
          )}
          {user?.role === 'STUDENT' && (
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={() => handleDownloadTranscript(user.id)}
            >
              Download Transcript
            </Button>
          )}
        </Box>
      </Box>

      <DataTable
        columns={columns}
        rows={rows}
        page={page}
        pageSize={pageSize}
        total={data?.totalElements || 0}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        emptyMessage="No grades found"
      />

      <FormDialog
        open={openDialog}
        title="Enter Grade"
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
              label="Component ID"
              type="number"
              value={formData.componentId}
              onChange={(e) => setFormData({ ...formData, componentId: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Marks Obtained"
              type="number"
              value={formData.marksObtained}
              onChange={(e) => setFormData({ ...formData, marksObtained: e.target.value })}
              required
              inputProps={{ step: '0.01' }}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Comments"
              value={formData.comments}
              onChange={(e) => setFormData({ ...formData, comments: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
        </Grid>
      </FormDialog>
    </Box>
  )
}

export default Grades
