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
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import { useAuthStore } from '../../store/authStore'


const Payments = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [formData, setFormData] = useState({
    invoiceId: '',
    amount: '',
    paymentMethod: 'CASH',
    description: '',
    notes: '',
    transactionReference: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['payments', page, pageSize],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.studentId = user.id
      }
      return financeApi.getPayments(params).then(res => res.data.data)
    },
  })

  // Fetch invoices for dropdown
  const { data: invoicesData } = useQuery({
    queryKey: ['invoices-for-payment'],
    queryFn: () => financeApi.getInvoices({ page: 0, size: 100, status: 'PENDING' }).then(res => res.data.data),
    enabled: user?.role !== 'STUDENT',
  })

  const invoices = invoicesData?.content || []

  const paymentMutation = useMutation({
    mutationFn: (data) => financeApi.processPayment(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['payments'])
      queryClient.invalidateQueries(['invoices'])
      setOpenDialog(false)
      setFormData({
        invoiceId: '',
        amount: '',
        paymentMethod: 'CASH',
        description: '',
        notes: '',
        transactionReference: '',
      })
    },
  })

  const downloadMutation = useMutation({
    mutationFn: (id) => financeApi.downloadReceipt(id),
    onSuccess: (response, id) => {
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `receipt-${id}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
  })

  const handleSubmit = () => {
    paymentMutation.mutate({
      ...formData,
      invoiceId: parseInt(formData.invoiceId),
      amount: parseFloat(formData.amount),
    })
  }

  const handleDownload = (payment) => {
    downloadMutation.mutate(payment.id)
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'success'
      case 'PENDING':
        return 'warning'
      case 'FAILED':
        return 'error'
      case 'REFUNDED':
        return 'info'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'receiptNumber', headerName: 'Receipt #' },
    { field: 'invoiceNumber', headerName: 'Invoice #' },
    { field: 'studentName', headerName: 'Student' },
    {
      field: 'amount',
      headerName: 'Amount',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'paymentMethod',
      headerName: 'Method',
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'paymentDate',
      headerName: 'Date',
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
    return <Alert severity="error">Failed to load payments</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Payments</Typography>
        {user?.role !== 'STUDENT' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenDialog(true)}
          >
            Process Payment
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
          download: handleDownload,
        }}
        emptyMessage="No payments found"
      />

      <FormDialog
        open={openDialog}
        title="Process Payment"
        onClose={() => {
          setOpenDialog(false)
          setFormData({
            invoiceId: '',
            amount: '',
            paymentMethod: 'CASH',
            description: '',
            notes: '',
            transactionReference: '',
          })
        }}
        onSubmit={handleSubmit}
        loading={paymentMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              select
              label="Invoice"
              value={formData.invoiceId}
              onChange={(e) => setFormData({ ...formData, invoiceId: e.target.value })}
              required
              helperText={invoices.length === 0 ? "No pending invoices available" : ""}
            >
              <MenuItem value=""><em>Select an invoice</em></MenuItem>
              {invoices.map((invoice) => (
                <MenuItem key={invoice.id} value={invoice.id}>
                  {invoice.invoiceNumber} - {invoice.studentName} (${invoice.totalAmount})
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Amount"
              type="number"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
              inputProps={{ step: '0.01' }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Payment Method"
              value={formData.paymentMethod}
              onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
              required
            >
              <MenuItem value="CASH">Cash</MenuItem>
              <MenuItem value="BANK_TRANSFER">Bank Transfer</MenuItem>
              <MenuItem value="ONLINE">Online</MenuItem>
              <MenuItem value="CHEQUE">Cheque</MenuItem>
              <MenuItem value="CARD">Card</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Transaction Reference"
              value={formData.transactionReference}
              onChange={(e) => setFormData({ ...formData, transactionReference: e.target.value })}
            />
          </Grid>
        </Grid>
      </FormDialog>
    </Box>
  )
}

export default Payments
