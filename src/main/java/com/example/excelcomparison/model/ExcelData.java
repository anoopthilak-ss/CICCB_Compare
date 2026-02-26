package com.example.excelcomparison.model;

import java.util.List;
import java.util.Map;

public class ExcelData {
    private String fileName;
    private List<String> headers;
    private List<Map<String, Object>> rows;
    private List<String> sheetNames;
    private String selectedSheet;

    public ExcelData() {}

    public ExcelData(String fileName, List<String> headers, List<Map<String, Object>> rows) {
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
    }

    public ExcelData(String fileName, List<String> headers, List<Map<String, Object>> rows, 
                   List<String> sheetNames, String selectedSheet) {
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
        this.sheetNames = sheetNames;
        this.selectedSheet = selectedSheet;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public String getSelectedSheet() {
        return selectedSheet;
    }

    public void setSelectedSheet(String selectedSheet) {
        this.selectedSheet = selectedSheet;
    }
}
