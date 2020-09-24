package cn.how2j.diytomcat.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

public class Response implements HttpServletResponse {
	
	private StringWriter stringWriter;
	private PrintWriter writer;
	private String contextType;
	private byte[] body;
	
	public Response(){
		this.stringWriter = new StringWriter();
		this.writer = new PrintWriter(stringWriter);
		this.contextType = "text/html";
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	public PrintWriter getWriter(){
		return this.writer;
	}

	@Override
	public void setCharacterEncoding(String s) {

	}

	@Override
	public void setContentLength(int i) {

	}

	@Override
	public void setContentType(String s) {

	}

	@Override
	public void setBufferSize(int i) {

	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {

	}

	@Override
	public void resetBuffer() {

	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public void setLocale(Locale locale) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}

	public String getContextType(){
		return this.contextType;
	}
	
	public void setContextType(String contextType){
		this.contextType = contextType;
	}
	
	public void setBody(byte[] bytes){
		this.body = bytes;
	}
	
	public byte[] getBody()throws UnsupportedEncodingException {
		if(this.body == null){
			String content = stringWriter.toString();
			this.body = content.getBytes("utf-8");
		}
		return this.body;
	}


	@Override
	public void addCookie(Cookie cookie) {

	}

	@Override
	public boolean containsHeader(String s) {
		return false;
	}

	@Override
	public String encodeURL(String s) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String s) {
		return null;
	}

	@Override
	public String encodeUrl(String s) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String s) {
		return null;
	}

	@Override
	public void sendError(int i, String s) throws IOException {

	}

	@Override
	public void sendError(int i) throws IOException {

	}

	@Override
	public void sendRedirect(String s) throws IOException {

	}

	@Override
	public void setDateHeader(String s, long l) {

	}

	@Override
	public void addDateHeader(String s, long l) {

	}

	@Override
	public void setHeader(String s, String s1) {

	}

	@Override
	public void addHeader(String s, String s1) {

	}

	@Override
	public void setIntHeader(String s, int i) {

	}

	@Override
	public void addIntHeader(String s, int i) {

	}

	@Override
	public void setStatus(int i) {

	}

	@Override
	public void setStatus(int i, String s) {

	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public String getHeader(String s) {
		return null;
	}

	@Override
	public Collection<String> getHeaders(String s) {
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return null;
	}
}
