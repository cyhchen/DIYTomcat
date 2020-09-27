package cn.how2j.diytomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;
    private String servletName;
    private Map<String, String> initParaments;

    public StandardServletConfig(ServletContext servletContext, String servletName, Map<String, String> initParaments){
        this.servletContext = servletContext;
        this.servletName = servletName;
        this.initParaments = initParaments;
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initParaments.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParaments.keySet());
    }
}
