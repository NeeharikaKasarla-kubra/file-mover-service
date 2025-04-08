package com.example.filemover.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Map;

@Service
public class FileMoverService {

    @Autowired
    private SecretService secretService;

    public void moveFile(String bucketName, String key) throws Exception {
        Map<String, String> creds = secretService.getSftpCredentials("sftp-creds");

        ResponseBytes<GetObjectResponse> objectBytes;
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()) {


            objectBytes = s3.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(key).build());
        }catch(Exception e) {
            throw new Exception("Unable to access S3.");
        }

        byte[] fileBytes = objectBytes.asByteArray();

        JSch jsch = new JSch();
        Session session = jsch.getSession(creds.get("username"), "localhost", 2222);
        session.setPassword(creds.get("password"));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        sftp.put(new ByteArrayInputStream(fileBytes), "upload/" + key);
        sftp.disconnect();
        session.disconnect();
    }
}

