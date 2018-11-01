package com.salmon.oss.security;

import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;

public interface OperationAccessControl {

  /**
   * 校验用户名和密码
   * @param userName 用户名
   * @param password 密码
   * @return
   */
  UserInfo checkLogin(String userName, String password);

  /**
   * 检验systemRole1 是否有 systemRole2的操作权限
   * @param systemRole1 角色1
   * @param systemRole2 角色2
   * @return
   */
  boolean checkSystemRole(SystemRole systemRole1, SystemRole systemRole2);

  /**
   * 检验systemRole1是否对某一个用户的操作权限
   * @param systemRole1
   * @param userId
   * @return
   */
  boolean checkSystemRole(SystemRole systemRole1, String userId);

  /**
   * 监测token是否是用户username 创建的
   * @param userName
   * @param token
   * @return
   */
  boolean checkTokenOwner(String userName, String token);

  /**
   * 检验某个用户是否有权限操作BUCKET
   * @param userName 用户名
   * @param bucketName bucket name
   * @return
   */
  boolean checkBucketOwner(String userName, String bucketName);

  /**
   * 检验某个TOKEN是否具有对bucket操作的权限
   * @param token token
   * @param bucket bucket name
   * @return
   */
  boolean checkPermission(String token, String bucket);

}
