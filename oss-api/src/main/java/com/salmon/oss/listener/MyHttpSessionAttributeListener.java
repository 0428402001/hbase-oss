package com.salmon.oss.listener;

import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.security.ContextUtil;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class MyHttpSessionAttributeListener implements HttpSessionListener{

    /**
     * Notification that a session was created.
     *
     * @param se the notification event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    /**
     * Notification that a session is about to be invalidated.
     *
     * @param se the notification event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        UserInfo userInfo = (UserInfo)se.getSession().getAttribute(ContextUtil.SESSION_KEY);
        if(userInfo != null) {
            ContextUtil.remove();
        }
    }
}
