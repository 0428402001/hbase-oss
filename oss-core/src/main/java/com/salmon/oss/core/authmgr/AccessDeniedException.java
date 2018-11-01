package com.salmon.oss.core.authmgr;


import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.OssException;

public class AccessDeniedException extends OssException {

  public AccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AccessDeniedException(String resPath, long userId, String accessType) {
    super(String.format("access denied:%d->%s,%s", userId, resPath, accessType), null);
  }

  public AccessDeniedException(String resPath, long userId) {
    super(String.format("access denied:%d->%s not owner", userId, resPath), null);
  }

  @Override
  public int errorCode() {
    return ConstantInfo.ERROR_PERMISSION_DENIED;
  }
}
