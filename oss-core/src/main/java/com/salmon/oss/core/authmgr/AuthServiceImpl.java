package com.salmon.oss.core.authmgr;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.salmon.oss.core.authmgr.mapper.ServiceAuthMapper;
import com.salmon.oss.core.authmgr.mapper.TokenInfoMapper;
import com.salmon.oss.core.authmgr.model.ServiceAuth;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Transactional(readOnly = true)
@Service
public class AuthServiceImpl implements AuthService {

  @Resource
  TokenInfoMapper tokenInfoMapper;
  @Resource
  ServiceAuthMapper serviceAuthMapper;

  @Override
  @Transactional
  public boolean addAuth(ServiceAuth auth) {
    serviceAuthMapper.addAuth(auth);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteAuth(String bucketName, String token) {
    serviceAuthMapper.deleteAuth(bucketName, token);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteAuthByBucket(String bucketName) {
    serviceAuthMapper.deleteAuthByBucket(bucketName);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteAuthByToken(String token) {
    serviceAuthMapper.deleteAuthByToken(token);
    return true;
  }

  @Override
  public ServiceAuth getServiceAuth(String bucketName, String token) {
    return serviceAuthMapper.getAuth(bucketName, token);
  }

  @Override
  @Transactional
  public boolean addToken(TokenInfo tokenInfo) {
    tokenInfoMapper.addToken(tokenInfo);
    return true;
  }


  @Override
  @Transactional
  public boolean updateToken(String token, int expireTime, boolean isActive) {
    tokenInfoMapper.updateToken(token, expireTime, isActive ? 1 : 0);
    return true;
  }

  @Override
  @Transactional
  public boolean refreshToken(String token) {
    tokenInfoMapper.refreshToken(token, new Date());
    return true;
  }

  @Override
  @Transactional
  public boolean deleteToken(String token) {
    tokenInfoMapper.deleteToken(token);
    //删除权限
    serviceAuthMapper.deleteAuthByToken(token);
    return true;
  }

  @Override
  public boolean checkToken(String token) {
    TokenInfo tokenInfo = tokenInfoMapper.getTokenInfo(token);
    if (tokenInfo == null) {
      return false;
    }
    if (!tokenInfo.isActive()) {
      return false;
    }
    Date nowDate = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(tokenInfo.getRefreshTime());
    cal.add(Calendar.DATE, tokenInfo.getExpireTime());

    return nowDate.before(cal.getTime());
  }

  @Override
  public TokenInfo getTokenInfo(String token) {
    return tokenInfoMapper.getTokenInfo(token);
  }

  @Override
  public List<TokenInfo> getTokenInfoByCreator(String creator) {
    return tokenInfoMapper.getTokenInfoByCreator(creator);
  }
}
