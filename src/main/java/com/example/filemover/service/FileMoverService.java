package com.example.filemover.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.util.Map;

@Service
public class FileMoverService {

    @Autowired
    private SecretService secretService;
    private final Logger log = LoggerFactory.getLogger(FileMoverService.class);

    public void moveFile(String bucketName, String key) throws Exception {
        log.info("Starting file transfer: bucket={}, key={}", bucketName, key);

        Map<String, String> creds = secretService.getSftpCredentials("sftp-creds");

        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:4566")) // replace with "http://localstack:4566" for K8s
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"))
                )
                .build())
        {

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Stream = s3.getObject(getRequest)) {

                JSch jsch = new JSch();
                Session session = jsch.getSession(creds.get("username"), "localhost", 2222); //sftp-server port: 22
                session.setPassword(creds.get("password"));
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();

                String destinationPath = "upload/" + key;
                log.info("Transferring to SFTP path: {}", destinationPath);
                sftp.put(s3Stream, destinationPath); // stream directly

                sftp.disconnect();
                session.disconnect();
                log.info("File transferred successfully!");
            } catch (Exception ex) {
                log.error("Error during file transfer", ex);
                throw ex;
            }
        }
    }
}

