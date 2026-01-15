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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for file operations.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FilesConfigProperties
 * @date 14-01-2026
 */
@Data
@ConfigurationProperties(prefix = "files")
public class FilesConfigProperties {

    /**
     * Search configuration.
     */
    private SearchConfig search = new SearchConfig();

    /**
     * Download configuration.
     */
    private DownloadConfig download = new DownloadConfig();

    /**
     * Preview configuration.
     */
    private PreviewConfig preview = new PreviewConfig();

    @Data
    public static class SearchConfig {
        /**
         * Default page size for search results.
         */
        private int defaultPageSize = 20;

        /**
         * Maximum allowed page size.
         */
        private int maxPageSize = 100;
    }

    @Data
    public static class DownloadConfig {
        /**
         * Buffer size for streaming downloads (bytes).
         */
        private int bufferSize = 8192;
    }

    @Data
    public static class PreviewConfig {
        /**
         * Default width for preview images (pixels).
         */
        private int defaultWidth = 300;

        /**
         * Default height for preview images (pixels).
         */
        private int defaultHeight = 400;

        /**
         * Default output format (png, jpeg).
         */
        private String defaultFormat = "png";

        /**
         * DPI for PDF rendering.
         */
        private int dpi = 150;
    }
}
