package com.salmon.oss.core.authmgr.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TokenInfo {

  private String token;
  private int expireTime;
  private Date refreshTime;
  private Date createTime;
  private boolean active;
  private String creator;

  public TokenInfo(String creator) {
    this.token = UUID.randomUUID().toString().replace("-", "");
    this.expireTime = 7;
    Date date = new Date();
    this.refreshTime = date;
    this.createTime = date;
    this.active = true;
    this.creator = creator;
  }

}
