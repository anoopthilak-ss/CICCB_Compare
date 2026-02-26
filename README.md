# Excel Comparison Tool

A web-based Excel comparison tool built with Java Spring Boot that allows users to upload two Excel files, select columns to compare, and download the comparison results.

## Features

- **File Upload**: Upload two Excel files (.xlsx, .xls) through a modern web interface
- **Column Selection**: Dynamically populate dropdown lists with column headers from uploaded files
- **Comparison Logic**: Compare selected columns between two Excel files
- **Result Download**: Download matched and mismatched rows as separate Excel files
- **Responsive Design**: Mobile-friendly interface with modern UI/UX
- **Error Handling**: Comprehensive error handling and validation

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Apache POI 5.2.4** (Excel processing)
- **Maven** (Build tool)

### Frontend
- **HTML5**
- **CSS3** (Modern responsive design)
- **JavaScript** (ES6+)

## Project Structure

```
excel-comparison-tool/
├── src/
│   ├── main/
│   │   ├── java/com/example/excelcomparison/
│   │   │   ├── controller/
│   │   │   │   └── ExcelComparisonController.java
│   │   │   ├── service/
│   │   │   │   ├── ExcelService.java
│   │   │   │   └── ComparisonService.java
│   │   │   ├── model/
│   │   │   │   ├── ExcelData.java
│   │   │   │   └── ComparisonRequest.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── ExcelComparisonApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── styles.css
│   │       │   └── script.js
│   │       └── application.properties
├── pom.xml
└── README.md
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Modern web browser** (Chrome, Firefox, Safari, Edge)

## Setup and Installation

### 1. Clone or Download the Project

```bash
# If using git
git clone <repository-url>
cd excel-comparison-tool

# Or download and extract the ZIP file
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Alternative: Run as JAR

```bash
java -jar target/excel-comparison-tool-1.0.0.jar
```

## Usage

### 1. Access the Application

Open your web browser and navigate to `http://localhost:8080`

### 2. Upload Excel Files

1. Click on "Choose First Excel File" and select your first Excel file
2. Click on "Choose Second Excel File" and select your second Excel file
3. Supported formats: `.xlsx` and `.xls`
4. Maximum file size: 10MB

### 3. Select Columns to Compare

1. After both files are uploaded, column selection dropdowns will appear
2. Select a column from the first file dropdown
3. Select a column from the second file dropdown
4. The "Compare Files" button will become enabled

### 4. Compare Files

1. Click the "Compare Files" button
2. Wait for the comparison to complete
3. Results will show the count of matched and mismatched rows

### 5. Download Results

1. **Matched Rows**: Click "Download Matched" to download an Excel file containing all matched rows
2. **Mismatched Rows**: Click "Download Mismatched" to download an Excel file containing all mismatched rows

## API Endpoints

### Upload Excel File
- **POST** `/api/upload`
- **Content-Type**: `multipart/form-data`
- **Parameters**: `file` (MultipartFile)
- **Response**: JSON with file ID, name, and headers

### Compare Files
- **POST** `/api/compare`
- **Content-Type**: `application/json`
- **Body**: 
  ```json
  {
    "file1Id": "uuid",
    "file2Id": "uuid", 
    "column1": "Column Name",
    "column2": "Column Name"
  }
  ```
- **Response**: JSON with comparison results and result ID

### Download Results
- **GET** `/api/download/{resultId}/{type}`
- **Parameters**: 
  - `resultId`: UUID from comparison response
  - `type`: "matched" or "mismatched"
- **Response**: Excel file download

### Get File Info
- **GET** `/api/files/{fileId}`
- **Response**: JSON with file information

## Comparison Logic

The tool compares Excel files based on the following logic:

1. **Column Value Mapping**: Creates a map of column values to row data for each file
2. **Value Comparison**: Compares values between the selected columns
3. **Match Detection**: 
   - **Matched**: Values exist in both files
   - **Mismatched**: Values exist in only one file
4. **Result Generation**: Creates separate Excel files for matched and mismatched rows

## Configuration

### Application Properties

Key configuration options in `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# File upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging levels
logging.level.com.example.excelcomparison=INFO
```

## Error Handling

The application includes comprehensive error handling for:

- **File Upload Errors**: Invalid file formats, oversized files
- **Parsing Errors**: Corrupted Excel files, invalid data
- **Comparison Errors**: Missing columns, invalid file references
- **System Errors**: Memory issues, IO exceptions

All errors are displayed as user-friendly messages in the web interface.

## Security Considerations

- **File Size Limits**: Configurable maximum file size (default: 10MB)
- **File Type Validation**: Only Excel files (.xlsx, .xls) are accepted
- **In-Memory Storage**: Files are stored temporarily in memory during comparison
- **No Persistent Storage**: No files are saved permanently on the server

## Performance Notes

- **Memory Usage**: Files are loaded into memory for processing
- **Recommended File Size**: Best performance with files under 5MB
- **Concurrent Users**: Single-user design (in-memory storage)

## Troubleshooting

### Common Issues

1. **File Upload Fails**
   - Check file format (must be .xlsx or .xls)
   - Verify file size is under 10MB
   - Ensure file is not corrupted

2. **Comparison Fails**
   - Verify selected columns exist in both files
   - Check for empty or null values in selected columns
   - Ensure both files were uploaded successfully

3. **Download Fails**
   - Wait for comparison to complete before downloading
   - Check browser download settings
   - Verify result ID is valid

### Logs

Check application logs for detailed error information:
```bash
mvn spring-boot:run
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

### IDE Setup
Import the project as a Maven project in your preferred IDE (IntelliJ, Eclipse, VS Code).

## License

This project is for educational and demonstration purposes.

## Contributing

Feel free to submit issues and enhancement requests!
