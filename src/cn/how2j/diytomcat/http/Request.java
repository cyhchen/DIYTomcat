package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Engine;
import cn.how2j.diytomcat.catalina.Service;
import cn.how2j.diytomcat.util.MiniBrowser;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

public class Request extends BaseRequest {
	private Socket socket;
	private String requestString;
	private String uri;
	private Service service;
	private Context context;
	
	public Request(Socket socket, Service service)throws IOException{
		this.socket = socket;
		this.service = service;
		parseHttpRequest();
		if(StrUtil.isEmpty(requestString)){
			return;
		}
		parseUri();
		parseContext();
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
}
	