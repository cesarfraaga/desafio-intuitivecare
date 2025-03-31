package com.intuitivecare.desafio.transform.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfToCsvService {

    private static final String PDF_PATH = "../../desafio-intuitivecare/desafio-scraping/scraping/downloads/tabela_estoque.pdf";
    private static final String CSV_NAME = "estoque.csv";
    private static final String ZIP_NAME = "Teste_Cesar_Estoque.zip";

    public void extractTableToCsv() throws Exception {
        File pdfFile = new File(PDF_PATH);

        if (!pdfFile.exists()) {
            System.err.println("File not found: " + pdfFile.getAbsolutePath());
            return;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String[] lines = text.split("\\r?\\n");

            List<String[]> records = new ArrayList<>();
            records.add(new String[]{
                    "SKU", "DESCRIÇÃO", "COMPARTIMENTO", "LOCAL", "UNIDADE",
                    "QUANT", "CUSTO", "VALOR DE ESTOQUE"
            });

            boolean tabelaEncontrada = false;

            for (String line : lines) {
                line = line.trim()
                        .replaceAll("R\\$\\s+", "R\\$");

                if (line.startsWith("SKU ")) {
                    tabelaEncontrada = true;
                    continue;
                }

                if (tabelaEncontrada && line.matches("^[A-Z0-9]{2,}\\s+.*")) {
                    String[] parts = line.split("\\s{2,}");

                    if (parts.length < 10) {
                        parts = line.split("\\s+");
                    }

                    String[] row = new String[10];
                    for (int i = 0; i < Math.min(parts.length, 10); i++) {
                        row[i] = parts[i].trim();
                    }
                    records.add(row);
                }
            }

            CSVWriter writer = new CSVWriter(
                    new FileWriter(CSV_NAME),
                    ';',
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END
            );
            writer.writeAll(records);
            writer.close();

            try (FileOutputStream fos = new FileOutputStream(ZIP_NAME);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                File csvFile = new File(CSV_NAME);
                ZipEntry zipEntry = new ZipEntry(csvFile.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = Files.readAllBytes(csvFile.toPath());
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }

            System.out.println("CSV structured and saved!");
            System.out.println("ZIP file created: " + ZIP_NAME);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
