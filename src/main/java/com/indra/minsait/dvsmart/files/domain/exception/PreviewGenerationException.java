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
 * Exception thrown when PDF preview generation fails.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class PreviewGenerationException
 * @date 14-01-2026
 */
public class PreviewGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private final String idUnico;

    public PreviewGenerationException(String idUnico, String message) {
        super("Failed to generate preview for file " + idUnico + ": " + message);
        this.idUnico = idUnico;
    }

    public PreviewGenerationException(String idUnico, String message, Throwable cause) {
        super("Failed to generate preview for file " + idUnico + ": " + message, cause);
        this.idUnico = idUnico;
    }

    public String getIdUnico() {
        return idUnico;
    }
}
