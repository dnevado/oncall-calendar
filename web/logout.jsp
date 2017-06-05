<%

request.getSession().invalidate();
response.sendRedirect(request.getContextPath() + "/login.jsp");

%>