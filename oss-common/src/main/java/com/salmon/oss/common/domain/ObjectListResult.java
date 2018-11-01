package com.salmon.oss.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 对象列表
 */
@Getter
@Setter
public class ObjectListResult {

  private String bucket;
  private String maxKey;
  private String minKey;
  private String nextMarker;
  private int maxKeyNumber;
  private int objectCount;
  private String listId;
  private List<OssObjectSummary> objectList;
}
