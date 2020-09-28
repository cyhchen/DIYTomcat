package cn.how2j.diytomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Response extends BaseResponse {
	
	private StringWriter stringWriter;
	private PrintWriter writer;
	private String contextType;
	private byte[] body;
	private int status;
	private List<Cookie> cookieList;

	public Response(){
		this.stringWriter = new StringWriter();
		this.writer = new PrintWriter(stringWriter);
		this.contextType = "text/html";
		this.cookieList = new ArrayList<>();
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

	@Override
	public void addCookie(Cookie cookie){
		cookieList.add(cookie);
	}

	public List<Cookie> getCookieList(){
		return this.cookieList;
	}

	public String getCookieHead(){
		if(this.cookieList == null){
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for(Cookie c : cookieList) {
			stringBuffer.append("\r\n");
			stringBuffer.append("Set-Cookie:");
			stringBuffer.append(c.getName() + "=" + c.getValue() + ";");
			String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
			SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
			if (c.getMaxAge() != -1) {
				stringBuffer.append("Expires=");
				Date now = new Date();
				Date expire = DateUtil.offset(now, DateField.MINUTE, c.getMaxAge());
				stringBuffer.append(sdf.format(expire));
				stringBuffer.append(";");
			}
		}
		return stringBuffer.toString();
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
