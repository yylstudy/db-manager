package com.linkcircle.dbmanager.common;

/**
 * @author yang.yonglian
 * @Description:
 * @date 2021/3/2
 */
@FunctionalInterface
public interface LinkcircleFunction<T, R> {
    R apply(T t) throws Exception;
}
