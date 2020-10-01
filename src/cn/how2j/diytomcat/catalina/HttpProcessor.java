package cn.how2j.diytomcat.catalina;

import cn.how2j.diytomcat.http.Request;
import cn.how2j.diytomcat.http.Response;
import cn.how2j.diytomcat.util.Constant;
import cn.how2j.diytomcat.util.SessionManager;
import cn.how2j.diytomcat.util.WebXMLUtil;
import cn.how2j.diytomcat.webappServlet.DefaultServlet;
import cn.how2j.diytomcat.webappServlet.HelloServlet;
import cn.how2j.diytomcat.webappServlet.InvokeServlet;
import cn.how2j.diytomcat.webappServlet.JspServlet;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class HttpProcessor {

    public void execute(Request request, Response response, Socket s){
        try{
            getHttp(request, response);
            String uri = request.getUri();
            if (null == uri)
                return;
            Context context = request.getContext();
            LogFactory.get().error("1.1 context is "+context.getPath() + " " + context.getDocBase() + " uri " + request.getUri());
            String servletClassName = context.getServletClassName(uri);
            HttpServlet workingServlet;
            if(servletClassName != null){
                workingServlet = InvokeServlet.getInstance();
            }
            else if(uri.endsWith(".jsp")){
                workingServlet = JspServlet.getInstance();
            }
            else{
                workingServlet = DefaultServlet.getInstance();
            }
            List<Filter> lists = request.getContext().getMatchedFilters(request.getRequestURI());
            LogFactory.get().error("Filters has " + lists.size());
            FilterChain filterChain = new ApplicationFilterChain(lists, workingServlet);
            filterChain.doFilter(request, response);

            if(request.isForward()){
                return;
            }
            if(response.getStatus() == 200){
                //处理200
                handle200(s, response);
            }else if(response.getStatus() == 302){
                handle302(s, response);
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

    private void getHttp(Request request, Response response){
        String id = request.getSessionIdFromCookie();
        LogFactory.get().error("4. httprocessor is: " + id);
        HttpSession httpSession = SessionManager.getSession(id, request, response);
        request.setHttpSession(httpSession);
    }

    private static void handle200(Socket s, Response response) throws IOException {
        String contentType = response.getContextType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType, response.getCookieHead());
        byte[] head = headText.getBytes();

        byte[] body = response.getBody();

        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }

    protected static void handle302(Socket s, Response response) throws IOException{
        OutputStream os = s.getOutputStream();
        String redirectPath = response.getRedirect();
        String head_txt = Constant.response_head_302;
        String header = StrUtil.format(head_txt, redirectPath);
        byte[] bytes = header.getBytes("utf-8");
        os.write(bytes);
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
