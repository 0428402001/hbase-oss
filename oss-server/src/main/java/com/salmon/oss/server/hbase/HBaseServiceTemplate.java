package com.salmon.oss.server.hbase;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;

import java.util.List;

public interface HBaseServiceTemplate {
    /**
     * createTable.
     *
     * @param tableName tableName
     * @param cfs cfs
     * @param splitKeys splitKeys
     * @return success of failed
     */
    boolean createTable(Connection connection, String tableName, String[] cfs, byte[][] splitKeys);

    /**
     * createTable.
     *
     * @param tableName tableName
     * @param cfs cfs
     * @return success of failed
     */
    boolean createTable(Connection connection, String tableName, String[] cfs);

    /**
     * deleteTable.
     *
     * @param tableName tableName
     * @return success of failed
     */
    boolean deleteTable(Connection connection, String tableName);

    /**
     * 删除ColumnFamily.
     */
    boolean deleteColumnFamily(Connection connection, String tableName,
                               String columnFamilyName);

    /**
     * 删除qualifier.
     */
    boolean deleteQualifier(Connection connection, String tableName, String rowName,
                            String columnFamilyName, String qualifierName);

    /**
     * 删除row.
     */
    boolean delete(Connection connection, String tableName, String rowName);

    /**
     * delete rows.
     *
     * @param tableName tableName
     * @param rows rows
     * @return success of failed
     */
    boolean delete(Connection connection, String tableName, List<String> rows);

    /**
     * 扫描整张表，记得释放rs.
     */
    ResultScanner scanner(Connection connection, String tableName);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName, String startRowKey,
                          String stopRowKey);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName, byte[] startRowKey,
                          byte[] stopRowKey);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName,
                          FilterList filterList);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName, Scan scan);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName, byte[] startRowKey, byte[] stopRowKey, FilterList filterList);

    /**
     * scanner.
     */
    ResultScanner scanner(Connection connection, String tableName, String startRowKey,
                          String stopRowKey, FilterList filterList);

    /**
     * existsRow.
     */
    boolean existsRow(Connection connection, String tableName, String row);

    /**
     * getRow.
     */
    Result getRow(Connection connection, String tableName, String row,
                  FilterList filterList);

    /**
     * getRow.
     */
    Result getRow(Connection connection, String tableName, Get get);

    /**
     * getRow.
     */
    Result getRow(Connection connection, String tableName, String row);

    /**
     * getRow.
     */
    Result getRow(Connection connection, String tableName, String row, byte[] column,
                  byte[] qualifier);

    /**
     * getRows.
     *
     * @param tableName tableName
     * @param rows rows
     * @param filterList filterList
     * @return Result
     */
    Result[] getRows(Connection connection, String tableName, List<String> rows,
                     FilterList filterList);

    /**
     * getRows.
     *
     * @param tableName tableName
     * @param rows rows
     * @return Result
     */
    Result[] getRows(Connection connection, String tableName, List<String> rows);

    /**
     * 利用Hbase自增列值方式 对 列值进行累加 num
     * @param connection
     * @param tableName
     * @param row
     * @param columnFamily
     * @param qualifier
     * @param num 要累加的值
     * @return
     */
    long incrementColumnValue(Connection connection, String tableName, String row, byte[] columnFamily, byte[] qualifier, int num);

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
    boolean putRow(Connection connection, String tableName, String row,
                   String columnFamily,
                   String qualifier, String data);

    boolean putRow(Connection connection, String tableName, Put put);

    /**
     * putRows.
     *
     * @param tableName tableName
     * @param puts puts
     * @return success of failed
     */
    boolean putRows(Connection connection, String tableName, List<Put> puts);
}
