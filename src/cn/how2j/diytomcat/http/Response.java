package cn.how2j.diytomcat.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class Response {
	
	private StringWriter stringWriter;
	private PrintWriter writer;
	private String contextType;
	private byte[] body;
	
	public Response(){
		this.stringWriter = new StringWriter();
		this.writer = new PrintWriter(stringWriter);
		this.contextType = "text/html";
	}
	
	public PrintWriter getWriter(){
		return this.writer;
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
	
	
	
}
