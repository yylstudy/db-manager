package com.linkcircle.dbmanager.common;

import cn.hutool.core.date.DateUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/22 18:12
 */
public enum TimeRule {
    YYYYMMDD("1","yyyyMMdd","20\\d{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}"){
        @Override
        long getBetweenNow(Date date ) {
            return DateUtil.betweenDay(date,new Date(),true);
        }
    },
    YYYY_MM_DD("2","yyyy_MM_dd","20\\d{2}_[0-1]{1}[0-9]{1}_[0-3]{1}[0-9]{1}"){
        @Override
        long getBetweenNow(Date date ) {
            return DateUtil.betweenDay(date,new Date(),true);
        }
    },
    YYYYMM("3","yyyyMM","20\\d{2}[0-1]{1}[0-9]{1}"){
        @Override
        long getBetweenNow(Date date ) {
            return DateUtil.betweenMonth(date,new Date(),true);
        }
    },
    YYYY_MM("4","yyyy_MM","20\\d{2}_[0-1]{1}[0-9]{1}"){
        @Override
        long getBetweenNow(Date date ) {
            return DateUtil.betweenMonth(date,new Date(),true);
        }
    }
    ;

    private String code;
    private String regex;
    private String dateFormat;

    TimeRule(String code,String dateFormat, String regex) {
        this.code = code;
        this.regex = regex;
        this.dateFormat = dateFormat;
    }

    public static Map<String,TimeRule> map = new HashMap<>();
    static{
        for(TimeRule timeRule:values()){
            map.put(timeRule.code,timeRule);
        }
    }
    public static TimeRule getTimeRuleEnum(String code){
        return map.get(code);
    }
    public static boolean isMatch(String code,String tableName,int cleatTimeStart,Integer clearTimeEnd ) {
        try{
            TimeRule timeRule = getTimeRuleEnum(code);
            if(timeRule==null){
                throw new RuntimeException("无效的日期格式");
            }
            Pattern p = Pattern.compile(timeRule.regex);
            Matcher matcher = p.matcher(tableName);
            List<String> matchStr = new ArrayList<>();
            while (matcher.find()){
                matchStr.add(matcher.group());
            }
            if(matchStr.isEmpty()){
                return false;
            }
            if(matchStr.size()>1){
                throw new RuntimeException("表名"+tableName+"存在多个匹配时间规则的字符，请检查正则");
            }
            String dateStr = matchStr.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat(timeRule.dateFormat);
            Date date = sdf.parse(dateStr);
            long before = timeRule.getBetweenNow(date);
            return before<=cleatTimeStart&&before>=clearTimeEnd;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    abstract long getBetweenNow(Date date);
}
