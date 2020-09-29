package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Engine;
import cn.how2j.diytomcat.catalina.Service;
import cn.how2j.diytomcat.util.MiniBrowser;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.*;

public class Request extends BaseRequest {
	private Socket socket;
	private String requestString;
	private String uri;
	private Service service;
	private Context context;
	private String method;
	private Map<String, String[]> paramMap;
	private Map<String, String> headMap;
	private Cookie[] cookies;
	private HttpSession session;

	public Request(Socket socket, Service service)throws IOException{
		this.socket = socket;
		this.service = service;
		this.paramMap = new HashMap<>();
		this.headMap = new HashMap<>();
		parseHttpRequest();
		if(StrUtil.isEmpty(requestString)){
			return;
		}
		parseUri();
		parseContext();
		parseMethod();
		parseParam();
		parseHead();
		parseCookies();
		if(!"/".equals(context.getPath())){
			uri = StrUtil.removePrefix(uri, context.getPath());
			if(StrUtil.isEmpty(uri)){
				uri = "/";
			}
		}
		LogFactory.get().info("Request final uri is:"+this.uri);
	}
	
	public String getUri(){
		return this.uri;
	}
	
	public String getRequestString(){
		return this.requestString;
	}

	public Context getContext() {
		return this.context;
	}

	public String getRealPath(String path){
		return this.context.getServletContext().getRealPath(path);
	}

	@Override
	public HttpSession getSession(){
		return this.session;
	}

	public void setHttpSession(HttpSession session){
		this.session = session;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context.getServletContext();
	}

	@Override
	public Cookie[] getCookies(){
		return this.cookies;
	}

	public String getSessionIdFromCookie(){
		for(Cookie i : this.cookies){
			LogFactory.get().error("5. cookies has : " + i.getName() + " : " + i.getValue());
			if("JSESSIONID".equals(i.getName())){
				return i.getValue();
			}
		}
		return null;
	}

	private void parseCookies(){
		List<Cookie> lists = new ArrayList<>();
		String value = this.headMap.get("cookie");
		if(value != null){
			String[] paras = StrUtil.split(value,";");
			for(String i : paras){
				if(StrUtil.isBlank(i)){
					continue;
				}
				String[] is = StrUtil.split(i,"=");
				String k = is[0].trim();
				String v = is[1].trim();
				Cookie cookie = new Cookie(k,v);
				lists.add(cookie);
			}
		}
		this.cookies = ArrayUtil.toArray(lists, Cookie.class);
	}

	private void parseHttpRequest() throws IOException {
		InputStream is = this.socket.getInputStream();
		byte[] bytes = MiniBrowser.readBytes(is, false);
		requestString = new String(bytes, "utf-8");
	}

	private void parseHead(){
		StringReader reader = new StringReader(this.requestString);
		List<String> list = new ArrayList<>();
		IoUtil.readLines(reader,list);
		for(int i = 1;i < list.size();i++){
			if(list.get(i).length() == 0){
				break;
			}
			LogFactory.get().error("strs is:" + i + "," + list.get(i));
			String[] strs = list.get(i).split(":");
			String name = strs[0].toLowerCase();
			String value = strs[1];
			headMap.put(name, value);
		}
	}
	
	private void parseUri(){
		if(StrUtil.isEmpty(this.requestString)){
			return;
		}
		String temp = StrUtil.subBetween(requestString," "," ");
		if(!StrUtil.contains(temp,"?")){
			uri = temp;
		}else{
			uri = StrUtil.subBefore(temp, "?",false);	
		}
	}
	
	private void parseContext(){
		Engine engine = service.getEngine();
		this.context = engine.getDefaultHost().getContext(this.uri);
		if(context != null){
			return;
		}
		
		String path = StrUtil.subBetween(this.uri, "/","/");
		if(path == null){
			path = "/";
		}else{
			path = "/" + path;
		}
		context = service.getEngine().getDefaultHost().getContext(path);
		if(context == null){
			context = service.getEngine().getDefaultHost().getContext("/");
		}
		LogFactory.get().info("contextPath is " + this.context.getPath());
	}

	private void parseParam(){
		String method = this.getMethod();
		String queryString = StrUtil.subBetween(this.requestString, " ", " ");
		if("GET".equals(method)){
			LogFactory.get().error("queryString1 is: "+queryString);
			if(StrUtil.contains(queryString, "?")){
				queryString = StrUtil.subAfter(queryString,"?", false);
			}else{
				queryString = null;
			}
		}else if("POST".equals(method)){
			queryString = StrUtil.subAfter(queryString, "\r\n\r\n", false);
		}
		if(queryString == null){
			return;
		}
		LogFactory.get().error("queryString2 is : " + queryString);
		String[] paramKeys = queryString.split("&");
		for(String i : paramKeys){
			LogFactory.get().error("queryString3 is: "+ queryString);
			String[] keys = i.split("=");
			LogFactory.get().error("keys0 is: "+ keys[0]);
			LogFactory.get().error("keys1 is: "+ keys[1]);
			String name = keys[0];
			String value = keys[1];
			String[] values = paramMap.get(name);
			if(values == null){
				paramMap.put(name, new String[]{value});
			}else{
				ArrayUtil.append(values, value);
				paramMap.put(name, values);
			}
		}
	}

	@Override
	public int getIntHeader(String s) {
		String value = headMap.get(s.toLowerCase());
		return Integer.valueOf(value);
	}

	@Override
	public String getHeader(String s){
		String value = headMap.get(s.toLowerCase());
		return value;
	}

	@Override
	public Enumeration getHeaderNames(){
		return Collections.enumeration(headMap.keySet());
	}

	public void parseMethod(){
		String str = StrUtil.subBefore(this.requestString, " ", false);
		this.method = str;
	}

	public String getMethod(){
		return this.method;
	}

	public String getParameter(String name) {
		String values[] = paramMap.get(name);
		if (null != values && 0 != values.length)
			return values[0];
		return null;
	}
	public Map getParameterMap() {
		return paramMap;
	}
	public Enumeration getParameterNames() {
		return Collections.enumeration(paramMap.keySet());
	}
	public String[] getParameterValues(String name) {
		return paramMap.get(name);
	}
}
	