import React, { useState } from "react";
import { useSearchParams, Link } from "react-router-dom";

export default function LoginPage() {
	const [searchParams] = useSearchParams();
	const redirect = searchParams.get("redirect") || "/app"; // default redirect

	const [form, setForm] = useState({
		email: "",
		pass: "",
	});

	const [message, setMessage] = useState("");
	const [isSuccess, setIsSuccess] = useState(null);

	const handleChange = (e) => {
		setForm((prev) => ({
			...prev,
			[e.target.id]: e.target.value,
		}));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();

		try {
			const res = await fetch(`${import.meta.env.VITE_AUTH_BACKEND}/v1/login`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					email: form.email,
					pass: form.pass,
				}),
				credentials: "include",
			});

			const json = await res.json();

			setMessage(json.message || "Unknown response.");
			setIsSuccess(json.type === "success");

			if (json.type === "success") {
				setTimeout(() => {
					window.location.href = redirect;
				}, 1000);
			}
		} catch (err) {
			console.error(err);
			setMessage("Network or server error.");
			setIsSuccess(false);
		}
	};

	return (
		<div style={{ maxWidth: "400px", margin: "auto", padding: "1rem" }}>
			<h2>Login</h2>
			<form onSubmit={handleSubmit}>
				<label htmlFor="email">Email:</label><br />
				<input
					type="email"
					id="email"
					value={form.email}
					onChange={handleChange}
					required
					autoComplete="email"
				/><br /><br />

				<label htmlFor="pass">Password:</label><br />
				<input
					type="password"
					id="pass"
					value={form.pass}
					onChange={handleChange}
					required
					autoComplete="current-password"
				/><br /><br />

				<button type="submit">Login</button>
			</form>

			{message && (
				<p style={{ color: isSuccess ? "green" : "red", marginTop: "1rem" }}>
					{message}
				</p>
			)}

			<p style={{ marginTop: "1rem" }}>
				Don't have an account?{" "}
				<Link to={`/register?redirect=${encodeURIComponent(redirect)}`} style={{ color: "blue", textDecoration: "underline" }}>
					Register here
				</Link>
			</p>
		</div>
	);
}
