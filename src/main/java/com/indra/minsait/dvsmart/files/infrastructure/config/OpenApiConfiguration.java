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
package com.indra.minsait.dvsmart.files.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class OpenApiConfiguration
 * @date 14-01-2026
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI dvsmartFilesApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DVSmart Files API")
                        .description("""
                                REST API for searching, viewing, and downloading PDF files 
                                from the DVSmart document management system.
                                
                                ## Features
                                - **Search**: Find files by name, document type, client, year, month
                                - **Metadata**: Get complete file information
                                - **Download**: Download PDF files with streaming support
                                - **View**: Open PDFs directly in browser
                                - **Preview**: Generate thumbnail images from PDF pages
                                - **Statistics**: Get aggregated file statistics
                                
                                ## Architecture
                                Files are stored on SFTP destination server using hash-partitioned 
                                directory structure for optimal performance.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DVSmart Team")
                                .email("dvsmart@minsait.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.minsait.com")))
                .servers(List.of(
                        new Server()
                                .url("/dvsmart_files_api")
                                .description("Default server")
                ));
    }
}
