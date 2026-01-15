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
package com.indra.minsait.dvsmart.files.domain.service;

import com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileSearchService Tests")
class FileSearchServiceTest {

    @Mock
    private FileMetadataPort fileMetadataPort;

    @InjectMocks
    private FileSearchService fileSearchService;

    private FileMetadata sampleMetadata;

    @BeforeEach
    void setUp() {
        sampleMetadata = FileMetadata.builder()
                .idUnico("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2")
                .fileName("factura_001.pdf")
                .fileSize(1048576L)
                .lastModificationDate(Instant.now())
                .destinationPath("/organized_data/a1/b2/c3/factura_001.pdf")
                .reorgStatus("COMPLETED")
                .tipoDocumento("FACTURA")
                .codigoCliente("CLI001")
                .anio(2025)
                .mes(1)
                .build();
    }

    @Test
    @DisplayName("Should find file by idUnico")
    void shouldFindFileByIdUnico() {
        // Given
        String idUnico = sampleMetadata.getIdUnico();
        when(fileMetadataPort.findByIdUnico(idUnico)).thenReturn(Optional.of(sampleMetadata));

        // When
        FileMetadata result = fileSearchService.execute(idUnico);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdUnico()).isEqualTo(idUnico);
        assertThat(result.getFileName()).isEqualTo("factura_001.pdf");
    }

    @Test
    @DisplayName("Should throw FileNotFoundException when file not found")
    void shouldThrowFileNotFoundExceptionWhenFileNotFound() {
        // Given
        String idUnico = "nonexistent";
        when(fileMetadataPort.findByIdUnico(idUnico)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> fileSearchService.execute(idUnico))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(idUnico);
    }

    @Test
    @DisplayName("Should search files with criteria")
    void shouldSearchFilesWithCriteria() {
        // Given
        SearchCriteria criteria = SearchCriteria.builder()
                .query("factura")
                .tipoDocumento("FACTURA")
                .page(0)
                .size(20)
                .build();

        PagedResult<FileMetadata> expectedResult = PagedResult.of(
                List.of(sampleMetadata),
                0, 20, 1
        );

        when(fileMetadataPort.search(any(SearchCriteria.class))).thenReturn(expectedResult);

        // When
        PagedResult<FileMetadata> result = fileSearchService.execute(criteria);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFileName()).isEqualTo("factura_001.pdf");
    }

    @Test
    @DisplayName("Should return empty result when no files match")
    void shouldReturnEmptyResultWhenNoFilesMatch() {
        // Given
        SearchCriteria criteria = SearchCriteria.builder()
                .query("nonexistent")
                .page(0)
                .size(20)
                .build();

        PagedResult<FileMetadata> emptyResult = PagedResult.of(List.of(), 0, 20, 0);
        when(fileMetadataPort.search(any(SearchCriteria.class))).thenReturn(emptyResult);

        // When
        PagedResult<FileMetadata> result = fileSearchService.execute(criteria);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
