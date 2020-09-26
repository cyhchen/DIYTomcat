package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ServerXMLUtil;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.log.LogFactory;
import org.apache.log4j.Logger;

public class Host {
	private static Logger logger = Logger.getLogger(Host.class);
	
	private Engine engine;
	private String name;
	private Map<String, Context> map;
	
	public Host(String name, Engine engine){
		this.map = new ConcurrentHashMap<>();
		this.name = name;
		this.engine = engine;
		//scanContextsOnWebappsFolder();
		scanContextsInServerXML();
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Context getContext(String path){
		return this.map.get(path);
	}

	public void reload(Context context){
        LogFactory.get().info("Reloading Context with name [{}] has started", context.getPath());
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.getReloadable();
        context.stop();
        map.remove(path);
        Context newContext = new Context(path, docBase, this, reloadable);
        map.put(path, newContext);
        LogFactory.get().info("Reloading Context with name [{}] has completed", context.getPath());
    }
	
	private  void scanContextsOnWebappsFolder(){
        File[] files = Constant.webappsFolder.listFiles();
        for(File file : files){
            if(!file.isDirectory()){
                continue;
            }
            loadContext(file);
        }
    }
    
    private void scanContextsInServerXML(){
        List<Context> list = ServerXMLUtil.getContext(this);
        for(Context context : list){
            map.put(context.getPath(), context);
            logger.info("contextMap has Path:"+context.getPath()+" has docPath:"+ context.getDocBase());
        }
    }
    
    private void loadContext(File file){
        String path = file.getName();
        if("ROOT".equals(path)){
            path = "/";
        }else{
            path = "/" + path;
        }
        String docPath = file.getAbsolutePath();
        Context context = new Context(path, docPath, this, true);
        map.put(context.getPath(), context);
    }
}
