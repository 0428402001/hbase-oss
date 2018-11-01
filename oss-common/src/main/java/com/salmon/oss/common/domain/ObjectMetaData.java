package com.salmon.oss.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 对象元数据信息
 */
@Getter
@Setter
public class ObjectMetaData {
  /**
   * Object 所在的bucket
   */
  private String bucket;
  /**
   * Object 所在的全路径，即目录表中的 key
   */
  private String key;
  //文件类型
  private String mediaType;
  //文件的长度
  private long length;
  //文件最后的修改时间
  private long lastModifyTime;
  //
  private Map<String, String> attrs;

  /**
   * 获取当前对象的编码
   */
  public String getContentEncoding() {
    return attrs != null ? attrs.get("content-encoding") : null;
  }
}
