package com.salmon.oss.core.authmgr.mapper;


import com.salmon.oss.core.authmgr.model.ServiceAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

@Mapper
public interface ServiceAuthMapper {

  void addAuth(@Param("auth") ServiceAuth auth);

  void deleteAuth(@Param("bucket") String bucketName, @Param("token") String token);

  void deleteAuthByToken(@Param("token") String token);

  void deleteAuthByBucket(@Param("bucket") String bucketName);

  @ResultMap("ServiceAuthResultMap")
  ServiceAuth getAuth(@Param("bucket") String bucketName, @Param("token") String token);
}
