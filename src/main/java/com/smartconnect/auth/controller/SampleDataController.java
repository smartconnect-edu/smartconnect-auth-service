package com.smartconnect.auth.controller;

import com.smartconnect.auth.dto.response.ApiResponse;
import com.smartconnect.auth.service.SampleDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for generating sample data
 * Public endpoint - no authentication required
 */
@Slf4j
@RestController
@RequestMapping("/v1/dev")
@RequiredArgsConstructor
@Tag(name = "Sample Data Generation", description = "Public API for generating sample data using Java Faker")
public class SampleDataController {

    private final SampleDataService sampleDataService;

    @PostMapping("/generate-sample-data")
    @Operation(
        summary = "Generate sample data",
        description = "Generate sample users, admins, teachers, and students using Java Faker. " +
                      "This is a public endpoint that does not require authentication. " +
                      "All generated users have password: Admin@123"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sample data generated successfully",
            content = @Content(schema = @Schema(implementation = SampleDataService.SampleDataSummary.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid parameters"
        )
    })
    public ResponseEntity<ApiResponse<SampleDataService.SampleDataSummary>> generateSampleData(
            @Parameter(description = "Number of admins to generate (default: 5)", example = "5")
            @RequestParam(defaultValue = "5") int adminCount,
            
            @Parameter(description = "Number of teachers to generate (default: 10)", example = "10")
            @RequestParam(defaultValue = "10") int teacherCount,
            
            @Parameter(description = "Number of students to generate (default: 20)", example = "20")
            @RequestParam(defaultValue = "20") int studentCount
    ) {
        log.info("Sample data generation request: {} admins, {} teachers, {} students", 
                 adminCount, teacherCount, studentCount);

        // Validate parameters
        if (adminCount < 0 || adminCount > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Admin count must be between 0 and 100"));
        }
        if (teacherCount < 0 || teacherCount > 1000) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Teacher count must be between 0 and 1000"));
        }
        if (studentCount < 0 || studentCount > 10000) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Student count must be between 0 and 10000"));
        }

        try {
            SampleDataService.SampleDataSummary summary = 
                    sampleDataService.generateSampleData(adminCount, teacherCount, studentCount);
            
            ApiResponse<SampleDataService.SampleDataSummary> response = 
                    ApiResponse.success("Sample data generated successfully", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating sample data", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate sample data: " + e.getMessage()));
        }
    }
}

