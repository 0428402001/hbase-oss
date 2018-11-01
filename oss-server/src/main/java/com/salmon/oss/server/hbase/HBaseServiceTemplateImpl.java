package com.salmon.oss.server.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.salmon.oss.server.OssServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import static com.salmon.oss.core.ConstantInfo.ERROR_HBASE;


@Component
@Slf4j
public class HBaseServiceTemplateImpl implements HBaseServiceTemplate {

  /**
   * createTable.
   *
   * @param tableName tableName
   * @param cfs cfs
   * @param splitKeys splitKeys 对于表预先分区，分区的键
   * @return success of failed
   */
  @Override
  public boolean createTable(Connection connection, String tableName, String[] cfs, byte[][] splitKeys) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      if (admin.tableExists(tableName)) {
        return false;
      }
      HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName));
      for (int i = 0; i < cfs.length; i++) {
        HColumnDescriptor hcolumnDesc = new HColumnDescriptor(cfs[i]);
        hcolumnDesc.setMaxVersions(1);
        tableDesc.addFamily(hcolumnDesc);
      }
      admin.createTable(tableDesc, splitKeys);
    } catch (Exception e) {
      String msg = String.format("create table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * createTable.
   *
   * @param tableName tableName
   * @param cfs cfs
   * @return success of failed
   */
  @Override
  public boolean createTable(Connection connection, String tableName, String[] cfs) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      if (admin.tableExists(tableName)) {
        return false;
      }
      HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName));
      for (int i = 0; i < cfs.length; i++) {
        HColumnDescriptor hcolumnDescriptor = new HColumnDescriptor(cfs[i]);
        hcolumnDescriptor.setMaxVersions(1);
        tableDesc.addFamily(hcolumnDescriptor);
      }
      admin.createTable(tableDesc);
    } catch (Exception e) {
      String msg = String.format("create table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * deleteTable.
   *
   * @param tableName tableName
   * @return success of failed
   */
  @Override
  public boolean deleteTable(Connection connection, String tableName) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      admin.disableTable(tableName); //先禁用
      admin.deleteTable(tableName); // 删除
    } catch (Exception e) {
      String msg = String.format("delete table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * 删除ColumnFamily.
   */
  @Override
  public boolean deleteColumnFamily(Connection connection, String tableName, String columnFamilyName) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      admin.deleteColumn(tableName, columnFamilyName);
    } catch (IOException e) {
      String msg = String
          .format("delete table=%s , column family=%s error. msg=%s", tableName, columnFamilyName,
              e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * 删除qualifier.
   */
  @Override
  public boolean deleteQualifier(Connection connection, String tableName, String rowName,
                                 String columnFamilyName, String qualifierName) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Delete delete = new Delete(rowName.getBytes());
      delete.addColumns(columnFamilyName.getBytes(), qualifierName.getBytes());
      table.delete(delete);
    } catch (IOException e) {
      String msg = String
          .format("delete table=%s , column family=%s , qualifier=%s error. msg=%s", tableName,
              columnFamilyName, qualifierName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * 删除row.
   */
  @Override
  public boolean delete(Connection connection, String tableName, String rowName) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Delete delete = new Delete(Bytes.toBytes(rowName));
      table.delete(delete);
    } catch (IOException e) {
      String msg = String
          .format("delete table=%s , row=%s error. msg=%s", tableName, rowName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * delete rows.
   *
   * @param tableName tableName
   * @param rows rows
   * @return success of failed
   */
  @Override
  public boolean delete(Connection connection, String tableName, List<String> rows) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      List<Delete> list = new ArrayList<>();
      for (String row : rows) {
        Delete d = new Delete(Bytes.toBytes(row));
        list.add(d);
      }
      if (list.size() > 0) {
        table.delete(list);
      }
    } catch (IOException e) {
      String msg = String
          .format("delete table=%s , rows error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * 扫描整张表，记得释放rs.
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName) {
    ResultScanner results;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Scan scan = new Scan();
      scan.setCaching(1000);
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String.format("scan table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * scanner 起止过滤
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, String startRowKey, String stopRowKey) {
    return scanner(connection, tableName, Bytes.toBytes(startRowKey), Bytes.toBytes(stopRowKey));
  }

  /**
   * scanner. 起止过滤
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, byte[] startRowKey, byte[] stopRowKey) {
    ResultScanner results = null;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Scan scan = new Scan();
      scan.setStartRow(startRowKey);
      scan.setStopRow(stopRowKey);
      scan.setCaching(1000);
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String
          .format("scan table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * scanner.
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, FilterList filterList) {
    ResultScanner results = null;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Scan scan = new Scan();
      scan.setCaching(1000);
      scan.setFilter(filterList);
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String
          .format("scan table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * scanner.
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, Scan scan) {
    ResultScanner results;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String
          .format("scan table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * scanner.
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, byte[] startRowKey, byte[] stopRowKey, FilterList filterList) {
    ResultScanner results;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Scan scan = new Scan();
      scan.setStartRow(startRowKey);
      scan.setStopRow(stopRowKey);
      scan.setCaching(1000);
      scan.setFilter(filterList);
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String
          .format("scan table=%s error. msg=%s", tableName, e.getMessage());
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * scanner.
   */
  @Override
  public ResultScanner scanner(Connection connection, String tableName, String startRowKey, String stopRowKey, FilterList filterList) {
    return scanner(connection, tableName, Bytes.toBytes(startRowKey), Bytes.toBytes(stopRowKey), filterList);
  }

  /**
   * existsRow.
   */
  @Override
  public boolean existsRow(Connection connection, String tableName, String row) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      return table.exists(g);
    } catch (IOException e) {
      String msg = String.format("check exists row from table=%s error. msg=%s", tableName, e.getMessage());
      throw new OssServerException(ERROR_HBASE, msg);
    }
  }

  /**
   * getRow.
   */
  @Override
  public Result getRow(Connection connection, String tableName, String row, FilterList filterList) {
    Result rs;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      g.setFilter(filterList);
      rs = table.get(g);
    } catch (IOException e) {
      String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return rs;
  }

  /**
   * getRow.
   */
  @Override
  public Result getRow(Connection connection, String tableName, Get get) {
    Result rs;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      rs = table.get(get);
    } catch (IOException e) {
      String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return rs;
  }

  /**
   * getRow.
   */
  @Override
  public Result getRow(Connection connection, String tableName, String row) {
    Result rs;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      rs = table.get(g);
    } catch (IOException e) {
      String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return rs;
  }

  /**
   * getRow.
   */
  @Override
  public Result getRow(Connection connection, String tableName, String row, byte[] column, byte[] qualifier) {
    Result rs;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      g.addColumn(column, qualifier);
      rs = table.get(g);
    } catch (IOException e) {
      String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return rs;
  }

  /**
   * getRows.
   *
   * @param tableName tableName
   * @param rows rows
   * @param filterList filterList
   * @return Result
   */
  @Override
  public Result[] getRows(Connection connection, String tableName, List<String> rows,
                          FilterList filterList) {
    Result[] results = null;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      List<Get> gets = new ArrayList<>();
      for (String row : rows) {
        if (row != null) {
          Get g = new Get(Bytes.toBytes(row));
          g.setFilter(filterList);
          gets.add(g);
        }
      }
      if (gets.size() > 0) {
        results = table.get(gets);
      }
    } catch (IOException e) {
      String msg = String.format("get rows from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }


  /**
   * getRows.
   *
   * @param tableName tableName
   * @param rows rows
   * @return Result
   */
  @Override
  public Result[] getRows(Connection connection, String tableName, List<String> rows) {
    Result[] results = null;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      List<Get> gets = new ArrayList<>();
      for (String row : rows) {
        if (row != null) {
          Get g = new Get(Bytes.toBytes(row));
          gets.add(g);
        }
      }
      if (gets.size() > 0) {
        results = table.get(gets);
      }
    } catch (IOException e) {
      String msg = String.format("get rows from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return results;
  }

  /**
   * 生成目录seqId  通过hbase的计数器来实现
   * @param connection
   * @param tableName
   * @param row
   * @param columnFamily
   * @param qualifier
   * @param num
   * @return
   */
  @Override
  public long incrementColumnValue(Connection connection, String tableName, String row, byte[] columnFamily, byte[] qualifier, int num) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      // 通过 HBASE的计数器来实现
      return table.incrementColumnValue(Bytes.toBytes(row), columnFamily, qualifier, num);
    } catch (IOException e) {
      String msg = String.format("incrementColumnValue table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
  }


  /**
   * putRow.
   *
   * @param tableName tableName
   * @param row row
   * @param columnFamily columnFamily
   * @param qualifier qualifier
   * @param data data
   * @return success of failed
   */
  @Override
  public boolean putRow(Connection connection, String tableName, String row, String columnFamily, String qualifier, String data) {
    try {
      Put put = new Put(Bytes.toBytes(row));
      put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), Bytes.toBytes(data));
      putRows(connection, tableName, Arrays.asList(put));
    } catch (Exception e) {
      String msg = String.format("put row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  @Override
  public boolean putRow(Connection connection, String tableName, Put put) {
    try {
      putRows(connection, tableName, Arrays.asList(put));
    } catch (Exception e) {
      String msg = String.format("put row from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }

  /**
   * putRows.
   *
   * @param tableName tableName
   * @param puts puts
   * @return success of failed
   */
  @Override
  public boolean putRows(Connection connection, String tableName, List<Put> puts) {
    final BufferedMutator.ExceptionListener listener = new BufferedMutator.ExceptionListener() {
      @Override
      public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator mutator) {
        String msg = String.format("put rows from table=%s error. msg=%s", tableName, e.getMessage());
        log.error(msg, e);
        throw new OssServerException(ERROR_HBASE, msg);
      }
    };
    BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(tableName)).listener(listener);
    params.writeBufferSize(5 * 1024 * 1024);
    try (final BufferedMutator mutator = connection.getBufferedMutator(params)) {
      mutator.mutate(puts);
      mutator.flush();
    } catch (IOException e) {
      String msg = String.format("put rows from table=%s error. msg=%s", tableName, e.getMessage());
      log.error(msg, e);
      throw new OssServerException(ERROR_HBASE, msg);
    }
    return true;
  }
}
