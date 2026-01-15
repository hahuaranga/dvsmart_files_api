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
package com.indra.minsait.dvsmart.files.infrastructure.persistence.repository;

import com.indra.minsait.dvsmart.files.infrastructure.persistence.document.FileIndexDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for file index documents.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileIndexMongoRepository
 * @date 14-01-2026
 */
@Repository
public interface FileIndexMongoRepository extends MongoRepository<FileIndexDocument, String> {

    /**
     * Finds a document by its unique identifier.
     *
     * @param idUnico unique identifier (SHA-256 hash)
     * @return optional containing document if found
     */
    Optional<FileIndexDocument> findByIdUnico(String idUnico);

    /**
     * Counts documents with completed reorganization.
     *
     * @param reorgStatus status to filter by
     * @return count of documents
     */
    long countByReorgStatus(String reorgStatus);
}
