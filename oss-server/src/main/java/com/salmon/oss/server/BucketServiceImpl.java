package com.salmon.oss.server;

import java.util.List;

import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.core.authmgr.AuthService;
import com.salmon.oss.core.authmgr.model.ServiceAuth;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.server.mapper.BucketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(readOnly = true)
public class BucketServiceImpl implements BucketService {

  @Resource
  private BucketMapper bucketMapper;

  @Autowired
  private AuthService authService;

  @Override
  @Transactional
  public boolean addBucket(UserInfo userInfo, String bucketName, String detail) {
    BucketInfo bucketInfo = new BucketInfo(bucketName, userInfo.getUserName(), detail);
    bucketMapper.addBucket(bucketInfo);
    //授权信息
    ServiceAuth serviceAuth = new ServiceAuth();
    serviceAuth.setBucketName(bucketName);
    serviceAuth.setTargetToken(userInfo.getUserId());// 设置授权的token
    authService.addAuth(serviceAuth);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteBucket(String bucketName) {
    bucketMapper.deleteBucket(bucketName);
    // 删除授权信息
    authService.deleteAuthByBucket(bucketName);
    return true;
  }

  @Override
  @Transactional
  public boolean updateBucket(String bucketName, String detail) {
    bucketMapper.updateBucket(bucketName, detail);
    return true;
  }

  @Override
  public BucketInfo getBucketById(String bucketId) {
    return bucketMapper.getBucket(bucketId);
  }

  @Override
  public BucketInfo getBucketByName(String bucketName) {
    return bucketMapper.getBucketByName(bucketName);
  }

  /**
   * 根据授权token，获取对应的Bucket信息
   * @param token 授权的token
   * @return 返回授权的 Bucket
   */
  @Override
  public List<BucketInfo> getUserBuckets(String token) {
    return bucketMapper.getUserAuthorizedBuckets(token);
  }
}
