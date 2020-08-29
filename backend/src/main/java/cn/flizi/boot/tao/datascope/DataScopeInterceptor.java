package cn.flizi.boot.tao.datascope;

import cn.flizi.boot.tao.security.SecurityUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Log4j2
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataScopeInterceptor extends AbstractSqlParserHandler implements Interceptor {

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // SQL 解析
        this.sqlParser(metaObject);

        // 先判断是不是SELECT操作, 跳过存储过程
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (SqlCommandType.SELECT != mappedStatement.getSqlCommandType()
                || StatementType.CALLABLE == mappedStatement.getStatementType()) {
            return invocation.proceed();
        }

        // 针对定义了rowBounds，做为mapper接口方法的参数
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Object paramObj = boundSql.getParameterObject();

        // 判断参数里是否有 DataScope 对象
        DataScope dataScope = findDataScopeObject(paramObj);

        // 不需要场合
        if (ObjectUtil.isNull(dataScope)) {
            return invocation.proceed();
        }

        String originalSql = boundSql.getSql();
        Connection connection = (Connection) invocation.getArgs()[0];

        // 根据用户信息 填充 dataScope
        if (CollUtil.isEmpty(dataScope.getDeptIds())) {
            queryDataScope(dataScope, SecurityUtils.roles(), connection);
        }

        // 全部查询
        if (DataScopeTypeEnum.ALL == dataScope.getDataScopeTypeEnum()) {
            return invocation.proceed();
        }

        // 其余情况中分两类:
        if (DataScopeTypeEnum.OWN == dataScope.getDataScopeTypeEnum()) {
            // 利用 userId
            Integer userId = SecurityUtils.userId();
            String scopeName = dataScope.getScopeOwnName();
            originalSql = "select * from (" + originalSql + ") temp_data_scope where temp_data_scope." + scopeName + "=" + userId;
        } else {
            // 利用 deptId
            String join = CollectionUtil.join(dataScope.getDeptIds(), ",");
            String scopeName = dataScope.getScopeName();
            originalSql = "select * from (" + originalSql + ") temp_data_scope where temp_data_scope." + scopeName + " in (" + join + ")";

        }
        metaObject.setValue("delegate.boundSql.sql", originalSql);

        return invocation.proceed();
    }

    protected void queryDataScope(DataScope dataScope, List<String> roles, Connection connection) {
        String sql = "SELECT roles.scope_type, roles.scope FROM roles where role IN (?) order by scope_type desc limit 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, CollectionUtil.join(roles, ","));
            int type;
            String scope;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    type = resultSet.getInt(1);
                    scope = resultSet.getString(2);
                    dataScope.setDataScopeTypeEnum(DataScopeTypeEnum.valueOf(type));
                    List<Integer> list = new ObjectMapper().readValue(scope, new TypeReference<List<Integer>>() {
                    });
                    dataScope.setDeptIds(list);
                }
            }
        } catch (Exception e) {
            throw ExceptionUtils.mpe("Error: Method queryDataScope execution error of sql : \n %s \n", e, sql);
        }
    }

    private DataScope findDataScopeObject(Object parameterObj) {
        if (parameterObj instanceof DataScope) {
            return (DataScope) parameterObj;
        } else if (parameterObj instanceof Map) {
            for (Object val : ((Map<?, ?>) parameterObj).values()) {
                if (val instanceof DataScope) {
                    return (DataScope) val;
                }
            }
        }
        return null;
    }
}
