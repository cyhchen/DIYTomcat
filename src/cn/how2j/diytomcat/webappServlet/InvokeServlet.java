package cn.how2j.diytomcat.webappServlet;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.hutool.core.util.ReflectUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

public class InvokeServlet extends HttpServlet {
    private static final InvokeServlet instance = new InvokeServlet();

    public static InvokeServlet getInstance(){
        return instance;
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;
        String uri = request.getUri();
        Context context = request.getContext();
        String className = context.getServletClassName(uri);
        try {
            System.out.println("className is :"+className);
            Class<?> clazz = context.getWebAppClassLoader().loadClass(className);
            Object object = ReflectUtil.newInstance(clazz);
            ReflectUtil.invoke(object, "service", request, response);
            response.setStatus(Constant.CODE_200);
            System.out.println("servletClass:" + clazz);
            System.out.println("servletClass'classLoader:" + clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
