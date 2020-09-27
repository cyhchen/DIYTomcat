package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.classloader.WebAppClassLoader;
import cn.how2j.diytomcat.exception.WebConFigDuplicatedException;
import cn.how2j.diytomcat.http.ApplicationContext;
import cn.how2j.diytomcat.http.StandardServletConfig;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ContextXMLUtil;
import cn.how2j.diytomcat.watcher.ContextFileChangeWatcher;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.log.LogFactory;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
	private WebAppClassLoader webAppClassLoader;
	private Host host;
	private boolean reloadable;
	private ContextFileChangeWatcher contextFileChangeWatcher;
	private ServletContext servletContext;
	private Map<Class<?>, HttpServlet> servletPool;
	private Map<String, Map<String, String>> servletName_initPrams;
	private List<String>loadLists;

	public Context(String path, String docBase, Host host, boolean reloadable){
		TimeInterval t = DateUtil.timer();
		this.path = path;
		this.docBase = docBase;
		this.host = host;
		this.reloadable = reloadable;
		this.contextFile = new File(this.docBase, ContextXMLUtil.getWatchedResource());
		//logger.info("Deploying web application direction "+ this.docBase);
		LogFactory.get().info("Deployment  "+ this.docBase + " has finished in ??????" + ContextXMLUtil.getWatchedResource());
		this.url_servletName = new HashMap<>();
		this.url_servletClassName = new HashMap<>();
		this.servletName_className = new HashMap<>();
		this.className_servletName = new HashMap<>();
		this.webAppClassLoader = new WebAppClassLoader(docBase, Thread.currentThread().getContextClassLoader());
		this.servletContext = new ApplicationContext(this);
		this.servletPool = new HashMap<>();
		this.servletName_initPrams = new HashMap<>();
		this.loadLists = new ArrayList<>();
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

	public boolean getReloadable(){
		return this.reloadable;
	}

	public void setReloadable(Boolean is){
		this.reloadable = is;
	}

	public WebAppClassLoader getWebAppClassLoader(){
		return this.webAppClassLoader;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public synchronized HttpServlet getServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException, ServletException, NoSuchMethodException, InvocationTargetException {
		HttpServlet httpServlet = servletPool.get(clazz);
		if(httpServlet != null){
			return httpServlet;
		}
		HttpServlet newHttpServlet = (HttpServlet) clazz.getConstructor().newInstance();
		String servletName = className_servletName.get(clazz.getName());
		Map<String, String> initmap = servletName_initPrams.get(clazz.getName());
		StandardServletConfig standardServletConfig = new StandardServletConfig(this.getServletContext(), servletName, initmap);
		newHttpServlet.init(standardServletConfig);
		servletPool.put(clazz, newHttpServlet);
		return newHttpServlet;
	}

	private void parseServletInitParams(Document d){
		Elements es = d.select("servlet-class");
		for(Element e : es){
			String className = e.text();
			Map<String,String> map = new HashMap<>();
			for(Element i : e.parents().select("init-param")){
				String name = i.select("param-name").text();
				String value = i.select("param-value").text();
				map.put(name, value);
			}
			this.servletName_initPrams.put(className, map);
		}
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
			parseServletInitParams(d);
			parseLoadOnStartUp(d);
			startUpServlet();
		}catch (WebConFigDuplicatedException e){
			e.printStackTrace();
		}
	}

	private void deploy(){
		TimeInterval timeInterval = DateUtil.timer();
		LogFactory.get().error("Deploying web application directory {}", this.docBase);
		LogFactory.get().error("this.contextFile = new File(this.docBase, ContextXMLUtil.getWatchedResource()) is :"+ this.docBase + ContextXMLUtil.getWatchedResource());
		init();
		if(reloadable){
			this.contextFileChangeWatcher = new ContextFileChangeWatcher(this);
			contextFileChangeWatcher.start();
		}
		LogFactory.get().error("Deployment of web application directory {} has finished in {} ms",this.getDocBase(),timeInterval.intervalMs());
	}

	private void parseLoadOnStartUp(Document d){
		Elements es = d.select("load-start");
		for(Element e : es){
			String className = e.parent().select("servlet-class").text();
			loadLists.add(className);
		}
	}

	private void startUpServlet(){
		try{
			for(String i : this.loadLists){
				Class<?> clazz = this.webAppClassLoader.loadClass(i);
				getServlet(clazz);
				LogFactory.get().error("clazz is: "+clazz);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public String getServletClassName(String url){
		return url_servletClassName.get(url);
	}

	public void stop(){
		webAppClassLoader.stop();
		contextFileChangeWatcher.stop();
		destroyServlets();
	}

	public void reload(){
		this.host.reload(this);
	}

	public void destroyServlets(){
		Collection<HttpServlet> c = servletPool.values();
		for(HttpServlet i : c){
			i.destroy();
		}
	}
}
