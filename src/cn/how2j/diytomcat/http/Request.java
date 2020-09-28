package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Engine;
import cn.how2j.diytomcat.catalina.Service;
import cn.how2j.diytomcat.util.MiniBrowser;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Request extends BaseRequest {
	private Socket socket;
	private String requestString;
	private String uri;
	private Service service;
	private Context context;
	private String method;
	private Map<String, String[]> paramMap;
	
	public Request(Socket socket, Service service)throws IOException{
		this.socket = socket;
		this.service = service;
		this.paramMap = new HashMap<>();
		parseHttpRequest();
		if(StrUtil.isEmpty(requestString)){
			return;
		}
		parseUri();
		parseContext();
		parseMethod();
		parseParam();
		LogFactory.get().info("context has text is :" + context);			        		 
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
	public ServletContext getServletContext() {
		return this.context.getServletContext();
	}

	private void parseHttpRequest() throws IOException {
		InputStream is = this.socket.getInputStream();
		byte[] bytes = MiniBrowser.readBytes(is, false);
		requestString = new String(bytes, "utf-8");
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
			}
		}else if("POST".equals(method)){
			queryString = StrUtil.subAfter(queryString, "\r\n\r\n", false);
		}else{
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
	