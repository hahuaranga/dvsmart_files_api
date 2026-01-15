/*
 * /////////////////////////////////////////////////////////////////////////////
 *
 * Copyright (c) 2026 Indra Sistemas, S.A. All Rights Reserved.
 * http://www.indracompany.com/
 *
 * The contents of this file are owned by Indra Sistemas, S.A. copyright holder.
 * This file can only be copied, distributed and used all or in part with the
 * written permission of Indra Sistemas, S.A, or in accordance with the terms and
 * conditions laid down in the agreement / contract under which supplied.
 *
 * /////////////////////////////////////////////////////////////////////////////
 */
package com.indra.minsait.dvsmart.files.domain.service;

import com.indra.minsait.dvsmart.files.domain.exception.FileNotAvailableException;
import com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException;
import com.indra.minsait.dvsmart.files.domain.exception.PreviewGenerationException;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.port.in.PreviewFileUseCase;
import com.indra.minsait.dvsmart.files.domain.port.out.FileContentPort;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Domain service for generating PDF previews.
 * <p>
 * Uses Apache PDFBox to render PDF pages as images.
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class PdfPreviewService
 * @date 14-01-2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfPreviewService implements PreviewFileUseCase {

    private final FileMetadataPort fileMetadataPort;
    private final FileContentPort fileContentPort;

    @Value("${files.preview.dpi:150}")
    private int dpi;

    @Override
    public byte[] execute(String idUnico, int width, int height, int page, String format) {
        log.debug("Generating preview for file: {}, page: {}, size: {}x{}, format: {}",
                idUnico, page, width, height, format);

        // 1. Get file metadata
        FileMetadata metadata = fileMetadataPort.findByIdUnico(idUnico)
                .orElseThrow(() -> {
                    log.warn("File not found for preview: {}", idUnico);
                    return new FileNotFoundException(idUnico);
                });

        // 2. Validate file is available
        if (!metadata.isAvailableForDownload()) {
            log.warn("File not available for preview: {} (status: {})",
                    idUnico, metadata.getReorgStatus());
            throw new FileNotAvailableException(idUnico, metadata.getReorgStatus());
        }

        // 3. Get PDF content from SFTP
        byte[] pdfContent = fileContentPort.getFileContent(metadata.getDestinationPath());

        // 4. Render PDF page as image
        return renderPdfPage(idUnico, pdfContent, width, height, page, format);
    }

    /**
     * Renders a specific page of a PDF as an image.
     */
    private byte[] renderPdfPage(String idUnico, byte[] pdfContent, int width, int height, 
                                  int page, String format) {
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            int pageIndex = page - 1; // Convert to 0-based index

            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new PreviewGenerationException(idUnico,
                        "Invalid page number: " + page + ". Document has " + 
                        document.getNumberOfPages() + " pages.");
            }

            PDFRenderer renderer = new PDFRenderer(document);

            // Render page at specified DPI
            BufferedImage fullImage = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);

            // Resize to requested dimensions
            BufferedImage resizedImage = resizeImage(fullImage, width, height);

            // Convert to output format
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String imageFormat = "png".equalsIgnoreCase(format) ? "png" : "jpeg";
            ImageIO.write(resizedImage, imageFormat, baos);

            log.debug("Preview generated successfully for file: {}, output size: {} bytes",
                    idUnico, baos.size());

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to render PDF preview for file: {}", idUnico, e);
            throw new PreviewGenerationException(idUnico, "PDF rendering failed", e);
        }
    }

    /**
     * Resizes an image to the specified dimensions while maintaining aspect ratio.
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        // Calculate dimensions maintaining aspect ratio
        double originalRatio = (double) original.getWidth() / original.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int newWidth, newHeight;
        if (originalRatio > targetRatio) {
            // Original is wider - fit to width
            newWidth = targetWidth;
            newHeight = (int) (targetWidth / originalRatio);
        } else {
            // Original is taller - fit to height
            newHeight = targetHeight;
            newWidth = (int) (targetHeight * originalRatio);
        }

        // Create resized image
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();

        // Use high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }
}
