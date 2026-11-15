package com.resumescreener.enterpriseresumescreener.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.IOException;

@Service
public class DocxExtractionService {
    public String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();


            if (text == null || text.isBlank()) {
                throw new IOException("Could not extract text from the .docx file.");
            }


            return text;
        }
    }
    public String extractTextFromDoc(MultipartFile file) throws IOException {
        try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();

            if (text == null || text.isBlank()) {
                throw new IOException("Could not extract text from the .doc file.");
            }

            return text;
        }
    }
}