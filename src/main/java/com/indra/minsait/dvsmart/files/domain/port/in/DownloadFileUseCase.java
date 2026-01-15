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

import java.io.OutputStream;

/**
 * Input port for downloading files.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class DownloadFileUseCase
 * @date 14-01-2026
 */
public interface DownloadFileUseCase {

    /**
     * Downloads a file by streaming its content to the output stream.
     * <p>
     * This method uses zero-copy streaming to efficiently handle large files
     * without loading them entirely into memory.
     * </p>
     *
     * @param idUnico      unique file identifier (SHA-256 hash)
     * @param outputStream target stream to write file content
     * @return file metadata for setting response headers
     * @throws com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException     if file not found
     * @throws com.indra.minsait.dvsmart.files.domain.exception.FileNotAvailableException if not ready for download
     */
    FileMetadata execute(String idUnico, OutputStream outputStream);
}
