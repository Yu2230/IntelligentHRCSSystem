package com.yyds.hrcscommon.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yyds.hrcscommon.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自动填充
 */
@Slf4j
@Component
public class JwtAutoFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("执行insert自动填充");

        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            userId = "SYSTEM";
            log.warn("UserContext为空，使用默认用户ID: {}", userId);
        }

        Date now = new Date();

        this.strictInsertFill(metaObject, "createBy", String.class, userId);
        this.strictInsertFill(metaObject, "updateBy", String.class, userId);
        this.strictInsertFill(metaObject, "createTime", Date.class, now);
        this.strictInsertFill(metaObject, "updateTime", Date.class, now);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = UserContext.getCurrentUserId();

        this.strictUpdateFill(metaObject, "updateBy", String.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
