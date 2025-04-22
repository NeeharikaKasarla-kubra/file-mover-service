package com.kubra.filemover.controller;

import com.kubra.filemover.service.FileMoverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;

@RestController
@RequestMapping
public class FileMoverController {

    @Autowired
    private FileMoverService fileMoverService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/move")
    public ResponseEntity<String> moveFile(@RequestBody(required = false) String cloudEventJson,
                                           @RequestParam(required = false) String bucket,
                                           @RequestParam(required = false) String key) {
        try {
            // If CloudEvent JSON is provided

            if (cloudEventJson != null) {

                String type = "com.kubra.filemove";
                URI source = URI.create("/move");
                String id = UUID.randomUUID().toString();
                OffsetDateTime time = OffsetDateTime.now();

                // Build CloudEvent using CloudEventBuilder from the incoming JSON payload
                new CloudEventBuilder()
                        .withType(type)
                        .withSource(source)
                        .withId(id)
                        .withTime(time)
                        .withData(cloudEventJson.getBytes())
                        .build();

                // Parsing the 'data' field to extract bucket and key
                JsonNode dataNode = objectMapper.readTree(cloudEventJson);
                if (dataNode != null) {
                    bucket = dataNode.get("bucket").asText();
                    key = dataNode.get("key").asText();
                }
            }

            // If URL query parameters are provided, use them
            if (bucket == null || key == null) {
                return ResponseEntity.badRequest().body("Error: Missing parameters (bucket or key)");
            }

            fileMoverService.moveFile(bucket, key);
            return ResponseEntity.ok("File moved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

