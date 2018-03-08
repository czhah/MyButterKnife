package com.zlf.ioc;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * @author chenzhuang
 * @time 2018/3/8 17:43
 * @class
 */

public class FieldViewBinding {

    private final String name;
    private final TypeName type;
    private final boolean required;

    FieldViewBinding(String name, TypeName type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public ClassName getRawType() {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        }
        return (ClassName) type;
    }

    public boolean isRequired() {
        return required;
    }
}
