package com.salmon.oss.common.domain;

import java.io.File;
import java.util.Map;

public class PutRequest {

  private String bucket;
  private String key;
  private File file;
  private byte[] content;
  private String contentEncoding;
  private String mediaType;
  private Map<String, String> attrs;

  public PutRequest(String bucket, String key, File file) {
    this.file = file;
    this.bucket = bucket;
    this.key = key;
  }

  public PutRequest(String bucket, String key, File file, String mediaType) {
    this.file = file;
    this.bucket = bucket;
    this.mediaType = mediaType;
    this.key = key;
  }

  public PutRequest(String bucket, String key, byte[] content, String mediaType) {
    this.content = content;
    this.bucket = bucket;
    this.mediaType = mediaType;
    this.key = key;
  }


  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }


  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public Map<String, String> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, String> attrs) {
    this.attrs = attrs;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }
}
