package com.salmon.oss.common.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListObjectRequest {

  private String bucket;
  private String startKey;
  private String endKey;
  private String prefix;
  private int maxKeyNumber;
  private String listId;

}
