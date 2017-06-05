<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="com.guardias.Util"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>

<%@page import="com.guardias.database.SuscriptoresDBImpl"%>

<%



boolean returnValue = SuscriptoresDBImpl.AddSuscriptores(request.getParameter("email"));

MailingUtil.SendSuscriptionRegistration(request.getParameter("email"));

out.println(returnValue);
%>

