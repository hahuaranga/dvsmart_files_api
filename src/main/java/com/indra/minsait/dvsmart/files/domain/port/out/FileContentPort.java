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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Output port for file content operations.
 * <p>
 * This port defines the contract for accessing file content
 * from the SFTP destination server.
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileContentPort
 * @date 14-01-2026
 */
public interface FileContentPort {

    /**
     * Gets an input stream to read file content.
     * <p>
     * The caller is responsible for closing the stream.
     * </p>
     *
     * @param remotePath path to the file in SFTP destination
     * @return input stream for reading file content
     */
    InputStream getFileStream(String remotePath);

    /**
     * Streams file content directly to an output stream.
     * <p>
     * This method is optimized for large files as it avoids
     * loading the entire file into memory.
     * </p>
     *
     * @param remotePath   path to the file in SFTP destination
     * @param outputStream target stream to write content
     */
    void streamFileTo(String remotePath, OutputStream outputStream);

    /**
     * Gets file content as byte array.
     * <p>
     * Use only for small files (e.g., previews). For large files,
     * use {@link #streamFileTo(String, OutputStream)}.
     * </p>
     *
     * @param remotePath path to the file in SFTP destination
     * @return file content as bytes
     */
    byte[] getFileContent(String remotePath);

    /**
     * Checks if a file exists at the given path.
     *
     * @param remotePath path to check
     * @return true if file exists
     */
    boolean fileExists(String remotePath);
}
