package com.yyds.hrcscommon.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class UploadResult {
    private String url;
    private String objectName;
    private Long fileSize;
    private String originalFilename;
}