package com.salmon.oss.sdk.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器信息类
 */
@Data
@NoArgsConstructor
public class ServerInfo {
    private String url;
    private String token;
}
