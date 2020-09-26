package cn.how2j.diytomcat;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Engine;
import cn.how2j.diytomcat.catalina.Host;
import cn.how2j.diytomcat.catalina.Server;
import cn.how2j.diytomcat.catalina.Service;
import cn.how2j.diytomcat.classloader.CommonClassLoader;
import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ServerXMLUtil;
import cn.how2j.diytomcat.util.ThreadPoolUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Bootstrap {
    
    public static void main(String[] args) throws Exception{
        CommonClassLoader commonClassLoader = new CommonClassLoader();

        Thread.currentThread().setContextClassLoader(commonClassLoader);

        String serverClassName = "cn.how2j.diytomcat.catalina.Server";

        Class<?> serverClazz = commonClassLoader.loadClass(serverClassName);

        Object serverObject = serverClazz.newInstance();

        Method m = serverClazz.getMethod("start");

        m.invoke(serverObject);
    }
    
}
