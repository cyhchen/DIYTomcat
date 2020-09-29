package cn.how2j.diytomcat.util;

import cn.how2j.diytomcat.http.StandardSession;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

public class SessionManager {
    public static Map<String, StandardSession> map = new HashMap<>();
    public static int defaultTime = getTimeout();
    private static int number = 0;

    static {
        checkOutdatedSessionThread();
    }

    public static synchronized String generateSessionId(){
        String res = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        res = new String(bytes);
        res = SecureUtil.md5(res);
        res = res.toUpperCase();
        return res;
    }

    public static StandardSession getSession(String sessionId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        LogFactory.get().error("getSeesion number is: "+ number);
        number++;
        LogFactory.get().error("3. SessionId is: " + sessionId);
        if(map.containsKey(sessionId)){
            StandardSession cur = map.get(sessionId);
            cur.setLastAccessTime(System.currentTimeMillis());
            createCookie(cur, httpServletRequest, httpServletResponse);
            return map.get(sessionId);
        }else{
            return newSession(httpServletRequest, httpServletResponse);
        }
    }

    private static StandardSession newSession(HttpServletRequest request, HttpServletResponse response){
        String id = generateSessionId();
        LogFactory.get().error("1. id is: " + id);
        ServletContext servletContext = request.getServletContext();
        StandardSession s = new StandardSession(id, servletContext);
        s.setLastAccessTime(System.currentTimeMillis());
        s.setMaxInactiveInterval(defaultTime);
        map.put(id, s);
        createCookie(s, request, response);
        return s;
    }

    private static void createCookie(StandardSession standardSession, HttpServletRequest request, HttpServletResponse response){
        Cookie cookie = new Cookie("JSESSIONID",standardSession.getId());
        LogFactory.get().error("2. cookie Id is: " + standardSession.getId());
        cookie.setMaxAge(standardSession.getMaxInactiveInterval());
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
        LogFactory.get().error("6. cookie is: " + cookie.getName() + " : " + cookie.getValue());
    }

    public static int getTimeout(){
        File file = Constant.webXmlFile;
        String xml = FileUtil.readUtf8String(file);
        Document d = Jsoup.parse(xml);
        Element e = d.select("session-config").first();
        Element e_c = e.select("session-timeout").first();
        String txt = e_c.text();
        if(txt == null){
            return 30;
        }else{
            return Convert.toInt(txt);
        }
    }

    private static void checkOutdatedSessionThread(){
        new Thread(){
            public void run(){
                while(true) {
                    checkOutdatedSession();
                    try {
                        ThreadUtil.sleep(30 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static void checkOutdatedSession(){
        List<String> outdated = new ArrayList<>();
        Set<String> sets = map.keySet();
        for(String i : sets){
            StandardSession standardSession = map.get(i);
            if((System.currentTimeMillis() - standardSession.getCreationTime())*1000 > standardSession.getMaxInactiveInterval()){
                outdated.add(i);
            }
        }
        for(String i : outdated){
            map.remove(i);
        }
    }



}
