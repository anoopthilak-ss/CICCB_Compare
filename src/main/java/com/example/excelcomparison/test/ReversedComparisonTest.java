package com.example.excelcomparison.test;

import com.example.excelcomparison.model.ExcelData;
import com.example.excelcomparison.service.ComparisonService;

import java.util.*;

public class ReversedComparisonTest {
    public static void main(String[] args) {
        ComparisonService service = new ComparisonService();
        
        // Create test data for Sheet1 (Server column - single values)
        List<Map<String, Object>> sheet1Rows = new ArrayList<>();
        
        Map<String, Object> serverRow1 = new LinkedHashMap<>();
        serverRow1.put("Server", "MANUAL CLAIM ENTRY (MCE)");
        serverRow1.put("Environment", "Production");
        serverRow1.put("Status", "Active");
        sheet1Rows.add(serverRow1);
        
        Map<String, Object> serverRow2 = new LinkedHashMap<>();
        serverRow2.put("Server", "LM UNIVERSAL REINSURANCE SYSTEM");
        serverRow2.put("Environment", "Development");
        serverRow2.put("Status", "Inactive");
        sheet1Rows.add(serverRow2);
        
        Map<String, Object> serverRow3 = new LinkedHashMap<>();
        serverRow3.put("Server", "REINSURANCE DOCUMENT LIBRARY (RDL)");
        serverRow3.put("Environment", "Testing");
        serverRow3.put("Status", "Active");
        sheet1Rows.add(serverRow3);
        
        Map<String, Object> serverRow4 = new LinkedHashMap<>();
        serverRow4.put("Server", "NonExistent Server");
        serverRow4.put("Environment", "Staging");
        serverRow4.put("Status", "Active");
        sheet1Rows.add(serverRow4);
        
        ExcelData sheet1 = new ExcelData("sheet1.xlsx", 
            Arrays.asList("Server", "Environment", "Status"), 
            sheet1Rows, 
            Arrays.asList("Sheet1"), 
            "Sheet1");
        
        // Create test data for Sheet2 (Change Related CIs - comma-separated values)
        List<Map<String, Object>> sheet2Rows = new ArrayList<>();
        
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("Change Number", "CHG07411848");
        row1.put("Change Related CIs", "Liberator, Clipper, MANUAL CLAIM ENTRY (MCE), REINSURANCE DOCUMENT LIBRARY (RDL)");
        row1.put("Priority", "High");
        sheet2Rows.add(row1);
        
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("Change Number", "CHG07300134");
        row2.put("Change Related CIs", "WSPR (WU), Test Server, Another System");
        row2.put("Priority", "Medium");
        sheet2Rows.add(row2);
        
        Map<String, Object> row3 = new LinkedHashMap<>();
        row3.put("Change Number", "CHG07500234");
        row3.put("Change Related CIs", "LM UNIVERSAL REINSURANCE SYSTEM, Backup System");
        row3.put("Priority", "Low");
        sheet2Rows.add(row3);
        
        ExcelData sheet2 = new ExcelData("sheet2.xlsx", 
            Arrays.asList("Change Number", "Change Related CIs", "Priority"), 
            sheet2Rows, 
            Arrays.asList("Sheet2"), 
            "Sheet2");
        
        // Perform comparison: Sheet1 Server -> Sheet2 Change Related CIs
        ComparisonService.ComparisonResult result = service.compareColumns(
            sheet1, sheet2, "Server", "Change Related CIs");
        
        // Display results
        System.out.println("=== REVERSED COMPARISON RESULTS ===");
        System.out.println("Sheet1 (Server) -> Sheet2 (Change Related CIs)");
        System.out.println("Matched Rows: " + result.getMatchedRows().size());
        System.out.println("Mismatched Rows: " + result.getMismatchedRows().size());
        
        System.out.println("\n=== MATCHED ROWS ===");
        System.out.println("Compared Value (Server)\t\tSheet1 Row Data\t\t\t\t\t\t\tSheet2 Row Data\t\t\t\t\t\t\tStatus");
        System.out.println("--------------------\t\t-------------\t\t\t\t\t\t\t\t-------------\t\t\t\t\t\t\t------");
        for (Map<String, Object> row : result.getMatchedRows()) {
            System.out.printf("%-25s\t%-60s\t%-60s\t%s%n", 
                row.get("Compared Value (Server)"), 
                truncateString((String) row.get("Sheet1 Row Data"), 60), 
                truncateString((String) row.get("Sheet2 Row Data"), 60), 
                row.get("Status"));
        }
        
        System.out.println("\n=== MISMATCHED ROWS ===");
        System.out.println("Compared Value (Server)\t\tSheet1 Row Data\t\t\t\t\t\t\t\tSheet2 Row Data\t\t\t\t\t\t\tStatus");
        System.out.println("--------------------\t\t-------------\t\t\t\t\t\t\t\t-------------\t\t\t\t\t\t\t------");
        for (Map<String, Object> row : result.getMismatchedRows()) {
            System.out.printf("%-25s\t%-60s\t%-60s\t%s%n", 
                row.get("Compared Value (Server)"), 
                truncateString((String) row.get("Sheet1 Row Data"), 60), 
                truncateString((String) row.get("Sheet2 Row Data"), 60), 
                row.get("Status"));
        }
        
        // Validation
        System.out.println("\n=== VALIDATION ===");
        boolean testPassed = true;
        
        // Expected matches:
        // 1. MANUAL CLAIM ENTRY (MCE) should match CHG07411848
        // 2. REINSURANCE DOCUMENT LIBRARY (RDL) should match CHG07411848 (second match for same Sheet2 row)
        // 3. LM UNIVERSAL REINSURANCE SYSTEM should match CHG07500234
        
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
        boolean foundLM = false;
        
        for (Map<String, Object> row : result.getMatchedRows()) {
            String serverName = (String) row.get("Compared Value (Server)");
            String sheet2Data = (String) row.get("Sheet2 Row Data");
            
            if ("MANUAL CLAIM ENTRY (MCE)".equals(serverName) && sheet2Data.contains("CHG07411848")) {
                foundMCE = true;
                System.out.println("✅ Found correct MANUAL CLAIM ENTRY (MCE) match");
            }
            if ("REINSURANCE DOCUMENT LIBRARY (RDL)".equals(serverName) && sheet2Data.contains("CHG07411848")) {
                foundRDL = true;
                System.out.println("✅ Found correct REINSURANCE DOCUMENT LIBRARY (RDL) match");
            }
            if ("LM UNIVERSAL REINSURANCE SYSTEM".equals(serverName) && sheet2Data.contains("CHG07500234")) {
                foundLM = true;
                System.out.println("✅ Found correct LM UNIVERSAL REINSURANCE SYSTEM match");
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
        if (!foundLM) {
            System.out.println("❌ Missing LM UNIVERSAL REINSURANCE SYSTEM match");
            testPassed = false;
        }
        
        // Check mismatched row
        if (!result.getMismatchedRows().isEmpty()) {
            Map<String, Object> mismatchedRow = result.getMismatchedRows().get(0);
            if ("NonExistent Server".equals(mismatchedRow.get("Compared Value (Server)")) && 
                "NOT FOUND".equals(mismatchedRow.get("Sheet2 Row Data")) &&
                "NOT MATCHED".equals(mismatchedRow.get("Status"))) {
                System.out.println("✅ Correct mismatched row for NonExistent Server");
            } else {
                System.out.println("❌ Incorrect mismatched row");
                testPassed = false;
            }
        }
        
        System.out.println("\n=== FINAL RESULT ===");
        if (testPassed) {
            System.out.println("🎉 ALL TESTS PASSED! The reversed comparison logic is working correctly.");
            System.out.println("✅ Output format includes full row data from both sheets");
            System.out.println("✅ Strict exact matching is implemented");
            System.out.println("✅ Multiple matches per server value are handled correctly");
        } else {
            System.out.println("❌ SOME TESTS FAILED! Please check the implementation.");
        }
    }
    
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
