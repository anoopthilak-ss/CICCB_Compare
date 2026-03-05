package com.example.excelcomparison.service;

import com.example.excelcomparison.model.ExcelData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ComparisonServiceTest {

    @Test
    public void testStrictCommaSeparatedComparison() {
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
        
        // Verify results
        List<Map<String, Object>> matchedRows = result.getMatchedRows();
        List<Map<String, Object>> mismatchedRows = result.getMismatchedRows();
        
        System.out.println("=== MATCHED ROWS ===");
        for (Map<String, Object> row : matchedRows) {
            System.out.println(row.get("Change Number") + " | " + row.get("Server Name") + " | " + row.get("Status"));
        }
        
        System.out.println("\n=== MISMATCHED ROWS ===");
        for (Map<String, Object> row : mismatchedRows) {
            System.out.println(row.get("Change Number") + " | " + row.get("Server Name") + " | " + row.get("Status"));
        }
        
        // Assertions
        assertEquals(3, matchedRows.size(), "Should have 3 matched rows");
        assertEquals(1, mismatchedRows.size(), "Should have 1 mismatched row");
        
        // Check specific matches
        boolean foundMCE = false;
        boolean foundRDL = false;
        boolean foundWSPR = false;
        
        for (Map<String, Object> row : matchedRows) {
            String serverName = (String) row.get("Server Name");
            String status = (String) row.get("Status");
            
            if ("MANUAL CLAIM ENTRY (MCE)".equals(serverName) && "MATCHED".equals(status)) {
                foundMCE = true;
                assertEquals("CHG07411848", row.get("Change Number"));
            }
            if ("REINSURANCE DOCUMENT LIBRARY (RDL)".equals(serverName) && "MATCHED".equals(status)) {
                foundRDL = true;
                assertEquals("CHG07411848", row.get("Change Number"));
            }
            if ("WSPR (WU)".equals(serverName) && "MATCHED".equals(status)) {
                foundWSPR = true;
                assertEquals("CHG07300134", row.get("Change Number"));
            }
        }
        
        assertTrue(foundMCE, "Should find MANUAL CLAIM ENTRY (MCE) match");
        assertTrue(foundRDL, "Should find REINSURANCE DOCUMENT LIBRARY (RDL) match");
        assertTrue(foundWSPR, "Should find WSPR (WU) match");
        
        // Check mismatched row
        Map<String, Object> mismatchedRow = mismatchedRows.get(0);
        assertEquals("NonExistent Server", mismatchedRow.get("Server Name"));
        assertEquals("NOT MATCHED", mismatchedRow.get("Status"));
        assertEquals("", mismatchedRow.get("Change Number"));
    }
    
    @Test
    public void testValueNormalization() {
        ComparisonService service = new ComparisonService();
        
        // Test normalizeValue method through the comparison
        List<Map<String, Object>> sheet1Rows = new ArrayList<>();
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("Change Number", "CHG001");
        row1.put("Change Related CIs", "  Server One  ,  Server Two, , SERVER THREE  , ");
        sheet1Rows.add(row1);
        
        ExcelData sheet1 = new ExcelData("sheet1.xlsx", 
            Arrays.asList("Change Number", "Change Related CIs"), 
            sheet1Rows, 
            Arrays.asList("Sheet1"), 
            "Sheet1");
        
        List<Map<String, Object>> sheet2Rows = new ArrayList<>();
        Map<String, Object> serverRow1 = new LinkedHashMap<>();
        serverRow1.put("Server", "server one");
        sheet2Rows.add(serverRow1);
        
        Map<String, Object> serverRow2 = new LinkedHashMap<>();
        serverRow2.put("Server", "SERVER THREE");
        sheet2Rows.add(serverRow2);
        
        ExcelData sheet2 = new ExcelData("sheet2.xlsx", 
            Arrays.asList("Server"), 
            sheet2Rows, 
            Arrays.asList("Sheet2"), 
            "Sheet2");
        
        ComparisonService.ComparisonResult result = service.compareColumns(
            sheet1, sheet2, "Change Related CIs", "Server");
        
        // Should find both matches despite case differences and extra spaces
        assertEquals(2, result.getMatchedRows().size());
    }
}
