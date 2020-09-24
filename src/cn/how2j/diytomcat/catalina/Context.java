package cn.how2j.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import org.apache.log4j.Logger;

public class Context {
	private String path;
	private String docBase;
	private static Logger logger = Logger.getLogger(Context.class);
	
	public Context(String path, String docBase){
		TimeInterval t = DateUtil.timer();
		this.path = path;
		this.docBase = docBase;
		logger.info("Deploying web application direction "+ this.docBase);
		logger.info("Deployment of web application directory "+ this.docBase + " has finished in " + t.intervalMs() +" ms");
	}
    
	public String getPath(){
		return path;
	}
	
	public String getDocBase(){
		return docBase;
	}
	
	public void setDocBase(String docBase){
		this.docBase = docBase;
	}
}
