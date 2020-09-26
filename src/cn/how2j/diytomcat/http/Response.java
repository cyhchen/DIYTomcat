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

public class Response extends BaseResponse {
	
	private StringWriter stringWriter;
	private PrintWriter writer;
	private String contextType;
	private byte[] body;
	private int status;
	
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

	public void setStatus(int num){
		this.status = num;
	}

	public int getStatus() {
		return this.status;
	}
}
