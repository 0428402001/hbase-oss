package com.salmon.oss.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 从Hbase中查询到的数据进行封装，封装后的类
 */
@Data
public class OssObjectSummary implements Comparable<OssObjectSummary>, Serializable {

  private String id;
  private String key;
  private String name;
  private long length;
  private String mediaType;
  private long lastModifyTime;
  private String bucket;
  private Map<String, String> attrs;

  public String getContentEncoding() {
    return attrs != null ? attrs.get("content-encoding") : null;
  }

  /**
   * 根据 key来比较大小
   */
  @Override
  public int compareTo(OssObjectSummary o) {
    return this.getKey().compareTo(o.getKey());
  }

}
