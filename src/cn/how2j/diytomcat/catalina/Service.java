package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.util.ServerXMLUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import java.util.List;
import java.util.logging.Logger;

public class Service {
	private String name;
	private Engine engine;
	private Server server;
	private List<Connector>connectors;
	
	public Service(Server server){
		//默认获取第一个service
		this.name = ServerXMLUtil.getServiceName();
		this.engine = new Engine(this);
		this.server = server;
		this.connectors = ServerXMLUtil.getConnector(this);
	}
	
	public void start(){
		init();
	}
	
	private void init(){
		TimeInterval time = DateUtil.timer();
		LogFactory.get().error("Connectors is:"+connectors.size());
		LogFactory.get().error("Connector1 is:"+connectors.get(0).getPort());
		for(Connector con : this.connectors){
			con.init();
		}
		LogFactory.get().error("Connector2 is:"+connectors.get(1).getPort());
		LogFactory.get().info("Initialization processed in {} ms",time.intervalMs());
		for(Connector con : this.connectors){
			con.start();
		}
	}
	
	public Engine getEngine(){
		return this.engine;
	}
	
	public Server getServer(){
		return this.server;
	}
}
