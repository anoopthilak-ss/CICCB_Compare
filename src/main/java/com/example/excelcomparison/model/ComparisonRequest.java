package com.example.excelcomparison.model;

public class ComparisonRequest {
    private String file1Id;
    private String file2Id;
    private String column1;
    private String column2;

    public ComparisonRequest() {}

    public ComparisonRequest(String file1Id, String file2Id, String column1, String column2) {
        this.file1Id = file1Id;
        this.file2Id = file2Id;
        this.column1 = column1;
        this.column2 = column2;
    }

    public String getFile1Id() {
        return file1Id;
    }

    public void setFile1Id(String file1Id) {
        this.file1Id = file1Id;
    }

    public String getFile2Id() {
        return file2Id;
    }

    public void setFile2Id(String file2Id) {
        this.file2Id = file2Id;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public boolean isValid() {
        return file1Id != null && !file1Id.trim().isEmpty() &&
               file2Id != null && !file2Id.trim().isEmpty() &&
               column1 != null && !column1.trim().isEmpty() &&
               column2 != null && !column2.trim().isEmpty();
    }
}
