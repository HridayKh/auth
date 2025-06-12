<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Logout</title>
</head>
<body>
	<h2>Logging you out...</h2>
	<p id="logoutStatus"></p>

	<script>
        async function logoutUser() {
            try {
                const res = await fetch("<%=request.getContextPath()%>/v1/logout", {
                    method: "POST",
                    credentials: "include" // send cookies
                });

                const json = await res.json();
                const msgElem = document.getElementById("logoutStatus");

                if (json.type === "success") {
                    msgElem.style.color = "green";
                    msgElem.textContent = json.message;

                    // Optionally redirect after logout
                    setTimeout(() => {
                        window.location.href = "<%=request.getContextPath()%>/login.jsp";
                    }, 1500);
                } else {
                    msgElem.style.color = "red";
                    msgElem.textContent = json.message || "Logout failed.";
                }

            } catch (err) {
                console.error(err);
                document.getElementById("logoutStatus").textContent = "Network or server error.";
            }
        }

        // Trigger logout on page load
        logoutUser();
    </script>
</body>
</html>
