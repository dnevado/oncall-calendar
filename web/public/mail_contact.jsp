<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="com.guardias.Util"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>

<%@page import="com.guardias.database.SuscriptoresDBImpl"%>

<%


//data: 'email=' + $("#contact_email").val() + '&name=' + $("#contact_name").val() + '&comments=' + $("#contact_comments").val(),

StringBuilder _sb = new StringBuilder();


_sb.append("De:" + request.getParameter("name") + "<br/>");
_sb.append("Email:" + request.getParameter("email") + "<br/>");
_sb.append("Comentarios:" + request.getParameter("comments") + "<br/>");

String[] to_ = {"medoncalls.info@gmail.com"};

Util.sendFromGMail(to_, "Información de Contacto", _sb.toString(), "", "");

out.println("OK");
%>

