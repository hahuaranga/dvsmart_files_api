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

import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Response DTO for paginated search results.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class PagedSearchResponse
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedSearchResponse {

    private List<FileSearchResultItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ISO_INSTANT;

    /**
     * Creates a response from domain model.
     *
     * @param pagedResult domain paginated result
     * @return response DTO
     */
    public static PagedSearchResponse fromDomain(PagedResult<FileMetadata> pagedResult) {
        List<FileSearchResultItem> items = pagedResult.getContent().stream()
                .map(PagedSearchResponse::toSearchResultItem)
                .toList();

        return PagedSearchResponse.builder()
                .content(items)
                .page(pagedResult.getPage())
                .size(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .hasNext(pagedResult.isHasNext())
                .hasPrevious(pagedResult.isHasPrevious())
                .build();
    }

    private static FileSearchResultItem toSearchResultItem(FileMetadata metadata) {
        return FileSearchResultItem.builder()
                .idUnico(metadata.getIdUnico())
                .fileName(metadata.getFileName())
                .fileSize(metadata.getFileSize())
                .fileSizeFormatted(metadata.getFileSizeFormatted())
                .lastModificationDate(metadata.getLastModificationDate() != null
                        ? DATE_FORMATTER.format(metadata.getLastModificationDate())
                        : null)
                .tipoDocumento(metadata.getTipoDocumento())
                .codigoCliente(metadata.getCodigoCliente())
                .anio(metadata.getAnio())
                .mes(metadata.getMes())
                .build();
    }
}
