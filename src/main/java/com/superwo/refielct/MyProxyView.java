package com.superwo.refielct;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangpengcheng on 2017/6/22.
 */
public class MyProxyView implements InvocationHandler {
    private Map<Object, Object> map = null;

    public static Object newInstance(Class[] interfaces) {
        return Proxy.newProxyInstance(MyProxyView.class.getClassLoader(), interfaces, new MyProxyView());
    }

    private MyProxyView() {
        this.map = new HashMap<Object, Object>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            String name = methodName.substring(methodName.indexOf("get") + 3);
            return map.get(name);
        } else if (methodName.startsWith("set")) {
            String name = methodName.substring(methodName.indexOf("set") + 3);
            map.put(name, args[0]);
            return null;
        } else if (methodName.startsWith("is")) {
            String name = methodName.substring(methodName.indexOf("is") + 2);
            return (map.get(name));
        }
        return result;
    }

}