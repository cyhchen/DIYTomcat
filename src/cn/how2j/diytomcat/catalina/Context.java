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
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
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
	private Map<String, Map<String, String>> servletName_initPrams;
	private Map<Class<?>, HttpServlet> servletPool;

	private Map<String, List<String>> url_filterClassName;
	private Map<String, List<String>> url_filterName;
	private Map<String, String> filterName_className;
	private Map<String, String> className_filterName;
	private Map<String, Map<String, String>> filterName_initPrams;
	private Map<String, Filter> filterPool;

	private File contextFile;
	private WebAppClassLoader webAppClassLoader;
	private Host host;
	private boolean reloadable;
	private ContextFileChangeWatcher contextFileChangeWatcher;
	private ServletContext servletContext;
	private List<ServletContextListener> listeners;
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
		this.servletName_initPrams = new HashMap<>();

		this.url_filterName = new HashMap<>();
		this.url_filterClassName = new HashMap<>();
		this.filterName_className = new HashMap<>();
		this.className_filterName = new HashMap<>();
		this.filterName_initPrams = new HashMap<>();
		this.filterPool = new HashMap<>();

		this.webAppClassLoader = new WebAppClassLoader(docBase, Thread.currentThread().getContextClassLoader());
		this.servletContext = new ApplicationContext(this);
		this.servletPool = new HashMap<>();

		this.listeners = new ArrayList<>();
		this.loadLists = new ArrayList<>();
		deploy();
	}

	public void addListener(ServletContextListener servletContextListener){
		this.listeners.add(servletContextListener);
	}

	private void loadListeners(){
		try{
			if(!contextFile.exists()){
				return;
			}
			String xml = FileUtil.readUtf8String(contextFile);
			Document d = Jsoup.parse(xml);
			Elements es = d.select("listener listener-class");
			for(Element e : es) {
				String className = e.text();
				System.out.println("className is " + className);
				Class<?> clazz = this.getWebAppClassLoader().loadClass(className);
				ServletContextListener s = (ServletContextListener) clazz.newInstance();
				this.listeners.add(s);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void fireEvent(String type){
		ServletContextEvent servletContextEvent = new ServletContextEvent(this.servletContext);
		for(ServletContextListener s : this.listeners){
			if("init".equals(type)) {
				s.contextInitialized(servletContextEvent);
			}
			if("destroy".equals(type)){
				s.contextDestroyed(servletContextEvent);
			}
		}
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

	public boolean match(String pattern, String uri){
		if(StrUtil.equals(pattern, uri)){
			return true;
		}
		if(StrUtil.equals(pattern,"/*")){
			return true;
		}
		if(StrUtil.startWith(pattern, "/*.")){
			String p1 = StrUtil.subAfter(pattern, '.', false);
			String u1 = StrUtil.subAfter(uri, '.', false);
			if(StrUtil.equals(p1, u1)){
				return true;
			}
		}
		return false;
	}

	public List<Filter> getMatchedFilters(String uri){
		List<Filter> res = new LinkedList<>();
		Set<String> patterns = url_filterClassName.keySet();
		Set<String> matchPatterns = new HashSet<String>();
		for(String i : patterns){
			LogFactory.get().error("first——url is : " + i);
			if(match(i, uri)){
				matchPatterns.add(i);
			}
		}
		Set<String> matchFilterClassName = new HashSet<>();
		for(String i : matchPatterns){
			LogFactory.get().error("second——url is : " + i);
			LogFactory.get().error("first——className is : " + this.url_filterClassName.get(i));
			for(String j : this.url_filterClassName.get(i)){
				matchFilterClassName.add(j);
			}
		}
		for(String i : matchFilterClassName){
			res.add(this.filterPool.get(i));
		}
		return res;

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

	private void parseFilterInitParams(Document d){
		Elements es = d.select("filter-class");
		for(Element e : es){
			String className = e.text();
			Map<String,String> map = new HashMap<>();
			for(Element i : e.parents().select("init-param")){
				String name = i.select("param-name").text();
				String value = i.select("param-value").text();
				map.put(name, value);
			}
			this.filterName_initPrams.put(className, map);
		}
	}

	private void parseFilterMapping(Document d){

		Elements mappingurlElements = d.select("filter-mapping url-pattern");
		for (Element mappingurlElement : mappingurlElements) {
			String urlPattern = mappingurlElement.text();
			String filterName = mappingurlElement.parent().select("filter-name").first().text();
			List<String> tmp = url_filterName.get(urlPattern);
			if(tmp == null){
				tmp = new ArrayList<>();
				tmp.add(filterName);
				url_filterName.put(urlPattern, tmp);
			}else {
				tmp.add(filterName);
				url_filterName.put(urlPattern, tmp);
			}
		}

		Elements filterNameElements = d.select("filter filter-name");
		for (Element filterNameElement : filterNameElements) {
			String filterName = filterNameElement.text();
			String filterClass = filterNameElement.parent().select("filter-class").first().text();
			filterName_className.put(filterName, filterClass);
			className_filterName.put(filterClass, filterName);
		}

		Set<String> urls = url_filterName.keySet();
		for (String url : urls) {
			List<String> filterNames = url_filterName.get(url);
			List<String> tmp = url_filterClassName.get(url);
			if(tmp == null){
				tmp = new ArrayList<>();
			}
			for(String i : filterNames) {
				String filterClassName = filterName_className.get(i);
				tmp.add(filterClassName);
			}
			url_filterClassName.put(url, tmp);
		}
	}

	private void initFilter(){
		Set<String> sets = className_filterName.keySet();
		for(String i : sets){
			System.out.println("FilterName is " + i );
			try{
				Class clazz = this.getWebAppClassLoader().loadClass(i);
				Map<String,String> map = filterName_initPrams.get(i);
				String name = className_filterName.get(i);
				FilterConfig filterConfg = new StandardFilterConfig(this.servletContext, map, name);
				Filter filter = filterPool.get(i);
				if(filter == null){
					filter = (Filter) ReflectUtil.newInstance(clazz);
					filter.init(filterConfg);
					filterPool.put(i, filter);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
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
			fireEvent("init");
			checkDuplicated();
			String xml = FileUtil.readUtf8String(this.contextFile);
			Document d = Jsoup.parse(xml);
			parseServletMapping(d);
			parseServletInitParams(d);
			parseLoadOnStartUp(d);
			parseFilterMapping(d);
			parseFilterInitParams(d);
			initFilter();
			startUpServlet();
		}catch (WebConFigDuplicatedException e){
			e.printStackTrace();
		}
	}

	private void deploy(){
		TimeInterval timeInterval = DateUtil.timer();
		//LogFactory.get().error("Deploying web application directory {}", this.docBase);
		LogFactory.get().error("this.contextFile = new File(this.docBase, ContextXMLUtil.getWatchedResource()) is :"+ this.docBase + ContextXMLUtil.getWatchedResource());
		loadListeners();
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
		fireEvent("stop");
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
