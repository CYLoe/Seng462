<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome to Day Trading Web Service</title>
</head>
<body>
	<h1>Welcome to Day Trading Web Service</h1>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		Password: <input type="password" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>ADD</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		Amount: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>QUOTE</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>BUY</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>COMMIT BUY</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		<input type="submit" value="Submit">
	</form>
	<h3>CANCEL BUY</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		<input type="submit" value="Submit">
	</form>
	<h3>SELL</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>COMMIT SELL</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		<input type="submit" value="Submit">
	</form>
	<h3>CANCEL SELL</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		<input type="submit" value="Submit">
	</form>
	<h3>SET BUY AMOUNT</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>CANCEL SET BUY</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>SET BUY TRIGGER</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>SET SELL AMOUNT</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>SET SELL TRIGGER</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		Amount: <input type="text" name="param3">
		<input type="submit" value="Submit">
	</form>
	<h3>CANCEL SET SELL</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		StockSymbol: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>DUMPLOG</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		FileName: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>DUMPLOG</h3>
	<form action="WebServlet" method="post">
		FileName: <input type="text" name="param2">
		<input type="submit" value="Submit">
	</form>
	<h3>DISPLAY SUMMARY</h3>
	<form action="WebServlet" method="post">
		Username: <input type="text" name="param1">
		<input type="submit" value="Submit">
	</form>
</body>
</html>