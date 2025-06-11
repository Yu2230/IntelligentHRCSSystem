package com.yyds.hrcscommon.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yyds.hrcscommon.utils.UserContext;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * 自动填充
 */
public class JwtAutoFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = UserContext.getCurrentUserId();
        Date now = new Date();

        this.strictInsertFill(metaObject, "createBy", String.class, userId);
        this.strictInsertFill(metaObject, "updateBy", String.class, userId);
        this.strictInsertFill(metaObject, "createTime", Date.class, now);
        this.strictInsertFill(metaObject, "updateTime", Date.class, now);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = UserContext.getCurrentUserId();

        this.strictUpdateFill(metaObject, "updateBy", String.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
