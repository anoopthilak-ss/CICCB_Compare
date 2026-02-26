package com.example.excelcomparison.service;

import com.example.excelcomparison.model.ExcelData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonService {

    public ComparisonResult compareColumns(ExcelData file1, ExcelData file2, String column1, String column2) {
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
        List<Map<String, Object>> matchedRows = new ArrayList<>();
        List<Map<String, Object>> mismatchedRows = new ArrayList<>();
        
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
        
        // Find common values (matched)
        Set<String> commonValues = new HashSet<>(file1Values);
        commonValues.retainAll(file2Values);
        
        // Find values only in file1 or only in file2 (mismatched)
        Set<String> onlyInFile1 = new HashSet<>(file1Values);
        onlyInFile1.removeAll(file2Values);
        
        Set<String> onlyInFile2 = new HashSet<>(file2Values);
        onlyInFile2.removeAll(file1Values);
        
        // Create matched rows with cross-sheet information
        for (String value : commonValues) {
            Map<String, Object> matchedRow = new LinkedHashMap<>();
            matchedRow.put(column1 + " (" + file1.getSelectedSheet() + ")", value);
            matchedRow.put(column2 + " (" + file2.getSelectedSheet() + ")", value);
            matchedRow.put("Status", "MATCHED");
            matchedRows.add(matchedRow);
        }
        
        // Create mismatched rows (values only in file1)
        for (String value : onlyInFile1) {
            Map<String, Object> mismatchedRow = new LinkedHashMap<>();
            mismatchedRow.put(column1 + " (" + file1.getSelectedSheet() + ")", value);
            mismatchedRow.put(column2 + " (" + file2.getSelectedSheet() + ")", "NOT FOUND");
            mismatchedRow.put("Status", "ONLY IN " + file1.getSelectedSheet().toUpperCase());
            mismatchedRows.add(mismatchedRow);
        }
        
        // Create mismatched rows (values only in file2)
        for (String value : onlyInFile2) {
            Map<String, Object> mismatchedRow = new LinkedHashMap<>();
            mismatchedRow.put(column1 + " (" + file1.getSelectedSheet() + ")", "NOT FOUND");
            mismatchedRow.put(column2 + " (" + file2.getSelectedSheet() + ")", value);
            mismatchedRow.put("Status", "ONLY IN " + file2.getSelectedSheet().toUpperCase());
            mismatchedRows.add(mismatchedRow);
        }
        
        return new ComparisonResult(matchedRows, mismatchedRows);
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
