package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.exception.WebConFigDuplicatedException;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ContextXMLUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.log.LogFactory;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Context {
	private String path;
	private String docBase;
	private static Logger logger = Logger.getLogger(Context.class);
	private File contextWebXmlFile;
	private Map<String, String> url_servletClassName;
	private Map<String, String> url_servletName;
	private Map<String, String> servletName_className;
	private Map<String, String> className_servletName;
	private File contextFile;


	public Context(String path, String docBase){
		TimeInterval t = DateUtil.timer();
		this.path = path;
		this.docBase = docBase;
		this.contextFile = new File(this.docBase, ContextXMLUtil.getWatchedResource());
		//logger.info("Deploying web application direction "+ this.docBase);
		LogFactory.get().info("Deployment  "+ this.docBase + " has finished in ??????" + ContextXMLUtil.getWatchedResource());
		this.url_servletName = new HashMap<>();
		this.url_servletClassName = new HashMap<>();
		this.servletName_className = new HashMap<>();
		this.className_servletName = new HashMap<>();
		deploy();
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

	private void parseServletMapping(Document d){
		// url_ServletName
		Elements mappingurlElements = d.select("servlet-mapping url-pattern");
		for (Element mappingurlElement : mappingurlElements) {
			String urlPattern = mappingurlElement.text();
			String servletName = mappingurlElement.parent().select("servlet-name").first().text();
			url_servletName.put(urlPattern, servletName);
		}
		// servletName_className / className_servletName
		Elements servletNameElements = d.select("servlet servlet-name");
		for (Element servletNameElement : servletNameElements) {
			String servletName = servletNameElement.text();
			String servletClass = servletNameElement.parent().select("servlet-class").first().text();
			servletName_className.put(servletName, servletClass);
			className_servletName.put(servletClass, servletName);
		}
		// url_servletClassName
		Set<String> urls = url_servletName.keySet();
		for (String url : urls) {
			String servletName = url_servletName.get(url);
			String servletClassName = servletName_className.get(servletName);
			url_servletClassName.put(url, servletClassName);
		}
	}

	private void checkDuplicated() throws WebConFigDuplicatedException{
		String xml = FileUtil.readUtf8String(this.contextFile);
		Document d = Jsoup.parse(xml);
		checkDuplicated(d, "servlet-mapping url-pattern", "url重复");
		checkDuplicated(d, "servlet servlet-class", "servlet类名重复");
		checkDuplicated(d, "servlet servlet-name", "servlet名称重复");
	}

	private void checkDuplicated(Document d, String mapping, String msg) throws WebConFigDuplicatedException{
		Elements es = d.select(mapping);
		List<String> lists = new ArrayList<>();
		for(Element e : es){
			lists.add(e.text());
		}
		Collections.sort(lists);
		for(int i = 0;i < lists.size()-2;i++){
			if(lists.get(i).equals(lists.get(i+1))){
				throw new WebConFigDuplicatedException(msg);
			}
		}
	}

	private void init(){
		if(!this.contextFile.exists()){
			LogFactory.get().error("this contextFile not exist");
			return;
		}
		try{
			checkDuplicated();
			String xml = FileUtil.readUtf8String(this.contextFile);
			Document d = Jsoup.parse(xml);
			parseServletMapping(d);
		}catch (WebConFigDuplicatedException e){
			e.printStackTrace();
		}
	}

	private void deploy(){
		TimeInterval timeInterval = DateUtil.timer();
		LogFactory.get().error("Deploying web application directory {}", this.docBase);
		LogFactory.get().error("this.contextFile = new File(this.docBase, ContextXMLUtil.getWatchedResource()) is :"+ this.docBase + ContextXMLUtil.getWatchedResource());
		init();
		LogFactory.get().error("Deployment of web application directory {} has finished in {} ms",this.getDocBase(),timeInterval.intervalMs());
	}

	public String getServletClassName(String url){
		return url_servletClassName.get(url);
	}
}
