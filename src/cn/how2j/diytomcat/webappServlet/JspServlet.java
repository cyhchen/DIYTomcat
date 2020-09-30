package cn.how2j.diytomcat.webappServlet;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class JspServlet extends HttpServlet {
    private static final JspServlet instance = new JspServlet();

    private JspServlet(){

    }

    public static JspServlet getInstance(){
        return instance;
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;
        Context context = request.getContext();
        String uri = request.getUri();
        if ("/".equals(uri)) {
            uri = WebXMLUtil.getWelcomeFile(request.getContext());
        }
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(request.getRealPath(fileName));
        if (file.exists()) {
            String extName = FileUtil.extName(file);
            String mime = WebXMLUtil.getMimeType(extName);
            response.setContextType(mime);
            LogFactory.get().info("response type is : " + response.getContextType());

            byte[] fileContent = FileUtil.readBytes(file);
            response.setBody(fileContent);
            if (fileName.equals("timeConsume.html")) {
                ThreadUtil.sleep(1000);
            }
            response.setStatus(Constant.CODE_200);
        } else {
            LogFactory.get().error("404 not found");
            response.setStatus(Constant.CODE_404);
        }
    }
}
