package com.salmon.oss.sdk;

import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.common.domain.ObjectListResult;
import com.salmon.oss.common.domain.OssObject;
import com.salmon.oss.common.domain.OssObjectSummary;
import com.salmon.oss.common.domain.PutRequest;
import com.salmon.oss.sdk.annotation.ApiServer;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.List;

@ApiServer
public interface WebClientSdk {

    /**
     * 初始化方法，必须先调用初始化方法完成初始化工作
     * @param url 请求的服务器url
     * @param token 请求的令牌
     */
    void init(String url, String token);

    @PostMapping("/bucket")
    ApiResponse createBucket(@RequestParam("bucket") String bucketName,
                             @RequestParam(name = "detail") String detail) throws IOException;

    @DeleteMapping("/bucket/{bucket}")
    ApiResponse deleteBucket(@PathVariable("bucket") String bucket) throws IOException;

    @GetMapping("/bucket/list")
    List<BucketInfo> listBucket() throws IOException;

    @GetMapping("/bucket/{bucket}")
    BucketInfo getBucketInfo(@PathVariable(name = "bucket") String bucket) throws IOException;

    @GetMapping("/object/list")
    ObjectListResult listObject(@RequestParam("bucket") String bucket,
                                @RequestParam("startKey") String startKey,
                                @RequestParam("endKey") String endKey) throws IOException;

    @GetMapping("/object/list/prefix")
    ObjectListResult listObjectByPrefix(@RequestParam("bucket") String bucket,
                                        @RequestParam("dir") String dir,
                                        @RequestParam("prefix") String prefix,
                                        @RequestParam("startKey") String start) throws IOException;

    /**
     * 列出bucket对应目录下的所有文件，当start key 不为空时，从start key进行查询
     * @param bucket
     * @param dir
     * @param startKey 可以为 ""  或者 null
     * @return
     * @throws IOException
     */
    @GetMapping("/object/list/dir")
    ObjectListResult listObjectByDir(@RequestParam("bucket") String bucket,
                                @RequestParam("dir") String dir,
                                @RequestParam("startKey") String startKey) throws IOException;


    @PostMapping("/object/delete")
    ApiResponse deleteObject(@RequestParam("bucket") String bucket,
                             @RequestParam("key") String key) throws IOException;

    @GetMapping("/object/info")
    OssObjectSummary getObjectSummary(@RequestParam("bucket") String bucket, @RequestParam("key") String key) throws IOException;


    @GetMapping("/object/download")
    OssObject getObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key) throws IOException;

    @PostMapping("/object/upload")
    ApiResponse putObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key) throws IOException;
    @PostMapping("/object/upload")
    ApiResponse putObject(@RequestParam("bucket") String bucket,@RequestParam("key") String key, @RequestParam("content") byte[] content, @RequestParam("mediaType") String mediaType) throws IOException;
    @PostMapping("/object/upload")
    ApiResponse putObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key, @RequestParam("content") String filePath) throws IOException;

}
