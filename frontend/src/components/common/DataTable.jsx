import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  IconButton,
  Box,
  Typography,
  Tooltip,
  Skeleton,
  Avatar,
} from '@mui/material'
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Download as DownloadIcon,
  Inbox as InboxIcon,
} from '@mui/icons-material'

const DataTable = ({
  title,
  columns,
  rows,
  page,
  pageSize,
  total,
  onPageChange,
  onPageSizeChange,
  actions = {},
  loading = false,
  emptyMessage = 'No records found',
}) => {
  const handleChangePage = (_, newPage) => {
    onPageChange?.(newPage)
  }

  const handleChangeRowsPerPage = (event) => {
    onPageSizeChange?.(parseInt(event.target.value, 10))
    onPageChange?.(0)
  }

  const hasActions = actions.view || actions.edit || actions.delete || actions.download

  return (
    <Paper
      sx={{
        borderRadius: 3,
        overflow: 'hidden',
        border: '1px solid #E2E8F0',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
      }}
    >
      {title && (
        <Box
          sx={{
            p: 3,
            pb: 2,
            borderBottom: '1px solid #E2E8F0',
            background: 'linear-gradient(135deg, #F8FAFC 0%, #EEF2FF 100%)',
          }}
        >
          <Typography variant="h6" fontWeight={600}>
            {title}
          </Typography>
        </Box>
      )}
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell
                  key={col.field}
                  sx={{
                    fontWeight: 600,
                    color: '#475569',
                    fontSize: '0.8rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    py: 2,
                    background: '#F8FAFC',
                    borderBottom: '2px solid #E2E8F0',
                  }}
                >
                  {col.headerName}
                </TableCell>
              ))}
              {hasActions && (
                <TableCell
                  align="right"
                  sx={{
                    fontWeight: 600,
                    color: '#475569',
                    fontSize: '0.8rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    py: 2,
                    background: '#F8FAFC',
                    borderBottom: '2px solid #E2E8F0',
                  }}
                >
                  Actions
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading && (
              [...Array(5)].map((_, index) => (
                <TableRow key={index}>
                  {columns.map((col, colIndex) => (
                    <TableCell key={colIndex}>
                      <Skeleton variant="text" width="80%" height={24} />
                    </TableCell>
                  ))}
                  {hasActions && (
                    <TableCell>
                      <Skeleton variant="text" width={80} height={24} />
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
            {!loading && rows?.length === 0 && (
              <TableRow>
                <TableCell colSpan={columns.length + (hasActions ? 1 : 0)}>
                  <Box
                    sx={{
                      py: 8,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      gap: 2,
                    }}
                  >
                    <Avatar
                      sx={{
                        width: 64,
                        height: 64,
                        bgcolor: '#F1F5F9',
                        color: '#94A3B8',
                      }}
                    >
                      <InboxIcon sx={{ fontSize: 32 }} />
                    </Avatar>
                    <Typography variant="body1" color="text.secondary" fontWeight={500}>
                      {emptyMessage}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            )}
            {!loading && rows?.map((row, rowIndex) => (
              <TableRow
                key={row.id || row.key || rowIndex}
                sx={{
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    bgcolor: 'rgba(99, 102, 241, 0.04)',
                  },
                  '&:last-child td': {
                    borderBottom: 0,
                  },
                }}
              >
                {columns.map((col) => (
                  <TableCell
                    key={col.field}
                    sx={{
                      py: 2,
                      color: '#334155',
                      fontSize: '0.875rem',
                    }}
                  >
                    {col.render ? col.render(row[col.field], row) : row[col.field]}
                  </TableCell>
                ))}
                {hasActions && (
                  <TableCell align="right" sx={{ py: 1.5 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
                      {actions.view && (
                        <Tooltip title="View" arrow>
                          <IconButton
                            size="small"
                            onClick={() => actions.view(row)}
                            sx={{
                              color: '#3B82F6',
                              bgcolor: '#EFF6FF',
                              '&:hover': {
                                bgcolor: '#DBEAFE',
                              },
                            }}
                          >
                            <ViewIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                      {actions.download && (
                        <Tooltip title="Download" arrow>
                          <IconButton
                            size="small"
                            onClick={() => actions.download(row)}
                            sx={{
                              color: '#10B981',
                              bgcolor: '#ECFDF5',
                              '&:hover': {
                                bgcolor: '#D1FAE5',
                              },
                            }}
                          >
                            <DownloadIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                      {actions.edit && (
                        <Tooltip title="Edit" arrow>
                          <IconButton
                            size="small"
                            onClick={() => actions.edit(row)}
                            sx={{
                              color: '#6366F1',
                              bgcolor: '#EEF2FF',
                              '&:hover': {
                                bgcolor: '#E0E7FF',
                              },
                            }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                      {actions.delete && (
                        <Tooltip title="Delete" arrow>
                          <IconButton
                            size="small"
                            onClick={() => actions.delete(row)}
                            sx={{
                              color: '#EF4444',
                              bgcolor: '#FEF2F2',
                              '&:hover': {
                                bgcolor: '#FEE2E2',
                              },
                            }}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                    </Box>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      {typeof total === 'number' && (
        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={pageSize}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[5, 10, 20, 50]}
          sx={{
            borderTop: '1px solid #E2E8F0',
            '.MuiTablePagination-selectLabel, .MuiTablePagination-displayedRows': {
              fontWeight: 500,
              color: '#64748B',
            },
          }}
        />
      )}
    </Paper>
  )
}

export default DataTable


