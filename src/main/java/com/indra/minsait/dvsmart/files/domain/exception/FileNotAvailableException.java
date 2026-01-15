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
 * Exception thrown when a file exists but is not available for download.
 * <p>
 * This typically happens when the file hasn't completed reorganization yet.
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileNotAvailableException
 * @date 14-01-2026
 */
public class FileNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private final String idUnico;
    private final String currentStatus;

    public FileNotAvailableException(String idUnico, String currentStatus) {
        super("File not available for download. Current reorg_status: " + currentStatus);
        this.idUnico = idUnico;
        this.currentStatus = currentStatus;
    }

    public String getIdUnico() {
        return idUnico;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
