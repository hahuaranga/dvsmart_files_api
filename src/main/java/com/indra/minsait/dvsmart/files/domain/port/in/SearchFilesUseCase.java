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
package com.indra.minsait.dvsmart.files.domain.port.in;

import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;

/**
 * Input port for searching files.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class SearchFilesUseCase
 * @date 14-01-2026
 */
public interface SearchFilesUseCase {

    /**
     * Searches files based on criteria.
     *
     * @param criteria search criteria with filters and pagination
     * @return paginated result of file metadata
     */
    PagedResult<FileMetadata> execute(SearchCriteria criteria);
}
