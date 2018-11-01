package com.salmon.oss.server;
import com.salmon.oss.core.OssException;


public class OssServerException extends OssException {

  private int code;
  private String message;

  public OssServerException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }

  public OssServerException(int code, String message) {
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
