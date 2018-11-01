package com.salmon.oss.core.authmgr;


import com.salmon.oss.core.OssException;

public class OssAuthmgrException extends OssException {

  private int code;
  private String message;

  public OssAuthmgrException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }

  public OssAuthmgrException(int code, String message) {
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