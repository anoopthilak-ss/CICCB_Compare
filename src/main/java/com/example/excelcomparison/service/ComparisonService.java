package com.example.excelcomparison.service;

import com.example.excelcomparison.model.ExcelData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonService {

    public ComparisonResult compareColumns(ExcelData file1, ExcelData file2, String column1, String column2) {
        // Memory monitoring and early warning system
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        System.out.println("=== Memory Status ===");
        System.out.println("Max JVM Memory: " + (maxMemory / 1024 / 1024) + "MB");
        System.out.println("Currently Used: " + (usedMemory / 1024 / 1024) + "MB (" + String.format("%.1f", memoryUsagePercent) + "%)");
        
        // Warning if memory usage is high
        if (memoryUsagePercent > 80) {
            System.out.println("WARNING: High memory usage detected! Consider:");
            System.out.println("1. Using smaller Excel files");
            System.out.println("2. Increasing JVM max heap (-Xmx)");
            System.out.println("3. Closing other applications");
            System.gc(); // Force garbage collection
        }
        
        // Validation
        if (file1 == null || file2 == null) {
            throw new IllegalArgumentException("Both files must be provided for comparison");
        }
        if (column1 == null || column1.trim().isEmpty() || column2 == null || column2.trim().isEmpty()) {
            throw new IllegalArgumentException("Both column names must be provided");
        }
        
        // Debug logging
        System.out.println("Column1: " + column1 + " from " + file1.getSelectedSheet());
        System.out.println("Column2: " + column2 + " from " + file2.getSelectedSheet());
        System.out.println("File1 rows: " + (file1.getRows() != null ? file1.getRows().size() : 0));
        System.out.println("File2 rows: " + (file2.getRows() != null ? file2.getRows().size() : 0));
        System.out.println("Cross-sheet comparison: " + file1.getSelectedSheet() + " -> " + file2.getSelectedSheet());
        
        // Use strict comparison for comma-separated values
        System.out.println("Using strict comma-separated value comparison");
        ComparisonResult result = compareWithStrictCommaSeparatedValues(file1, file2, column1, column2);
        System.out.println("Strict comparison - Matched: " + result.getMatchedRows().size() + ", Mismatched: " + result.getMismatchedRows().size());
        return result;
    }
    
    private ComparisonResult compareWithStrictCommaSeparatedValues(ExcelData file1, ExcelData file2, String column1, String column2) {
        // Memory monitoring
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        System.out.println("=== Memory Status ===");
        System.out.println("Max JVM Memory: " + (maxMemory / 1024 / 1024) + "MB");
        System.out.println("Currently Used: " + (usedMemory / 1024 / 1024) + "MB (" + String.format("%.1f", memoryUsagePercent) + "%)");
        
        // Warning if memory usage is high
        if (memoryUsagePercent > 70) {
            System.out.println("WARNING: High memory usage detected!");
            System.gc();
        }
        
        // Validation
        if (!file1.getHeaders().contains(column1)) {
            throw new IllegalArgumentException("Column '" + column1 + "' not found in first file");
        }
        if (!file2.getHeaders().contains(column2)) {
            throw new IllegalArgumentException("Column '" + column2 + "' not found in second file");
        }
        
        List<Map<String, Object>> matchedRows = new ArrayList<>();
        List<Map<String, Object>> mismatchedRows = new ArrayList<>();
        
        // Process all rows from file1 (Server column)
        for (Map<String, Object> file1Row : file1.getRows()) {
            if (file1Row == null) continue;
            
            Object serverValueObj = file1Row.get(column1);
            if (serverValueObj == null) continue;
            
            String serverValueOriginal = serverValueObj.toString();
            String serverValueNormalized = normalizeValue(serverValueOriginal);
            if (serverValueNormalized.isEmpty()) continue;
            
            boolean foundMatch = false;
            
            // Check against all rows in file2 (Change Related CIs)
            for (Map<String, Object> file2Row : file2.getRows()) {
                if (file2Row == null) continue;
                
                Object changeRelatedCIsObj = file2Row.get(column2);
                if (changeRelatedCIsObj == null) continue;
                
                String changeRelatedCIs = changeRelatedCIsObj.toString();
                Set<String> normalizedValues = splitAndNormalizeValues(changeRelatedCIs);
                
                // Strict exact match
                if (normalizedValues.contains(serverValueNormalized)) {
                    Map<String, Object> matchedRow = new LinkedHashMap<>();
                    
                    // First Column: The value being compared (Server value from Sheet1)
                    matchedRow.put("Compared Value (Server)", serverValueOriginal.trim());
                    
                    // Second Section: All column data from the matched row of Sheet1
                    matchedRow.put("Sheet1 Row Data", formatRowData(file1Row, file1.getHeaders()));
                    
                    // Third Section: All column data from the matched row of Sheet2
                    matchedRow.put("Sheet2 Row Data", formatRowData(file2Row, file2.getHeaders()));
                    
                    // Status Column
                    matchedRow.put("Status", "MATCHED");
                    
                    matchedRows.add(matchedRow);
                    foundMatch = true;
                    // Don't break - continue to find multiple matches in Sheet2
                }
            }
            
            // If no match found, add to mismatched
            if (!foundMatch) {
                Map<String, Object> mismatchedRow = new LinkedHashMap<>();
                
                // First Column: The value being compared (Server value from Sheet1)
                mismatchedRow.put("Compared Value (Server)", serverValueOriginal.trim());
                
                // Second Section: All column data from Sheet1 row
                mismatchedRow.put("Sheet1 Row Data", formatRowData(file1Row, file1.getHeaders()));
                
                // Third Section: No match found in Sheet2
                mismatchedRow.put("Sheet2 Row Data", "NOT FOUND");
                
                // Status Column
                mismatchedRow.put("Status", "NOT MATCHED");
                
                mismatchedRows.add(mismatchedRow);
            }
        }
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory after comparison: " + (afterMemory / 1024 / 1024) + "MB");
        
        return new ComparisonResult(matchedRows, mismatchedRows);
    }
    
    private Set<String> splitAndNormalizeValues(String commaSeparatedValue) {
        Set<String> normalizedValues = new HashSet<>();
        
        if (commaSeparatedValue == null || commaSeparatedValue.trim().isEmpty()) {
            return normalizedValues;
        }
        
        // Split by comma
        String[] values = commaSeparatedValue.split(",");
        
        for (String value : values) {
            String normalized = normalizeValue(value);
            if (!normalized.isEmpty()) {
                normalizedValues.add(normalized);
            }
        }
        
        return normalizedValues;
    }
    
    private String normalizeValue(String value) {
        if (value == null) return "";
        
        // Trim leading/trailing spaces
        String normalized = value.trim();
        
        // Remove trailing commas
        while (normalized.endsWith(",")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        
        // Convert to uppercase for case-insensitive comparison
        normalized = normalized.toUpperCase();
        
        return normalized;
    }
    
    private String formatRowData(Map<String, Object> row, List<String> headers) {
        if (row == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (String header : headers) {
            Object value = row.get(header);
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(header).append(": ").append(value != null ? value.toString() : "");
        }
        return sb.toString();
    }

    public static class ComparisonResult {
        private final List<Map<String, Object>> matchedRows;
        private final List<Map<String, Object>> mismatchedRows;

        public ComparisonResult(List<Map<String, Object>> matchedRows, List<Map<String, Object>> mismatchedRows) {
            this.matchedRows = matchedRows;
            this.mismatchedRows = mismatchedRows;
        }

        public List<Map<String, Object>> getMatchedRows() {
            return matchedRows;
        }

        public List<Map<String, Object>> getMismatchedRows() {
            return mismatchedRows;
        }
    }
}
