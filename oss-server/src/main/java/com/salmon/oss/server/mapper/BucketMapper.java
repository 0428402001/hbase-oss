package com.salmon.oss.server.mapper;

import java.util.List;

import com.salmon.oss.common.domain.BucketInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface BucketMapper {

  void addBucket(@Param("bucket") BucketInfo bucketInfo);

  int updateBucket(@Param("bucketName") String bucketName, @Param("detail") String detail);

  int deleteBucket(@Param("bucketName") String bucketName);

  BucketInfo getBucket(@Param("bucketId") String bucketId);

  BucketInfo getBucketByName(@Param("bucketName") String bucketName);

  List<BucketInfo> getBucketByCreator(@Param("creator") String creator);

  List<BucketInfo> getUserAuthorizedBuckets(@Param("token") String token);
}
