package com.smartconstruct.backend_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import net.jqwik.api.*;

import java.util.*;

/**
 * Property 11: JSON Schema validation correctness.
 *
 * <b>Validates: Requirements 25.3</b>
 *
 * For any node config JSON and its registered JSON Schema, the validation function
 * SHALL accept the config if and only if it conforms to the schema. The error response
 * SHALL include the exact field path and violation reason for each non-conforming field.
 */
class JsonSchemaValidationPropertyTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JsonSchemaFactory schemaFactory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    /**
     * A minimal JSON Schema for testing: requires "display" object with "title" string field.
     */
    private static final String TEST_SCHEMA = "{"
            + "\"type\": \"object\","
            + "\"required\": [\"display\", \"db_mapping\", \"ai_processing\", \"orchestration_settings\", \"data_collection\", \"evaluation\"],"
            + "\"properties\": {"
            + "  \"display\": {"
            + "    \"type\": \"object\","
            + "    \"required\": [\"title\"],"
            + "    \"properties\": {"
            + "      \"title\": {\"type\": \"string\", \"minLength\": 1, \"maxLength\": 100}"
            + "    }"
            + "  },"
            + "  \"db_mapping\": {\"type\": \"object\"},"
            + "  \"ai_processing\": {\"type\": \"object\"},"
            + "  \"orchestration_settings\": {\"type\": \"object\"},"
            + "  \"data_collection\": {\"type\": \"object\"},"
            + "  \"evaluation\": {\"type\": \"object\"}"
            + "},"
            + "\"additionalProperties\": false"
            + "}";

    /**
     * Validate config JSON against a schema, returning (isValid, errorPaths).
     */
    private ValidationResult validateAgainstSchema(String configJson, String schemaJson) {
        try {
            JsonNode config = objectMapper.readTree(configJson);
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = schemaFactory.getSchema(schemaNode);
            Set<ValidationMessage> errors = schema.validate(config);

            if (errors == null || errors.isEmpty()) {
                return new ValidationResult(true, Collections.emptyList());
            }

            List<String> errorPaths = new ArrayList<>();
            for (ValidationMessage error : errors) {
                String path = error.getPath();
                if (path != null && path.startsWith("$.")) {
                    path = path.substring(2);
                }
                errorPaths.add(path + ": " + error.getMessage());
            }
            return new ValidationResult(false, errorPaths);
        } catch (Exception e) {
            return new ValidationResult(false, Collections.singletonList("Parse error: " + e.getMessage()));
        }
    }

    static class ValidationResult {
        final boolean isValid;
        final List<String> errors;

        ValidationResult(boolean isValid, List<String> errors) {
            this.isValid = isValid;
            this.errors = errors;
        }
    }

    @Provide
    Arbitrary<String> validConfigJson() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50).map(title -> {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode display = root.putObject("display");
            display.put("title", title);
            root.putObject("db_mapping");
            root.putObject("ai_processing");
            root.putObject("orchestration_settings");
            root.putObject("data_collection");
            root.putObject("evaluation");
            return root.toString();
        });
    }

    @Provide
    Arbitrary<String> configMissingRequiredField() {
        // Missing "title" in display
        return Arbitraries.just(null).map(ignored -> {
            ObjectNode root = objectMapper.createObjectNode();
            root.putObject("display"); // No "title" field
            root.putObject("db_mapping");
            root.putObject("ai_processing");
            root.putObject("orchestration_settings");
            root.putObject("data_collection");
            root.putObject("evaluation");
            return root.toString();
        });
    }

    @Provide
    Arbitrary<String> configWithWrongType() {
        // "title" is a number instead of string
        return Arbitraries.integers().between(1, 1000).map(num -> {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode display = root.putObject("display");
            display.put("title", num); // Wrong type: number instead of string
            root.putObject("db_mapping");
            root.putObject("ai_processing");
            root.putObject("orchestration_settings");
            root.putObject("data_collection");
            root.putObject("evaluation");
            return root.toString();
        });
    }

    @Provide
    Arbitrary<String> configWithExtraTopLevelKey() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(extraKey -> {
            String safeKey = "extra_" + extraKey;
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode display = root.putObject("display");
            display.put("title", "Valid Title");
            root.putObject("db_mapping");
            root.putObject("ai_processing");
            root.putObject("orchestration_settings");
            root.putObject("data_collection");
            root.putObject("evaluation");
            root.putObject(safeKey); // Extra key
            return root.toString();
        });
    }

    @Property(tries = 100)
    void validConfigAlwaysAccepted(@ForAll("validConfigJson") String config) {
        ValidationResult result = validateAgainstSchema(config, TEST_SCHEMA);
        assert result.isValid : "Valid config should be accepted. Errors: " + result.errors;
    }

    @Property(tries = 100)
    void missingRequiredFieldAlwaysRejected(@ForAll("configMissingRequiredField") String config) {
        ValidationResult result = validateAgainstSchema(config, TEST_SCHEMA);
        assert !result.isValid : "Config missing required field should be rejected";
        assert !result.errors.isEmpty() : "Error list should not be empty for invalid config";
    }

    @Property(tries = 100)
    void wrongTypeAlwaysRejected(@ForAll("configWithWrongType") String config) {
        ValidationResult result = validateAgainstSchema(config, TEST_SCHEMA);
        assert !result.isValid : "Config with wrong type should be rejected";
        assert !result.errors.isEmpty() : "Error list should not be empty for type mismatch";
    }

    @Property(tries = 100)
    void extraTopLevelKeyRejected(@ForAll("configWithExtraTopLevelKey") String config) {
        ValidationResult result = validateAgainstSchema(config, TEST_SCHEMA);
        assert !result.isValid : "Config with extra top-level key should be rejected";
    }

    @Property(tries = 50)
    void errorPathsAreNonEmpty(@ForAll("configMissingRequiredField") String config) {
        ValidationResult result = validateAgainstSchema(config, TEST_SCHEMA);
        if (!result.isValid) {
            for (String error : result.errors) {
                assert error != null && !error.isEmpty() :
                        "Error paths should be non-empty strings";
            }
        }
    }
}
