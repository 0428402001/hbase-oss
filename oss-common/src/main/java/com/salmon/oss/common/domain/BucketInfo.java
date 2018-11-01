package com.salmon.oss.common.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BucketInfo {

    private String bucketId;
    private String bucketName;
    private String creator;
    private String detail;
    private Date createTime;

    public BucketInfo(String bucketName, String creator, String detail) {
        this.bucketId = UUID.randomUUID().toString().replace("-", "");
        this.bucketName = bucketName;
        this.creator = creator;
        this.detail = detail;
        this.createTime = new Date();
    }
}
