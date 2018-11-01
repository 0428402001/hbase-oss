package com.salmon.oss.server.store;

import com.salmon.oss.common.domain.ObjectListResult;
import com.salmon.oss.common.domain.OssObject;
import com.salmon.oss.common.domain.OssObjectSummary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;


public interface OssStoreService {

  /**
   * 在HBase中创建序列生成表
   * @throws IOException
   */
  void createSeqTable() throws IOException;

  void put(String bucket, String key, ByteBuffer content, long length, String mediaType, Map<String, String> properties) throws Exception;

  OssObjectSummary getSummary(String bucket, String key) throws IOException;

  List<OssObjectSummary> list(String bucket, String startKey, String endKey) throws IOException;

  ObjectListResult listDir(String bucket, String dir, String start, int maxCount) throws IOException;

  ObjectListResult listByPrefix(String bucket, String dir, String keyPrefix, String start, int maxCount) throws IOException;

  OssObject getObject(String bucket, String key) throws IOException;

  void deleteObject(String bucket, String key) throws Exception;

  void deleteBucketStore(String bucket) throws IOException;

  void createBucketStore(String bucket) throws IOException;

}
