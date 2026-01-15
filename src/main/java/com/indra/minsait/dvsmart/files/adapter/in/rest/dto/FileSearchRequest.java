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
package com.indra.minsait.dvsmart.files.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file search operations.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileSearchRequest
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequest {

    /**
     * Search query for file name (partial, case-insensitive).
     */
    private String q;

    /**
     * Filter by document type (FACTURA, CONTRATO, RECIBO, etc.).
     */
    @Pattern(regexp = "^[A-Z0-9_]{1,50}$", message = "Invalid tipoDocumento format")
    private String tipoDocumento;

    /**
     * Filter by client code.
     */
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "Invalid codigoCliente format")
    private String codigoCliente;

    /**
     * Filter by year.
     */
    @Min(value = 2000, message = "Year must be >= 2000")
    @Max(value = 2100, message = "Year must be <= 2100")
    private Integer anio;

    /**
     * Filter by month (1-12).
     */
    @Min(value = 1, message = "Month must be >= 1")
    @Max(value = 12, message = "Month must be <= 12")
    private Integer mes;

    /**
     * Page number (0-based).
     */
    @Min(value = 0, message = "Page must be >= 0")
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size.
     */
    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    /**
     * Sort field (fileName, fileSize, lastModificationDate).
     */
    @Pattern(regexp = "^(fileName|fileSize|lastModificationDate|date)$", 
             message = "Invalid sort field")
    private String sort;

    /**
     * Sort direction (asc, desc).
     */
    @Pattern(regexp = "^(asc|desc)$", message = "Direction must be 'asc' or 'desc'")
    @Builder.Default
    private String direction = "asc";
}
