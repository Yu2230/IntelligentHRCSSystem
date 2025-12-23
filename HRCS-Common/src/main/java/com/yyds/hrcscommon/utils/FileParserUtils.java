package com.yyds.hrcscommon.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.context.annotation.Configuration;


import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件内容解析工具类
 * 支持格式：.pdf, .docx, .doc
 * 依赖版本：PDFBox 3.0.3, POI 5.3.0, Hutool 5.8.34
 */
@Slf4j
public class FileParserUtils {

    /**
     * 从输入流中提取文本内容
     * @param inputStream 文件输入流（调用方负责关闭）
     * @param fileExt 文件扩展名：pdf/docx/doc
     * @return 提取的纯文本内容
     * @throws IOException 解析失败时抛出
     */
    public static String extractText(InputStream inputStream, String fileExt) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件输入流不能为空");
        }
        if (fileExt == null || fileExt.trim().isEmpty()) {
            throw new IllegalArgumentException("文件扩展名不能为空");
        }

        fileExt = fileExt.toLowerCase().trim();
        log.info("开始解析文件，类型: {}", fileExt);

        try {
            switch (fileExt) {
                case "pdf":
                    return extractPdfText(inputStream);
                case "docx":
                    return extractDocxText(inputStream);
                case "doc":
                    return extractDocText(inputStream);
                default:
                    throw new IllegalArgumentException("不支持的文件类型: " + fileExt + "，仅支持 pdf/docx/doc");
            }
        } catch (IOException e) {
            log.error("文件解析IO异常: {}", fileExt, e);
            throw e;
        } catch (Exception e) {
            log.error("文件解析失败: {}", fileExt, e);
            throw new IOException("文件解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析PDF文件（PDFBox 3.x 版本）
     */
    private static String extractPdfText(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF解析成功，页数: {}, 字数: {}", document.getNumberOfPages(), text.length());
            return text;
        }
    }

    /**
     * 解析DOCX文件（Word 2007+）
     */
    private static String extractDocxText(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String text = paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .collect(Collectors.joining("\n"));
            log.info("DOCX解析成功，段落数: {}, 字数: {}", paragraphs.size(), text.length());
            return text;
        }
    }

    /**
     * 解析DOC文件（Word 97-2003）
     */
    private static String extractDocText(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            String[] paragraphs = extractor.getParagraphText();
            String text = String.join("\n", paragraphs);
            log.info("DOC解析成功，段落数: {}, 字数: {}", paragraphs.length, text.length());
            return text;
        }
    }

    /**
     * 判断是否为支持的文件类型
     */
    public static boolean isSupportedFile(String fileExt) {
        return "pdf".equalsIgnoreCase(fileExt) ||
                "docx".equalsIgnoreCase(fileExt) ||
                "doc".equalsIgnoreCase(fileExt);
    }
}