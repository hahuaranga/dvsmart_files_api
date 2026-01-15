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
package com.indra.minsait.dvsmart.files.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * MongoDB document representing a file in the index.
 * <p>
 * Maps to the 'files_index' collection created by dvsmart_indexing_api.
 * </p>
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileIndexDocument
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files_index")
public class FileIndexDocument {

    @Id
    private String id;

    /**
     * Unique identifier (SHA-256 hash of source path).
     */
    @Indexed(unique = true)
    private String idUnico;

    /**
     * Original source path (historical reference).
     */
    private String sourcePath;

    /**
     * File name (always ends with .pdf).
     */
    @Indexed
    private String fileName;

    /**
     * File extension (always "pdf").
     */
    private String extension;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Last modification date.
     */
    private Instant lastModificationDate;

    // Indexing state
    @Field("indexing_status")
    private String indexingStatus;

    @Field("indexing_indexedAt")
    private Instant indexedAt;

    // Reorganization state
    @Field("reorg_status")
    @Indexed
    private String reorgStatus;

    @Field("reorg_destinationPath")
    private String reorgDestinationPath;

    @Field("reorg_completedAt")
    private Instant reorgCompletedAt;

    // Business metadata
    @Field("business_tipoDocumento")
    @Indexed
    private String businessTipoDocumento;

    @Field("business_codigoCliente")
    @Indexed
    private String businessCodigoCliente;

    @Field("business_anio")
    private Integer businessAnio;

    @Field("business_mes")
    private Integer businessMes;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    private Instant updatedAt;
}
