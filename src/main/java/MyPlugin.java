import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * @useages: 拦截StatementHandler，使输出结果不超过50条
 * @author: yinweicheng
 * @date: 2019-02-13
 * @Copyright (c) 2019, lianjia.com All Rights Reserved
 **/
@Intercepts({@Signature(type = StatementHandler.class,
method = "prepare",
args = {Connection.class})})
public class MyPlugin implements Interceptor {
    //限制返回行数
    private int limit;
    //
    private String dbType;

    private static final String LMT_TABLE_NAME = "limit_Table_Nmae_xxx";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        while(metaObject.hasGetter("h")){
            Object object = metaObject.getValue("h");
            metaObject = SystemMetaObject.forObject(object);
        }
        while (metaObject.hasGetter("target")){
            Object object = metaObject.getValue("target");
            metaObject = SystemMetaObject.forObject(object);
        }
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");
        String limitSql;
        if("mysql".equals(this.dbType) &&sql.indexOf(LMT_TABLE_NAME)==-1){
            sql = sql.trim();
            limitSql = "select * from ("+ sql +")" + LMT_TABLE_NAME + "limit" +limit;
            metaObject.setValue("delegate.boundSql.sql",limitSql);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o,this);
    }

    @Override
    public void setProperties(Properties properties) {
        String strLimit = (String) properties.getProperty("limit", "50");
        this.limit = Integer.parseInt(strLimit);
        this.dbType = (String) properties.getProperty("dbType", "mysql");
    }
}
