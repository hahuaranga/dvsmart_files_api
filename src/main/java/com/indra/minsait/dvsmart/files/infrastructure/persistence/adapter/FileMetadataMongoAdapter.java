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
package com.indra.minsait.dvsmart.files.infrastructure.persistence.adapter;

import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.FileStatistics;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import com.indra.minsait.dvsmart.files.infrastructure.persistence.document.FileIndexDocument;
import com.indra.minsait.dvsmart.files.infrastructure.persistence.mapper.FileMetadataMapper;
import com.indra.minsait.dvsmart.files.infrastructure.persistence.repository.FileIndexMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MongoDB adapter implementing FileMetadataPort.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileMetadataMongoAdapter
 * @date 14-01-2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMetadataMongoAdapter implements FileMetadataPort {

    private static final String REORG_STATUS_COMPLETED = "COMPLETED";
    private static final String COLLECTION_NAME = "files_index";

    private final FileIndexMongoRepository repository;
    private final MongoTemplate mongoTemplate;
    private final FileMetadataMapper mapper;

    @Override
    public Optional<FileMetadata> findByIdUnico(String idUnico) {
        log.debug("Finding file by idUnico: {}", idUnico);
        return repository.findByIdUnico(idUnico)
                .map(mapper::toDomain);
    }

    @Override
    public PagedResult<FileMetadata> search(SearchCriteria criteria) {
        log.debug("Searching files with criteria: {}", criteria);

        // Build query
        Query query = buildSearchQuery(criteria);

        // Count total matching documents
        long total = mongoTemplate.count(query, FileIndexDocument.class);

        // Apply pagination and sorting
        Sort sort = buildSort(criteria);
        query.with(PageRequest.of(criteria.getPage(), criteria.getSize(), sort));

        // Execute query
        List<FileIndexDocument> documents = mongoTemplate.find(query, FileIndexDocument.class);

        // Map to domain models
        List<FileMetadata> content = documents.stream()
                .map(mapper::toDomain)
                .toList();

        return PagedResult.of(content, criteria.getPage(), criteria.getSize(), total);
    }

    @Override
    public FileStatistics getStatistics() {
        log.debug("Getting file statistics");

        // Total count and size
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("reorg_status").is(REORG_STATUS_COMPLETED)),
                Aggregation.group()
                        .count().as("totalFiles")
                        .sum("fileSize").as("totalSize")
        );

        AggregationResults<Map> totalResults = mongoTemplate.aggregate(
                totalAgg, COLLECTION_NAME, Map.class);
        Map totalData = totalResults.getUniqueMappedResult();

        long totalFiles = totalData != null ? ((Number) totalData.getOrDefault("totalFiles", 0L)).longValue() : 0;
        long totalSize = totalData != null ? ((Number) totalData.getOrDefault("totalSize", 0L)).longValue() : 0;

        // Count by tipo documento
        Map<String, Long> byTipoDocumento = aggregateByField("business_tipoDocumento");

        // Count by status
        Map<String, Long> byStatus = aggregateByField("reorg_status");

        // Count by year
        Map<Integer, Long> byYear = aggregateByIntField("business_anio");

        return FileStatistics.builder()
                .totalFiles(totalFiles)
                .totalSize(totalSize)
                .byTipoDocumento(byTipoDocumento)
                .byStatus(byStatus)
                .byYear(byYear)
                .build();
    }

    @Override
    public long countAvailableFiles() {
        return repository.countByReorgStatus(REORG_STATUS_COMPLETED);
    }

    /**
     * Builds a MongoDB query from search criteria.
     */
    private Query buildSearchQuery(SearchCriteria criteria) {
        Query query = new Query();

        // Always filter by reorg_status = COMPLETED
        query.addCriteria(Criteria.where("reorg_status").is(REORG_STATUS_COMPLETED));

        // Search by file name (partial, case-insensitive)
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            query.addCriteria(Criteria.where("fileName")
                    .regex(criteria.getQuery(), "i"));
        }

        // Filter by tipo documento
        if (criteria.getTipoDocumento() != null && !criteria.getTipoDocumento().isBlank()) {
            query.addCriteria(Criteria.where("business_tipoDocumento")
                    .is(criteria.getTipoDocumento()));
        }

        // Filter by codigo cliente
        if (criteria.getCodigoCliente() != null && !criteria.getCodigoCliente().isBlank()) {
            query.addCriteria(Criteria.where("business_codigoCliente")
                    .is(criteria.getCodigoCliente()));
        }

        // Filter by year
        if (criteria.getAnio() != null) {
            query.addCriteria(Criteria.where("business_anio").is(criteria.getAnio()));
        }

        // Filter by month
        if (criteria.getMes() != null) {
            query.addCriteria(Criteria.where("business_mes").is(criteria.getMes()));
        }

        return query;
    }

    /**
     * Builds sort from criteria.
     */
    private Sort buildSort(SearchCriteria criteria) {
        String field = mapSortField(criteria.getSortField());
        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    /**
     * Maps API sort field names to document field names.
     */
    private String mapSortField(String sortField) {
        if (sortField == null) {
            return "fileName";
        }
        return switch (sortField.toLowerCase()) {
            case "filesize" -> "fileSize";
            case "lastmodificationdate", "date" -> "lastModificationDate";
            default -> "fileName";
        };
    }

    /**
     * Aggregates count by a string field.
     */
    private Map<String, Long> aggregateByField(String fieldName) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(fieldName).ne(null)),
                Aggregation.group("$" + fieldName).count().as("count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, COLLECTION_NAME, Map.class);

        Map<String, Long> resultMap = new HashMap<>();
        for (Map doc : results.getMappedResults()) {
            String key = (String) doc.get("_id");
            Long count = ((Number) doc.get("count")).longValue();
            if (key != null) {
                resultMap.put(key, count);
            }
        }
        return resultMap;
    }

    /**
     * Aggregates count by an integer field.
     */
    private Map<Integer, Long> aggregateByIntField(String fieldName) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(fieldName).ne(null)),
                Aggregation.group("$" + fieldName).count().as("count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, COLLECTION_NAME, Map.class);

        Map<Integer, Long> resultMap = new HashMap<>();
        for (Map doc : results.getMappedResults()) {
            Object keyObj = doc.get("_id");
            if (keyObj instanceof Number) {
                Integer key = ((Number) keyObj).intValue();
                Long count = ((Number) doc.get("count")).longValue();
                resultMap.put(key, count);
            }
        }
        return resultMap;
    }
}
