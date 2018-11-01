package com.salmon;

import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.common.domain.ObjectListResult;
import com.salmon.oss.common.domain.OssObject;
import com.salmon.oss.common.domain.OssObjectSummary;
import com.salmon.oss.common.utils.JsonUtils;
import com.salmon.oss.sdk.WebClientSdk;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SdkTest extends BaseTest {

    @Value("${oss.api.server.url}")
    private String apiServer;

    @Value("${oss.api.server.token}")
    private String token;

    @Resource
    private WebClientSdk webClientSdk;

    /**
     * 每次测试之前，都进行初始化，必须初始化之后才能获取到服务器和token的信息
     */
    @Before
    public void init() {
        webClientSdk.init(this.apiServer,this.token);
    }

    @Test
    public void createBucket() {
        try {
            ApiResponse a = webClientSdk.createBucket("mytest1","mytest1");
            System.out.println(a);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listBucket() {
        try {
            List<BucketInfo> a = webClientSdk.listBucket();
            System.out.println(a.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getBucket() {
        try {
            BucketInfo a = webClientSdk.getBucketInfo("mytest");
            System.out.println(a.getBucketName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getObject() {
        try {
            OssObject ossObject = webClientSdk.getObject("mytest", "/dir1/4.jpg");
            byte[] buffer = new byte[32 * 1024];
            int len;
            InputStream inputStream = ossObject.getContent();
            File outFile = new File("/Users/wanghongqing/" + ossObject.getMetaData().getKey()
                    .substring((ossObject.getMetaData().getKey().lastIndexOf("/") + 1)));
            OutputStream outputStream = new FileOutputStream(outFile);
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putObject() {
        try {
            String filePath  = "/Users/wanghongqing/101.jpg";
            ApiResponse apiResponse = webClientSdk.putObject("mytest","/dir1/中文.jpg", filePath);
            System.out.println(apiResponse.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putObjectForDir() {
        try {
            String dir  = "/abc/def/";
            ApiResponse apiResponse = webClientSdk.putObject("mytest",dir);
            System.out.println(apiResponse.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void putObjectForBytes() {
        try {
            String dir  = "/abc/ObjectStore.docx";
            String path  = "/Users/wanghongqing/数据存储方案-new.docx";
            FileInputStream in = new FileInputStream(path);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = -1;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            in.close();
            byte [] content = out.toByteArray();
            out.close();
            ApiResponse apiResponse = webClientSdk.putObject("mytest",dir,content,"docx");
            System.out.println(apiResponse.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getObjectSummary() {
        try {
            OssObjectSummary ossObjectSummary = webClientSdk.getObjectSummary("mytest","/dir1/中文.jpg");
            System.out.println(ossObjectSummary.getBucket());
            System.out.println(ossObjectSummary.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteBucket() {
        try {
            ApiResponse apiResponse = webClientSdk.deleteBucket("salmonbucket");
            System.out.println(JsonUtils.toJson(apiResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listObjectByDir() {
        try {
            ObjectListResult objectListResult = webClientSdk.listObjectByDir("mytest","/dir1/", null);
            System.out.println(JsonUtils.toJson(objectListResult));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteObj() {
        try {
            ApiResponse apiResponse = webClientSdk.deleteObject("mytest","/dir1/中文.jpg");
            System.out.println(JsonUtils.toJson(apiResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
