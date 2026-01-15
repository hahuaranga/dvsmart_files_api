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
package com.indra.minsait.dvsmart.files.domain.exception;

/**
 * Exception thrown when SFTP connection or operation fails.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class SftpConnectionException
 * @date 14-01-2026
 */
public class SftpConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public SftpConnectionException(String message) {
        super(message);
    }

    public SftpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
