package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.Bootstrap;
import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ThreadPoolUtil;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Server {
	private static Logger log = Logger.getLogger(Bootstrap.class);
  	
	private Service service;
	public Server(){
		this.service = new Service(this);
	}
	
	public void start(){
		TimeInterval time = DateUtil.timer();
		init();
		LogFactory.get().info("Server startup in {} ms",time.intervalMs());
	}
	
    private void init(){
		service.start();
	}
//    private static void logJVM() {
////    	System.out.println("logJVM start!");
////        Map<String,String> infos = new LinkedHashMap<>();
////        infos.put("Server version", "How2J DiyTomcat/1.0.1");
////        infos.put("Server built", "2020-04-08 10:20:22");
////        infos.put("Server number", "1.0.1");
////        infos.put("OS Name\t", SystemUtil.get("os.name"));
////        infos.put("OS Version", SystemUtil.get("os.version"));
////        infos.put("Architecture", SystemUtil.get("os.arch"));
////        infos.put("Java Home", SystemUtil.get("java.home"));
////        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
////        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));
////
////        Set<String> keys = infos.keySet();
////        for (String key : keys) {
////            LogFactory.get().info(key+":\t\t" + infos.get(key));
////        }
////    }
    
}
