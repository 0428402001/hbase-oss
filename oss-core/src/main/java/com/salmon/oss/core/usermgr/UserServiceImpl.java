package com.salmon.oss.core.usermgr;

import java.util.Date;

import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.authmgr.mapper.TokenInfoMapper;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import com.salmon.oss.core.usermgr.mapper.UserInfoMapper;
import com.salmon.oss.core.usermgr.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Transactional(readOnly = true)
@Service
@Slf4j
public class UserServiceImpl implements UserService {

  //过期时间 单位天
  private final static int LONG_EXPIRE_TIME = 36500;

  @Resource
  UserInfoMapper userInfoMapper;

  @Resource
  TokenInfoMapper tokenInfoMapper;

  @Override
  @Transactional
  public boolean addUser(UserInfo userInfo) {
    userInfoMapper.addUser(userInfo);
    Date date = new Date();
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setToken(userInfo.getUserId());
    tokenInfo.setActive(true);
    tokenInfo.setCreateTime(date);
    tokenInfo.setCreator(ConstantInfo.SYSTEM_USER_NAME);
    tokenInfo.setExpireTime(LONG_EXPIRE_TIME);
    tokenInfo.setRefreshTime(date);
    // 用户信息添加到token
    tokenInfoMapper.addToken(tokenInfo);
    return true;
  }

  @Override
  @Transactional
  public boolean updateUserInfo(String userId, String password, String detail) {
    userInfoMapper
        .updateUserInfo(userId,
            StringUtils.isEmpty(password) ? null : DigestUtils.md5DigestAsHex(password.getBytes()),
            StringUtils.isEmpty(detail) ? null : detail);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteUser(String userId) {
    userInfoMapper.deleteUser(userId);
    //删除令牌
    tokenInfoMapper.deleteToken(userId);
    return true;
  }

  @Override
  public UserInfo getUserInfo(String userId) {
    return userInfoMapper.getUserInfo(userId);
  }

  @Override
  public UserInfo checkPassword(String userName, String password) {
    return userInfoMapper.checkPassword(userName, password);
  }

  @Override
  public UserInfo getUserInfoByName(String userName) {
    return userInfoMapper.getUserInfoByName(userName);
  }
}
