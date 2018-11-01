package com.salmon.oss.common.domain;

import java.io.IOException;
import java.io.InputStream;

public class OssObject {

  private ObjectMetaData metaData;

  private InputStream content;

  public OssObject() {

  }

  public void close() {
    try {
      if (content != null) {
        this.content.close();
      }
    } catch (IOException ioe) {
      //nothing to do
    }
  }

  public ObjectMetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(ObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public InputStream getContent() {
    return content;
  }

  public void setContent(InputStream content) {
    this.content = content;
  }


}
