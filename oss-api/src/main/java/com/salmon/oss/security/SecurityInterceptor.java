package com.salmon.oss.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.common.utils.JsonUtils;
import com.salmon.oss.core.authmgr.AuthService;
import com.salmon.oss.core.authmgr.model.TokenInfo;
import com.salmon.oss.core.usermgr.UserService;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


@Component
public class SecurityInterceptor extends HandlerInterceptorAdapter {


  @Autowired
  private AuthService authService;

  @Autowired
  private UserService userService;

  private Cache<String, UserInfo> userInfoCache =
          CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String uri = request.getRequestURI();
    if (uri.equals("/login") || uri.contains("swagger")) {
      return true;
    }
    response.setCharacterEncoding("UTF-8");
    String token;
    HttpSession session = request.getSession();
    if (session.getAttribute(ContextUtil.SESSION_KEY) != null) {
      //获取登录人的 token，默认和用户名一致
      token = ((UserInfo)session.getAttribute(ContextUtil.SESSION_KEY)).getUserId();
    } else {
      //Authorization: Bearer xxxxx
      //如果从session中获取不到，就从 消息头中获取， 解决用户不用登录来调用服务
      token = request.getHeader("Authorization");
      if(!StringUtils.isEmpty(token)) {
        String[] auth = StringUtils.split(token, " ");
        if(auth.length == 2) {
          if("Bearer".equalsIgnoreCase(auth[0])) {
            token = auth[1];
          } else {
              buildResult(412, "授权前缀必须为 Bearer 类型", response);
            return false;
          }
        } else {
          buildResult(412, "请确认授权信息", response);
        }
      }
    }
    TokenInfo tokenInfo = authService.getTokenInfo(token);
    if (tokenInfo == null) {
      //String url = "/login";
      //response.sendRedirect(url);
      buildResult(403,"无权访问，需要授权后才能访问", response);
      return false;
    }
    UserInfo userInfo = userInfoCache.getIfPresent(tokenInfo.getToken());
    if (userInfo == null) {
      userInfo = userService.getUserInfo(token);
      if (userInfo == null) {
        userInfo = new UserInfo();
        userInfo.setUserId(token);
        userInfo.setUserName("NOT_EXIST_USER");
        userInfo.setDetail("a temporary visitor");
        userInfo.setSystemRole(SystemRole.VISITER);
      }
      userInfoCache.put(tokenInfo.getToken(), userInfo);
    }
    ContextUtil.setCurrentUser(userInfo);
    return true;
  }


  private void buildResult(int code, String message, HttpServletResponse response) throws Exception {
    response.setStatus(code);
    String result = JsonUtils.toJson(ApiResponse.ofMessage(code,message));
    response.setContentType("application/json;charset=UTF-8");
    PrintWriter out  = response.getWriter();
    out.println(result);
    out.flush();
    out.close();
  }

}
