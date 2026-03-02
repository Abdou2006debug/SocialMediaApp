package com.example.SocialMediaApp.Upload.domain;

import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class UploadFinilazing {
    private List<String> filePaths;
    private List<String> failedUploadIds;
}
