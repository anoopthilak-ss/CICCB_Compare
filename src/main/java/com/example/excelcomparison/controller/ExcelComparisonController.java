package com.example.excelcomparison.controller;

import com.example.excelcomparison.model.ComparisonRequest;
import com.example.excelcomparison.model.ExcelData;
import com.example.excelcomparison.service.ComparisonService;
import com.example.excelcomparison.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExcelComparisonController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ComparisonService comparisonService;

    private final Map<String, ExcelData> fileStorage = new ConcurrentHashMap<>();
    private final Map<String, byte[]> originalFileContents = new ConcurrentHashMap<>();
    private final Map<String, String> originalFileNames = new ConcurrentHashMap<>();

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
                response.put("success", false);
                response.put("message", "Please upload an Excel file (.xlsx or .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            // Get sheet names first
            List<String> sheetNames = excelService.getSheetNames(file);
            
            // Parse first sheet by default
            ExcelData excelData = excelService.parseExcelFile(file, 0);
            String fileId = UUID.randomUUID().toString();
            fileStorage.put(fileId, excelData);
            originalFileContents.put(fileId, file.getBytes());
            originalFileNames.put(fileId, file.getOriginalFilename());

            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileId", fileId);
            response.put("fileName", excelData.getFileName());
            response.put("headers", excelData.getHeaders());
            response.put("sheetNames", sheetNames);
            response.put("selectedSheet", excelData.getSelectedSheet());
            response.put("hasMultipleSheets", sheetNames.size() > 1);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/select-sheet")
    public ResponseEntity<Map<String, Object>> selectSheet(@RequestParam("fileId") String fileId, 
                                                          @RequestParam("sheetIndex") int sheetIndex) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("Sheet selection request: fileId=" + fileId + ", sheetIndex=" + sheetIndex);
        
        try {
            ExcelData currentData = fileStorage.get(fileId);
            if (currentData == null) {
                System.out.println("File not found: " + fileId);
                response.put("success", false);
                response.put("message", "File not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Get the original file data to re-parse with selected sheet
            byte[] originalFileContent = originalFileContents.get(fileId);
            if (originalFileContent == null) {
                System.out.println("Original file content not found: " + fileId);
                response.put("success", false);
                response.put("message", "Original file content not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> sheetNames = excelService.getSheetNames(originalFileContent);
            System.out.println("Available sheets: " + sheetNames);
            
            if (sheetIndex >= 0 && sheetIndex < sheetNames.size()) {
                // Re-parse the Excel file with the selected sheet index
                String originalFileName = originalFileNames.get(fileId);
                System.out.println("Re-parsing file: " + originalFileName + " with sheet index: " + sheetIndex);
                ExcelData newExcelData = excelService.parseExcelFile(originalFileContent, sheetIndex, originalFileName);
                
                System.out.println("Parsed new sheet: " + newExcelData.getSelectedSheet());
                System.out.println("New headers: " + newExcelData.getHeaders());
                
                // Update the stored data with the new sheet information
                currentData.setSheetNames(newExcelData.getSheetNames());
                currentData.setSelectedSheet(newExcelData.getSelectedSheet());
                currentData.setHeaders(newExcelData.getHeaders());
                currentData.setRows(newExcelData.getRows());
                
                response.put("success", true);
                response.put("message", "Sheet selected successfully");
                response.put("selectedSheet", newExcelData.getSelectedSheet());
                response.put("headers", newExcelData.getHeaders());
                
                System.out.println("Response prepared: " + response);
            } else {
                System.out.println("Invalid sheet index: " + sheetIndex + ", available: 0-" + (sheetNames.size() - 1));
                response.put("success", false);
                response.put("message", "Invalid sheet index");
            }
        } catch (Exception e) {
            System.out.println("Error in sheet selection: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error selecting sheet: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareFiles(@RequestBody ComparisonRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (request == null || !request.isValid()) {
                response.put("success", false);
                response.put("message", "Invalid comparison request. All fields are required.");
                return ResponseEntity.badRequest().body(response);
            }

            ExcelData file1 = fileStorage.get(request.getFile1Id());
            ExcelData file2 = fileStorage.get(request.getFile2Id());

            if (file1 == null || file2 == null) {
                response.put("success", false);
                response.put("message", "One or both files not found. Please upload files again.");
                return ResponseEntity.badRequest().body(response);
            }

            ComparisonService.ComparisonResult result = comparisonService.compareColumns(
                file1, file2, request.getColumn1(), request.getColumn2()
            );

            String resultId = UUID.randomUUID().toString();
            fileStorage.put(resultId + "_matched", new ExcelData("matched", file1.getHeaders(), result.getMatchedRows()));
            fileStorage.put(resultId + "_mismatched", new ExcelData("mismatched", file1.getHeaders(), result.getMismatchedRows()));

            response.put("success", true);
            response.put("message", "Comparison completed successfully");
            response.put("resultId", resultId);
            response.put("matchedCount", result.getMatchedRows().size());
            response.put("mismatchedCount", result.getMismatchedRows().size());
            
            // Include the actual result data for immediate display
            response.put("matchedRows", result.getMatchedRows());
            response.put("mismatchedRows", result.getMismatchedRows());
            
            System.out.println("Comparison response: " + response);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during comparison: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download/{resultId}/{type}")
    public ResponseEntity<byte[]> downloadResult(@PathVariable String resultId, @PathVariable String type) {
        try {
            String key = resultId + "_" + type;
            ExcelData resultData = fileStorage.get(key);

            if (resultData == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] excelBytes = excelService.createComparisonResult(
                type.equals("matched") ? resultData.getRows() : new ArrayList<>(),
                type.equals("mismatched") ? resultData.getRows() : new ArrayList<>()
            );

            String filename = type.equals("matched") ? "matched_rows.xlsx" : "mismatched_rows.xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        ExcelData excelData = fileStorage.get(fileId);

        if (excelData == null) {
            response.put("success", false);
            response.put("message", "File not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("success", true);
        response.put("fileName", excelData.getFileName());
        response.put("headers", excelData.getHeaders());
        response.put("rows", excelData.getRows());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-results")
    public ResponseEntity<Map<String, Object>> getComparisonResults(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String resultId = request.get("resultId");
            String type = request.get("type");
            
            if (resultId == null || type == null) {
                response.put("success", false);
                response.put("message", "Result ID and type are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String key = resultId + "_" + type;
            ExcelData resultData = fileStorage.get(key);

            if (resultData == null) {
                response.put("success", false);
                response.put("message", "Results not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("rows", resultData.getRows());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving results: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
