package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.catalina.Context;

import java.io.File;
import java.util.*;

public class ApplicationContext extends BaseServletContext{
    private Map<String, Object> attributesMap;
    private Context context;

    public ApplicationContext(Context context){
        this.context = context;
        this.attributesMap = new HashMap<>();
    }

    public void removeAttribute(String name){
        attributesMap.remove(name);
    }

    public void setAttributesMap(String name, Object obj){
        attributesMap.put(name, obj);
    }

    public Object getAttribute(String name){
        return attributesMap.get(name);
    }

    public Enumeration<String> getAttributeNames(){
        Set<String> sets = attributesMap.keySet();
        return Collections.enumeration(sets);
    }

    public String getRealPath(String path){
        return new File(context.getDocBase(), path).getAbsolutePath();
    }
}
