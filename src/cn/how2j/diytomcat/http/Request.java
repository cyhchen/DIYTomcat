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

public class Request implements HttpServletRequest {
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


	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return new Cookie[0];
	}

	@Override
	public long getDateHeader(String s) {
		return 0;
	}

	@Override
	public String getHeader(String s) {
		return null;
	}

	@Override
	public Enumeration<String> getHeaders(String s) {
		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return null;
	}

	@Override
	public int getIntHeader(String s) {
		return 0;
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String s) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean b) {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
		return false;
	}

	@Override
	public void login(String s, String s1) throws ServletException {

	}

	@Override
	public void logout() throws ServletException {

	}

	@Override
	public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
		return null;
	}

	@Override
	public Part getPart(String s) throws IOException, IllegalStateException, ServletException {
		return null;
	}

	@Override
	public Object getAttribute(String s) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getParameter(String s) {
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	public String[] getParameterValues(String s) {
		return new String[0];
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public void setAttribute(String s, Object o) {

	}

	@Override
	public void removeAttribute(String s) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String s) {
		return null;
	}

	@Override
	public String getRealPath(String s) {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public AsyncContext startAsync() {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}
}
	