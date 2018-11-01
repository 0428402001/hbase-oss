package com.salmon.oss.core.authmgr;

import java.util.Date;
import java.util.List;

import com.salmon.oss.core.BaseTest;
import com.salmon.oss.core.authmgr.model.ServiceAuth;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import org.junit.Test;

import javax.annotation.Resource;


public class AuthServiceTest extends BaseTest {

  @Resource
  private AuthService authService;

  @Test
  public void addToken() {
    TokenInfo tokenInfo = new TokenInfo("salmon");
    authService.addToken(tokenInfo);
  }

  @Test
  public void refreshToken() {
    List<TokenInfo> tokenInfos = authService.getTokenInfoByCreator("salmon");
    tokenInfos.forEach(tokenInfo -> {
      authService.refreshToken(tokenInfo.getToken());
    });
  }

  @Test
  public void deleteToken() {
    List<TokenInfo> tokenInfos = authService.getTokenInfoByCreator("salmon");
    if (tokenInfos.size() > 0) {
      authService.deleteToken(tokenInfos.get(0).getToken());
    }
  }

  @Test
  public void addAuth() {
    List<TokenInfo> tokenInfos = authService.getTokenInfoByCreator("salmon");
    if (tokenInfos.size() > 0) {
      ServiceAuth serviceAuth = new ServiceAuth();
      serviceAuth.setAuthTime(new Date());
      serviceAuth.setBucketName("testBucket");
      serviceAuth.setTargetToken(tokenInfos.get(0).getToken());
      authService.addAuth(serviceAuth);
    }
  }

  @Test
  public void deleteAuth() {
    List<TokenInfo> tokenInfos = authService.getTokenInfoByCreator("salmon");
    if (tokenInfos.size() > 0) {
      authService.deleteAuth("testBucket", tokenInfos.get(0).getToken());
    }
  }
}
