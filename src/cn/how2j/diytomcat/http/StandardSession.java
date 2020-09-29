package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.util.SessionManager;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardSession implements HttpSession {
    private Map<String, Object> attributeMap;
    private String id;
    private long createTime;
    private long lastAccessTime;
    private int maxInactiveTime;
    private ServletContext servletContext;

    public StandardSession(String id, ServletContext servletContext){
        this.servletContext = servletContext;
        this.id = id;
        this.attributeMap = new ConcurrentHashMap<>();
        this.createTime = System.currentTimeMillis();
    }

    public void setLastAccessTime(long lastAccessTime){
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public long getCreationTime() {
        return this.createTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        this.maxInactiveTime = i;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveTime;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return this.attributeMap.get(s);
    }

    @Override
    public Object getValue(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributeMap.keySet());
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String s, Object o) {
        LogFactory.get().error("setAttribute is: " + s + " : " + o);
        this.attributeMap.put(s, o);
    }

    @Override
    public void putValue(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {
        this.attributeMap.remove(s);
    }

    @Override
    public void removeValue(String s) {

    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }
}
