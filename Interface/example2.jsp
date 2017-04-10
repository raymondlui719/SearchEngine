
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Search Engine</title>

<link rel="stylesheet" type="text/css" href="stylesheet.css" />

</head>

<body>
<img src="img/logo.png" alt="logo" height="80" width="200"/>
	<div>
		
		<form id="searchForm" method="post">
		
		
       <input id="s" type="text" name="text" value=<%=request.getParameter("text")%> />
            
            <input type="submit" value="Submit" id="submitButton"/>
   		</form>
   	</div>

    <div id="page">
    <% 
		if(request.getParameter("text")!=null)
		{
		out.println("You input "+request.getParameter("text"));
		} else{
		out.println("You input nothing");
	}
	%>	
    	
   	</div>
</body>
</html>



