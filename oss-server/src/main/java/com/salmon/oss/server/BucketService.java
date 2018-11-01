package com.salmon.oss.server;

import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.core.usermgr.model.UserInfo;

import java.util.List;

public interface BucketService {

  boolean addBucket(UserInfo userInfo, String bucketName, String detail);

  boolean deleteBucket(String bucketName);

  boolean updateBucket(String bucketName, String detail);

  BucketInfo getBucketById(String bucketId);

  BucketInfo getBucketByName(String bucketName);

  List<BucketInfo> getUserBuckets(String token);
}
