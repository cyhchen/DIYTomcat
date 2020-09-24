package cn.how2j.diytomcat.test;

import cn.how2j.diytomcat.util.MiniBrowser;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTomcat {
	private static int port = 18080;
	private static String ip = "127.0.0.1";
	
	@BeforeClass
	public static void beforeClass(){
		//查看tomcat是否已经启动
		if(NetUtil.isUsableLocalPort(port)){
			System.out.println("请先开启位于端口"+port+"的Tomcat");
			return;
		}else{
			System.out.println("检测到端口号"+port+"已经启动，开始单元测试......");
		}
	}
	
	private String getContentString(String uri){
		String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
		String content = MiniBrowser.getContentString(url);
		return content;
	}
	
	private String getHttpString(String uri){
		String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
		String http = MiniBrowser.getHttpString(url);
		return http;
	}
	
	private byte[] getContentBytes(String uri){
		return getContentBytes(uri, false);
	}
	
	private byte[] getContentBytes(String uri, boolean flag){
		String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
		byte[] http = MiniBrowser.getContentBytes(url, flag);
		return http;
	}
	
	@Test
	public void test404(){
		String res = getHttpString("/a/not_exist.html");
		System.out.println(res);
		Assert.assertTrue(res.contains("HTTP/1.1 404 Not Found"));
	}
	
	@Test
	public void test500(){
		String res = getHttpString("/500.html");
		System.out.println(res);
		Assert.assertTrue(res.contains("HTTP/1.1 500 Internet Server Error"));
	}
	
	@Test
	public void testHelloTomcat() throws InterruptedException {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20,20,60, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(10));
		TimeInterval timeInterval = DateUtil.timer();
		for(int i = 0;i < 3;i++){
			threadPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					getContentString("/timeConsume.html");
				}
			});
		}
		threadPoolExecutor.shutdown();
		threadPoolExecutor.awaitTermination(1,TimeUnit.HOURS);
		long duration = timeInterval.intervalMs();
		Assert.assertTrue(duration < 3000);
	}	
	
	@Test
	public void testIndex(){
		String html = getContentString("/a");
		Assert.assertEquals("Hello DIY Tomcat from index.html@a", html);
	} 
	
	@Test
	public void testBIndex(){
		String html = getContentString("/");
		Assert.assertEquals("Hello DIY Tomcat from index.html@b", html);
	}
	
	@Test
	public void testMimeType(){
		String txt = getHttpString("/a/a.txt");
		System.out.println(txt);
		Assert.assertTrue(txt.contains("Context -Type: text/plain"));
	}
	
	@Test
	public void testPNG(){
		byte[] bytes = getContentBytes("/logo.png");
		int length = 1672;
		Assert.assertEquals(length, bytes.length);
	}
	
	@Test
	public void testPDF(){
		String uri = "/etf.pdf";
		String url = StrUtil.format("http://{}:{}{}",ip, port, uri);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long length = HttpUtil.download(url, baos, true);
		int expectlength = 3590775;
		Assert.assertEquals(expectlength, length);
	}
}
