package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.ThreadPoolUtil;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

public class Connector implements Runnable {
	private int port;
	private Service service;
	
	public Connector(Service service){
		this.service = service;
	}
	
	public Service getService(){
		return this.service;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public void init(){
		LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]",port);
	}
	
	public void start(){
		LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]",port);
		new Thread(this).start();
	}

	public void run() {
		try {
			LogFactory.get().error("new Thread is running");
			ServerSocket ss = new ServerSocket(this.port);
			while (true) {
				Socket s = ss.accept();
				Runnable r = new Runnable() {
					@Override
					public void run() {
						try {
							Request request = new Request(s, service);
							System.out.println("浏览器的输入信息： \r\n" + request.getRequestString());
							System.out.println("uri:" + request.getUri());
							Response response = new Response();
	                        HttpProcessor httpProcessor = new HttpProcessor();
							httpProcessor.execute(request, response, s);
						} catch (Exception e) {
							LogFactory.get().error(e);
							e.printStackTrace();
						} finally {
							try {
								if (!s.isClosed()) {
									s.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				ThreadPoolUtil.run(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
