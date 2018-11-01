package com.salmon.oss.server.hdfs;

import java.io.IOException;
import java.io.InputStream;

/**
 * HDFS 操作接口
 */
public interface HdfsService {

  /**
   * 上传文件
   * @param dir 目录
   * @param name 文件名称
   * @param input 文件的输入流
   * @param length 文件的长度
   * @param replication 备份数量
   * @throws IOException
   */
  void saveFile(String dir, String name, InputStream input, long length, short replication) throws IOException;

  /**
   * 删除文件
   * @param dir 目录
   * @param name 文件名称
   * @throws IOException
   */
  void deleteFile(String dir, String name) throws IOException;

  /**
   * 打开文件
   * @param dir 目录
   * @param name 文件名
   * @return
   * @throws IOException
   */
  InputStream openFile(String dir, String name) throws IOException;

  /**
   * 创建目录
   * @param dir 要创建的目录
   * @throws IOException
   */
  void mikDir(String dir) throws IOException;

  /**
   * 删除目录
   * @param dir 要删除的目录
   * @throws IOException
   */
  void deleteDir(String dir) throws IOException;

}
