package com.example.filemover.controller;

import com.example.filemover.service.FileMoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class FileMoverController {

    @Autowired
    private FileMoverService fileMoverService;

    @PostMapping(path = "/move")
    public ResponseEntity<String> moveFile(@RequestParam String bucket,
                                           @RequestParam String key) {
        try {
            fileMoverService.moveFile(bucket, key);
            return ResponseEntity.ok("File moved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

