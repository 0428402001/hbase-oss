package com.salmon.oss.server;

import com.salmon.oss.core.usermgr.UserService;
import com.salmon.oss.core.usermgr.model.UserInfo;
import org.junit.Test;

import javax.annotation.Resource;

public class BucketMapperTest extends BaseTest {

    @Resource
    private BucketService bucketService;
    @Resource
    private UserService userService;

    @Test
    public void addBucket() {
        UserInfo userInfo = userService.getUserInfoByName("Jerry");
        bucketService.addBucket(userInfo,"mybucket", "我的测试");
    }

    @Test
    public void updateBucket() {
    }

    @Test
    public void deleteBucket() {
    }

    @Test
    public void getBucket() {
    }

    @Test
    public void getBucketByName() {
    }

    @Test
    public void getBucketByCreator() {
    }

    @Test
    public void getUserAuthorizedBuckets() {
    }
}