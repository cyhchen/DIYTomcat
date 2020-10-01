package cn.how2j.diytomcat.catalina;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

public class ApplicationFilterChain implements FilterChain {
    private Filter[] filters;
    private Servlet servlet;
    private int pos = 0;

    public ApplicationFilterChain(List<Filter> filterList, Servlet servlet){
        this.filters = filterList.toArray(new Filter[]{});
        this.servlet = servlet;
        this.pos = 0;
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if(this.pos < this.filters.length){
            Filter filter = this.filters[pos];
            pos++;
            filter.doFilter(servletRequest, servletResponse, this);
        }else{
            servlet.service(servletRequest, servletResponse);
        }
    }
}
