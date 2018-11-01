package com.salmon.oss.core.usermgr.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "userId")
public class UserInfo {

  private String userId;
  private String userName;
  private String password;
  private String detail;
  private SystemRole systemRole;
  private Date createTime;

  public UserInfo(String userName, String password, SystemRole systemRole, String detail) {
    this.userId = UUID.randomUUID().toString().replace("-", "");
    this.userName = userName;
    this.password = DigestUtils.md5DigestAsHex(password.getBytes());
    this.systemRole = systemRole;
    this.detail = detail;
    this.createTime = new Date();
  }

}
