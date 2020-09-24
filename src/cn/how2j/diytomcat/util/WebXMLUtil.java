package cn.how2j.diytomcat.util;

import cn.how2j.diytomcat.catalina.Connector;
import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Service;
import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebXMLUtil {
	
	private static Map<String, String>mimeTypeMapping = new HashMap<>();
	
	public static String getWelcomeFile(Context context){
		String xml = FileUtil.readUtf8String(Constant.webXmlFile);
		Document d = Jsoup.parse(xml);
		Elements es = d.select("welcome-file");
		for(Element e : es){
			String welcomeFile = e.text();
			File file = new File(context.getDocBase(), welcomeFile);
			if(file.exists()){
				return file.getName();
			}
		}
		return "index.html";
	}
	
	public static synchronized String getMimeType(String extName){
		if(mimeTypeMapping.isEmpty()){
			initMimeType();
		}
		String mime = mimeTypeMapping.get(extName);
		if(mime == null){
			return "text/html";
		}
		return mime;
	}
	
	private static void initMimeType(){
		String textMime = FileUtil.readUtf8String(Constant.webXmlFile);
		Document d = Jsoup.parse(textMime);
		Elements es = d.select("mime-mapping");
		for(Element e:es){
			String extName = e.select("extension").first().text();
			String mime = e.select("mime-type").first().text();
			mimeTypeMapping.put(extName, mime);
		}
	}

}
