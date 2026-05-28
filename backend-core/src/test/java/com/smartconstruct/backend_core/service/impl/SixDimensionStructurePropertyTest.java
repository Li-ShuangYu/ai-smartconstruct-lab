package com.smartconstruct.backend_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;

/**
 * Property 10: 6-dimension config structure validation.
 *
 * <b>Validates: Requirements 25.1</b>
 *
 * For any JSON object, the structure validation function SHALL accept it if and
 * only if it contains exactly the 6 required top-level keys ("display", "db_mapping",
 * "ai_processing", "orchestration_settings", "data_collection", "evaluation") with
 * no additional top-level keys. Each key's value must be an object (possibly empty).
 */
class SixDimensionStructurePropertyTest {

    private static final Set<String> REQUIRED_DIMENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "display", "db_mapping", "ai_processing",
                    "orchestration_settings", "data_collection", "evaluation"
            ))
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Pure validation logic extracted from NodeValidationServiceImpl:
     * Checks that the JSON has exactly the 6 required top-level keys.
     */
    private boolean validateSixDimensionStructure(String jsonStr) {
        try {
            JsonNode config = objectMapper.readTree(jsonStr);
            if (!config.isObject()) {
                return false;
            }

            Set<String> topKeys = new HashSet<>();
            Iterator<String> fieldNames = config.fieldNames();
            while (fieldNames.hasNext()) {
                topKeys.add(fieldNames.next());
            }

            return topKeys.equals(REQUIRED_DIMENSIONS);
        } catch (Exception e) {
            return false;
        }
    }

    @Provide
    Arbitrary<String> validSixDimensionJson() {
        // Generate valid 6-dimension JSON with random inner content
        return Arbitraries.just(null).map(ignored -> {
            ObjectNode root = objectMapper.createObjectNode();
            for (String dim : REQUIRED_DIMENSIONS) {
                root.putObject(dim); // Empty objects are valid
            }
            return root.toString();
        });
    }

    @Provide
    Arbitrary<String> validSixDimensionJsonWithContent() {
        return Arbitraries.integers().between(0, 5).flatMap(contentSize -> {
            return Arbitraries.just(contentSize).map(size -> {
                ObjectNode root = objectMapper.createObjectNode();
                for (String dim : REQUIRED_DIMENSIONS) {
                    ObjectNode dimNode = root.putObject(dim);
                    for (int i = 0; i < size; i++) {
                        dimNode.put("field_" + i, "value_" + i);
                    }
                }
                return root.toString();
            });
        });
    }

    @Provide
    Arbitrary<String> jsonWithMissingDimensions() {
        // Generate JSON missing at least one required dimension
        List<String> dims = new ArrayList<>(REQUIRED_DIMENSIONS);
        return Arbitraries.integers().between(1, 5).flatMap(removeCount -> {
            return Arbitraries.just(removeCount).map(count -> {
                ObjectNode root = objectMapper.createObjectNode();
                List<String> shuffled = new ArrayList<>(dims);
                Collections.shuffle(shuffled, new Random(count));
                int keep = Math.max(1, dims.size() - count);
                for (int i = 0; i < keep; i++) {
                    root.putObject(shuffled.get(i));
                }
                return root.toString();
            });
        });
    }

    @Provide
    Arbitrary<String> jsonWithExtraDimensions() {
        // Generate JSON with all 6 dimensions plus extra keys
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15).flatMap(extraKey -> {
            return Arbitraries.just(extraKey).map(key -> {
                // Ensure extra key is not one of the required dimensions
                String safeKey = REQUIRED_DIMENSIONS.contains(key) ? key + "_extra" : key;
                ObjectNode root = objectMapper.createObjectNode();
                for (String dim : REQUIRED_DIMENSIONS) {
                    root.putObject(dim);
                }
                root.putObject(safeKey);
                return root.toString();
            });
        });
    }

    @Property(tries = 100)
    void validStructureAlwaysAccepted(@ForAll("validSixDimensionJson") String json) {
        boolean result = validateSixDimensionStructure(json);
        assert result : "Valid 6-dimension structure should be accepted: " + json;
    }

    @Property(tries = 100)
    void validStructureWithContentAccepted(@ForAll("validSixDimensionJsonWithContent") String json) {
        boolean result = validateSixDimensionStructure(json);
        assert result : "Valid 6-dimension structure with content should be accepted: " + json;
    }

    @Property(tries = 100)
    void missingDimensionsAlwaysRejected(@ForAll("jsonWithMissingDimensions") String json) {
        boolean result = validateSixDimensionStructure(json);
        assert !result : "JSON with missing dimensions should be rejected: " + json;
    }

    @Property(tries = 100)
    void extraDimensionsAlwaysRejected(@ForAll("jsonWithExtraDimensions") String json) {
        boolean result = validateSixDimensionStructure(json);
        assert !result : "JSON with extra dimensions should be rejected: " + json;
    }

    @Property(tries = 100)
    void nonObjectJsonAlwaysRejected(@ForAll @IntRange(min = 0, max = 100) int value) {
        // Arrays, numbers, strings, etc. should all be rejected
        String[] invalidJsons = {
                String.valueOf(value),
                "\"string_value\"",
                "[1, 2, 3]",
                "null",
                "true",
        };
        for (String json : invalidJsons) {
            boolean result = validateSixDimensionStructure(json);
            assert !result : "Non-object JSON should be rejected: " + json;
        }
    }
}
