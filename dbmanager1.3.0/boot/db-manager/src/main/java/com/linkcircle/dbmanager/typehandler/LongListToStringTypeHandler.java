package com.linkcircle.dbmanager.typehandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/11 10:31
 */
public class LongListToStringTypeHandler extends BaseTypeHandler<List<Long>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i,parameter.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if(StringUtils.isEmpty(value)){
            return new ArrayList<>();
        }
        List<Long> list = Arrays.stream(value.split(",")).map(Long::parseLong).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if(StringUtils.isEmpty(value)){
            return new ArrayList<>();
        }
        List<Long> list = Arrays.stream(value.split(",")).map(Long::parseLong).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if(StringUtils.isEmpty(value)){
            return new ArrayList<>();
        }
        List<Long> list = Arrays.stream(value.split(",")).map(Long::parseLong).collect(Collectors.toList());
        return list;
    }
}
