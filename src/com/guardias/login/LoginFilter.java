package com.guardias.login;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter("/LoginFilter")
public class LoginFilter implements Filter {
	
	private String pathToBeIgnored ="guest";

    /**
     * Default constructor. 
     */
    public LoginFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest request = (HttpServletRequest) req;
	    HttpServletResponse response = (HttpServletResponse) res;
	    HttpSession session = request.getSession();
	    String path = ((HttpServletRequest) request).getRequestURI();
	    if (!path.contains(pathToBeIgnored)  &&  // login.jsp img css js
	    			!path.contains("/js")   && 
	    					!path.contains("img") && 
	    						!path.contains("css") &&  
	    							!path.contains("/guest") &&
	    								!path.contains("/public") &&
	    									!path.contains("font") &&
	    									( session == null || session.getAttribute("User") == null )) {
	        response.sendRedirect(request.getContextPath() + "/login.jsp"); // No logged-in user found, so redirect to login page.
	    } else {
	        chain.doFilter(req, res); // Logged-in user found, so just continue request.
	    }
	}
	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
		 pathToBeIgnored = fConfig.getInitParameter("UrlLogin");
	}

}
