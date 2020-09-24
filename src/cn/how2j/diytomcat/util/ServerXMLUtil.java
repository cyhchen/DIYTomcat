package cn.how2j.diytomcat.util;

import cn.how2j.diytomcat.catalina.Connector;
import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Engine;
import cn.how2j.diytomcat.catalina.Host;
import cn.how2j.diytomcat.catalina.Service;
import cn.hutool.core.io.FileUtil;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ServerXMLUtil {
	
	public static List<Connector> getConnector(Service service){
		String xml = FileUtil.readUtf8String(Constant.serverxml);
		Document d = Jsoup.parse(xml);
		Elements es = d.select("Connector");
		List<Connector> lists = new ArrayList<>();
		for(Element e : es){
			int port = Integer.valueOf(e.attr("port"));
			Connector connector = new Connector(service);
			connector.setPort(port);
			lists.add(connector);
		}
		return lists;
	}
	
	public static List<Context> getContext(){
		List<Context> list = new ArrayList<>();
		String xml = FileUtil.readUtf8String(Constant.serverxml);
		Document d = Jsoup.parse(xml);
		Elements es = d.select("Context");
		for(Element e : es){
			String path = e.attr("Path");
			String docBase = e.attr("docBase");
			Context context = new Context(path, docBase);
			list.add(context);
		}
		return list;
	}
	
	public static String getEngineDefaultHost(){
		String xml = FileUtil.readUtf8String(Constant.serverxml);
		Document d = Jsoup.parse(xml);
		Element e = d.select("Engine").first();
		return e.attr("defaultHost");
	}
	
	public static List<Host> getHosts(Engine e){
		List<Host> list = new ArrayList<>();
		String xml = FileUtil.readUtf8String(Constant.serverxml);
		Document d = Jsoup.parse(xml);
		Elements es = d.select("Host");
		for(Element ele : es){
			String name = ele.attr("name");
			Host host = new Host(name, e);
			list.add(host);
		}
		return list;
	}
	
	public static String getServiceName(){
		String xml = FileUtil.readUtf8String(Constant.serverxml);
		Document d = Jsoup.parse(xml);
		Element service = d.select("Service").first();
		return service.attr("name");
	}
	
}