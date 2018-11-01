package com.salmon.oss.security;

import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.core.authmgr.AuthService;
import com.salmon.oss.core.authmgr.model.ServiceAuth;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import com.salmon.oss.core.usermgr.UserService;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.server.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;


@Component
public class DefaultOperationAccessControl implements OperationAccessControl {

  @Autowired
  AuthService authService;

  @Autowired
  UserService userService;

  @Autowired
  BucketService bucketService;

  @Override
  public UserInfo checkLogin(String userName, String password) {
    UserInfo userInfo = userService.getUserInfoByName(userName);
    if (userInfo == null) {
      return null;
    } else {
      return userInfo.getPassword().equals(DigestUtils.md5DigestAsHex(password.getBytes())) ? userInfo : null;
    }
  }

  /**
   * 监测 systemRole1的权限是否比systemRole2大
   * @param systemRole1 角色1
   * @param systemRole2 角色2
   * @return
   */
  @Override
  public boolean checkSystemRole(SystemRole systemRole1, SystemRole systemRole2) {
    if (systemRole1.equals(SystemRole.ROOT)) { // 如果是超级管理员 就返回true
      return true;
    }
    //systemRole1角色是管理员 并且 systemRole2为用户角色
    return systemRole1.equals(SystemRole.ADMIN) && systemRole2.equals(SystemRole.USER);
  }

  /**
   * 监测token是否是用户username 创建的
   * @param userName 用户名
   * @param token token
   * @return
   */
  @Override
  public boolean checkTokenOwner(String userName, String token) {
    TokenInfo tokenInfo = authService.getTokenInfo(token);
    return tokenInfo.getCreator().equals(userName);
  }

  /**
   * 检验是否是系统角色
   * @param systemRole1
   * @param userId
   * @return
   */
  @Override
  public boolean checkSystemRole(SystemRole systemRole1, String userId) {
    if (systemRole1.equals(SystemRole.ROOT)) {
      return true;
    }
    UserInfo userInfo = userService.getUserInfo(userId);
    return systemRole1.equals(SystemRole.ADMIN) && userInfo.getSystemRole().equals(SystemRole.USER);
  }

  /**
   * 检验某个用户是否有权限操作BUCKET
   * @param userName 用户名
   * @param bucketName bucket name
   * @return
   */
  @Override
  public boolean checkBucketOwner(String userName, String bucketName) {
    BucketInfo bucketModel = bucketService.getBucketByName(bucketName);
    return bucketModel.getCreator().equals(userName);
  }

  /**
   * 检验某个TOKEN是否具有对bucket操作的权限
   * @param token token
   * @param bucket bucket name
   * @return
   */
  @Override
  public boolean checkPermission(String token, String bucket) {
    if (authService.checkToken(token)) {
      ServiceAuth serviceAuth = authService.getServiceAuth(bucket, token);
      if (serviceAuth != null) {
        return true;
      }
    }
    return false;
  }
}
