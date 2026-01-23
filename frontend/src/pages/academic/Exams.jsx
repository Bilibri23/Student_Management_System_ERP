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
import { Add as AddIcon, Download as DownloadIcon } from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { useAuthStore } from '../../store/authStore'


const Exams = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedExam, setSelectedExam] = useState(null)
  const [formData, setFormData] = useState({
    courseId: '',
    examType: 'MIDTERM',
    examDate: '',
    startTime: '',
    endTime: '',
    duration: '',
    location: '',
    totalMarks: '',
    instructions: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['exams', page, pageSize],
    queryFn: () =>
      academicApi.getExams({ page, size: pageSize }).then(res => res.data.data),
  })

  // Fetch courses for dropdown
  const { data: coursesData } = useQuery({
    queryKey: ['courses-list'],
    queryFn: () => academicApi.getCourses({ page: 0, size: 100 }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  const courses = coursesData?.content || []

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.createExam(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['exams'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => academicApi.updateExam(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['exams'])
      setOpenDialog(false)
      setSelectedExam(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => academicApi.deleteExam(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['exams'])
      setOpenDeleteDialog(false)
      setSelectedExam(null)
    },
  })

  const downloadMutation = useMutation({
    mutationFn: (examId) => academicApi.downloadAdmitCard(examId),
    onSuccess: (response, examId) => {
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `admit-card-${examId}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
  })

  const resetForm = () => {
    setFormData({
      courseId: '',
      examType: 'MIDTERM',
      examDate: '',
      startTime: '',
      endTime: '',
      duration: '',
      location: '',
      totalMarks: '',
      instructions: '',
    })
  }

  const handleEdit = (exam) => {
    setSelectedExam(exam)
    setFormData({
      courseId: exam.courseId || '',
      examType: exam.examType || 'MIDTERM',
      examDate: exam.examDate ? new Date(exam.examDate).toISOString().split('T')[0] : '',
      startTime: exam.startTime || '',
      endTime: exam.endTime || '',
      duration: exam.duration || '',
      location: exam.location || '',
      totalMarks: exam.totalMarks || '',
      instructions: exam.instructions || '',
    })
    setOpenDialog(true)
  }

  const handleDelete = (exam) => {
    setSelectedExam(exam)
    setOpenDeleteDialog(true)
  }

  const handleDownload = (exam) => {
    downloadMutation.mutate(exam.id)
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      courseId: parseInt(formData.courseId),
      duration: parseInt(formData.duration),
      totalMarks: parseFloat(formData.totalMarks),
    }

    if (selectedExam) {
      updateMutation.mutate({ id: selectedExam.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const columns = [
    { field: 'courseName', headerName: 'Course' },
    {
      field: 'examType',
      headerName: 'Type',
    },
    {
      field: 'examDate',
      headerName: 'Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    { field: 'startTime', headerName: 'Start Time' },
    { field: 'endTime', headerName: 'End Time' },
    { field: 'location', headerName: 'Location' },
    {
      field: 'totalMarks',
      headerName: 'Total Marks',
      render: (value) => value || '-',
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
    return <Alert severity="error">Failed to load exams</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Exams</Typography>
        {user?.role !== 'STUDENT' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setSelectedExam(null)
              resetForm()
              setOpenDialog(true)
            }}
          >
            Schedule Exam
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
          edit: handleEdit,
          delete: handleDelete,
          download: user?.role === 'STUDENT' ? handleDownload : undefined,
        }}
        emptyMessage="No exams found"
      />

      <FormDialog
        open={openDialog}
        title={selectedExam ? 'Edit Exam' : 'Schedule Exam'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedExam(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Course"
              value={formData.courseId}
              onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
              required
            >
              <MenuItem value=""><em>Select a course</em></MenuItem>
              {courses.map((course) => (
                <MenuItem key={course.id} value={course.id}>
                  {course.courseCode} - {course.courseName}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Exam Type"
              value={formData.examType}
              onChange={(e) => setFormData({ ...formData, examType: e.target.value })}
              required
            >
              <MenuItem value="MIDTERM">Midterm</MenuItem>
              <MenuItem value="FINAL">Final</MenuItem>
              <MenuItem value="QUIZ">Quiz</MenuItem>
              <MenuItem value="ASSIGNMENT">Assignment</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Exam Date"
              type="date"
              value={formData.examDate}
              onChange={(e) => setFormData({ ...formData, examDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Location"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Start Time"
              type="time"
              value={formData.startTime}
              onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="End Time"
              type="time"
              value={formData.endTime}
              onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Duration (minutes)"
              type="number"
              value={formData.duration}
              onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Total Marks"
              type="number"
              value={formData.totalMarks}
              onChange={(e) => setFormData({ ...formData, totalMarks: e.target.value })}
              required
              inputProps={{ step: '0.01' }}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Instructions"
              value={formData.instructions}
              onChange={(e) => setFormData({ ...formData, instructions: e.target.value })}
              multiline
              rows={3}
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Exam"
        description="Are you sure you want to delete this exam?"
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedExam(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedExam?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Exams
