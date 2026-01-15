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

import com.indra.minsait.dvsmart.files.domain.exception.FileNotAvailableException;
import com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.port.out.FileContentPort;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileDownloadService Tests")
class FileDownloadServiceTest {

    @Mock
    private FileMetadataPort fileMetadataPort;

    @Mock
    private FileContentPort fileContentPort;

    @InjectMocks
    private FileDownloadService fileDownloadService;

    private FileMetadata completedFile;
    private FileMetadata pendingFile;

    @BeforeEach
    void setUp() {
        completedFile = FileMetadata.builder()
                .idUnico("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2")
                .fileName("factura_001.pdf")
                .fileSize(1048576L)
                .lastModificationDate(Instant.now())
                .destinationPath("/organized_data/a1/b2/c3/factura_001.pdf")
                .reorgStatus("COMPLETED")
                .build();

        pendingFile = FileMetadata.builder()
                .idUnico("b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2a1")
                .fileName("documento_pending.pdf")
                .fileSize(512000L)
                .destinationPath(null)
                .reorgStatus("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should download file successfully")
    void shouldDownloadFileSuccessfully() {
        // Given
        String idUnico = completedFile.getIdUnico();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(fileMetadataPort.findByIdUnico(idUnico)).thenReturn(Optional.of(completedFile));
        doNothing().when(fileContentPort).streamFileTo(eq(completedFile.getDestinationPath()), any(OutputStream.class));

        // When
        FileMetadata result = fileDownloadService.execute(idUnico, outputStream);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("factura_001.pdf");
        verify(fileContentPort).streamFileTo(completedFile.getDestinationPath(), outputStream);
    }

    @Test
    @DisplayName("Should throw FileNotFoundException when file not found")
    void shouldThrowFileNotFoundExceptionWhenFileNotFound() {
        // Given
        String idUnico = "nonexistent";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(fileMetadataPort.findByIdUnico(idUnico)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> fileDownloadService.execute(idUnico, outputStream))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(idUnico);

        verify(fileContentPort, never()).streamFileTo(any(), any());
    }

    @Test
    @DisplayName("Should throw FileNotAvailableException when reorg not completed")
    void shouldThrowFileNotAvailableExceptionWhenReorgNotCompleted() {
        // Given
        String idUnico = pendingFile.getIdUnico();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(fileMetadataPort.findByIdUnico(idUnico)).thenReturn(Optional.of(pendingFile));

        // When/Then
        assertThatThrownBy(() -> fileDownloadService.execute(idUnico, outputStream))
                .isInstanceOf(FileNotAvailableException.class)
                .hasMessageContaining("PENDING");

        verify(fileContentPort, never()).streamFileTo(any(), any());
    }
}
