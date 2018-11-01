package com.salmon.oss.security;


import com.salmon.oss.core.usermgr.model.UserInfo;

public class ContextUtil {

  public final static String SESSION_KEY = "user_token";

  private static ThreadLocal<UserInfo> userInfoThreadLocal = new ThreadLocal<>();

  public static UserInfo getCurrentUser() {
    return userInfoThreadLocal.get();
  }

  public static void setCurrentUser(UserInfo userInfo) {
    userInfoThreadLocal.set(userInfo);
  }

  public static void remove() {
    userInfoThreadLocal.remove();
  }

}
