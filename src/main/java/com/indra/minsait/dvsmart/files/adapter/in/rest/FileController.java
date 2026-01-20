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

package com.indra.minsait.dvsmart.files.adapter.in.rest;

import com.indra.minsait.dvsmart.files.adapter.in.rest.dto.*;
import com.indra.minsait.dvsmart.files.adapter.in.rest.mapper.SearchCriteriaMapper;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;
import com.indra.minsait.dvsmart.files.domain.port.in.*;
import com.indra.minsait.dvsmart.files.infrastructure.config.FilesConfigProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

/**
 * REST Controller for file operations.
 * <p>
 * Provides endpoints for searching, viewing, and downloading PDF files.
 * </p>
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileController
 * @date 14-01-2026
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String ID_UNICO_PATTERN = "^[a-f0-9]{64}$";

    private final SearchFilesUseCase searchFilesUseCase;
    private final GetFileUseCase getFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final PreviewFileUseCase previewFileUseCase;
    private final GetStatisticsUseCase getStatisticsUseCase;
    private final SearchCriteriaMapper criteriaMapper;
    private final FilesConfigProperties config;

    // ==================== SEARCH ====================

    @GetMapping("/search")
    public ResponseEntity<PagedSearchResponse> searchFiles(@Valid FileSearchRequest request) {
        log.info("Search request: q={}, tipoDocumento={}, page={}, size={}",
                request.getQ(), request.getTipoDocumento(), request.getPage(), request.getSize());

        SearchCriteria criteria = criteriaMapper.toDomain(request);
        PagedResult<FileMetadata> result = searchFilesUseCase.execute(criteria);

        return ResponseEntity.ok(PagedSearchResponse.fromDomain(result));
    }

    // ==================== GET METADATA ====================

    @GetMapping("/{idUnico}")
    public ResponseEntity<FileResponse> getFile(
            @PathVariable 
            @Pattern(regexp = ID_UNICO_PATTERN, message = "Invalid idUnico format")
            //@Parameter(description = "Unique file identifier (SHA-256 hash)")
            String idUnico) {

        log.info("Get file metadata: {}", idUnico);

        FileMetadata metadata = getFileUseCase.execute(idUnico);
        return ResponseEntity.ok(FileResponse.fromDomain(metadata));
    }

    // ==================== DOWNLOAD ====================

    @GetMapping("/{idUnico}/download")
    public void downloadFile(
            @PathVariable
            @Pattern(regexp = ID_UNICO_PATTERN, message = "Invalid idUnico format")
            String idUnico,
            HttpServletResponse response) throws IOException {

        log.info("Download request: {}", idUnico);

        // Set response headers before streaming
        response.setContentType(CONTENT_TYPE_PDF);

        // Stream file and get metadata
        FileMetadata metadata = downloadFileUseCase.execute(idUnico, response.getOutputStream());

        // Set additional headers after we have metadata
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + metadata.getFileName() + "\"");
        if (metadata.getFileSize() != null) {
            response.setContentLengthLong(metadata.getFileSize());
        }
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        response.flushBuffer();
        log.info("Download completed: {} ({} bytes)", metadata.getFileName(), metadata.getFileSize());
    }

    // ==================== VIEW (INLINE) ====================

    @GetMapping("/{idUnico}/view")
    public void viewFile(
            @PathVariable
            @Pattern(regexp = ID_UNICO_PATTERN, message = "Invalid idUnico format")
            String idUnico,
            HttpServletResponse response) throws IOException {

        log.info("View request: {}", idUnico);

        response.setContentType(CONTENT_TYPE_PDF);

        FileMetadata metadata = downloadFileUseCase.execute(idUnico, response.getOutputStream());

        // Use inline instead of attachment for browser viewing
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + metadata.getFileName() + "\"");
        if (metadata.getFileSize() != null) {
            response.setContentLengthLong(metadata.getFileSize());
        }

        response.flushBuffer();
        log.info("View completed: {}", metadata.getFileName());
    }

    // ==================== PREVIEW ====================

    @GetMapping("/{idUnico}/preview")
    public ResponseEntity<byte[]> previewFile(
            @PathVariable
            @Pattern(regexp = ID_UNICO_PATTERN, message = "Invalid idUnico format")
            String idUnico,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false) String format) {

        log.info("Preview request: {}, page={}, size={}x{}, format={}",
                idUnico, page, width, height, format);

        int w = width != null ? width : config.getPreview().getDefaultWidth();
        int h = height != null ? height : config.getPreview().getDefaultHeight();
        String fmt = format != null ? format : config.getPreview().getDefaultFormat();

        byte[] imageBytes = previewFileUseCase.execute(idUnico, w, h, page, fmt);

        MediaType mediaType = "jpeg".equalsIgnoreCase(fmt) 
                ? MediaType.IMAGE_JPEG 
                : MediaType.IMAGE_PNG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(imageBytes.length)
                .body(imageBytes);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats")
    public ResponseEntity<FileStatisticsResponse> getStatistics() {
        log.info("Statistics request");
        return ResponseEntity.ok(FileStatisticsResponse.fromDomain(getStatisticsUseCase.execute()));
    }
}
