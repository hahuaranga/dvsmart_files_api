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
package com.indra.minsait.dvsmart.files.domain.port.out;

import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.FileStatistics;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;

import java.util.Optional;

/**
 * Output port for file metadata operations.
 * <p>
 * This port defines the contract for accessing file metadata
 * stored in the persistence layer (MongoDB).
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileMetadataPort
 * @date 14-01-2026
 */
public interface FileMetadataPort {

    /**
     * Finds a file by its unique identifier.
     *
     * @param idUnico unique file identifier (SHA-256 hash)
     * @return optional containing file metadata if found
     */
    Optional<FileMetadata> findByIdUnico(String idUnico);

    /**
     * Searches files based on criteria.
     *
     * @param criteria search criteria including filters and pagination
     * @return paginated result of file metadata
     */
    PagedResult<FileMetadata> search(SearchCriteria criteria);

    /**
     * Gets file statistics.
     *
     * @return aggregated statistics
     */
    FileStatistics getStatistics();

    /**
     * Counts total files with completed reorganization.
     *
     * @return count of available files
     */
    long countAvailableFiles();
}
