package com.salmon.oss.sdk.impl;

import com.salmon.oss.common.OssHeaders;
import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.common.domain.ObjectMetaData;
import com.salmon.oss.common.domain.OssObject;
import com.salmon.oss.common.utils.JsonUtils;
import com.salmon.oss.sdk.bean.MethodInfo;
import com.salmon.oss.sdk.bean.ServerInfo;
import com.salmon.oss.sdk.interfaces.RestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

@Slf4j
public class RestHandlerImpl implements RestHandler {

    //临时路径
    private final static String TMP_DIR = System.getProperty("user.dir") + File.separator + "tmp";

    private ServerInfo serverInfo;

    private RestTemplate restTemplate;

    /**
     * 初始化服务器信息
     *
     * @param serverInfo 服务器信息
     */
    @Override
    public void init(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
/*        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        //请求工厂类是否应用缓冲请求正文内部，默认值为true，
        //当post或者put大文件的时候会造成内存溢出情况，设置为false将数据直接流入底层HttpURLConnection
        requestFactory.setBufferRequestBody(false);*/
        this.restTemplate = new RestTemplate();
    }

    /**
     * 调用RESTFUL API
     *
     * @param methodInfo 方法信息
     * @return 返回调用结果
     */
    @Override
    public Object invokeRest(MethodInfo methodInfo) throws IOException {
        //构造请求URL
        String url = this.serverInfo.getUrl() + methodInfo.getUrl();
        //设置 HttpEntity
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(methodInfo.getRequestParams(), buildHeader());
        try {
            ResponseEntity<ApiResponse> result = restTemplate.exchange(url, methodInfo.getMethod(), entity, ApiResponse.class, methodInfo.getPathParams());
            if (methodInfo.getReturnElementType().isAssignableFrom(ApiResponse.class)) {
                return result.getBody();
            } else {
                if (result.getBody().getData() != null) {
                    if (methodInfo.isList())
                        return JsonUtils.fromJsonList(methodInfo.getReturnElementType(), JsonUtils.toJson(result.getBody().getData()));
                    return JsonUtils.fromJson(methodInfo.getReturnElementType(), JsonUtils.toJson(result.getBody().getData()));
                } else {
                    if(result.getBody().getData() == null){
                        throw new IOException(result.getStatusCode().getReasonPhrase());
                    } else {
                        throw new IOException(result.getBody().getMessage());
                    }
                }
            }
        } catch (RestClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Object download(MethodInfo methodInfo) throws IOException {
        try {
            String url = this.serverInfo.getUrl() + methodInfo.getUrl();
            HttpHeaders headers = buildHeader();
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(headers);
            ResponseEntity<Resource> response = restTemplate.exchange(url, methodInfo.getMethod(), entity, Resource.class, methodInfo.getPathParams());
            Resource result = response.getBody();
            OssObject ossObject = new OssObject();
            ossObject.setContent(result.getInputStream());
            ossObject.setMetaData(buildMetaData(response));
            return ossObject;
        } catch (Exception e) {
           throw new IOException(e.getCause());
        }
    }

    @Override
    public Object upload(MethodInfo methodInfo) throws IOException {
        final String url = this.serverInfo.getUrl() + methodInfo.getUrl();
        MultiValueMap<String, Object> params = methodInfo.getRequestParams();
        HttpHeaders headers = buildHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final String key = (String) params.getFirst("key");
        if (!key.startsWith("/")) {
            throw new IOException("object key must start with /");
        }
        if(params.size() == 3) { // 三个参数
            String filePath = (String) params.getFirst("content");
            if(StringUtils.isEmpty(filePath)) {
                throw new IOException("file path is empty");
            }
            File upFile = new File(filePath);
            if(key.endsWith("/")) {
                params.remove("key");
                String upFileName = key + upFile.getName();
                params.add("key", upFileName);
            }
            FileSystemResource resource = new FileSystemResource(upFile);
            params.remove("content");
            params.add("content", resource);
            params.add("mediaType", filePath.substring(filePath.lastIndexOf(".") + 1));
        } else if(params.size() == 4) { //4 个参数
            byte []content = (byte[]) params.getFirst("content");
            if(content == null) {
                throw new IOException("update content is empty");
            }
            if(key.endsWith("/")){
                throw new IOException("key must contain file name");
            }
            params.remove("content");
            // ByteArrayResource类需要实现getFileName()方法返回一个文件名称
            ByteArrayResource resource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    String uploadFileName = key.substring(key.lastIndexOf("/") + 1);
                    return TMP_DIR + File.separator + uploadFileName;
                }
            };
            params.add("content", resource);
        }
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params,headers);
        ResponseEntity<ApiResponse> responseEntity = restTemplate.exchange(url, methodInfo.getMethod(),httpEntity, ApiResponse.class);
        return responseEntity.getBody();
    }

    private ObjectMetaData buildMetaData(ResponseEntity<Resource> response) throws IOException {
        ObjectMetaData metaData = new ObjectMetaData();
        HttpHeaders headers = response.getHeaders();
        metaData.setBucket(headers.getFirst(OssHeaders.COMMON_OBJ_BUCKET));//从头获取bucket信息
        //从头获取到 key的值，即上传的路径和文件 例如 /dir1/file1.jpg
        String key = URLDecoder.decode(headers.getFirst(OssHeaders.COMMON_OBJ_KEY),"UTF-8");
        metaData.setKey(key);
        metaData.setLastModifyTime(headers.getLastModified());
        //metaData.setMediaType(headers.getContentType().getSubtype());
        metaData.setMediaType(key.substring(key.lastIndexOf(".") + 1));
        metaData.setLength(Long.parseLong(headers.getFirst(OssHeaders.RESPONSE_OBJ_LENGTH)));
        return metaData;
    }

    private HttpHeaders buildHeader() {
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.serverInfo.getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
