package com.salmon.oss.controller;

import com.google.common.base.Splitter;
import com.salmon.oss.common.OssHeaders;
import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.common.domain.BucketInfo;
import com.salmon.oss.common.domain.ObjectListResult;
import com.salmon.oss.common.domain.OssObject;
import com.salmon.oss.common.domain.OssObjectSummary;
import com.salmon.oss.common.utils.MimeUtils;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.security.ContextUtil;
import com.salmon.oss.security.OperationAccessControl;
import com.salmon.oss.server.BucketService;
import com.salmon.oss.server.store.OssStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 对象存储服务管理， 实现目录创建， 文件上传、文件下载、删除 等
 */
@RestController
@RequestMapping("/oss/${oss.version}")
@Slf4j
@Api(tags = "对象存储服务", description = "对象存储服务操作类")
public class OssController {

  @Autowired
  private BucketService bucketService;

  @Autowired
  private OssStoreService ossStoreService;

  @Autowired
  private OperationAccessControl operationAccessControl;

  // 内存最大缓存文件大小 2M
  private final static long MAX_FILE_IN_MEMORY = 2 * 1024 * 1024;

  //读缓存区大小
  private final static int readBufferSize = 32 * 1024;

  //临时路径
  private final static String TMP_DIR = System.getProperty("user.dir") + File.separator + "tmp";

  /**
   * 构造方法，完成对临时目录的创建
   */
  public OssController() {
    File file = new File(TMP_DIR);
    file.mkdirs();
  }

  /**
   * 创建bucket
   * @param bucketName bucket name
   * @param detail bucket 描述信息
   * @return
   */
  @PostMapping("/bucket")
  @ApiOperation("创建Bucket")
  public ApiResponse createBucket(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucketName,
                                  @ApiParam("Bucket描述信息") @RequestParam(name = "detail", required = false, defaultValue = "") String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    //判断当前用户是否有权限创建 BUCKET
    if (!currentUser.getSystemRole().equals(SystemRole.VISITER)) {
      bucketService.addBucket(currentUser, bucketName, detail);
      try {
        ossStoreService.createBucketStore(bucketName);
      } catch (IOException ioe) {
        // 如果创建bucket失败，删除已经插入到数据库（mysql）中的 bucket 信息
        bucketService.deleteBucket(bucketName);
        return ApiResponse.ofMessage(50002, "create bucket error");
      }
      return new ApiResponse();
    }
    return ApiResponse.ofStatus(ApiResponse.Status.PERMISSION_DENIED);
  }

