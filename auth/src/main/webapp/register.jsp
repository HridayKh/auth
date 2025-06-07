<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register - Test Page</title>
</head>
<body>
    <h2>Register New User</h2>
    <form id="registerForm">
        <label>Email:</label><br>
        <input type="email" id="email" required><br><br>

        <label>Password:</label><br>
        <input type="password" id="pass" required><br><br>

        <label>Full Name:</label><br>
        <input type="text" id="fullName" required><br><br>

        <label>Redirect URL:</label><br>
        <input type="text" id="redirect" value="http://localhost:8080/auth/app"><br><br>

        <button type="submit">Register</button>
    </form>

    <!-- Message paragraph to show success/error -->
    <p id="responseMsg"></p>

    <script>
        // Add an event listener for the form's submit event
        document.getElementById("registerForm").addEventListener("submit", async function(e) {
            // Prevent the form from actually submitting and refreshing the page
            e.preventDefault();
            
            // Create a JS object similar to a Java POJO with the required fields
            const payload = {
                email: document.getElementById("email").value,
                pass: document.getElementById("pass").value,
                fullName: document.getElementById("fullName").value,
                redirect: document.getElementById("redirect").value
            };

            try {
                // Use Fetch API to send a POST request to the /register servlet
                const res = await fetch("<%=request.getContextPath()%>/register", {
                    method: "POST", // HTTP method
                    headers: {
                        "Content-Type": "application/json" // Tell server it's JSON
                    },
                    body: JSON.stringify(payload) // Convert JS object to JSON string
                });

                // Wait for the JSON response from the servlet
                const json = await res.json();

                // Grab the <p> element for displaying messages
                const msgElem = document.getElementById("responseMsg");

                // Show the message from the response JSON
                msgElem.textContent = json.message || "Unknown response.";

                // If type is "success", show in green, else show in red
                if (json.type === "success") {
                    msgElem.style.color = "green";
                } else {
                    msgElem.style.color = "red";
                }
			console.log(json);
            } catch (err) {
                // If fetch failed (server down, network error, etc.)
                console.error(err); // Log error in browser console
                const msgElem = document.getElementById("responseMsg");
                msgElem.style.color = "red";
                msgElem.textContent = "Network or server error.";
            }
        });
    </script>
</body>
</html>
