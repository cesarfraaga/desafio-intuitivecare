package com.intuitivecare.desafio.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ScrapingService {

    private static final String TARGET_URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String DOWNLOAD_DIR = "scraping/downloads";

    public void executeScraping() throws Exception {
        System.out.println("Starting scraping...");

        Document doc = Jsoup.connect(TARGET_URL).get();

        Elements links = doc.select("a[href$=.pdf]");

        /*
         Os PDFs não estavam sendo encontrados inicialmente porque o código
         procurava por "anexo-i"/"anexo-ii", mas no site os arquivos estavam com "anexo_i"/"anexo_ii".
         Para identificar isso, imprimi todos os PDFs da página com Sysout e corrigi a lógica.
        */

        Files.createDirectories(Paths.get(DOWNLOAD_DIR));

        int count = 0;

        for (Element link : links) {
            String url = link.absUrl("href");
            String fileName = getFileName(url);

            if (fileName.toLowerCase().contains("anexo_i") || fileName.toLowerCase().contains("anexo_ii")) {
                System.out.println("Downloading: " + fileName);
                downloadFile(url, DOWNLOAD_DIR + "/" + fileName);
                count++;
            }
        }

        if (count > 0) {
            zipFiles(DOWNLOAD_DIR, "anexos.zip");
            System.out.println("Files successfully compressed!");
        } else {
            System.out.println("No attachments found.");
        }
    }

    private String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private void downloadFile(String fileURL, String savePath) throws IOException {
        try (InputStream in = new URL(fileURL).openStream()) {
            Files.copy(in, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void zipFiles(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Paths.get(zipFilePath);
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Files.walk(Paths.get(sourceDirPath))
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(path.getFileName().toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Error while compressing: " + e.getMessage());
                        }
                    });
        }
    }
}

