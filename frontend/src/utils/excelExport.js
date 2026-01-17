import * as XLSX from 'xlsx'

/**
 * Export data to Excel file
 * @param {Array} data - Array of objects to export
 * @param {String} filename - Name of the file (without extension)
 * @param {String} sheetName - Name of the Excel sheet
 */
export const exportToExcel = (data, filename = 'export', sheetName = 'Sheet1') => {
  if (!data || data.length === 0) {
    console.warn('No data to export')
    return
  }

  // Create a new workbook
  const workbook = XLSX.utils.book_new()

  // Convert data to worksheet
  const worksheet = XLSX.utils.json_to_sheet(data)

  // Auto-size columns
  const columnWidths = Object.keys(data[0]).map(key => ({
    wch: Math.max(key.length, 15),
  }))
  worksheet['!cols'] = columnWidths

  // Add worksheet to workbook
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)

  // Generate Excel file and trigger download
  XLSX.writeFile(workbook, `${filename}.xlsx`)
}

/**
 * Export multiple sheets to Excel file
 * @param {Object} sheets - Object with sheet names as keys and data arrays as values
 * @param {String} filename - Name of the file (without extension)
 */
export const exportMultipleSheets = (sheets, filename = 'export') => {
  const workbook = XLSX.utils.book_new()

  Object.entries(sheets).forEach(([sheetName, data]) => {
    if (data && data.length > 0) {
      const worksheet = XLSX.utils.json_to_sheet(data)
      
      // Auto-size columns
      const columnWidths = Object.keys(data[0]).map(key => ({
        wch: Math.max(key.length, 15),
      }))
      worksheet['!cols'] = columnWidths
      
      XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
    }
  })

  XLSX.writeFile(workbook, `${filename}.xlsx`)
}

