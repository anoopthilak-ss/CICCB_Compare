package com.example.excelcomparison.service;

import com.example.excelcomparison.model.ExcelData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ExcelService {

    public List<String> getSheetNames(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        // Memory monitoring
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        System.out.println("=== Excel Service Memory Status ===");
        System.out.println("Max JVM Memory: " + (maxMemory / 1024 / 1024) + "MB");
        System.out.println("Currently Used: " + (usedMemory / 1024 / 1024) + "MB (" + String.format("%.1f", memoryUsagePercent) + "%)");
        
        if (memoryUsagePercent > 75) {
            System.out.println("WARNING: High memory usage during Excel parsing!");
            System.gc();
        }
        
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<String> sheetNames = new ArrayList<>();
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        
        workbook.close();
        return sheetNames;
    }

    public List<String> getSheetNames(byte[] fileContent) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("File content cannot be null or empty");
        }
        
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileContent));
        List<String> sheetNames = new ArrayList<>();
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        
        workbook.close();
        return sheetNames;
    }

    public ExcelData parseExcelFile(MultipartFile file) throws IOException {
        return parseExcelFile(file, 0); // Default to first sheet
    }

    public ExcelData parseExcelFile(MultipartFile file, int sheetIndex) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        return parseExcelFromWorkbook(workbook, sheetIndex, file.getOriginalFilename());
    }

    public ExcelData parseExcelFile(byte[] fileContent, int sheetIndex, String fileName) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("File content cannot be null or empty");
        }
        
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileContent));
        return parseExcelFromWorkbook(workbook, sheetIndex, fileName);
    }

    private ExcelData parseExcelFromWorkbook(Workbook workbook, int sheetIndex, String fileName) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        
        // Get all sheet names
        List<String> sheetNames = new ArrayList<>(workbook.getNumberOfSheets());
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        
        // Pre-size collections based on sheet size to reduce memory reallocation
        int estimatedRows = Math.min(sheet.getLastRowNum(), 10000); // Cap at 10k for safety
        List<String> headers = new ArrayList<>(20); // Typical Excel has <20 columns
        List<Map<String, Object>> rows = new ArrayList<>(estimatedRows);
        
        // Log memory usage before parsing
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory before Excel parsing: " + (beforeMemory / 1024 / 1024) + "MB");
        System.out.println("Estimated rows to process: " + estimatedRows);
        
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
        }
        
        // Process rows with memory management
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Map<String, Object> rowData = new LinkedHashMap<>(headers.size()); // Initial capacity based on header count
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValueAsString(cell));
                }
                rows.add(rowData);
                
                // Periodic garbage collection for large files
                if (i % 1000 == 0) {
                    System.gc();
                    System.out.println("Processed " + i + " rows...");
                }
            }
        }
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory after Excel parsing: " + (afterMemory / 1024 / 1024) + "MB");
        System.out.println("Memory used for parsing: " + ((afterMemory - beforeMemory) / 1024 / 1024) + "MB");
        
        String selectedSheet = workbook.getSheetName(sheetIndex);
        workbook.close();
        return new ExcelData(fileName, headers, rows, sheetNames, selectedSheet);
    }

    public byte[] createComparisonResult(List<Map<String, Object>> matchedRows, 
                                       List<Map<String, Object>> mismatchedRows) throws IOException {
        if (matchedRows == null) matchedRows = new ArrayList<>();
        if (mismatchedRows == null) mismatchedRows = new ArrayList<>();
        
        Workbook workbook = new XSSFWorkbook();
        
        if (!matchedRows.isEmpty()) {
            Sheet matchedSheet = workbook.createSheet("Matched Rows");
            createSheetWithData(matchedSheet, matchedRows);
        }
        
        if (!mismatchedRows.isEmpty()) {
            Sheet mismatchedSheet = workbook.createSheet("Mismatched Rows");
            createSheetWithData(mismatchedSheet, mismatchedRows);
        }
        
        // If both lists are empty, create a sheet with a message
        if (matchedRows.isEmpty() && mismatchedRows.isEmpty()) {
            Sheet emptySheet = workbook.createSheet("No Results");
            Row row = emptySheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No comparison results available");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void createSheetWithData(Sheet sheet, List<Map<String, Object>> data) {
        if (sheet == null || data == null || data.isEmpty()) return;
        
        Row headerRow = sheet.createRow(0);
        Map<String, Object> firstRow = data.get(0);
        if (firstRow == null) return;
        
        int colIndex = 0;
        for (String header : firstRow.keySet()) {
            if (header != null) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(header);
            }
        }
        
        int rowIndex = 1;
        for (Map<String, Object> rowData : data) {
            if (rowData != null) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;
                for (Object value : rowData.values()) {
                    Cell cell = row.createCell(colIndex++);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
