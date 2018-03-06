package com.zlf.ioc;

import android.app.Activity;
import android.view.View;

/**
 * @author chenzhuang
 * @time 2018/2/26 15:03
 * @class
 */

public class ViewInjector {

    private static final String SUFFIX = "$$ViewInject";

    public static void injectView(Activity activity) {
        ViewInject proxyActivity = findProxyActivity(activity);
        proxyActivity.inject(activity, activity);
    }

    public static void injectView(Object object, View view) {
        ViewInject proxyActivity = findProxyActivity(object);
        proxyActivity.inject(object, view);
    }

    private static ViewInject findProxyActivity(Object activity) {
        try {
            Class aClass = activity.getClass();
            Class injectClass = Class.forName(aClass.getName() + SUFFIX);
            return (ViewInject) injectClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", activity.getClass().getSimpleName() + SUFFIX));
    }
}
