package com.salmon.oss.core.authmgr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAuth {
  private String bucketName;
  private String targetToken;
  private Date authTime;
}
