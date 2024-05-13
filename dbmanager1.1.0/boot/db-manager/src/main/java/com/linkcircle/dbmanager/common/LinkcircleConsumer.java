package com.linkcircle.dbmanager.common;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/23 9:38
 */
@FunctionalInterface
public interface LinkcircleConsumer<T> {
    void accept(T t) throws Exception;
}
