package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.how2j.diytomcat.webappServlet.HelloServlet;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class HttpProcessor {

    public void execute(Request request, Response response, Socket s){
        try{
            String uri = request.getUri();
            if (null == uri)
                return;
            LogFactory.get().error("uri is              "+ uri);
            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);
            if(servletClassName != null){
                LogFactory.get().error("servletClassName is : " + servletClassName);
                Object servlet = ReflectUtil.newInstance(servletClassName);
                ReflectUtil.invoke(servlet, "doGet", request, response);
            }
            else {
                //处理500
                if ("/500.html".equals(uri)) {
                    throw new RuntimeException("this is a exception");
                }
                else {
                    if ("/".equals(uri)) {
                        uri = WebXMLUtil.getWelcomeFile(request.getContext());
                    }
                    String fileName = StrUtil.removePrefix(uri, "/");
                    File file = FileUtil.file(context.getDocBase(), fileName);
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
                    } else {
                        LogFactory.get().error("404 not found");
                        handle404(s, uri);
                        return;
                    }
                }
            }
            //处理200
            handle200(s, response);
        }catch (Exception e){
            e.printStackTrace();
            LogFactory.get().error(e);
            handle500(s, e);
        }finally {
            try{
                if(!s.isClosed()){
                    s.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    private static void handle200(Socket s, Response response) throws IOException {
        String contentType = response.getContextType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType);
        byte[] head = headText.getBytes();

        byte[] body = response.getBody();

        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }

    protected static void handle404(Socket s, String uri) throws IOException{
        OutputStream os = s.getOutputStream();
        String responseBody = StrUtil.format(Constant.textFormat_404, uri,uri);
        String responseHead = Constant.response_head_404;
        String response = responseHead + responseBody;
        byte[] bytes = response.getBytes("Utf-8");
        os.write(bytes);
    }

    protected static void handle500(Socket socket, Exception e){
        try {
            String errorHead = Constant.response_head_500;
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            StringBuilder stringBuilder = new StringBuilder();
            for (StackTraceElement s : stackTraceElements) {
                stringBuilder.append(s.toString());
            }
            String errorBody = StrUtil.format(Constant.testFormat_500,e.getMessage(),stringBuilder.toString());
            String errorText = errorHead + errorBody;
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(errorText.getBytes("utf-8"));
        }catch(IOException i){
            i.printStackTrace();
        }
    }
}
