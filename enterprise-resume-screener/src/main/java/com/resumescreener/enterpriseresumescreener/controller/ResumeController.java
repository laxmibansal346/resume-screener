package com.resumescreener.enterpriseresumescreener.controller;

import com.resumescreener.enterpriseresumescreener.dto.BiasCheckResult;
import com.resumescreener.enterpriseresumescreener.dto.ErrorResponse;
import com.resumescreener.enterpriseresumescreener.dto.HybridAnalysisResult;
import com.resumescreener.enterpriseresumescreener.service.BiasDetectionService;
import com.resumescreener.enterpriseresumescreener.service.DocxExtractionService;
import com.resumescreener.enterpriseresumescreener.service.HybridScoringService;
import com.resumescreener.enterpriseresumescreener.service.PdfExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ResumeController {

    private final PdfExtractionService pdfExtractionService;
    private final DocxExtractionService docxExtractionService;
    private final HybridScoringService hybridScoringService;
    private final BiasDetectionService biasDetectionService;

    public ResumeController(PdfExtractionService pdfExtractionService,
                            DocxExtractionService docxExtractionService,
                            HybridScoringService hybridScoringService,
                            BiasDetectionService biasDetectionService) {
        this.pdfExtractionService = pdfExtractionService;
        this.docxExtractionService = docxExtractionService;
        this.hybridScoringService = hybridScoringService;
        this.biasDetectionService = biasDetectionService;
    }

    private String extractResumeText(MultipartFile resumeFile) throws Exception {
        String filename = resumeFile.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Could not read the uploaded file name.");
        }
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".pdf")) {
            return pdfExtractionService.extractText(resumeFile);
        } else if (lowerFilename.endsWith(".docx")) {
            return docxExtractionService.extractTextFromDocx(resumeFile);
        } else if (lowerFilename.endsWith(".doc")) {
            return docxExtractionService.extractTextFromDoc(resumeFile);
        } else {
            throw new IllegalArgumentException("Only PDF, DOC, and DOCX files are allowed.");
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription
    ) {
        if (resumeFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Resume file is required."));
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Job description cannot be empty."));
        }
        if (jobDescription.trim().length() < 50) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Job description seems too short. Please paste the complete job posting."));
        }

        try {
            String resumeText = extractResumeText(resumeFile);
            HybridAnalysisResult result = hybridScoringService.analyze(resumeText, jobDescription);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze-bias")
    public ResponseEntity<?> analyzeBias(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam("candidateNames") String candidateNamesRaw
    ) {
        if (resumeFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Resume file is required."));
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Job description cannot be empty."));
        }
        if (jobDescription.trim().length() < 50) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Job description seems too short. Please paste the complete job posting."));
        }
        if (candidateNamesRaw == null || candidateNamesRaw.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("At least 2 candidate names are required, comma-separated."));
        }

        List<String> candidateNames = Arrays.stream(candidateNamesRaw.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();

        if (candidateNames.size() < 2) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Please provide at least 2 candidate names, comma-separated."));
        }

        try {
            String resumeText = extractResumeText(resumeFile);
            BiasCheckResult result = biasDetectionService.analyzeForBias(resumeText, jobDescription, candidateNames);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong: " + e.getMessage()));
        }
    }
}