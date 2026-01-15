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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileController
 * @date 14-01-2026
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
@Tag(name = "Files", description = "PDF file search, view, and download operations")
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
    @Operation(summary = "Search PDF files", 
               description = "Search files by name, document type, client, year, and month")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedSearchResponse> searchFiles(@Valid FileSearchRequest request) {
        log.info("Search request: q={}, tipoDocumento={}, page={}, size={}",
                request.getQ(), request.getTipoDocumento(), request.getPage(), request.getSize());

        SearchCriteria criteria = criteriaMapper.toDomain(request);
        PagedResult<FileMetadata> result = searchFilesUseCase.execute(criteria);

        return ResponseEntity.ok(PagedSearchResponse.fromDomain(result));
    }

    // ==================== GET METADATA ====================

    @GetMapping("/{idUnico}")
    @Operation(summary = "Get file metadata", description = "Retrieves complete metadata for a file")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File metadata"),
        @ApiResponse(responseCode = "404", description = "File not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FileResponse> getFile(
            @PathVariable 
            @Pattern(regexp = ID_UNICO_PATTERN, message = "Invalid idUnico format")
            @Parameter(description = "Unique file identifier (SHA-256 hash)")
            String idUnico) {

        log.info("Get file metadata: {}", idUnico);

        FileMetadata metadata = getFileUseCase.execute(idUnico);
        return ResponseEntity.ok(FileResponse.fromDomain(metadata));
    }

    // ==================== DOWNLOAD ====================

    @GetMapping("/{idUnico}/download")
    @Operation(summary = "Download PDF file", 
               description = "Downloads the PDF file as attachment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF file stream",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "409", description = "File not available (reorganization pending)")
    })
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
    @Operation(summary = "View PDF in browser", 
               description = "Opens the PDF file inline in the browser")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF file stream for viewing",
                     content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "409", description = "File not available")
    })
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
    @Operation(summary = "Preview PDF page as image", 
               description = "Renders a PDF page as PNG/JPEG image")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preview image",
                     content = @Content(mediaType = "image/png")),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Preview generation failed")
    })
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
    @Operation(summary = "Get file statistics", 
               description = "Returns aggregated statistics about indexed files")
    @ApiResponse(responseCode = "200", description = "File statistics")
    public ResponseEntity<FileStatisticsResponse> getStatistics() {
        log.info("Statistics request");
        return ResponseEntity.ok(FileStatisticsResponse.fromDomain(getStatisticsUseCase.execute()));
    }
}
