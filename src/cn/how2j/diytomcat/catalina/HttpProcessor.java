package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.how2j.diytomcat.webappServlet.DefaultServlet;
import cn.how2j.diytomcat.webappServlet.HelloServlet;
import cn.how2j.diytomcat.webappServlet.InvokeServlet;
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
            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);
            if(servletClassName != null){
                InvokeServlet.getInstance().service(request, response);
            }
            else {
                DefaultServlet.getInstance().service(request, response);
            }
            if(response.getStatus() == 200){
                //处理200
                handle200(s, response);
            }else if(response.getStatus() == 404){
                handle404(s, uri);
            }

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
