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
  Card,
  CardContent,
  Avatar,
  Skeleton,
} from '@mui/material'
import {
  Add as AddIcon,
  PersonAdd as LeadIcon,
  HourglassEmpty as NewIcon,
  Phone as ContactedIcon,
  CheckCircle as ConvertedIcon,
} from '@mui/icons-material'
import { marketingApi } from '../../api/marketing'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import PageHeader from '../../components/common/PageHeader'

const Leads = () => {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedLead, setSelectedLead] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    source: 'WEBSITE',
    inquiry: '',
    notes: '',
    interestedProgram: '',
    inquiryDate: new Date().toISOString().split('T')[0],
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['leads', page, pageSize, statusFilter],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (statusFilter) {
        params.status = statusFilter
      }
      return marketingApi.getLeads(params).then(res => res.data.data)
    },
  })

  const createMutation = useMutation({
    mutationFn: (data) => marketingApi.createLead(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leads'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => marketingApi.updateLead(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leads'])
      setOpenDialog(false)
      setSelectedLead(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => marketingApi.deleteLead(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['leads'])
      setOpenDeleteDialog(false)
      setSelectedLead(null)
    },
  })

  const convertMutation = useMutation({
    mutationFn: (id) => marketingApi.convertLead(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['leads'])
    },
  })

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      source: 'WEBSITE',
      inquiry: '',
      notes: '',
      interestedProgram: '',
      inquiryDate: new Date().toISOString().split('T')[0],
    })
  }

  const handleEdit = (lead) => {
    setSelectedLead(lead)
    setFormData({
      firstName: lead.firstName || '',
      lastName: lead.lastName || '',
      email: lead.email || '',
      phone: lead.phone || '',
      address: lead.address || '',
      source: lead.source || 'WEBSITE',
      inquiry: lead.inquiry || '',
      notes: lead.notes || '',
      interestedProgram: lead.interestedProgram || '',
      inquiryDate: lead.inquiryDate
        ? new Date(lead.inquiryDate).toISOString().split('T')[0]
        : new Date().toISOString().split('T')[0],
    })
    setOpenDialog(true)
  }

  const handleDelete = (lead) => {
    setSelectedLead(lead)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    if (selectedLead) {
      updateMutation.mutate({ id: selectedLead.id, data: formData })
    } else {
      createMutation.mutate(formData)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONVERTED':
        return 'success'
      case 'CONTACTED':
        return 'info'
      case 'NEW':
        return 'warning'
      case 'LOST':
        return 'error'
      default:
        return 'default'
    }
  }

  const columns = [
    {
      field: 'name',
      headerName: 'Name',
      render: (_, row) => `${row.firstName || ''} ${row.lastName || ''}`.trim(),
    },
    { field: 'email', headerName: 'Email' },
    { field: 'phone', headerName: 'Phone' },
    {
      field: 'source',
      headerName: 'Source',
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'inquiryDate',
      headerName: 'Inquiry Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    { field: 'interestedProgram', headerName: 'Program' },
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
    return <Alert severity="error">Failed to load leads</Alert>
  }

  const statsCards = [
    { title: 'Total Leads', value: data?.totalElements || 0, icon: <LeadIcon />, color: '#6366F1' },
    { title: 'New', value: rows.filter(l => l.status === 'NEW').length, icon: <NewIcon />, color: '#F59E0B' },
    { title: 'Contacted', value: rows.filter(l => l.status === 'CONTACTED').length, icon: <ContactedIcon />, color: '#3B82F6' },
    { title: 'Converted', value: rows.filter(l => l.status === 'CONVERTED').length, icon: <ConvertedIcon />, color: '#10B981' },
  ]

  return (
    <Box>
      <PageHeader
        title="Leads"
        subtitle="Manage prospective student leads"
        action={() => { setSelectedLead(null); resetForm(); setOpenDialog(true) }}
        actionLabel="Add Lead"
        badge={`${data?.totalElements || 0} total`}
      />

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statsCards.map((stat, index) => (
          <Grid item xs={6} sm={3} key={index}>
            <Card
              sx={{
                background: `linear-gradient(135deg, ${stat.color}10 0%, ${stat.color}05 100%)`,
                border: `1px solid ${stat.color}20`,
                transition: 'all 0.3s ease',
                '&:hover': { transform: 'translateY(-2px)', boxShadow: `0 8px 24px ${stat.color}20` },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 2 }}>
                <Avatar sx={{ width: 40, height: 40, bgcolor: `${stat.color}20`, color: stat.color }}>
                  {stat.icon}
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color={stat.color}>{stat.value}</Typography>
                  <Typography variant="caption" color="text.secondary">{stat.title}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Filter */}
      <Card sx={{ mb: 3, border: '1px solid #E2E8F0' }}>
        <CardContent sx={{ py: 2 }}>
          <TextField
            select
            label="Filter by Status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            size="small"
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="">All Statuses</MenuItem>
            <MenuItem value="NEW">New</MenuItem>
            <MenuItem value="CONTACTED">Contacted</MenuItem>
            <MenuItem value="CONVERTED">Converted</MenuItem>
            <MenuItem value="LOST">Lost</MenuItem>
          </TextField>
        </CardContent>
      </Card>

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
        }}
        emptyMessage="No leads found"
      />

      <FormDialog
        open={openDialog}
        title={selectedLead ? 'Edit Lead' : 'Add Lead'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedLead(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="First Name"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Last Name"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Phone"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Source"
              value={formData.source}
              onChange={(e) => setFormData({ ...formData, source: e.target.value })}
              required
            >
              <MenuItem value="WEBSITE">Website</MenuItem>
              <MenuItem value="REFERRAL">Referral</MenuItem>
              <MenuItem value="SOCIAL_MEDIA">Social Media</MenuItem>
              <MenuItem value="EVENT">Event</MenuItem>
              <MenuItem value="OTHER">Other</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Interested Program"
              value={formData.interestedProgram}
              onChange={(e) => setFormData({ ...formData, interestedProgram: e.target.value })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Inquiry"
              value={formData.inquiry}
              onChange={(e) => setFormData({ ...formData, inquiry: e.target.value })}
              multiline
              rows={3}
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
        title="Delete Lead"
        description={`Are you sure you want to delete lead for "${selectedLead?.firstName} ${selectedLead?.lastName}"?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedLead(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedLead?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Leads
