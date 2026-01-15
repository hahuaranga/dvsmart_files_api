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
package com.indra.minsait.dvsmart.files.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing search criteria for file queries.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class SearchCriteria
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {

    /**
     * Search query for file name (partial, case-insensitive).
     */
    private String query;

    /**
     * Filter by document type (FACTURA, CONTRATO, RECIBO, etc.).
     */
    private String tipoDocumento;

    /**
     * Filter by client code.
     */
    private String codigoCliente;

    /**
     * Filter by year.
     */
    private Integer anio;

    /**
     * Filter by month (1-12).
     */
    private Integer mes;

    /**
     * Page number (0-based).
     */
    @Builder.Default
    private int page = 0;

    /**
     * Page size.
     */
    @Builder.Default
    private int size = 20;

    /**
     * Sort field (fileName, fileSize, lastModificationDate).
     */
    @Builder.Default
    private String sortField = "fileName";

    /**
     * Sort direction (asc, desc).
     */
    @Builder.Default
    private String sortDirection = "asc";

    /**
     * Checks if any filter is applied.
     *
     * @return true if at least one filter is set
     */
    public boolean hasFilters() {
        return query != null && !query.isBlank()
                || tipoDocumento != null && !tipoDocumento.isBlank()
                || codigoCliente != null && !codigoCliente.isBlank()
                || anio != null
                || mes != null;
    }
}
