package com.salmon.oss.server.hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Slf4j
@Component
public class HdfsServiceImpl implements HdfsService {

  private FileSystem fileSystem;
  // 128M
  private long defaultBlockSize = 128 * 1024 * 1024;
  /*
    HDFS 默认的BlockSize大小为128M，要求大约20M的文件保存的HDFS中，128M 和 20M还有有点差距呀
    设置中间值， initBlockSize 当文件大小小于64M的时候，手动将BlockSize设置成64M
   */
  private long initBlockSize = defaultBlockSize / 2;

  @Value("${hadoop.conf.dir}")
  private String hadoopConfDir;

  @Value("${hadoop.hdfs.addr}")
  private String hadoopHdfsAddr;

  /**
   * 当注入到spring container 后进行初始化的方法
   * @throws Exception
   */
  @PostConstruct
  public void init() throws Exception {
    String confDir = System.getenv("HADOOP_CONF_DIR");
    if (confDir == null)
      confDir = System.getProperty("HADOOP_CONF_DIR");
    if (confDir == null)
      confDir = hadoopConfDir;
    if (!new File(confDir).exists()) {
      throw new FileNotFoundException(confDir);
    }
    String hdfsAddr = System.getenv("HADOOP_HDFS_ADDR");
    if(hdfsAddr == null)
      hdfsAddr = System.getProperty("HADOOP_HDFS_ADDR");
    if(hdfsAddr == null)
      hdfsAddr = hadoopHdfsAddr;
    Configuration conf = new Configuration();
    conf.addResource(new Path(confDir + "/core-site.xml"));
    conf.addResource(new Path(confDir + "/hdfs-site.xml"));
//    conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
//    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
    fileSystem = FileSystem.get(new URI(hdfsAddr), conf);
  }

  @PreDestroy
  public void destroy() {
    try {
      if(fileSystem != null)
        fileSystem.close();
    } catch (IOException e) {
      // nothing
    }
  }

  /**
   * 上传文件
   * @param dir 目录
   * @param name 文件名称
   * @param input 文件的输入流
   * @param length 文件的长度
   * @param replication 备份数量
   * @throws IOException
   */
  @Override
  public void saveFile(String dir, String name,
      InputStream input, long length, short replication) throws IOException {
    Path dirPath = new Path(dir);
    try {
      if (!fileSystem.exists(dirPath)) { //目录不存在，新建
        boolean succ = fileSystem.mkdirs(dirPath, FsPermission.getDirDefault());
        log.info("create dir " + dirPath + " success" + succ);
        if (!succ) {
          log.error("create dir " + dirPath + " failure");
          throw new IOException("dir create failed:" + dir);
        }
      }
    } catch (FileExistsException ex) {
      //do nothing
    }
    Path path = new Path(dir + "/" + name);
    long blockSize = length <= initBlockSize ? initBlockSize : defaultBlockSize;
    FSDataOutputStream outputStream =
        fileSystem.create(path, true, 512 * 1024, replication, blockSize); // buffer 512K
    try {
      fileSystem.setPermission(path, FsPermission.getFileDefault()); //设置权限
      // 写入
      byte[] buffer = new byte[512 * 1024];
      int len = -1;
      while ((len = input.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
    } finally {
      input.close();
      outputStream.close();
    }
  }

  @Override
  public void deleteFile(String dir, String name) throws IOException {
    fileSystem.delete(new Path(dir + "/" + name), false);
  }

  @Override
  public InputStream openFile(String dir, String name) throws IOException {
    return fileSystem.open(new Path(dir + "/" + name));
  }

  @Override
  public void mikDir(String dir) throws IOException {
    fileSystem.mkdirs(new Path(dir));
  }

  @Override
  public void deleteDir(String dir) throws IOException {
    this.fileSystem.delete(new Path(dir), true);
  }
}
