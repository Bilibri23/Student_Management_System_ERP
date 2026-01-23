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
import { userApi } from '../../api/user'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import { useAuthStore } from '../../store/authStore'

const Certificates = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [formData, setFormData] = useState({
    studentId: user?.id || '',
    certificateType: 'BONAFIDE',
    remarks: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['certificates', page, pageSize],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      return academicApi.getCertificates(params).then(res => res.data.data)
    },
  })

  // Fetch students for dropdown (staff only)
  const { data: studentsData } = useQuery({
    queryKey: ['students-list'],
    queryFn: () => userApi.getStudents({ page: 0, size: 100 }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  const students = studentsData?.content || []

  const createMutation = useMutation({
    mutationFn: (data) => academicApi.requestCertificate(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['certificates'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const downloadMutation = useMutation({
    mutationFn: (id) => academicApi.downloadCertificate(id),
    onSuccess: (response, id) => {
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `certificate-${id}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
  })

  const resetForm = () => {
    setFormData({
      studentId: user?.id || '',
      certificateType: 'BONAFIDE',
      remarks: '',
    })
  }

  const handleSubmit = () => {
    createMutation.mutate({
      ...formData,
      studentId: parseInt(formData.studentId),
    })
  }

  const handleDownload = (certificate) => {
    if (certificate.status === 'ISSUED') {
      downloadMutation.mutate(certificate.id)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'ISSUED':
        return 'success'
      case 'APPROVED':
        return 'info'
      case 'PENDING':
        return 'warning'
      case 'REJECTED':
        return 'error'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'studentName', headerName: 'Student' },
    { field: 'enrollmentNumber', headerName: 'Enrollment #' },
    {
      field: 'certificateType',
      headerName: 'Type',
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'requestedAt',
      headerName: 'Requested',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      field: 'issuedAt',
      headerName: 'Issued',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
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
    return <Alert severity="error">Failed to load certificates</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Certificates</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            resetForm()
            setOpenDialog(true)
          }}
        >
          Request Certificate
        </Button>
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
          download: (row) => row.status === 'ISSUED' && handleDownload(row),
        }}
        emptyMessage="No certificates found"
      />

      <FormDialog
        open={openDialog}
        title="Request Certificate"
        onClose={() => {
          setOpenDialog(false)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading}
      >
        <Grid container spacing={2}>
          {user?.role !== 'STUDENT' && (
            <Grid item xs={12}>
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
                    {student.firstName} {student.lastName} ({student.email})
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
          )}
          <Grid item xs={12}>
            <TextField
              fullWidth
              select
              label="Certificate Type"
              value={formData.certificateType}
              onChange={(e) => setFormData({ ...formData, certificateType: e.target.value })}
              required
            >
              <MenuItem value="BONAFIDE">Bonafide</MenuItem>
              <MenuItem value="TRANSFER">Transfer</MenuItem>
              <MenuItem value="CHARACTER">Character</MenuItem>
              <MenuItem value="STUDY">Study</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Remarks"
              value={formData.remarks}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              multiline
              rows={3}
            />
          </Grid>
        </Grid>
      </FormDialog>
    </Box>
  )
}

export default Certificates
