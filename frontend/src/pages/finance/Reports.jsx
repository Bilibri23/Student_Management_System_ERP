import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Grid,
  TextField,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material'
import { Download as DownloadIcon } from '@mui/icons-material'
import { financeApi } from '../../api/finance'

const FinanceReports = () => {
  const [reportType, setReportType] = useState('revenue')
  const [startDate, setStartDate] = useState(
    new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0]
  )
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0])

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['financeReport', reportType, startDate, endDate],
    queryFn: () => {
      const params = { startDate, endDate }
      switch (reportType) {
        case 'revenue':
          return financeApi.getRevenueReport(params).then(res => res.data.data)
        case 'expenses':
          return financeApi.getExpenseReport(params).then(res => res.data.data)
        case 'profitLoss':
          return financeApi.getProfitLoss(params).then(res => res.data.data)
        case 'cashFlow':
          return financeApi.getCashFlow(params).then(res => res.data.data)
        default:
          return financeApi.getRevenueReport(params).then(res => res.data.data)
      }
    },
    enabled: !!startDate && !!endDate,
    retry: 1,
    onError: (err) => {
      console.error('Report API Error:', err)
    },
  })

  const handleExport = () => {
    if (!data) {
      alert('No data to export. Please generate a report first.')
      return
    }

    // Export as CSV
    let csvContent = ''
    const reportTypeLabel = reportType
      .split(/(?=[A-Z])/)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')

    csvContent += `${reportTypeLabel} Report\n`
    csvContent += `Date Range: ${startDate} to ${endDate}\n\n`

    switch (reportType) {
      case 'revenue':
        csvContent += `Total Revenue,${data.totalRevenue || 0}\n`
        csvContent += `Total Payments,${data.totalPayments || 0}\n`
        csvContent += `Average Payment,${data.averagePayment || 0}\n\n`
        if (data.revenueByMonth) {
          csvContent += 'Month,Revenue,Payments\n'
          data.revenueByMonth.forEach((item) => {
            csvContent += `${item.month},${item.revenue || 0},${item.payments || 0}\n`
          })
        }
        break
      case 'expenses':
        csvContent += `Total Expenses,${data.totalExpenses || 0}\n`
        csvContent += `Pending Expenses,${data.pendingExpenses || 0}\n`
        csvContent += `Approved Expenses,${data.approvedExpenses || 0}\n\n`
        if (data.expensesByCategory) {
          csvContent += 'Category,Amount,Count\n'
          data.expensesByCategory.forEach((item) => {
            csvContent += `${item.category},${item.amount || 0},${item.count || 0}\n`
          })
        }
        break
      case 'profitLoss':
        csvContent += `Total Revenue,${data.totalRevenue || 0}\n`
        csvContent += `Total Expenses,${data.totalExpenses || 0}\n`
        csvContent += `Net Profit/Loss,${data.netProfit || 0}\n`
        break
      case 'cashFlow':
        csvContent += `Opening Balance,${data.openingBalance || 0}\n`
        csvContent += `Total Inflow,${data.totalInflow || 0}\n`
        csvContent += `Total Outflow,${data.totalOutflow || 0}\n`
        csvContent += `Closing Balance,${data.closingBalance || 0}\n`
        break
    }

    // Download CSV
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `${reportType}_report_${startDate}_to_${endDate}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  const renderReportContent = () => {
    if (isLoading) {
      return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      )
    }

    if (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Failed to load report'
      return (
        <Alert severity="error">
          {errorMessage}
          <Box mt={1}>
            <Button variant="outlined" size="small" onClick={() => refetch()}>
              Retry
            </Button>
          </Box>
        </Alert>
      )
    }

    if (!data) {
      return <Alert severity="info">Select date range and generate report</Alert>
    }

    switch (reportType) {
      case 'revenue':
        return (
          <Box>
            <Grid container spacing={3} mb={3}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Revenue
                    </Typography>
                    <Typography variant="h4">
                      ${parseFloat(data.totalRevenue || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Payments
                    </Typography>
                    <Typography variant="h4">{data.totalPayments || 0}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Average Payment
                    </Typography>
                    <Typography variant="h4">
                      ${parseFloat(data.averagePayment || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            {data.revenueByMonth && (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Month</TableCell>
                      <TableCell align="right">Revenue</TableCell>
                      <TableCell align="right">Payments</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.revenueByMonth.map((item, index) => (
                      <TableRow key={index}>
                        <TableCell>{item.month}</TableCell>
                        <TableCell align="right">${parseFloat(item.revenue || 0).toFixed(2)}</TableCell>
                        <TableCell align="right">{item.payments || 0}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        )

      case 'expenses':
        return (
          <Box>
            <Grid container spacing={3} mb={3}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Expenses
                    </Typography>
                    <Typography variant="h4">
                      ${parseFloat(data.totalExpenses || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Pending Expenses
                    </Typography>
                    <Typography variant="h4">{data.pendingExpenses || 0}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Approved Expenses
                    </Typography>
                    <Typography variant="h4">{data.approvedExpenses || 0}</Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            {data.expensesByCategory && (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Category</TableCell>
                      <TableCell align="right">Amount</TableCell>
                      <TableCell align="right">Count</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.expensesByCategory.map((item, index) => (
                      <TableRow key={index}>
                        <TableCell>{item.category}</TableCell>
                        <TableCell align="right">${parseFloat(item.amount || 0).toFixed(2)}</TableCell>
                        <TableCell align="right">{item.count || 0}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        )

      case 'profitLoss':
        return (
          <Box>
            <Grid container spacing={3} mb={3}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Revenue
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      ${parseFloat(data.totalRevenue || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Expenses
                    </Typography>
                    <Typography variant="h4" color="error.main">
                      ${parseFloat(data.totalExpenses || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Net Profit/Loss
                    </Typography>
                    <Typography
                      variant="h4"
                      color={
                        parseFloat(data.netProfit || 0) >= 0 ? 'success.main' : 'error.main'
                      }
                    >
                      ${parseFloat(data.netProfit || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Box>
        )

      case 'cashFlow':
        return (
          <Box>
            <Grid container spacing={3} mb={3}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Opening Balance
                    </Typography>
                    <Typography variant="h4">
                      ${parseFloat(data.openingBalance || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Inflow
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      ${parseFloat(data.totalInflow || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Outflow
                    </Typography>
                    <Typography variant="h4" color="error.main">
                      ${parseFloat(data.totalOutflow || 0).toFixed(2)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Box>
        )

      default:
        return <Alert severity="info">Select a report type</Alert>
    }
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Financial Reports</Typography>
        <Button
          variant="outlined"
          startIcon={<DownloadIcon />}
          onClick={handleExport}
          disabled={!data || isLoading}
        >
          Export Report
        </Button>
      </Box>

      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={3}>
          <TextField
            fullWidth
            select
            label="Report Type"
            value={reportType}
            onChange={(e) => setReportType(e.target.value)}
          >
            <MenuItem value="revenue">Revenue Report</MenuItem>
            <MenuItem value="expenses">Expense Report</MenuItem>
            <MenuItem value="profitLoss">Profit & Loss</MenuItem>
            <MenuItem value="cashFlow">Cash Flow</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Start Date"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="End Date"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
      </Grid>

      {renderReportContent()}
    </Box>
  )
}

export default FinanceReports
