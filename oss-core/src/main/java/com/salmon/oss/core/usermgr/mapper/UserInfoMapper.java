package com.salmon.oss.core.usermgr.mapper;

import com.salmon.oss.core.usermgr.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserInfoMapper {

  void addUser(@Param("userInfo") UserInfo userInfo);

  int updateUserInfo(@Param("userId") String userId, @Param("password") String password,
                     @Param("detail") String detail);

  int deleteUser(@Param("userId") String userId);

  UserInfo getUserInfo(@Param("userId") String userId);

  UserInfo checkPassword(@Param("userName") String userName,
                         @Param("password") String password);

  UserInfo getUserInfoByName(@Param("userName") String userName);
}
