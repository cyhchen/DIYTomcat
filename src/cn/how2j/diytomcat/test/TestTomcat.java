package cn.how2j.diytomcat.test;

import cn.how2j.diytomcat.util.MiniBrowser;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
		String html = getContentString("/b/");
		Assert.assertEquals("Hello DIY Tomcat from index.html@b", html);
	}

	@Test
	public void testMimeType(){
		String txt = getHttpString("/a/a.txt");
		System.out.println(txt);
		Assert.assertTrue(txt.contains("Context-Type: text/plain"));
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

	@Test
	public void testhello() {
		String html = getContentString("/j2ee/hello");
		Assert.assertEquals(html,"Hello DIY Tomcat from HelloServlet");
	}

	@Test
	public void testJavawebHello() {
		String html1 = getContentString("/javaweb/hello");
		String html2 = getContentString("/javaweb/hello");
		Assert.assertEquals(html1,html2);
	}

	@Test
	public void testgetParam() {
		String uri = "/javaweb/param";
		String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
		Map<String,Object> params = new HashMap<>();
		params.put("name","meepo");
		String html = MiniBrowser.getContentString(url, params, true);
		Assert.assertEquals(html,"get name:meepo");
	}

	@Test
	public void testHead(){
		String uri = "/javaweb/header";
		String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
		String txt = MiniBrowser.getContentString(url);
		Assert.assertEquals("how2j mini brower / java1.8",txt);
	}

	@Test
	public void testsetCookie() {
		String html = getHttpString("/javaweb/setcookie");
		System.out.println("html is: "+html);
		Assert.assertTrue(html.contains("Set-Cookie:name=Gareen(cookie);Expires="));
	}

	@Test
	public void testgetCookie() throws IOException {
		String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getcookie");
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestProperty("Cookie","name=Gareen(cookie)");
		conn.connect();
		InputStream is = conn.getInputStream();
		String html = IoUtil.read(is, "utf-8");
		Assert.assertTrue(html.contains("name:Gareen(cookie)"));
	}


	@Test
	public void testSession() throws IOException {
		String jsessionid = getContentString("/javaweb/setsession");
		System.out.println("jsessionId is " + jsessionid);
		if(null!=jsessionid)
			jsessionid = jsessionid.trim();
		String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getsession");
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestProperty("Cookie","JSESSIONID="+jsessionid);
		conn.connect();
		InputStream is = conn.getInputStream();
		String html = IoUtil.read(is, "utf-8");
		System.out.println("html is: " + html);
		Assert.assertTrue(html.contains("jsession"));
	}

	@Test
	public void testsetJSP() {
		String html = getContentString("/javaweb/");
		System.out.println("html is: "+html);
		Assert.assertTrue(html.contains("hello jsp@javaweb"));
	}

	@Test
	public void testClientJump(){
		String http_servlet = getHttpString("/javaweb/clientjump");
		containAssert(http_servlet,"HTTP/1.1 302 Found");
	}

	@Test
	public void testServerJump(){
		String http_servlet = getHttpString("/javaweb/serverjump");
		System.out.println(http_servlet);
		containAssert(http_servlet, "jack");
	}

	private void containAssert(String html, String string) {
		boolean match = StrUtil.containsAny(html, string);
		Assert.assertTrue(match);
	}



}
