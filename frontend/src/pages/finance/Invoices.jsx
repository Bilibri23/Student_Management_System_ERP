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
import { financeApi } from '../../api/finance'
import { userApi } from '../../api/user'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import { useAuthStore } from '../../store/authStore'

const Invoices = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [selectedInvoice, setSelectedInvoice] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')

  const { data, isLoading, error } = useQuery({
    queryKey: ['invoices', page, pageSize, statusFilter],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      if (statusFilter) {
        params.status = statusFilter
      }
      return financeApi.getInvoices(params).then(res => res.data.data)
    },
  })

  // Fetch students for dropdown
  const { data: studentsData } = useQuery({
    queryKey: ['students-list'],
    queryFn: () => userApi.getStudents({ page: 0, size: 100 }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  const students = studentsData?.content || []

  const downloadMutation = useMutation({
    mutationFn: ({ invoiceNumber, enrollmentNumber }) =>
      financeApi.downloadInvoice(invoiceNumber, enrollmentNumber),
    onSuccess: (response, variables) => {
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `invoice-${variables.invoiceNumber}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
  })

  const handleDownload = (invoice) => {
    downloadMutation.mutate({
      invoiceNumber: invoice.invoiceNumber,
      enrollmentNumber: invoice.enrollmentNumber,
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'PAID':
        return 'success'
      case 'PENDING':
        return 'warning'
      case 'OVERDUE':
        return 'error'
      case 'PARTIAL':
        return 'info'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'invoiceNumber', headerName: 'Invoice #' },
    { field: 'studentName', headerName: 'Student' },
    { field: 'enrollmentNumber', headerName: 'Enrollment #' },
    { field: 'program', headerName: 'Program' },
    { field: 'semester', headerName: 'Semester' },
    {
      field: 'totalAmount',
      headerName: 'Total Amount',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'remainingAmount',
      headerName: 'Remaining',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'dueDate',
      headerName: 'Due Date',
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
    return <Alert severity="error">Failed to load invoices</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Invoices</Typography>
        <Box display="flex" gap={2}>
          {user?.role !== 'STUDENT' && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                setSelectedInvoice(null)
                setOpenDialog(true)
              }}
            >
              Generate Invoice
            </Button>
          )}
        </Box>
      </Box>

      <Box mb={2}>
        <TextField
          select
          label="Filter by Status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="PENDING">Pending</MenuItem>
          <MenuItem value="PAID">Paid</MenuItem>
          <MenuItem value="OVERDUE">Overdue</MenuItem>
          <MenuItem value="PARTIAL">Partial</MenuItem>
        </TextField>
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
          download: handleDownload,
        }}
        emptyMessage="No invoices found"
      />

      <FormDialog
        open={openDialog}
        title={selectedInvoice ? 'Edit Invoice' : 'Generate Invoice'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedInvoice(null)
        }}
        onSubmit={() => {
          // TODO: Implement invoice generation form
          setOpenDialog(false)
        }}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              select
              label="Student"
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
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Program" required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Semester" required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Academic Year" required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Due Date"
              type="date"
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
        </Grid>
      </FormDialog>
    </Box>
  )
}

export default Invoices
