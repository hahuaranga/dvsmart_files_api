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
package com.indra.minsait.dvsmart.files.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for file search results (summary version for lists).
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileSearchResultItem
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileSearchResultItem {

    private String idUnico;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted;
    private String lastModificationDate;
    private String tipoDocumento;
    private String codigoCliente;
    private Integer anio;
    private Integer mes;
}
