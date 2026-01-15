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

/**
 * Input port for generating file previews.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class PreviewFileUseCase
 * @date 14-01-2026
 */
public interface PreviewFileUseCase {

    /**
     * Generates a preview image for a PDF file.
     *
     * @param idUnico unique file identifier (SHA-256 hash)
     * @param width   desired width in pixels
     * @param height  desired height in pixels
     * @param page    page number to render (1-based)
     * @param format  output format (png, jpeg)
     * @return preview image as byte array
     * @throws com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException     if file not found
     * @throws com.indra.minsait.dvsmart.files.domain.exception.FileNotAvailableException if not ready
     * @throws com.indra.minsait.dvsmart.files.domain.exception.PreviewGenerationException if rendering fails
     */
    byte[] execute(String idUnico, int width, int height, int page, String format);
}
