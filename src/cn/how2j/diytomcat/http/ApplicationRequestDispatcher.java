package cn.how2j.diytomcat.http;

import cn.how2j.diytomcat.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class ApplicationRequestDispatcher implements RequestDispatcher {

    private String uri;

    public ApplicationRequestDispatcher(String uri){
        if(!uri.startsWith("/")){
            this.uri = "/" + uri;
        }else{
            this.uri = uri;
        }
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;
        request.setUri(this.uri);
        HttpProcessor httpProcessor = new HttpProcessor();
        httpProcessor.execute(request, response, request.getSocket());
        request.setForward(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }
}