  /**
   * 删除Bucket
   * @param bucket bucket name
   */
  @DeleteMapping("/bucket/{bucket}")
  @ApiOperation("删除给定的Bucket")
  public ApiResponse deleteBucket(@ApiParam(value = "Bucket名称", required = true) @PathVariable("bucket") String bucket) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    // 判断当前用户是否有权对 bucket 进行删除
    if (operationAccessControl.checkBucketOwner(currentUser.getUserName(), bucket)) {
      try {
        ossStoreService.deleteBucketStore(bucket);
      } catch (IOException ioe) {
        return ApiResponse.ofMessage(50003, "delete bucket error");
      }
      bucketService.deleteBucket(bucket);
      return new ApiResponse();
    }
    return ApiResponse.ofStatus(ApiResponse.Status.PERMISSION_DENIED);
  }

  /**
   * 更新bucket信息
   * @param bucket bucket name
   * @param detail bucket detail
   * @return
   */
  @PutMapping("/bucket")
  @ApiOperation("更新Bucket")
  public ApiResponse updateBucket(
          @ApiParam(value = "Bucket名称", required = true) @RequestParam(name = "bucket") String bucket,
          @ApiParam("Bucket描述信息") @RequestParam(name = "detail", defaultValue = "", required = false) String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    BucketInfo bucketModel = bucketService.getBucketByName(bucket);
    if (operationAccessControl
        .checkBucketOwner(currentUser.getUserName(), bucketModel.getBucketName())) {
      bucketService.updateBucket(bucket, detail);
      return new ApiResponse();
    }
    return ApiResponse.ofStatus(ApiResponse.Status.PERMISSION_DENIED);
  }

  /**
   * 根据Bucket名称获取bucket 信息
   * @param bucket bucket name
   * @return
   */
  @GetMapping("/bucket/{bucket}")
  @ApiOperation("根据Bucket名称获取bucket信息")
  public ApiResponse getBucket(@ApiParam(value = "Bucket名称", required = true) @PathVariable(name = "bucket") String bucket) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    BucketInfo bucketInfo = bucketService.getBucketByName(bucket);
    if (operationAccessControl
        .checkPermission(currentUser.getUserId(), bucketInfo.getBucketName())) {
      return ApiResponse.ofSuccess(bucketInfo);
    }
    return ApiResponse.ofStatus(ApiResponse.Status.PERMISSION_DENIED);
  }

  /**
   * 获取当前登录人所拥有的bucket信息
   */
  @GetMapping("/bucket/list")
  @ApiOperation("获取当前登录人创建的bucket信息")
  public ApiResponse getBucket() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    return ApiResponse.ofSuccess(bucketService.getUserBuckets(currentUser.getUserId()));
  }

  /**
   * 上传文件 并创建相应的目录
   * @param bucket
   * @param key
   * @param mediaType
   * @param file
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @ApiOperation("上传文件")
  @RequestMapping(value = "/object/upload", method = {RequestMethod.PUT, RequestMethod.POST})
  public ApiResponse putObject(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                               @ApiParam(value = "Row Key", required = true) @RequestParam("key") String key,
                               @ApiParam(value = "文件类型", required = true) @RequestParam(value = "mediaType", required = false) String mediaType,
                               @ApiParam(value = "文件", required = true) @RequestParam(value = "content", required = false) MultipartFile file,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    // 检验某个TOKEN是否具有对bucket操作的权限, 用户的userId 和token值在创建时保持一致
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ApiResponse.ofMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
    }
    if (!key.startsWith("/")) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "object key must start with /");
    }

    Enumeration<String> headNames = request.getHeaderNames();
    Map<String, String> attrs = new HashMap<>();
    String contentEncoding = request.getHeader("content-encoding");
    if (contentEncoding != null) {
      attrs.put("content-encoding", contentEncoding);
    }
    while (headNames.hasMoreElements()) {
      String header = headNames.nextElement();
      if (header.startsWith(OssHeaders.COMMON_ATTR_PREFIX)) {
        attrs.put(header.replace(OssHeaders.COMMON_ATTR_PREFIX, ""), request.getHeader(header));
      }
    }
    ByteBuffer buffer = null;
    File distFile = null;
    try {
      //判断是否是创建目录
      if (key.endsWith("/")) { // 如果创建目录
        if (file != null) {
          response.setStatus(HttpStatus.BAD_REQUEST.value());
          file.getInputStream().close();
          return ApiResponse.ofMessage(400,"key is a dir");
        }
        ossStoreService.put(bucket, key, null, 0, mediaType, attrs);
        response.setStatus(HttpStatus.OK.value());
        return new ApiResponse();
      }
      if (file == null || file.getSize() == 0) { // 上传文件内容为空
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "object content could not be empty");
      }
      if (file.getSize() > MAX_FILE_IN_MEMORY) {
        // 缓存到本地目录
        distFile = new File(TMP_DIR + File.separator + UUID.randomUUID().toString());
        file.transferTo(distFile);
        file.getInputStream().close();
        buffer = new FileInputStream(distFile).getChannel().map(MapMode.READ_ONLY, 0, file.getSize());
      } else {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        org.apache.commons.io.IOUtils.copy(file.getInputStream(), outputStream);
        buffer = ByteBuffer.wrap(outputStream.toByteArray());
        file.getInputStream().close();
      }
      //保存文件
      ossStoreService.put(bucket, key, buffer, file.getSize(), mediaType, attrs);
      return new ApiResponse();
    } catch (IOException ioe) {
      log.error("",ioe);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      return ApiResponse.ofMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    } finally {
      if (buffer != null) {
        buffer.clear();
      }
      if (file != null) {
        try {
          file.getInputStream().close();
        } catch (Exception e) {
          //nothing to do
        }
      }
      if (distFile != null) {
        distFile.delete();
      }
    }
  }

  @GetMapping("/object/list")
  @ApiOperation("对象起止检索")
  public ApiResponse listObject(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                @ApiParam(value = "起止Row Key", required = true) @RequestParam("startKey") String startKey,
                                @ApiParam(value = "终止Row Key", required = true) @RequestParam("endKey") String endKey,
                                     HttpServletResponse response)
      throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ApiResponse.ofMessage(HttpStatus.FORBIDDEN.value(), "Permission denied");
    }
    if (startKey.compareTo(endKey) > 0) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return null;
    }
    ObjectListResult result = new ObjectListResult();
    List<OssObjectSummary> summaryList = ossStoreService.list(bucket, startKey, endKey);
    result.setBucket(bucket);
    if (summaryList.size() > 0) {
      result.setMaxKey(summaryList.get(summaryList.size() - 1).getKey());
      result.setMinKey(summaryList.get(0).getKey());
    }
    result.setObjectCount(summaryList.size());
    result.setObjectList(summaryList);
    return ApiResponse.ofSuccess(result);
  }

  /**
   * 获取 bucket
   * @param bucket
   * @param key
   * @param response
   * @return
   * @throws IOException
   */
  @GetMapping("/object/info")
  @ApiOperation("根据Row Key以及Bucket名称来检索数据")
  public ApiResponse getSummary(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                @ApiParam(value = "Row Key", required = true) @RequestParam("key") String key,
                                HttpServletResponse response)
      throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ApiResponse.ofMessage(HttpStatus.FORBIDDEN.value(),"Permission denied");
    }
    OssObjectSummary summary = ossStoreService.getSummary(bucket, key);
    if (summary == null) {
      response.setStatus(HttpStatus.NOT_FOUND.value());
    }
    return ApiResponse.ofSuccess(summary);
  }

  /**
   * 根据前缀进行过滤
   * @param bucket
   * @param dir
   * @param prefix
   * @param start
   * @param response
   * @return
   * @throws IOException
   */
  @GetMapping("/object/list/prefix")
  @ApiOperation("根据根据前缀进行过滤")
  public ApiResponse listObjectByPrefix(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                        @ApiParam(value = "目录名称", required = true) @RequestParam("dir") String dir,
                                        @ApiParam(value = "前缀", required = true) @RequestParam("prefix") String prefix,
                                        @ApiParam("起始row key") @RequestParam(value = "startKey", required = false, defaultValue = "") String start,
      HttpServletResponse response)
      throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ApiResponse.ofMessage(HttpStatus.FORBIDDEN.value(),"Permission denied");
    }
    if (!dir.startsWith("/") || !dir.endsWith("/")) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"dir must start with / and end with /");
    }
    if ("".equals(start) || start.equals("/")) {
      start = null;
    }
    if (start != null) {
      List<String> segs = StreamSupport.stream(Splitter
          .on("/")
          .trimResults()
          .omitEmptyStrings().split(start).spliterator(), false).collect(Collectors.toList());
      start = segs.get(segs.size() - 1);
    }
    ObjectListResult result = this.ossStoreService.listByPrefix(bucket, dir, prefix, start, 100);
    return ApiResponse.ofSuccess(result);
  }

  /**
   * 根据目录来检索数据
   * @param bucket
   * @param dir
   * @param start
   * @param response
   * @return
   * @throws Exception
   */
  @GetMapping("/object/list/dir")
  @ApiOperation("根据目录来检索数据")
  public ApiResponse listObjectByDir(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                     @ApiParam(value = "目录名称", required = true) @RequestParam("dir") String dir,
                                     @ApiParam("起始row key") @RequestParam(value = "startKey", required = false, defaultValue = "") String start,
      HttpServletResponse response)
      throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ApiResponse.ofMessage(HttpStatus.FORBIDDEN.value(),"Permission denied");
    }
    if (!dir.startsWith("/") || !dir.endsWith("/")) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"dir must start with / and end with /");
    }
    if ("".equals(start) || start.equals("/")) {
      start = null;
    }
    if (start != null) {
      List<String> segs = StreamSupport.stream(Splitter
          .on("/")
          .trimResults()
          .omitEmptyStrings().split(start).spliterator(), false).collect(Collectors.toList());
      start = segs.get(segs.size() - 1);
    }

    ObjectListResult result = this.ossStoreService.listDir(bucket, dir, start, 100);
    return ApiResponse.ofSuccess(result);
  }


  /**
   * 删除bucket中的对象
   * @param bucket bucket name
   * @param key row key
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/object/delete", method = {RequestMethod.POST,RequestMethod.DELETE})
  @ApiOperation("删除bucket中的对象")
  public ApiResponse deleteObject(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                  @ApiParam(value = "row key", required = true) @RequestParam("key") String key) throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      return  ApiResponse.ofStatus(ApiResponse.Status.PERMISSION_DENIED);
    }
    this.ossStoreService.deleteObject(bucket, key);
    return new ApiResponse();
  }

  /**
   * 下载文件
   * @param bucket bucket name
   * @param key row key
   * @throws IOException
   */
  @GetMapping("/object/content")
  @ApiOperation("下载存储的对象")
  public void getObject(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                        @ApiParam(value = "row key", required = true) @RequestParam("key") String key, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.getWriter().write("Permission denied");
      return;
    }
    OssObject object = this.ossStoreService.getObject(bucket, key);
    if (object == null) {
      response.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }
    response.setHeader(OssHeaders.COMMON_OBJ_BUCKET, bucket);
    response.setHeader(OssHeaders.COMMON_OBJ_KEY, key);
    response.setHeader(OssHeaders.RESPONSE_OBJ_LENGTH, "" + object.getMetaData().getLength());
    String iflastModify = request.getHeader("If-Modified-Since");
    String lastModify = object.getMetaData().getLastModifyTime() + "";
    response.setHeader("Last-Modified", lastModify);
    String contentEncoding = object.getMetaData().getContentEncoding();
    if (contentEncoding != null) {
      response.setHeader("content-encoding", contentEncoding);
    }
    if (iflastModify != null && iflastModify.equals(lastModify)) {
      response.setStatus(HttpStatus.NOT_MODIFIED.value());
      return;
    }
    // response.setHeader(OssHeaders.COMMON_OBJ_BUCKET, object.getMetaData().getBucket());
    String mediaType = MimeUtils.getFileMimeType(object.getMetaData().getMediaType());
    response.setContentType(mediaType);
    OutputStream outputStream = response.getOutputStream();
    InputStream inputStream = object.getContent();
    try {
      byte[] buffer = new byte[readBufferSize];
      int len = -1;
      while ((len = inputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      response.flushBuffer();
    } finally {
      inputStream.close();
      outputStream.close();
    }

  }

  @GetMapping("/object/download")
  @ApiOperation("下载存储的对象")
  public ResponseEntity<Object> getFile(@ApiParam(value = "Bucket名称", required = true) @RequestParam("bucket") String bucket,
                                         @ApiParam(value = "row key", required = true) @RequestParam("key") String key) throws IOException {

    UserInfo currentUser = ContextUtil.getCurrentUser();
    //校验权限
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    OssObject object = this.ossStoreService.getObject(bucket, key);
    if (object == null) { // 如果未能查询到要下载的文件
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    HttpHeaders headers = new HttpHeaders();
    Resource resource = new InputStreamResource(object.getContent());
    headers.add(OssHeaders.COMMON_OBJ_BUCKET, bucket);
    headers.add(OssHeaders.COMMON_OBJ_KEY, URLEncoder.encode(key,"UTF-8")); // key进行encode
    headers.add(OssHeaders.RESPONSE_OBJ_LENGTH, "" + object.getMetaData().getLength());
    String contentEncoding = object.getMetaData().getContentEncoding();
    if (contentEncoding != null) {
      headers.add("content-encoding", contentEncoding);
    }
    String fileName = object.getMetaData().getKey().substring(object.getMetaData().getKey().lastIndexOf("/") + 1);
    headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "UTF-8"));
    String mediaType = MimeUtils.getFileMimeType(object.getMetaData().getMediaType());
    return ResponseEntity.ok().headers(headers).lastModified(object.getMetaData().getLastModifyTime()).contentType(MediaType.parseMediaType(mediaType)).body(resource);
  }

}
