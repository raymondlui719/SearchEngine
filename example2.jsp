<html>
<body>
<%
if(request.getParameter("text")!=null)
{
	out.println("You input "+request.getParameter("text"));
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>
