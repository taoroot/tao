package com.github.taoroot.tao.utils;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.servlet.annotation.HandlesTypes;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Java Int数组与MySQL String转换器
 * 比如[1,2,3] --> ",1,2,3,"
 */
public class IntegerToIntArrayHandler extends BaseTypeHandler<Integer[]> {

    private static final String splitCharset = ",";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Integer[] objects, JdbcType jdbcType) throws SQLException {
        String str = arrayToString(objects);
        ps.setString(i, str);
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        return stringToArray(str);
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        return stringToArray(str);
    }

    @Override
    public Integer[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        return stringToArray(str);
    }

    // --- private methods ---

    /**
     * Integer数组转String
     * 注：使用提前设定好的分隔符分割数组的每一项
     */
    private static String arrayToString(Integer[] array) {
        StringBuilder res = new StringBuilder();
        if (array != null && array.length > 0) {
            for (Object o : array) {
                res.append(splitCharset).append(o.toString());
            }
            res.append(splitCharset);
        }
        return res.length() > 0 ? res.toString() : null;
    }

       /**
     * 从String转Integer数组
     * 注：String是用分隔符分割的，使用String.split方法可以分解为数组
     */
    private static Integer[] stringToArray(String str) {
        List<Integer> list = new ArrayList<>();
        if (str != null) {
            String[] array = str.split(splitCharset);
            if (array.length > 0) {
                for (String o : array) {
                    if (o != null && o.length() > 0) {
                        list.add(Integer.parseInt(o));
                    }
                }
            }
        }
        return list.toArray(new Integer[0]);
    }
}
