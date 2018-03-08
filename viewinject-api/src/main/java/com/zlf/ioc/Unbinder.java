package com.zlf.ioc;

/**
 * @author chenzhuang
 * @time 2018/3/7 17:30
 * @class
 */

public interface Unbinder {

    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() { }
    };
}
