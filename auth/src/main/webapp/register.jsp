<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Register - Test Page</title>
</head>
<body>
	<h2>Register New User</h2>
	<form id="registerForm">
		<label>Email:</label><br> <input type="email" id="email" required><br>
		<br> s <label>Password:</label><br> <input type="password"
			id="pass" required><br> <br> <label>Full
			Name:</label><br> <input type="text" id="fullName" required><br>
		<br> <label>Redirect URL:</label><br> <input type="text"
			id="redirect" value="http://localhost:8080/auth/app"><br>
		<br>

		<button type="submit">Register</button>
	</form>

	<!-- Message paragraph to show success/error -->
	<p id="responseMsg"></p>

	<script>
    document.getElementById("registerForm").addEventListener("submit", async function(e) {
        e.preventDefault();

        const payload = {
            email: document.getElementById("email").value,
            pass: document.getElementById("pass").value,
            fullName: document.getElementById("fullName").value,
            redirect: document.getElementById("redirect").value
        };

        try {
            const res = await fetch("<%=request.getContextPath()%>/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const json = await res.json();
            const msgElem = document.getElementById("responseMsg");

            msgElem.textContent = json.message || "Unknown response.";
            msgElem.style.color = json.type === "success" ? "green" : "red";

            // Show the reverify button if "reverify" is true
            if (json.reverify) {
                showReverifyButton(payload.email, payload.redirect);
            }

            console.log(json);
        } catch (err) {
            console.error(err);
            const msgElem = document.getElementById("responseMsg");
            msgElem.style.color = "red";
            msgElem.textContent = "Network or server error.";
        }
    });

    function showReverifyButton(email, redirect) {
        // Avoid creating duplicates
        if (document.getElementById("reverifyBtn")) return;

        const btn = document.createElement("button");
        btn.id = "reverifyBtn";
        btn.textContent = "Resend Verification Email";
        btn.style.display = "block";
        btn.style.marginTop = "10px";

        btn.onclick = async function() {
            const res = await fetch("<%=request.getContextPath()%>/reVerify", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, redirect })
            });
            const json = await res.json();
            const msgElem = document.getElementById("responseMsg");
            msgElem.textContent = json.message || "Unknown response.";
            msgElem.style.color = json.type === "success" ? "green" : "red";
        };

        document.body.appendChild(btn);
    }
</script>

</body>
</html>
