package com.salmon.oss.controller;

import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.authmgr.AuthService;
import com.salmon.oss.core.authmgr.model.ServiceAuth;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import com.salmon.oss.core.usermgr.UserService;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.security.ContextUtil;
import com.salmon.oss.security.OperationAccessControl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 对象存储系统中系统管理类，实现对用户、bucket auth 等信息的管理
 */
@RestController
@RequestMapping("/oss/${oss.version}/sys")
@Api(tags = "用户、授权管理", description = "对象存储系统管理类")
public class ManageController {

  @Autowired
  private OperationAccessControl operationAccessControl;

  @Autowired
  private AuthService authService;

  @Autowired
  private UserService userService;

  /**
   * 创建用户
   * @param userName
   * @param password
   * @param detail
   * @param role
   * @return
   */
  @PostMapping("/user")
  @ApiOperation("创建用户")
  public ApiResponse createUser(@ApiParam("用户名") @RequestParam("userName") String userName,
                                @ApiParam("密码") @RequestParam("password") String password,
                                @ApiParam("用户描述") @RequestParam(name = "detail", required = false, defaultValue = "") String detail,
                                @ApiParam(value = "用户角色[ADMIN,USER]", defaultValue = "USER") @RequestParam(name = "role", required = false, defaultValue = "USER") String role) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //当前登录人的角色 是否有 给定角色(role) 的操作权限
    if (operationAccessControl.checkSystemRole(currentUser.getSystemRole(), SystemRole.valueOf(role))) {
      UserInfo userInfo = new UserInfo(userName, password, SystemRole.valueOf(role), detail);
      userService.addUser(userInfo);
      return new ApiResponse();
    }
    return ApiResponse.ofStatus(ApiResponse.Status.NOT_ADMIN);
  }

  /**
   * 删除用户
   * @param userId 用户ID
   */
  @DeleteMapping("/user/{userId}")
  @ApiOperation("删除用户")
  public ApiResponse deleteUser(@ApiParam("用户ID") @PathVariable("userId") String userId) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //检验当前登录用户是否对某一个用户的操作权限
    if (operationAccessControl.checkSystemRole(currentUser.getSystemRole(), userId)) {
      userService.deleteUser(userId);
      return new ApiResponse();
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 更新用户信息
   * @param password 密码
   * @param detail 描述信息
   * @return
   */
  @PutMapping("/user")
  @ApiOperation("更新当前登录用户信息")
  public ApiResponse updateUserInfo(
          @ApiParam("密码") @RequestParam(name = "password", required = false, defaultValue = "") String password,
          @ApiParam("用户描述") @RequestParam(name = "detail", required = false, defaultValue = "") String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //判断是否是访客，如果是访客 无权更新用户
    if (currentUser.getSystemRole().equals(SystemRole.VISITER)) {
      return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
    }
    userService.updateUserInfo(currentUser.getUserId(), password, detail);
    return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
  }

  /**
   * 获取当前登录人的用户信息
   */
  @GetMapping("/user")
  @ApiOperation("获取当前登录用户信息")
  public ApiResponse getUserInfo() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    return ApiResponse.ofSuccess(currentUser);
  }

  /**
   * 创建TOKEN
   * @param expireTime 过去时间，默认7天
   * @param isActive 是否可用 默认是 true
   * @return 返回创建的token信息
   */
  @PostMapping("/token")
  @ApiOperation("创建TOKEN")
  public ApiResponse createToken(
          @ApiParam(value = "过期时间[天]", defaultValue = "7") @RequestParam(name = "expireTime", required = false, defaultValue = "7") String expireTime,
          @ApiParam(value = "是否可用", defaultValue = "true") @RequestParam(name = "isActive", required = false, defaultValue = "true") String isActive) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    // 判断当前登录人是否有权创建token，如果是访客 将无权进行创建
    if (!currentUser.getSystemRole().equals(SystemRole.VISITER)) {
      TokenInfo tokenInfo = new TokenInfo(currentUser.getUserName());
      tokenInfo.setExpireTime(Integer.parseInt(expireTime));
      tokenInfo.setActive(Boolean.parseBoolean(isActive));
      authService.addToken(tokenInfo);
      return ApiResponse.ofSuccess(tokenInfo);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "NOT USER");
  }

  /**
   * 删除token
   * @param token token
   */
  @DeleteMapping("/token/{token}")
  @ApiOperation("删除TOKEN")
  public ApiResponse deleteToken(@ApiParam("token的值") @PathVariable("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //判断要删除的token是否是当前用户创建的
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.deleteToken(token);
      return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 更新token信息
   * @param token token
   * @param expireTime 过期时间 默认7天
   * @param isActive 是否可用 默认 true
   * @return
   */
  @PutMapping("/token")
  @ApiOperation("更新TOKEN")
  public ApiResponse updateTokenInfo(
          @ApiParam("token的值") @RequestParam("token") String token,
          @ApiParam(value = "过期时间[天]", defaultValue = "7") @RequestParam(name = "expireTime", required = false, defaultValue = "7") Integer expireTime,
          @ApiParam(value = "是否可用", defaultValue = "true") @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //判断要更新的token是否是当前用户创建的
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.updateToken(token, expireTime, isActive);
      return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 根据TOKEN信息获取token详情
   * @param token TOKEN值
   * @return 返回TOKEN详情
   */
  @GetMapping("/token/{token}")
  @ApiOperation("根据TOKEN信息获取token详情")
  public ApiResponse getTokenInfo(@ApiParam("token的值") @PathVariable("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      TokenInfo tokenInfo = authService.getTokenInfo(token);
      return ApiResponse.ofSuccess(tokenInfo);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 获取当前登录人创建的token
   * @return 当前登录人创建的token列表
   */
  @GetMapping("/token/list")
  @ApiOperation("获取当前登录人创建的TOKEN")
  public ApiResponse getTokenInfoList() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!currentUser.getSystemRole().equals(SystemRole.VISITER)) {
      List<TokenInfo> tokenInfos = authService.getTokenInfoByCreator(currentUser.getUserName());
      return ApiResponse.ofSuccess(tokenInfos);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 刷新令牌
   * @param token token值
   * @return
   */
  @ApiOperation("刷新TOKEN")
  @PostMapping("/token/refresh")
  public ApiResponse refreshToken(@ApiParam("原始TOKEN") @RequestParam("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    // 判断当前登录用户是否有权对token进行刷新
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.refreshToken(token);
      return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 创建授权信息
   * @param serviceAuth 授权信息，必须是json格式的字符串
   * @return
   */
  @PostMapping("/auth")
  @ApiOperation("创建授权信息")
  @ApiImplicitParam(name = "serviceAuth", value = "授权信息实体", required =true, dataType ="ServiceAuth")
  public ApiResponse createAuth(@RequestBody ServiceAuth serviceAuth) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl
        .checkBucketOwner(currentUser.getUserName(), serviceAuth.getBucketName())
        && operationAccessControl
        .checkTokenOwner(currentUser.getUserName(), serviceAuth.getTargetToken())) {
      authService.addAuth(serviceAuth);
      return new ApiResponse();
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }

  /**
   * 删除授权信息
   * @param bucket bucket name
   * @param token token
   */
  @DeleteMapping("/auth/{bucket}/{token}")
  @ApiOperation("删除授权信息")
  public ApiResponse deleteAuth(@ApiParam("Bucket名称") @PathVariable("bucket") String bucket,
                                @ApiParam("Token名称") @PathVariable("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl
            .checkBucketOwner(currentUser.getUserName(), bucket)  //检验某个用户是否有权限操作BUCKET
            && operationAccessControl
            //监测token是否是用户username 创建的
            .checkTokenOwner(currentUser.getUserName(), token)) {
      authService.deleteAuth(bucket, token);
      return new ApiResponse();
    }
    return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "PERMISSION DENIED");
  }
}
