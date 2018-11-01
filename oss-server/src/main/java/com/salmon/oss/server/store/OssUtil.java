package com.salmon.oss.server.store;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Hbase 常用常量信息
 */
public class OssUtil {

  /**
   * HBase 分区策略 预先分区
   */
  static final byte[][] OBJ_REGIONS = new byte[][]{
      Bytes.toBytes("1"),
      Bytes.toBytes("4"),
      Bytes.toBytes("7")
  };
  // 对象表前缀
  private static final String OBJ_TABLE_PREFIX = "oss_obj_";
  // 目录表前缀
  private static final String DIR_TABLE_PREFIX = "oss_dir_";

  /*目录表 列族信息*/
  //目录基本属性列族名称
  private static final String DIR_META_CF = "cf";
  static final byte[] DIR_META_CF_BYTES = DIR_META_CF.getBytes();
  //子目录列族
  static final String DIR_SUBDIR_CF = "sub";
  static final byte[] DIR_SUBDIR_CF_BYTES = DIR_SUBDIR_CF.getBytes();

  /*文件表 列族信息*/
  //文件内容列族
  private static final String OBJ_CONT_CF = "c";
  static final byte[] OBJ_CONT_CF_BYTES = OBJ_CONT_CF.getBytes();
  //文件基本属性列族
  private static final String OBJ_META_CF = "cf";
  static final byte[] OBJ_META_CF_BYTES = OBJ_META_CF.getBytes();

  /*常用列名标识*/
  // 目录seqId qualifies
  static final byte[] DIR_SEQID_QUALIFIER = "u".getBytes();
  //文件内容
  static final byte[] OBJ_CONT_QUALIFIER = "c".getBytes();
  // 文件长度属性 qualifies
  static final byte[] OBJ_LEN_QUALIFIER = "l".getBytes();
  // 文件属性 qualifier
  static final byte[] OBJ_PROPS_QUALIFIER = "p".getBytes();
  // 文件类型 qualifier
  static final byte[] OBJ_MEDIATYPE_QUALIFIER = "m".getBytes();

  //在hbase存储的根路径
  static final String FILE_STORE_ROOT = "/oss";
  //存储到hbase 和hdfs文件大小的分界值 20M
  static final int FILE_STORE_THRESHOLD = 20 * 1024 * 1024;

  static final int OBJ_LIST_MAX_COUNT = 200;
  // 存储seqid的表，协助生成seqid的表
  static final String BUCKET_DIR_SEQ_TABLE = "oss_dir_seq";
  // seqid 表 列族
  static final String BUCKET_DIR_SEQ_CF = "s";
  static final byte[] BUCKET_DIR_SEQ_CF_BYTES = BUCKET_DIR_SEQ_CF.getBytes();
  // seqid qualifies
  static final byte[] BUCKET_DIR_SEQ_QUALIFIER = "s".getBytes();

  static final FilterList OBJ_META_SCAN_FILTER = new FilterList(Operator.MUST_PASS_ONE);

  static {
    try {
      byte[][] qualifiers = new byte[][]{OssUtil.DIR_SEQID_QUALIFIER,
          OssUtil.OBJ_LEN_QUALIFIER,
          OssUtil.OBJ_MEDIATYPE_QUALIFIER};
      for (byte[] b : qualifiers) {
        Filter filter = new QualifierFilter(CompareOp.EQUAL,
            new BinaryComparator(b));
        OBJ_META_SCAN_FILTER.addFilter(filter);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 获取目录表的名字
   * @param bucket
   * @return
   */
  public static String getDirTableName(String bucket) {
    return DIR_TABLE_PREFIX + bucket;
  }

  /**
   * 获取文件表的名字
   * @param bucket
   * @return
   */
  public static String getObjTableName(String bucket) {
    return OBJ_TABLE_PREFIX + bucket;
  }

  /**
   * 获取目录表的所有列族
   */
  public static String[] getDirColumnFamily() {
    return new String[]{DIR_SUBDIR_CF, DIR_META_CF};
  }

  /**
   * 获取文件表的所有列族
   */
  public static String[] getObjColumnFamily() {
    return new String[]{OBJ_META_CF, OBJ_CONT_CF};
  }
}
