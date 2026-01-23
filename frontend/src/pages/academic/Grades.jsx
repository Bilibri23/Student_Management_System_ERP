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
  Card,
  CardContent,
  Avatar,
  Skeleton,
  Chip,
} from '@mui/material'
import {
  Add as AddIcon,
  Download as DownloadIcon,
  Grade as GradeIcon,
  TrendingUp as TrendingUpIcon,
  EmojiEvents as AchievementIcon,
  School as CourseIcon,
  Settings as SettingsIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material'
import { academicApi } from '../../api/academic'
import { userApi } from '../../api/user'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import PageHeader from '../../components/common/PageHeader'
import { useAuthStore } from '../../store/authStore'

const Grades = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openComponentDialog, setOpenComponentDialog] = useState(false)
  const [selectedCourseForComponents, setSelectedCourseForComponents] = useState('')
  const [formData, setFormData] = useState({
    studentId: '',
    courseId: '',
    componentId: '',
    marksObtained: '',
    comments: '',
  })
  const [componentFormData, setComponentFormData] = useState({
    courseId: '',
    name: '',
    description: '',
    weightage: '',
    maxMarks: '',
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

  // Fetch grade components when course is selected
  const { data: componentsData } = useQuery({
    queryKey: ['grade-components', formData.courseId],
    queryFn: () => academicApi.getGradeComponents(formData.courseId).then(res => res.data.data),
    enabled: !!formData.courseId && user?.role !== 'STUDENT',
  })

  const gradeComponents = componentsData || []

  // Fetch grade components for the selected course in component management
  const { data: managedComponentsData, refetch: refetchComponents } = useQuery({
    queryKey: ['grade-components-manage', selectedCourseForComponents],
    queryFn: () => academicApi.getGradeComponents(selectedCourseForComponents).then(res => res.data.data),
    enabled: !!selectedCourseForComponents && user?.role !== 'STUDENT',
  })

  const managedComponents = managedComponentsData || []

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

  const createComponentMutation = useMutation({
    mutationFn: (data) => academicApi.createGradeComponent(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['grade-components'])
      refetchComponents()
      setComponentFormData({ courseId: '', name: '', description: '', weightage: '', maxMarks: '' })
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

  const handleCreateComponent = () => {
    createComponentMutation.mutate({
      ...componentFormData,
      courseId: parseInt(selectedCourseForComponents),
      weightage: parseFloat(componentFormData.weightage),
      maxMarks: parseFloat(componentFormData.maxMarks),
    })
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

  // API returns array directly, not PageResponse
  const rows = Array.isArray(data) ? data : (data?.content || [])
  const isStudent = user?.role === 'STUDENT'

  const calculateAverage = () => {
    if (!rows.length) return 0
    const validGrades = rows.filter(r => r.marksObtained && r.maxMarks)
    if (!validGrades.length) return 0
    const total = validGrades.reduce((acc, r) => acc + (r.marksObtained / r.maxMarks) * 100, 0)
    return (total / validGrades.length).toFixed(1)
  }

  const getGradeColor = (grade) => {
    if (!grade) return '#64748B'
    if (grade.startsWith('A')) return '#10B981'
    if (grade.startsWith('B')) return '#3B82F6'
    if (grade.startsWith('C')) return '#F59E0B'
    return '#EF4444'
  }

  const totalCount = Array.isArray(data) ? data.length : (data?.totalElements || 0)

  const statsCards = isStudent ? [
    { title: 'Total Grades', value: totalCount, icon: <GradeIcon />, color: '#6366F1' },
    { title: 'Average Score', value: `${calculateAverage()}%`, icon: <TrendingUpIcon />, color: '#10B981' },
    { title: 'Courses Graded', value: [...new Set(rows.map(r => r.courseName))].length, icon: <CourseIcon />, color: '#8B5CF6' },
  ] : [
    { title: 'Total Grades', value: totalCount, icon: <GradeIcon />, color: '#6366F1' },
    { title: 'Students Graded', value: [...new Set(rows.map(r => r.studentName))].length, icon: <AchievementIcon />, color: '#10B981' },
    { title: 'Courses', value: [...new Set(rows.map(r => r.courseName))].length, icon: <CourseIcon />, color: '#8B5CF6' },
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
    return <Alert severity="error" sx={{ borderRadius: 3 }}>Failed to load grades</Alert>
  }

  return (
    <Box>
      <PageHeader
        title={isStudent ? "My Grades" : "Grades"}
        subtitle={isStudent ? "View your academic performance" : "Manage student grades"}
        action={!isStudent ? () => { resetForm(); setOpenDialog(true) } : undefined}
        actionLabel="Enter Grade"
        badge={`${totalCount} records`}
      >
        {isStudent && (
          <Button
            variant="contained"
            startIcon={<DownloadIcon />}
            onClick={() => handleDownloadTranscript(user.id)}
            sx={{
              background: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
              boxShadow: '0 4px 14px 0 rgba(16, 185, 129, 0.39)',
              '&:hover': {
                background: 'linear-gradient(135deg, #059669 0%, #10B981 100%)',
              },
            }}
          >
            Download Transcript
          </Button>
        )}
      </PageHeader>

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

      {/* Grade Components Management Section - Only for Staff */}
      {!isStudent && (
        <Card sx={{ mb: 4, border: '1px solid #E2E8F0' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <SettingsIcon color="primary" />
              <Typography variant="h6" fontWeight={600}>Grade Components</Typography>
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Before entering grades, you must create grade components (e.g., Midterm, Final Exam, Assignment) for each course.
            </Typography>
            
            <Grid container spacing={2} alignItems="flex-end">
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  select
                  label="Select Course"
                  value={selectedCourseForComponents}
                  onChange={(e) => setSelectedCourseForComponents(e.target.value)}
                  size="small"
                >
                  <MenuItem value=""><em>Select a course</em></MenuItem>
                  {courses.map((course) => (
                    <MenuItem key={course.id} value={course.id}>
                      {course.courseCode} - {course.courseName}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={8}>
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => setOpenComponentDialog(true)}
                  disabled={!selectedCourseForComponents}
                >
                  Add Component
                </Button>
              </Grid>
            </Grid>

            {/* Show existing components for selected course */}
            {selectedCourseForComponents && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>Existing Components:</Typography>
                {managedComponents.length === 0 ? (
                  <Alert severity="info" sx={{ borderRadius: 2 }}>
                    No grade components found for this course. Add components like "Midterm", "Final Exam", "Assignment", etc.
                  </Alert>
                ) : (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {managedComponents.map((comp) => (
                      <Chip
                        key={comp.id}
                        label={`${comp.name} (${comp.weightage}% - Max: ${comp.maxMarks})`}
                        color="primary"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      )}

      <DataTable
        columns={columns}
        rows={rows}
        page={page}
        pageSize={pageSize}
        total={totalCount}
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
              select
              label="Student"
              value={formData.studentId}
              onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
              required
            >
              <MenuItem value=""><em>Select a student</em></MenuItem>
              {students.map((student) => (
                <MenuItem key={student.id} value={student.id}>
                  {student.firstName} {student.lastName}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
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
              label="Grade Component"
              value={formData.componentId}
              onChange={(e) => setFormData({ ...formData, componentId: e.target.value })}
              required
              disabled={!formData.courseId}
              helperText={!formData.courseId ? "Select a course first" : gradeComponents.length === 0 ? "No components found. Create grade components for this course first." : ""}
            >
              <MenuItem value=""><em>Select a component</em></MenuItem>
              {gradeComponents.map((comp) => (
                <MenuItem key={comp.id} value={comp.id}>
                  {comp.name} (Max: {comp.maxMarks}, Weight: {comp.weightage}%)
                </MenuItem>
              ))}
            </TextField>
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

      {/* Grade Component Creation Dialog */}
      <FormDialog
        open={openComponentDialog}
        title="Add Grade Component"
        onClose={() => {
          setOpenComponentDialog(false)
          setComponentFormData({ courseId: '', name: '', description: '', weightage: '', maxMarks: '' })
        }}
        onSubmit={handleCreateComponent}
        loading={createComponentMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Alert severity="info" sx={{ mb: 1 }}>
              Creating component for: <strong>{courses.find(c => c.id == selectedCourseForComponents)?.courseName || 'Selected Course'}</strong>
            </Alert>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Component Name"
              value={componentFormData.name}
              onChange={(e) => setComponentFormData({ ...componentFormData, name: e.target.value })}
              required
              placeholder="e.g., Midterm Exam, Final Exam, Assignment 1"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Description"
              value={componentFormData.description}
              onChange={(e) => setComponentFormData({ ...componentFormData, description: e.target.value })}
              placeholder="Optional description"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Weightage (%)"
              type="number"
              value={componentFormData.weightage}
              onChange={(e) => setComponentFormData({ ...componentFormData, weightage: e.target.value })}
              required
              placeholder="e.g., 30 for 30%"
              inputProps={{ min: 0, max: 100 }}
              helperText="Percentage weight towards final grade"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Maximum Marks"
              type="number"
              value={componentFormData.maxMarks}
              onChange={(e) => setComponentFormData({ ...componentFormData, maxMarks: e.target.value })}
              required
              placeholder="e.g., 100"
              inputProps={{ min: 0 }}
              helperText="Maximum possible marks for this component"
            />
          </Grid>
        </Grid>
      </FormDialog>
    </Box>
  )
}

export default Grades
