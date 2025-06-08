import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function LogoutPage() {
	const [message, setMessage] = useState("Logging you out...");
	const [isSuccess, setIsSuccess] = useState(false);
	const navigate = useNavigate();

	useEffect(() => {
		async function logoutUser() {
			try {
				const res = await fetch(import.meta.env.VITE_AUTH_BACKEND + "/v1/logout", {
					method: "POST",
					credentials: "include", // send cookies
				});

				const json = await res.json();

				if (json.type === "success") {
					setMessage(json.message || "Logged out successfully.");
					setIsSuccess(true);
					setTimeout(() => {
						// Redirect to login page after 2s
						navigate("/login");
					}, 2000);
				} else {
					setMessage(json.message || "Logout failed.");
					setIsSuccess(false);
				}
			} catch (err) {
				console.error(err);
				setMessage("Network or server error.");
				setIsSuccess(false);
			}
		}

		logoutUser();
	}, [navigate]);

	return (
		<div style={{ maxWidth: "500px", margin: "auto", padding: "1rem", textAlign: "center" }}>
			<h2>Logout</h2>
			<p style={{ color: isSuccess ? "green" : "red" }}>{message}</p>
		</div>
	);
}
