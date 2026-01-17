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
} from '@mui/material'
import { Edit as EditIcon, Delete as DeleteIcon, Visibility as ViewIcon, Download as DownloadIcon } from '@mui/icons-material'

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

  return (
    <Paper>
      {title && (
        <Box p={2} pb={0}>
          <Typography variant="h6">{title}</Typography>
        </Box>
      )}
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell key={col.field}>{col.headerName}</TableCell>
              ))}
              {(actions.view || actions.edit || actions.delete || actions.download) && (
                <TableCell align="right">Actions</TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows?.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={columns.length + 1}>
                  <Box p={2} textAlign="center">
                    <Typography variant="body2" color="textSecondary">
                      {emptyMessage}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            )}
            {rows?.map((row) => (
              <TableRow key={row.id || row.key}>
                {columns.map((col) => (
                  <TableCell key={col.field}>
                    {col.render ? col.render(row[col.field], row) : row[col.field]}
                  </TableCell>
                ))}
                {(actions.view || actions.edit || actions.delete || actions.download) && (
                  <TableCell align="right">
                    {actions.view && (
                      <IconButton size="small" onClick={() => actions.view(row)}>
                        <ViewIcon fontSize="inherit" />
                      </IconButton>
                    )}
                    {actions.download && (
                      <IconButton size="small" onClick={() => actions.download(row)}>
                        <DownloadIcon fontSize="inherit" />
                      </IconButton>
                    )}
                    {actions.edit && (
                      <IconButton size="small" onClick={() => actions.edit(row)}>
                        <EditIcon fontSize="inherit" />
                      </IconButton>
                    )}
                    {actions.delete && (
                      <IconButton size="small" color="error" onClick={() => actions.delete(row)}>
                        <DeleteIcon fontSize="inherit" />
                      </IconButton>
                    )}
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
        />
      )}
    </Paper>
  )
}

export default DataTable


