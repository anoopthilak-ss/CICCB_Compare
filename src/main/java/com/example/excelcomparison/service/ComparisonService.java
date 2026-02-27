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
        
        // Check if columns contain comma-separated values
        boolean hasCommaValues1 = hasCommaSeparatedValues(file1, column1);
        boolean hasCommaValues2 = hasCommaSeparatedValues(file2, column2);
        
        // Debug logging
        System.out.println("Column1: " + column1 + " from " + file1.getSelectedSheet() + ", hasCommaValues: " + hasCommaValues1);
        System.out.println("Column2: " + column2 + " from " + file2.getSelectedSheet() + ", hasCommaValues: " + hasCommaValues2);
        System.out.println("File1 rows: " + (file1.getRows() != null ? file1.getRows().size() : 0));
        System.out.println("File2 rows: " + (file2.getRows() != null ? file2.getRows().size() : 0));
        System.out.println("Cross-sheet comparison: " + file1.getSelectedSheet() + " -> " + file2.getSelectedSheet());
        
        // If either column has comma-separated values, use full row comparison
        if (hasCommaValues1 || hasCommaValues2) {
            System.out.println("Using full row comparison");
            ComparisonResult result = compareWithFullRowData(file1, file2, column1, column2);
            System.out.println("Full row comparison - Matched: " + result.getMatchedRows().size() + ", Mismatched: " + result.getMismatchedRows().size());
            return result;
        }
        
        // Otherwise, use cross-sheet value comparison
        System.out.println("Using cross-sheet value comparison");
        ComparisonResult result = compareWithCrossSheetValues(file1, file2, column1, column2);
        System.out.println("Cross-sheet comparison - Matched: " + result.getMatchedRows().size() + ", Mismatched: " + result.getMismatchedRows().size());
        return result;
    }
    
    private boolean hasCommaSeparatedValues(ExcelData data, String columnName) {
        if (data == null || columnName == null || !data.getHeaders().contains(columnName)) {
            return false;
        }
        
        for (Map<String, Object> row : data.getRows()) {
            if (row != null) {
                Object value = row.get(columnName);
                if (value != null && value.toString().contains(",")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private ComparisonResult compareWithCrossSheetValues(ExcelData file1, ExcelData file2, String column1, String column2) {
        // Aggressive memory monitoring for IDE environments
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        System.out.println("=== Memory Status ===");
        System.out.println("Max JVM Memory: " + (maxMemory / 1024 / 1024) + "MB");
        System.out.println("Currently Used: " + (usedMemory / 1024 / 1024) + "MB (" + String.format("%.1f", memoryUsagePercent) + "%)");
        
        // Critical memory check - fail fast if insufficient memory
        long availableMemory = maxMemory - usedMemory;
        long requiredMemory = (file1.getRows().size() + file2.getRows().size()) * 1024; // Estimate 1KB per row
        
        if (availableMemory < requiredMemory) {
            System.out.println("CRITICAL: Insufficient memory for comparison!");
            System.out.println("Available: " + (availableMemory / 1024 / 1024) + "MB");
            System.out.println("Required: " + (requiredMemory / 1024 / 1024) + "MB");
            System.out.println("SOLUTIONS:");
            System.out.println("1. Close other applications");
            System.out.println("2. Use start-dynamic-memory.sh script");
            System.out.println("3. Increase IDE JVM heap (-Xmx)");
            throw new RuntimeException("Insufficient memory for Excel comparison. Please increase JVM heap size.");
        }
        
        // Warning if memory usage is high
        if (memoryUsagePercent > 70) {
            System.out.println("WARNING: High memory usage detected! Consider:");
            System.out.println("1. Using smaller Excel files");
            System.out.println("2. Increasing JVM max heap (-Xmx)");
            System.out.println("3. Closing other applications");
            System.gc(); // Force garbage collection
        }
        
        // Use ArrayList with initial capacity to reduce memory reallocation
        List<Map<String, Object>> matchedRows = new ArrayList<>(1000);
        List<Map<String, Object>> mismatchedRows = new ArrayList<>(1000);
        
        // Get only values from selected columns
        Set<String> file1Values = getColumnValues(file1, column1);
        Set<String> file2Values = getColumnValues(file2, column2);
        
        // Check if columns exist in files
        if (file1Values.isEmpty() && !file1.getHeaders().contains(column1)) {
            throw new IllegalArgumentException("Column '" + column1 + "' not found in first file");
        }
        if (file2Values.isEmpty() && !file2.getHeaders().contains(column2)) {
            throw new IllegalArgumentException("Column '" + column2 + "' not found in second file");
        }
        
        System.out.println("File1 values count: " + file1Values.size());
        System.out.println("File2 values count: " + file2Values.size());
        
        // Find common values (matched)
        Set<String> commonValues = new HashSet<>(file1Values);
        commonValues.retainAll(file2Values);
        
        // Find values only in file1 or only in file2 (mismatched)
        Set<String> onlyInFile1 = new HashSet<>(file1Values);
        onlyInFile1.removeAll(file2Values);
        
        Set<String> onlyInFile2 = new HashSet<>(file2Values);
        onlyInFile2.removeAll(file1Values);
        
        // Process in batches to avoid memory issues
        processBatchedResults(commonValues, matchedRows, column1, column2, file1, file2, "MATCHED");
        processBatchedResults(onlyInFile1, mismatchedRows, column1, column2, file1, file2, "ONLY IN " + file1.getSelectedSheet().toUpperCase());
        processBatchedResults(onlyInFile2, mismatchedRows, column1, column2, file1, file2, "ONLY IN " + file2.getSelectedSheet().toUpperCase());
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory after comparison: " + (afterMemory / 1024 / 1024) + "MB");
        System.out.println("Memory used: " + ((afterMemory - usedMemory) / 1024 / 1024) + "MB");
        
        return new ComparisonResult(matchedRows, mismatchedRows);
    }
    
    private void processBatchedResults(Set<String> values, List<Map<String, Object>> results, 
                                    String column1, String column2, ExcelData file1, ExcelData file2, String status) {
        final int BATCH_SIZE = 1000;
        int processed = 0;
        
        for (String value : values) {
            Map<String, Object> row = new LinkedHashMap<>(4); // Initial capacity to reduce resizing
            if ("MATCHED".equals(status)) {
                row.put(column1 + " (" + file1.getSelectedSheet() + ")", value);
                row.put(column2 + " (" + file2.getSelectedSheet() + ")", value);
            } else if (status.contains(file1.getSelectedSheet().toUpperCase())) {
                row.put(column1 + " (" + file1.getSelectedSheet() + ")", value);
                row.put(column2 + " (" + file2.getSelectedSheet() + ")", "NOT FOUND");
            } else {
                row.put(column1 + " (" + file1.getSelectedSheet() + ")", "NOT FOUND");
                row.put(column2 + " (" + file2.getSelectedSheet() + ")", value);
            }
            row.put("Status", status);
            results.add(row);
            
            // Periodically trigger garbage collection for large datasets
            if (++processed % BATCH_SIZE == 0 && processed > 0) {
                System.gc(); // Suggest garbage collection
                System.out.println("Processed " + processed + " records...");
            }
        }
    }
    
    private ComparisonResult compareWithFullRowData(ExcelData file1, ExcelData file2, String column1, String column2) {
        List<Map<String, Object>> matchedRows = new ArrayList<>();
        List<Map<String, Object>> mismatchedRows = new ArrayList<>();
        
        Map<String, Map<String, Object>> file1Map = createExpandedColumnValueMap(file1, column1);
        Map<String, Map<String, Object>> file2Map = createExpandedColumnValueMap(file2, column2);
        
        // Check if columns exist in files
        if (file1Map.isEmpty() && !file1.getHeaders().contains(column1)) {
            throw new IllegalArgumentException("Column '" + column1 + "' not found in first file");
        }
        if (file2Map.isEmpty() && !file2.getHeaders().contains(column2)) {
            throw new IllegalArgumentException("Column '" + column2 + "' not found in second file");
        }
        
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(file1Map.keySet());
        allKeys.addAll(file2Map.keySet());
        
        for (String key : allKeys) {
            Map<String, Object> row1 = file1Map.get(key);
            Map<String, Object> row2 = file2Map.get(key);
            
            if (row1 != null && row2 != null) {
                Map<String, Object> matchedRow = new LinkedHashMap<>();
                matchedRow.put("Comparison_Value", key);
                matchedRow.put("File1_Row_Data", formatRowData(row1));
                matchedRow.put("File2_Row_Data", formatRowData(row2));
                matchedRow.put("Status", "MATCHED");
                matchedRows.add(matchedRow);
            } else {
                Map<String, Object> mismatchedRow = new LinkedHashMap<>();
                mismatchedRow.put("Comparison_Value", key);
                mismatchedRow.put("File1_Row_Data", row1 != null ? formatRowData(row1) : "NOT FOUND");
                mismatchedRow.put("File2_Row_Data", row2 != null ? formatRowData(row2) : "NOT FOUND");
                mismatchedRow.put("Status", "MISMATCHED");
                mismatchedRows.add(mismatchedRow);
            }
        }
        
        return new ComparisonResult(matchedRows, mismatchedRows);
    }
    
    private Map<String, Map<String, Object>> createExpandedColumnValueMap(ExcelData data, String columnName) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        
        if (data == null || columnName == null || columnName.trim().isEmpty()) {
            return resultMap;
        }
        
        if (data.getHeaders() == null || !data.getHeaders().contains(columnName)) {
            return resultMap;
        }
        
        if (data.getRows() == null) {
            return resultMap;
        }
        
        for (Map<String, Object> row : data.getRows()) {
            if (row != null) {
                Object value = row.get(columnName);
                if (value != null) {
                    String[] values = value.toString().split(",");
                    for (String val : values) {
                        String key = val.trim();
                        if (!key.isEmpty()) {
                            resultMap.put(key, row);
                        }
                    }
                }
            }
        }
        
        return resultMap;
    }
    
    private String formatRowData(Map<String, Object> row) {
        if (row == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return sb.toString();
    }

    private Set<String> getColumnValues(ExcelData data, String columnName) {
        Set<String> values = new HashSet<>();
        
        if (data == null || columnName == null || columnName.trim().isEmpty()) {
            return values;
        }
        
        if (data.getHeaders() == null || !data.getHeaders().contains(columnName)) {
            return values;
        }
        
        if (data.getRows() == null) {
            return values;
        }
        
        for (Map<String, Object> row : data.getRows()) {
            if (row != null) {
                Object value = row.get(columnName);
                if (value != null) {
                    String stringValue = value.toString().trim();
                    if (!stringValue.isEmpty()) {
                        values.add(stringValue);
                    }
                }
            }
        }
        
        return values;
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
