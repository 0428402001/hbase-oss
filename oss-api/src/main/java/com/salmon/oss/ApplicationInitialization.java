package com.salmon.oss;

import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.usermgr.UserService;
import com.salmon.oss.core.usermgr.model.SystemRole;
import com.salmon.oss.core.usermgr.model.UserInfo;
import com.salmon.oss.server.store.OssStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInitialization implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private OssStoreService ossStoreService;

    /**
     * 启动时创建管理员用户以及序列表，密码默认和用户名一致
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //创建管理员用户
        UserInfo userInfo = userService.getUserInfoByName(ConstantInfo.SYSTEM_USER_NAME);
        if(userInfo == null) {
            userService.addUser(new UserInfo(ConstantInfo.SYSTEM_USER_NAME, ConstantInfo.SYSTEM_USER_NAME,SystemRole.ROOT,"系统管理员"));
        }
        ossStoreService.createSeqTable();
    }
}
