package com.salmon.oss.core.usermgr;


import com.salmon.oss.core.OssException;

/**
 * 用户管理模块异常.
 */
public class OssUsermgrException extends OssException {

  private int code;
  private String message;

  public OssUsermgrException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }

  public OssUsermgrException(int code, String message) {
    super(message, null);
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int errorCode() {
    return this.code;
  }
}
