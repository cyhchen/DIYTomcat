package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ServerXMLUtil;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.how2j.diytomcat.watcher.WarFileWatcher;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
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
		scanWarsOnWebappsFolder();

		new WarFileWatcher(this).start();
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

	public void load(File file){
	    String path = file.getName();
	    if("ROOT".equals(path)){
	        path = "/";
        }else{
	        path = "/" + path;
        }
	    String docPath_tmp = file.getAbsolutePath();
	    String docPath = docPath_tmp;
	    //String docPath = docPath_tmp.replace('\\', '/');
	    System.out.println("war absolutePath is: " + docPath);
		System.out.println("war Path is " + path);
	    Context context = new Context(path, docPath, this, false);
	    LogFactory.get().error("new context is " + context.getPath() + "   " + context.getDocBase());
	    map.put(context.getPath(), context);
    }

	public void loadWar(File warFile) {
		String fileName = warFile.getName();
		String folderName = StrUtil.subBefore(fileName,".",true);
		//看看是否已经有对应的 Context了
		Context context= getContext("/"+folderName);
		if(null!=context)
			return;
		//先看是否已经有对应的文件夹
		File folder = new File(Constant.webappsFolder,folderName);
		if(folder.exists())
			return;
		//移动war文件，因为jar 命令只支持解压到当前目录下
		File tempWarFile = FileUtil.file(Constant.webappsFolder, folderName, fileName);
		File contextFolder = tempWarFile.getParentFile();
		contextFolder.mkdir();
		FileUtil.copyFile(warFile, tempWarFile);
		//解压
		String command = "jar xvf " + fileName;
//		System.out.println(command);
		Process p =RuntimeUtil.exec(null, contextFolder, command);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//解压之后删除临时war
		tempWarFile.delete();
		//然后创建新的 Context
		load(contextFolder);
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
	
	private void scanContextsOnWebappsFolder(){
        File[] files = Constant.webappsFolder.listFiles();
        for(File file : files){
            if(!file.isDirectory()){
                continue;
            }
            loadContext(file);
        }
    }

    private void scanWarsOnWebappsFolder(){
	    File[] files = Constant.webappsFolder.listFiles();
	    for(File file : files){
	        if(!file.getName().toLowerCase().endsWith(".war")){
	            continue;
            }
	        loadWar(file);
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
