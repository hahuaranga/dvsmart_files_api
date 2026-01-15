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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for file metadata.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileResponse
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponse {

    private String idUnico;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted;
    private Instant lastModificationDate;
    private String reorgStatus;
    private Instant reorgCompletedAt;
    private String indexingStatus;
    private Instant indexedAt;
    private BusinessMetadata business;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessMetadata {
        private String tipoDocumento;
        private String codigoCliente;
        private Integer anio;
        private Integer mes;
    }

    /**
     * Creates a response from domain model.
     *
     * @param metadata domain model
     * @return response DTO
     */
    public static FileResponse fromDomain(FileMetadata metadata) {
        return FileResponse.builder()
                .idUnico(metadata.getIdUnico())
                .fileName(metadata.getFileName())
                .fileSize(metadata.getFileSize())
                .fileSizeFormatted(metadata.getFileSizeFormatted())
                .lastModificationDate(metadata.getLastModificationDate())
                .reorgStatus(metadata.getReorgStatus())
                .reorgCompletedAt(metadata.getReorgCompletedAt())
                .indexingStatus(metadata.getIndexingStatus())
                .indexedAt(metadata.getIndexedAt())
                .business(BusinessMetadata.builder()
                        .tipoDocumento(metadata.getTipoDocumento())
                        .codigoCliente(metadata.getCodigoCliente())
                        .anio(metadata.getAnio())
                        .mes(metadata.getMes())
                        .build())
                .build();
    }
}
