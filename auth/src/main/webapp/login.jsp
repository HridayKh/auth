<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login</title>
</head>
<body>

	<%
	// String msg = request.getParameter("msg");
	// String type = request.getParameter("type");
	// String c = type.equals("error") ? "red" : "green";
	// if (msg != null && !msg.isEmpty() && type != null && !type.isEmpty()) {
	
  //<!-- <div style="color: <%=c</div> -->
	
	// }
	%>


	<form onsubmit="submitForm(event)">
		<input type="email" name="email" placeholder="Email" required>
		<input type="password" name="pass" placeholder="Password" required>
		<button type="submit"> login </button>
	</form>

	<div id="msg"></div>

	<script>
		async function submitForm(event) {
			event.preventDefault();
			const form = event.target;
			const email = form.email.value;
			const pass = form.pass.value;

			const response = await
			fetch(
					"login",
					{
						method : "POST",
						headers : {
							"Content-Type" : "application/x-www-form-urlencoded"
						},
						body : `email=${encodeURIComponent(email)}&pass=${encodeURIComponent(pass)}`
					});

			const msg = document.getElementById("msg");

			try {
				const data = await
				response.json();
				msg.textContent = data.message || "No message received";
				msg.style.color = data.type === "success" ? "green" : "red";
			console.log(data.jwt);

if (data.jwt) {
  // Save JWT JSON string in cookie, expire in 1 hour
  const expires = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toUTCString();
  document.cookie = `user_jwt=${encodeURIComponent(JSON.stringify(data.jwt))}; expires=${expires}; path=/; Secure; SameSite=Strict`;
}

       } catch (e) {
				msg.textContent = "Invalid response from server.";
				msg.style.color = "red";
			}
		}
	</script>
</body>
</html>
