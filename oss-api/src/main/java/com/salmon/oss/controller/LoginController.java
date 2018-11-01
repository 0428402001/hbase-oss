package com.salmon.oss.controller;

import com.google.common.base.Strings;
import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.security.ContextUtil;
import com.salmon.oss.security.OperationAccessControl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@Api(tags = "登录、注销", description = "用户登录注销")
public class LoginController {

  @Autowired
  private OperationAccessControl operationAccessControl;

  @PostMapping("/login")
  @ApiOperation("用户登录")
  public ApiResponse login(@ApiParam("用户名") String username, @ApiParam("密码") String password, HttpSession session) {
    if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
      return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "username or password can not be null");
    }
    UserInfo userInfo = operationAccessControl.checkLogin(username, password);
    if (userInfo != null) {
      session.setAttribute(ContextUtil.SESSION_KEY, userInfo);
      return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
    } else {
      return ApiResponse.ofMessage(ConstantInfo.ERROR_PERMISSION_DENIED, "login error");
    }
  }

  @GetMapping("/logout")
  @ApiOperation("用户注销")
  public ApiResponse logout(HttpSession session) {
    session.removeAttribute(ContextUtil.SESSION_KEY);
    session.invalidate();
    return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
  }
}
