<%@page import="com.mcore.mybible.services.webservices.rest.utilities.AuditUtils"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Downloads Log (Fail File)</title>
</head>
<body>
<pre><%=AuditUtils.getInstance().displayFailDownloadFile() %></pre>
</body>
</html>