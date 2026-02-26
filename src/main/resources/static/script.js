let file1Data = null;
let file2Data = null;
let currentResultId = null;
let matchedResults = [];
let mismatchedResults = [];
let currentTab = 'matched';
let currentPage = {
    matched: 1,
    mismatched: 1
};
const rowsPerPage = 10;

// File upload handlers
document.getElementById('file1').addEventListener('change', function(e) {
    handleFileUpload(e, 'file1');
});

document.getElementById('file2').addEventListener('change', function(e) {
    handleFileUpload(e, 'file2');
});

// Sheet selection handlers
document.getElementById('sheet1').addEventListener('change', function(e) {
    handleSheetSelection(e, 'file1');
    hideResults(); // Hide results when sheet changes
    resetComparisonData(); // Reset data when sheet changes
});

document.getElementById('sheet2').addEventListener('change', function(e) {
    handleSheetSelection(e, 'file2');
    hideResults(); // Hide results when sheet changes
    resetComparisonData(); // Reset data when sheet changes
});

async function handleFileUpload(event, fileKey) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
        showLoading(true);
        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            console.log('File upload successful:', result);
            
            // Store file data with sheet information
            if (fileKey === 'file1') {
                file1Data = {
                    fileId: result.fileId,
                    fileName: result.fileName,
                    headers: result.headers,
                    sheetNames: result.sheetNames,
                    selectedSheet: result.selectedSheet,
                    hasMultipleSheets: result.hasMultipleSheets
                };
            } else {
                file2Data = result;
            }

            // Update UI
            updateFileUI(fileKey, result.fileName);
            populateSheetSelection(fileKey, result);
            
            // Only populate columns immediately if there's only one sheet
            if (!result.hasMultipleSheets) {
                console.log('Single sheet detected, populating columns immediately:', result.headers);
                populateColumnSelection(fileKey, result.headers);
            } else {
                console.log('Multiple sheets detected, waiting for sheet selection');
            }
            
            // Hide results when new file is uploaded
            hideResults();
            resetComparisonData(); // Reset data when new file is uploaded
            
            // Show appropriate sections
            if (file1Data && file2Data) {
                showSheetSelection();
            }
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        showMessage('Error uploading file: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

function updateFileUI(fileKey, fileName) {
    const fileInput = document.getElementById(fileKey);
    const fileInfo = document.getElementById(fileKey + '-info');
    const fileNameSpan = fileInfo.querySelector('.file-name');
    
    fileInput.style.display = 'none';
    fileInfo.classList.remove('hidden');
    fileNameSpan.textContent = fileName;
}

function removeFile(fileKey) {
    // Clear file data
    if (fileKey === 'file1') {
        file1Data = null;
    } else {
        file2Data = null;
    }

    // Reset UI
    const fileInput = document.getElementById(fileKey);
    const fileInfo = document.getElementById(fileKey + '-info');
    
    fileInput.style.display = 'block';
    fileInput.value = '';
    fileInfo.classList.add('hidden');

    // Clear dropdowns
    const columnDropdown = document.getElementById(fileKey === 'file1' ? 'column1' : 'column2');
    const sheetDropdown = document.getElementById(fileKey === 'file1' ? 'sheet1' : 'sheet2');
    columnDropdown.innerHTML = '<option value="">Select a column</option>';
    sheetDropdown.innerHTML = '<option value="">Select a sheet</option>';
    
    // Hide sheet info
    const sheetInfo = document.getElementById(fileKey + '-sheet-info');
    sheetInfo.classList.add('hidden');

    // Hide sections if needed
    hideResults();
    resetComparisonData(); // Reset data when file is removed
    checkReadyForComparison();
}

function populateSheetSelection(fileKey, result) {
    const sheetSelect = document.getElementById(fileKey === 'file1' ? 'sheet1' : 'sheet2');
    sheetSelect.innerHTML = '<option value="">Select a sheet</option>';
    
    if (result.sheetNames && result.sheetNames.length > 0) {
        result.sheetNames.forEach((sheetName, index) => {
            const option = document.createElement('option');
            option.value = index;
            option.textContent = sheetName;
            option.selected = sheetName === result.selectedSheet;
            sheetSelect.appendChild(option);
        });
        
        // Show sheet selection if multiple sheets exist
        if (result.hasMultipleSheets) {
            document.getElementById('sheet-selection').classList.remove('hidden');
        }
    }
}

async function handleSheetSelection(event, fileKey) {
    const sheetIndex = event.target.value;
    if (!sheetIndex) return;
    
    console.log('handleSheetSelection called:', { fileKey, sheetIndex });
    
    const fileData = fileKey === 'file1' ? file1Data : file2Data;
    if (!fileData) return;
    
    try {
        showLoading(true);
        const formData = new FormData();
        formData.append('fileId', fileData.fileId);
        formData.append('sheetIndex', sheetIndex);
        
        console.log('Sending sheet selection request:', { fileId: fileData.fileId, sheetIndex });
        
        const response = await fetch('/api/select-sheet', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        console.log('Sheet selection response:', result);
        
        if (result.success) {
            // Update file data with new headers
            fileData.selectedSheet = result.selectedSheet;
            fileData.headers = result.headers;
            
            console.log('Updated file data:', {
                selectedSheet: fileData.selectedSheet,
                headers: fileData.headers,
                headersLength: fileData.headers ? fileData.headers.length : 0
            });
            
            // Update column dropdown
            if (result.headers && result.headers.length > 0) {
                console.log('Populating columns with new headers:', result.headers);
                populateColumnSelection(fileKey, result.headers);
            } else {
                // Show no columns message
                console.log('No columns available in selected sheet');
                const columnSelect = document.getElementById(fileKey === 'file1' ? 'column1' : 'column2');
                columnSelect.innerHTML = '<option value="">No columns available in selected sheet</option>';
            }
            
            // Show selected sheet info
            const sheetInfo = document.getElementById(fileKey + '-sheet-info');
            sheetInfo.querySelector('.selected-sheet').textContent = `Selected: ${result.selectedSheet}`;
            sheetInfo.classList.remove('hidden');
            
            checkReadyForComparison();
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        showMessage('Error selecting sheet: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

function showSheetSelection() {
    // Show sheet selection if either file has multiple sheets
    const file1HasMultipleSheets = file1Data && file1Data.hasMultipleSheets;
    const file2HasMultipleSheets = file2Data && file2Data.hasMultipleSheets;
    
    console.log('showSheetSelection called:', {
        file1HasMultipleSheets,
        file2HasMultipleSheets,
        file1Data: file1Data ? { hasMultipleSheets: file1Data.hasMultipleSheets, headers: file1Data.headers ? file1Data.headers.length : 0 } : null,
        file2Data: file2Data ? { hasMultipleSheets: file2Data.hasMultipleSheets, headers: file2Data.headers ? file2Data.headers.length : 0 } : null
    });
    
    if (file1HasMultipleSheets || file2HasMultipleSheets) {
        document.getElementById('sheet-selection').classList.remove('hidden');
    }
    
    // Show column selection and check if columns should be populated
    if (file1Data && file2Data) {
        document.getElementById('column-selection').classList.remove('hidden');
        
        // Always populate columns for single-sheet files
        if (!file1HasMultipleSheets && file1Data.headers && file1Data.headers.length > 0) {
            console.log('Auto-populating columns for file1 (single sheet):', file1Data.headers);
            populateColumnSelection('file1', file1Data.headers);
        }
        if (!file2HasMultipleSheets && file2Data.headers && file2Data.headers.length > 0) {
            console.log('Auto-populating columns for file2 (single sheet):', file2Data.headers);
            populateColumnSelection('file2', file2Data.headers);
        }
        
        // For multi-sheet files, populate columns with default sheet headers if available
        if (file1HasMultipleSheets && file1Data.headers && file1Data.headers.length > 0) {
            console.log('Auto-populating columns for file1 (multi-sheet, default):', file1Data.headers);
            populateColumnSelection('file1', file1Data.headers);
        }
        if (file2HasMultipleSheets && file2Data.headers && file2Data.headers.length > 0) {
            console.log('Auto-populating columns for file2 (multi-sheet, default):', file2Data.headers);
            populateColumnSelection('file2', file2Data.headers);
        }
    }
    
    checkReadyForComparison();
}

function checkReadyForComparison() {
    const file1Ready = file1Data && file1Data.selectedSheet;
    const file2Ready = file2Data && file2Data.selectedSheet;
    const column1Selected = document.getElementById('column1').value;
    const column2Selected = document.getElementById('column2').value;
    const compareSection = document.getElementById('compare-section');
    const compareBtn = document.getElementById('compare-btn');
    
    if (file1Ready && file2Ready && column1Selected && column2Selected) {
        compareSection.classList.remove('hidden');
        compareBtn.disabled = false;
    } else {
        compareSection.classList.add('hidden');
        compareBtn.disabled = true;
    }
}

function populateColumnSelection(fileKey, headers) {
    console.log('populateColumnSelection called:', { fileKey, headers });
    
    const columnSelect = document.getElementById(fileKey === 'file1' ? 'column1' : 'column2');
    columnSelect.innerHTML = '<option value="">Select a column</option>';
    
    if (headers && headers.length > 0) {
        headers.forEach(header => {
            const option = document.createElement('option');
            option.value = header;
            option.textContent = header;
            columnSelect.appendChild(option);
        });
        
        // Update file data with new headers
        if (fileKey === 'file1' && file1Data) {
            file1Data.headers = headers;
        } else if (fileKey === 'file2' && file2Data) {
            file2Data.headers = headers;
        }
        
        console.log('Columns populated successfully for', fileKey);
    } else {
        // Show no columns message
        columnSelect.innerHTML = '<option value="">No columns available in selected sheet</option>';
        console.log('No columns available for', fileKey);
    }
    
    checkReadyForComparison();
}

function populateColumnDropdowns() {
    if (file1Data) {
        const column1Select = document.getElementById('column1');
        column1Select.innerHTML = '<option value="">Select a column</option>';
        file1Data.headers.forEach(header => {
            const option = document.createElement('option');
            option.value = header;
            option.textContent = header;
            column1Select.appendChild(option);
        });
    }

    if (file2Data) {
        const column2Select = document.getElementById('column2');
        column2Select.innerHTML = '<option value="">Select a column</option>';
        file2Data.headers.forEach(header => {
            const option = document.createElement('option');
            option.value = header;
            option.textContent = header;
            column2Select.appendChild(option);
        });
    }

    // Show column selection section
    if (file1Data && file2Data) {
        document.getElementById('column-selection').classList.remove('hidden');
    }
}

// Column selection handlers
document.getElementById('column1').addEventListener('change', function() {
    hideResults(); // Hide results when columns change
    resetComparisonData(); // Reset data when columns change
    checkReadyForComparison();
});

document.getElementById('column2').addEventListener('change', function() {
    hideResults(); // Hide results when columns change
    resetComparisonData(); // Reset data when columns change
    checkReadyForComparison();
});

async function compareFiles() {
    if (!file1Data || !file2Data) {
        showMessage('Please upload both files first', 'error');
        return;
    }

    const column1 = document.getElementById('column1').value;
    const column2 = document.getElementById('column2').value;

    if (!column1 || !column2) {
        showMessage('Please select columns to compare', 'error');
        return;
    }

    const requestData = {
        file1Id: file1Data.fileId,
        file2Id: file2Data.fileId,
        column1: column1,
        column2: column2
    };

    try {
        showLoading(true);
        const response = await fetch('/api/compare', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();

        console.log('Comparison response received:', result);

        if (result.success) {
            currentResultId = result.resultId;
            console.log('Set currentResultId:', currentResultId);
            displayResults(
                result.matchedCount, 
                result.mismatchedCount, 
                result.matchedRows, 
                result.mismatchedRows
            );
            showMessage('Comparison completed successfully!', 'success');
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        showMessage('Error during comparison: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

function displayResults(matchedCount, mismatchedCount, matchedRows, mismatchedRows) {
    console.log('displayResults called with:', {
        matchedCount,
        mismatchedCount,
        matchedRows: matchedRows ? matchedRows.length : 'null',
        mismatchedRows: mismatchedRows ? mismatchedRows.length : 'null'
    });
    
    // Clear previous results
    hideResults();
    
    // Show results section
    document.getElementById('matched-count').textContent = matchedCount;
    document.getElementById('mismatched-count').textContent = mismatchedCount;
    document.getElementById('results').classList.remove('hidden');

    // Store the actual result data for immediate display
    if (matchedRows) {
        matchedResults = matchedRows;
        console.log('Stored matchedResults:', matchedResults.length, 'items');
    }
    if (mismatchedRows) {
        mismatchedResults = mismatchedRows;
        console.log('Stored mismatchedResults:', mismatchedResults.length, 'items');
    }

    // Setup download buttons
    const downloadMatchedBtn = document.getElementById('download-matched');
    const downloadMismatchedBtn = document.getElementById('download-mismatched');
    const viewMatchedBtn = document.getElementById('view-matched');
    const viewMismatchedBtn = document.getElementById('view-mismatched');

    downloadMatchedBtn.onclick = () => downloadResult('matched');
    downloadMismatchedBtn.onclick = () => downloadResult('mismatched');
    viewMatchedBtn.onclick = () => showTabularResults('matched');
    viewMismatchedBtn.onclick = () => showTabularResults('mismatched');

    // Setup search and export
    setupSearchAndExport();
    
    console.log('Results display completed');
}

function downloadResult(type) {
    console.log('Download clicked - currentResultId:', currentResultId);
    
    if (!currentResultId) {
        showMessage('No comparison result available', 'error');
        return;
    }

    const url = `/api/download/${currentResultId}/${type}`;
    
    // Add timestamp to make filename unique
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    const timestamp = `${year}${month}${day}_${hours}${minutes}${seconds}`;
    
    console.log('Generated timestamp:', timestamp);
    
    const baseFilename = type === 'matched' ? 'matched_rows' : 'mismatched_rows';
    const filename = `${baseFilename}_${timestamp}.xlsx`;
    
    console.log('Download filename:', filename);
    console.log('Download URL:', url);
    
    // Create download link with proper attributes
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.style.display = 'none';
    
    // Add to DOM and trigger download
    document.body.appendChild(link);
    
    // Small delay to ensure everything is set
    setTimeout(() => {
        link.click();
        document.body.removeChild(link);
        console.log('Download triggered');
    }, 100);
}

function hideResults() {
    // Hide results section when sheet changes or file is removed
    const resultsSection = document.getElementById('results');
    if (resultsSection) {
        resultsSection.classList.add('hidden');
    }
    
    // Also hide tabular results
    const tabularResults = document.getElementById('tabular-results');
    if (tabularResults) {
        tabularResults.classList.add('hidden');
    }
    
    // Hide loading indicator
    showLoading(false);
    
    // Don't reset comparison data here - only hide UI elements
    // currentResultId = null;
    // matchedResults = [];
    // mismatchedResults = [];
}

function resetComparisonData() {
    // Reset comparison data when files are changed or removed
    currentResultId = null;
    matchedResults = [];
    mismatchedResults = [];
}

function showLoading(show) {
    const loading = document.getElementById('loading');
    if (show) {
        loading.classList.remove('hidden');
    } else {
        loading.classList.add('hidden');
    }
}

// Tabular Results Functions
async function fetchAndDisplayResults(type) {
    if (!currentResultId) {
        showMessage('No comparison result available', 'error');
        return;
    }

    try {
        showLoading(true);
        // Use simple POST endpoint to avoid static resource conflicts
        const response = await fetch('/api/get-results', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                resultId: currentResultId,
                type: type
            })
        });
        
        const result = await response.json();

        if (result.success) {
            if (type === 'matched') {
                matchedResults = result.rows || [];
            } else {
                mismatchedResults = result.rows || [];
            }
            
            showTabularResults(type);
        } else {
            showMessage(result.message, 'error');
        }
    } catch (error) {
        showMessage('Error fetching results: ' + error.message, 'error');
        console.error('Fetch error:', error);
    } finally {
        showLoading(false);
    }
}

function showTabularResults(type) {
    console.log('showTabularResults called for type:', type);
    console.log('Current data:', {
        matchedResults: matchedResults ? matchedResults.length : 'null',
        mismatchedResults: mismatchedResults ? mismatchedResults.length : 'null'
    });
    
    const tabularResults = document.getElementById('tabular-results');
    tabularResults.classList.remove('hidden');
    
    // Set the active tab to the requested type
    currentTab = type;
    showTab(type);
    renderTable(type);
    renderPagination(type);
}

function showTab(type) {
    currentTab = type;
    
    console.log('showTab called for type:', type);
    
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.getElementById(`${type}-tab`).classList.add('active');
    
    // Show/hide table containers
    document.getElementById('matched-table-container').classList.toggle('hidden', type !== 'matched');
    document.getElementById('mismatched-table-container').classList.toggle('hidden', type !== 'mismatched');
    
    // Render the table and pagination for the selected tab
    renderTable(type);
    renderPagination(type);
}

function renderTable(type) {
    const data = type === 'matched' ? matchedResults : mismatchedResults;
    const thead = document.getElementById(`${type}-thead`);
    const tbody = document.getElementById(`${type}-tbody`);
    
    console.log('renderTable called for type:', type);
    console.log('Data available:', data ? data.length : 'null');
    
    if (!data || data.length === 0) {
        console.log('No data to render - showing "No results found"');
        thead.innerHTML = '';
        tbody.innerHTML = '<tr><td colspan="100%" class="no-results">No results found</td></tr>';
        return;
    }
    
    console.log('Rendering table with', data.length, 'rows');
    console.log('Sample data:', data[0]);
    
    // Create headers
    const headers = Object.keys(data[0]);
    thead.innerHTML = headers.map(header => `<th>${header}</th>`).join('');
    
    // Create rows with pagination
    const page = currentPage[type];
    const startIndex = (page - 1) * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    const pageData = data.slice(startIndex, endIndex);
    
    console.log('Rendering page', page, 'rows', startIndex, 'to', endIndex, 'showing', pageData.length, 'rows');
    
    tbody.innerHTML = pageData.map(row => {
        const cells = headers.map(header => {
            const value = row[header];
            let cellContent = value;
            
            // Add status badge styling
            if (header === 'Status') {
                const statusClass = getStatusClass(value);
                cellContent = `<span class="status-badge ${statusClass}">${value}</span>`;
            }
            
            return `<td>${cellContent}</td>`;
        }).join('');
        
        return `<tr>${cells}</tr>`;
    }).join('');
}

function getStatusClass(status) {
    switch (status) {
        case 'MATCHED': return 'status-match';
        case 'MISMATCHED': return 'status-mismatch';
        case 'ONLY IN FILE1': return 'status-only-file1';
        case 'ONLY IN FILE2': return 'status-only-file2';
        default: return '';
    }
}

function renderPagination(type) {
    const data = type === 'matched' ? matchedResults : mismatchedResults;
    const pagination = document.getElementById(`${type}-pagination`);
    
    if (!data || data.length === 0) {
        pagination.innerHTML = '';
        return;
    }
    
    const totalPages = Math.ceil(data.length / rowsPerPage);
    const currentPageNum = currentPage[type];
    
    let paginationHTML = '';
    
    // Previous button
    paginationHTML += `<button ${currentPageNum === 1 ? 'disabled' : ''} onclick="changePage('${type}', ${currentPageNum - 1})">Previous</button>`;
    
    // Page numbers
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPageNum - 2 && i <= currentPageNum + 2)) {
            paginationHTML += `<button class="${i === currentPageNum ? 'active' : ''}" onclick="changePage('${type}', ${i})">${i}</button>`;
        } else if (i === currentPageNum - 3 || i === currentPageNum + 3) {
            paginationHTML += '<span>...</span>';
        }
    }
    
    // Next button
    paginationHTML += `<button ${currentPageNum === totalPages ? 'disabled' : ''} onclick="changePage('${type}', ${currentPageNum + 1})">Next</button>`;
    
    pagination.innerHTML = paginationHTML;
}

function changePage(type, page) {
    const data = type === 'matched' ? matchedResults : mismatchedResults;
    const totalPages = Math.ceil(data.length / rowsPerPage);
    
    if (page < 1 || page > totalPages) return;
    
    currentPage[type] = page;
    renderTable(type);
    renderPagination(type);
}

function setupSearchAndExport() {
    // Setup search for matched results
    const matchedSearch = document.getElementById('matched-search');
    matchedSearch.addEventListener('input', (e) => {
        filterResults('matched', e.target.value);
    });
    
    // Setup search for mismatched results
    const mismatchedSearch = document.getElementById('mismatched-search');
    mismatchedSearch.addEventListener('input', (e) => {
        filterResults('mismatched', e.target.value);
    });
    
    // Setup export buttons
    document.getElementById('export-matched-csv').onclick = () => exportToCSV('matched');
    document.getElementById('export-mismatched-csv').onclick = () => exportToCSV('mismatched');
}

function filterResults(type, searchTerm) {
    const data = type === 'matched' ? matchedResults : mismatchedResults;
    const tbody = document.getElementById(`${type}-tbody`);
    
    if (!searchTerm) {
        renderTable(type);
        return;
    }
    
    const filteredData = data.filter(row => {
        return Object.values(row).some(value => 
            value && value.toString().toLowerCase().includes(searchTerm.toLowerCase())
        );
    });
    
    if (filteredData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="100%" class="no-results">No results found matching your search</td></tr>';
        document.getElementById(`${type}-pagination`).innerHTML = '';
        return;
    }
    
    // Render filtered results
    const headers = Object.keys(filteredData[0]);
    const thead = document.getElementById(`${type}-thead`);
    thead.innerHTML = headers.map(header => `<th>${header}</th>`).join('');
    
    tbody.innerHTML = filteredData.map(row => {
        const cells = headers.map(header => {
            const value = row[header];
            let cellContent = value;
            
            if (header === 'Status') {
                const statusClass = getStatusClass(value);
                cellContent = `<span class="status-badge ${statusClass}">${value}</span>`;
            }
            
            return `<td>${cellContent}</td>`;
        }).join('');
        
        return `<tr>${cells}</tr>`;
    }).join('');
    
    document.getElementById(`${type}-pagination`).innerHTML = '';
}

function exportToCSV(type) {
    const data = type === 'matched' ? matchedResults : mismatchedResults;
    
    if (!data || data.length === 0) {
        showMessage('No data to export', 'error');
        return;
    }
    
    const headers = Object.keys(data[0]);
    const csvContent = [
        headers.join(','),
        ...data.map(row => 
            headers.map(header => {
                const value = row[header] || '';
                return `"${value.toString().replace(/"/g, '""')}"`;
            }).join(',')
        )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${type}_results.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    showMessage(`Exported ${data.length} ${type} results to CSV`, 'success');
}

function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.classList.remove('hidden');

    // Auto-hide after 5 seconds
    setTimeout(() => {
        messageDiv.classList.add('hidden');
    }, 5000);
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    // Hide all sections initially
    document.getElementById('column-selection').classList.add('hidden');
    document.getElementById('compare-section').classList.add('hidden');
    document.getElementById('results').classList.add('hidden');
});
