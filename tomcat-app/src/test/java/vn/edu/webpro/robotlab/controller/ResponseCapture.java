package vn.edu.webpro.robotlab.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.servlet.http.HttpServletResponse;

/** Response trong bộ nhớ để khóa hợp đồng JSON mà không cần database. */
final class ResponseCapture implements InvocationHandler {
    private int status = 200;
    private StringWriter body = new StringWriter();
    private PrintWriter writer = new PrintWriter(body);

    HttpServletResponse response() {
        return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HttpServletResponse.class}, this);
    }

    int status() { return status; }
    String body() { writer.flush(); return body.toString(); }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if ("setStatus".equals(method.getName())) {
            status = (Integer) args[0];
        } else if ("getWriter".equals(method.getName())) {
            return writer;
        }
        if (method.getReturnType() == boolean.class) {
            return false;
        }
        if (method.getReturnType() == int.class) {
            return 0;
        }
        return null;
    }
}
