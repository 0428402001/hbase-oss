package com.salmon.oss.core;

/**
 * 所有异常信息的基类
 */
public abstract class OssException extends RuntimeException {

  protected String errorMessage;

  public OssException(String message, Throwable cause) {
    super(cause);
    this.errorMessage = message;
  }

  public abstract int errorCode();

  public String errorMessage() {
    return this.errorMessage;
  }
}
