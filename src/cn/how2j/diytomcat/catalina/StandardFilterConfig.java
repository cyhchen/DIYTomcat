package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.http.StandardServletConfig;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class StandardFilterConfig implements FilterConfig {
    private ServletContext servletContext;
    private Map<String, String> initMap;
    private String filterName;

    public StandardFilterConfig(ServletContext servletContext, Map<String, String> map, String name){
        this.servletContext = servletContext;
        this.initMap = map;
        this.filterName = name;
        if(this.initMap == null){
            this.initMap = new HashMap<>();
        }
    }

    @Override
    public String getFilterName() {
        return this.filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return this.initMap.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initMap.keySet());
    }
}
