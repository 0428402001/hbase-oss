package com.salmon.oss.core.usermgr;

import com.salmon.oss.core.BaseTest;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import org.junit.Test;

import javax.annotation.Resource;


public class UserServiceTest extends BaseTest {

  @Resource
  private UserService userService;

  @Test
  public void addUser() {
    UserInfo userInfo = new UserInfo("testxx", "123456", SystemRole.ADMIN, "no desc");
    userService.addUser(userInfo);
  }

  @Test
  public void getUser() {
    UserInfo userInfo = userService.getUserInfoByName("salmon");
    System.out.println(userInfo);
  }

  @Test
  public void deleteUser() {
    UserInfo userInfo = userService.getUserInfoByName("salmon");
    userService.deleteUser(userInfo.getUserId());
  }
}
