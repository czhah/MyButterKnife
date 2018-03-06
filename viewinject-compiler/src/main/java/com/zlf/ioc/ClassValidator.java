package com.zlf.ioc;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author chenzhuang
 * @time 2018/2/26 15:30
 * @class
 */

public class ClassValidator {

    static boolean isPrivate(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(Modifier.PRIVATE);
    }

    static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString()
                .substring(packageLen)
                .replace('.', '$');
    }
}
