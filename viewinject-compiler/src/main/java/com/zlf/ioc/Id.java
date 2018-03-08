package com.zlf.ioc;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

/**
 * @author chenzhuang
 * @time 2018/3/8 15:34
 * @class
 */

public class Id {

    private static final ClassName ANDROID_R = ClassName.get("android", "R");

    final int value;
    final CodeBlock code;
    final boolean qualifed;

    public Id(int value) {
        this.value = value;
        this.code = CodeBlock.of("&L", value);
        this.qualifed = false;
    }


}
