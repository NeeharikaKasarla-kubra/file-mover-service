package com.kubra.filemover.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.Map;

@Service
public class SecretService {

    private final SecretsManagerClient secretsClient;

    public SecretService() {
        this.secretsClient = SecretsManagerClient.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }

    public Map<String, String> getSftpCredentials(String secretName) {
        GetSecretValueResponse response = secretsClient.getSecretValue(
                GetSecretValueRequest.builder().secretId(secretName).build());

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.secretString(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret JSON", e);
        }
    }
}

