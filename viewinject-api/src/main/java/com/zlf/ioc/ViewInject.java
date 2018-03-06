package com.zlf.ioc;

/**
 * @author chenzhuang
 * @time 2018/2/26 15:02
 * @class
 */

public interface ViewInject<T> {
    void inject(T t, Object source);
}
