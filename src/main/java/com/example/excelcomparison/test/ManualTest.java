package com.example.excelcomparison.test;

import com.example.excelcomparison.model.ExcelData;
import com.example.excelcomparison.service.ComparisonService;

import java.util.*;

public class ManualTest {
    public static void main(String[] args) {
        ComparisonService service = new ComparisonService();
        
        // Create test data for Sheet1 (Change Related CIs)
        List<Map<String, Object>> sheet1Rows = new ArrayList<>();
        
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("Change Number", "CHG07411848");
        row1.put("Change Related CIs", "Liberator, Clipper, MANUAL CLAIM ENTRY (MCE), REINSURANCE DOCUMENT LIBRARY (RDL)");
        sheet1Rows.add(row1);
        
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("Change Number", "CHG07300134");
        row2.put("Change Related CIs", "WSPR (WU), Test Server");
        sheet1Rows.add(row2);
        
        ExcelData sheet1 = new ExcelData("sheet1.xlsx", 
            Arrays.asList("Change Number", "Change Related CIs"), 
            sheet1Rows, 
            Arrays.asList("Sheet1"), 
            "Sheet1");
        
        // Create test data for Sheet2 (Server)
        List<Map<String, Object>> sheet2Rows = new ArrayList<>();
        
        Map<String, Object> serverRow1 = new LinkedHashMap<>();
        serverRow1.put("Server", "MANUAL CLAIM ENTRY (MCE)");
        sheet2Rows.add(serverRow1);
        
        Map<String, Object> serverRow2 = new LinkedHashMap<>();
        serverRow2.put("Server", "REINSURANCE DOCUMENT LIBRARY (RDL)");
        sheet2Rows.add(serverRow2);
        
        Map<String, Object> serverRow3 = new LinkedHashMap<>();
        serverRow3.put("Server", "WSPR (WU)");
        sheet2Rows.add(serverRow3);
        
        Map<String, Object> serverRow4 = new LinkedHashMap<>();
        serverRow4.put("Server", "NonExistent Server");
        sheet2Rows.add(serverRow4);
        
        ExcelData sheet2 = new ExcelData("sheet2.xlsx", 
            Arrays.asList("Server"), 
            sheet2Rows, 
            Arrays.asList("Sheet2"), 
            "Sheet2");
        
        // Perform comparison
        ComparisonService.ComparisonResult result = service.compareColumns(
            sheet1, sheet2, "Change Related CIs", "Server");
        
        // Display results
        System.out.println("=== COMPARISON RESULTS ===");
        System.out.println("Matched Rows: " + result.getMatchedRows().size());
        System.out.println("Mismatched Rows: " + result.getMismatchedRows().size());
        
        System.out.println("\n=== MATCHED ROWS ===");
        System.out.println("Change Number\t\tServer Name\t\t\t\tStatus");
        System.out.println("------------\t\t----------\t\t\t\t------");
        for (Map<String, Object> row : result.getMatchedRows()) {
            System.out.printf("%-15s\t%-35s\t%s%n", 
                row.get("Change Number"), 
                row.get("Server Name"), 
                row.get("Status"));
        }
        
        System.out.println("\n=== MISMATCHED ROWS ===");
        System.out.println("Change Number\t\tServer Name\t\t\t\tStatus");
        System.out.println("------------\t\t----------\t\t\t\t------");
        for (Map<String, Object> row : result.getMismatchedRows()) {
            System.out.printf("%-15s\t%-35s\t%s%n", 
                row.get("Change Number"), 
                row.get("Server Name"), 
                row.get("Status"));
        }
        
        // Validation
        System.out.println("\n=== VALIDATION ===");
        boolean testPassed = true;
        
        if (result.getMatchedRows().size() != 3) {
            System.out.println("❌ Expected 3 matched rows, got " + result.getMatchedRows().size());
            testPassed = false;
        } else {
            System.out.println("✅ Correct number of matched rows: 3");
        }
        
        if (result.getMismatchedRows().size() != 1) {
            System.out.println("❌ Expected 1 mismatched row, got " + result.getMismatchedRows().size());
            testPassed = false;
        } else {
            System.out.println("✅ Correct number of mismatched rows: 1");
        }
        
        // Check specific matches
        boolean foundMCE = false;
        boolean foundRDL = false;
        boolean foundWSPR = false;
        
        for (Map<String, Object> row : result.getMatchedRows()) {
            String serverName = (String) row.get("Server Name");
            String changeNumber = (String) row.get("Change Number");
            
            if ("MANUAL CLAIM ENTRY (MCE)".equals(serverName) && "CHG07411848".equals(changeNumber)) {
                foundMCE = true;
                System.out.println("✅ Found correct MANUAL CLAIM ENTRY (MCE) match");
            }
            if ("REINSURANCE DOCUMENT LIBRARY (RDL)".equals(serverName) && "CHG07411848".equals(changeNumber)) {
                foundRDL = true;
                System.out.println("✅ Found correct REINSURANCE DOCUMENT LIBRARY (RDL) match");
            }
            if ("WSPR (WU)".equals(serverName) && "CHG07300134".equals(changeNumber)) {
                foundWSPR = true;
                System.out.println("✅ Found correct WSPR (WU) match");
            }
        }
        
        if (!foundMCE) {
            System.out.println("❌ Missing MANUAL CLAIM ENTRY (MCE) match");
            testPassed = false;
        }
        if (!foundRDL) {
            System.out.println("❌ Missing REINSURANCE DOCUMENT LIBRARY (RDL) match");
            testPassed = false;
        }
        if (!foundWSPR) {
            System.out.println("❌ Missing WSPR (WU) match");
            testPassed = false;
        }
        
        // Check mismatched row
        if (!result.getMismatchedRows().isEmpty()) {
            Map<String, Object> mismatchedRow = result.getMismatchedRows().get(0);
            if ("NonExistent Server".equals(mismatchedRow.get("Server Name")) && 
                "NOT MATCHED".equals(mismatchedRow.get("Status")) &&
                "".equals(mismatchedRow.get("Change Number"))) {
                System.out.println("✅ Correct mismatched row for NonExistent Server");
            } else {
                System.out.println("❌ Incorrect mismatched row");
                testPassed = false;
            }
        }
        
        System.out.println("\n=== FINAL RESULT ===");
        if (testPassed) {
            System.out.println("🎉 ALL TESTS PASSED! The strict comparison logic is working correctly.");
        } else {
            System.out.println("❌ SOME TESTS FAILED! Please check the implementation.");
        }
    }
}
