package com.zlf.ioc;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author chenzhuang
 * @time 2018/3/7 17:22
 * @class
 */

public class ViewInjectImpl {

    static final Map<Class<?>, Constructor<? extends Unbinder>> BINDINGS = new LinkedHashMap<>();

    public static void bind(Activity source) {
        View view = source.getWindow().getDecorView();
        createBinding(source, view);
    }

    private static Unbinder createBinding(Object target, View view) {
        Class<?> targetClass = target.getClass();
        Constructor<? extends Unbinder> constructor = findBindingConstructorForClass(targetClass);

        if(constructor == null) {
            return Unbinder.EMPTY;
        }
        try {
            return constructor.newInstance(target, view);
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to invoke "+ constructor, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke "+ constructor, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if(cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to invoke "+ constructor, e);
        }
    }

    private static Constructor<? extends Unbinder> findBindingConstructorForClass(Class<?> targetClass) {
        Constructor<? extends Unbinder> bindingCtor = BINDINGS.get(targetClass);
        if(bindingCtor != null) {
            return bindingCtor;
        }
        String name = targetClass.getName();

        try {
            Class<?> bindingClass = targetClass.getClassLoader().loadClass(name + "_ViewBinding");
            bindingCtor = (Constructor<? extends Unbinder>) bindingClass.getConstructor(targetClass, View.class);
        } catch (ClassNotFoundException e) {
            bindingCtor = findBindingConstructorForClass(targetClass.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for "+name, e);
        }
        return bindingCtor;
    }

}
