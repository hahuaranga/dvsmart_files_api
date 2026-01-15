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
package com.indra.minsait.dvsmart.files.infrastructure.persistence.mapper;

import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.infrastructure.persistence.document.FileIndexDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper between MongoDB document and domain model.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileMetadataMapper
 * @date 14-01-2026
 */
@Component
public class FileMetadataMapper {

    /**
     * Converts a MongoDB document to domain model.
     *
     * @param document MongoDB document
     * @return domain model
     */
    public FileMetadata toDomain(FileIndexDocument document) {
        if (document == null) {
            return null;
        }

        return FileMetadata.builder()
                .idUnico(document.getIdUnico())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .lastModificationDate(document.getLastModificationDate())
                .sourcePath(document.getSourcePath())
                .destinationPath(document.getReorgDestinationPath())
                .indexingStatus(document.getIndexingStatus())
                .indexedAt(document.getIndexedAt())
                .reorgStatus(document.getReorgStatus())
                .reorgCompletedAt(document.getReorgCompletedAt())
                .tipoDocumento(document.getBusinessTipoDocumento())
                .codigoCliente(document.getBusinessCodigoCliente())
                .anio(document.getBusinessAnio())
                .mes(document.getBusinessMes())
                .build();
    }
}
