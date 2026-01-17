import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  TextField,
  Popper,
  Paper,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Box,
  Typography,
  Divider,
  InputAdornment,
  IconButton,
} from '@mui/material'
import { Search as SearchIcon, Close as CloseIcon } from '@mui/icons-material'
import { useQuery } from '@tanstack/react-query'
import { searchApi } from '../../api/search'
import { useAuthStore } from '../../store/authStore'

const GlobalSearch = () => {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const [anchorEl, setAnchorEl] = useState(null)
  const searchRef = useRef(null)
  const navigate = useNavigate()
  const { user } = useAuthStore()

  const { data, isLoading } = useQuery({
    queryKey: ['globalSearch', query],
    queryFn: () => searchApi.globalSearch(query, 5),
    enabled: query.length >= 2,
    staleTime: 30000,
    retry: false,
    onError: (err) => {
      console.error('Search failed:', err)
    },
  })

  useEffect(() => {
    setAnchorEl(searchRef.current)
  }, [])

  const handleChange = (e) => {
    const value = e.target.value
    setQuery(value)
    setOpen(value.length >= 2)
  }

  const handleClose = () => {
    setOpen(false)
    setQuery('')
  }

  const handleResultClick = (url) => {
    navigate(url)
    handleClose()
  }

  const searchResults = data?.data?.data || {}

  const hasResults =
    (searchResults.courses?.length || 0) +
    (searchResults.students?.length || 0) +
    (searchResults.invoices?.length || 0) +
    (searchResults.leads?.length || 0) >
    0

  return (
    <Box sx={{ flexGrow: 1, maxWidth: 600, mx: 2 }}>
      <TextField
        ref={searchRef}
        fullWidth
        size="small"
        placeholder="Search courses, students, invoices..."
        value={query}
        onChange={handleChange}
        onFocus={() => query.length >= 2 && setOpen(true)}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
          endAdornment: query && (
            <InputAdornment position="end">
              <IconButton size="small" onClick={handleClose}>
                <CloseIcon fontSize="small" />
              </IconButton>
            </InputAdornment>
          ),
        }}
      />

      <Popper
        open={open && query.length >= 2}
        anchorEl={anchorEl}
        placement="bottom-start"
        style={{ zIndex: 1300, width: anchorEl?.offsetWidth }}
      >
        <Paper sx={{ width: '100%', maxHeight: 400, overflow: 'auto', mt: 1 }}>
          {isLoading && (
            <Box p={2}>
              <Typography variant="body2" color="textSecondary">
                Searching...
              </Typography>
            </Box>
          )}

          {!isLoading && !hasResults && query.length >= 2 && (
            <Box p={2}>
              <Typography variant="body2" color="textSecondary">
                No results found
              </Typography>
            </Box>
          )}

          {!isLoading && hasResults && (
            <List dense>
              {searchResults.courses?.length > 0 && (
                <>
                  <ListItem>
                    <Typography variant="subtitle2" color="primary">
                      Courses
                    </Typography>
                  </ListItem>
                  {searchResults.courses.map((course) => (
                    <ListItemButton
                      key={course.id}
                      onClick={() => handleResultClick(`/academic/courses?id=${course.id}`)}
                    >
                      <ListItemText
                        primary={course.courseName}
                        secondary={course.courseCode}
                      />
                    </ListItemButton>
                  ))}
                  {searchResults.students?.length > 0 && <Divider />}
                </>
              )}

              {searchResults.students?.length > 0 && (user?.role === 'ADMIN' || user?.role === 'ACADEMIC_STAFF' || user?.role === 'FINANCE_STAFF') && (
                <>
                  <ListItem>
                    <Typography variant="subtitle2" color="primary">
                      Students
                    </Typography>
                  </ListItem>
                  {searchResults.students.map((student) => (
                    <ListItemButton
                      key={student.id}
                      onClick={() => handleResultClick(student.url)}
                    >
                      <ListItemText
                        primary={student.title}
                        secondary={student.subtitle}
                      />
                    </ListItemButton>
                  ))}
                  {searchResults.invoices?.length > 0 && <Divider />}
                </>
              )}

              {searchResults.invoices?.length > 0 && (
                <>
                  <ListItem>
                    <Typography variant="subtitle2" color="primary">
                      Invoices
                    </Typography>
                  </ListItem>
                  {searchResults.invoices.map((invoice) => (
                    <ListItemButton
                      key={invoice.id}
                      onClick={() => handleResultClick(`/finance/invoices?id=${invoice.id}`)}
                    >
                      <ListItemText
                        primary={`Invoice #${invoice.invoiceNumber}`}
                        secondary={`${invoice.studentName} - $${invoice.totalAmount}`}
                      />
                    </ListItemButton>
                  ))}
                  {searchResults.leads?.length > 0 && <Divider />}
                </>
              )}

              {searchResults.leads?.length > 0 && (user?.role === 'ADMIN' || user?.role === 'HR_OFFICER') && (
                <>
                  <ListItem>
                    <Typography variant="subtitle2" color="primary">
                      Leads
                    </Typography>
                  </ListItem>
                  {searchResults.leads.map((lead) => (
                    <ListItemButton
                      key={lead.id}
                      onClick={() => handleResultClick(`/marketing/leads?id=${lead.id}`)}
                    >
                      <ListItemText
                        primary={`${lead.firstName} ${lead.lastName}`}
                        secondary={lead.email}
                      />
                    </ListItemButton>
                  ))}
                </>
              )}
            </List>
          )}
        </Paper>
      </Popper>
    </Box>
  )
}

export default GlobalSearch

