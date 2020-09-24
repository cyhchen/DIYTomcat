package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.util.ServerXMLUtil;
import java.util.List;

public class Engine {
	private List<Host> list;
	private String defautHost;
	private Service service;
	
	public Engine(Service service){
		this.defautHost = ServerXMLUtil.getEngineDefaultHost();
		this.list = ServerXMLUtil.getHosts(this);
		this.service = service;
		checkDefault();
	}
	
	public Host getDefaultHost(){
		for(Host i : list){
			if(i.getName().equals(defautHost)){
				return i;
			}
		}
		System.out.println("defaultHost :" + defautHost);
		return null;
	}
	
	public Service getService(){
		return this.service;
	}
	
	private void checkDefault(){
		if(this.getDefaultHost() == null){
			throw new RuntimeException("default not exist");
		}
	}
	
	
}
