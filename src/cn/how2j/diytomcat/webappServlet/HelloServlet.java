package cn.how2j.diytomcat.webappServlet;

import cn.hutool.log.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try{
            LogFactory.get().error("response is:"+response);
            response.getWriter().println("Hello DIY Tomcat from HelloServlet");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
