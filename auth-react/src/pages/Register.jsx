import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

export default function RegisterPage() {
	const [form, setForm] = useState({
		email: "",
		pass: "",
		confirmPass: "",
		fullName: "",
		redirect: "",
	});

	const [message, setMessage] = useState(null);
	const [isSuccess, setIsSuccess] = useState(false);
	const [showReverify, setShowReverify] = useState(false);

	useEffect(() => {
		const urlParams = new URLSearchParams(window.location.search);
		const redirectParam = urlParams.get("redirect") || "/";
		setForm((prev) => ({ ...prev, redirect: redirectParam }));

		const type = urlParams.get("type");
		const msg = urlParams.get("msg");
		if (type && msg && (type === "success" || type === "error")) {
			setMessage(msg);
			setIsSuccess(type === "success");
		}
	}, []);

	const handleChange = (e) => {
		setForm((prev) => ({
			...prev,
			[e.target.id]: e.target.value,
		}));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();

		if (form.pass !== form.confirmPass) {
			setMessage("Passwords do not match.");
			setIsSuccess(false);
			return;
		}

		try {
			const res = await fetch(import.meta.env.VITE_AUTH_BACKEND + "/v1/register", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					email: form.email,
					pass: form.pass,
					fullName: form.fullName,
					redirect: form.redirect,
				}),
			});

			const json = await res.json();
			setMessage(json.message || "Unknown response.");
			setIsSuccess(json.type === "success");
			setShowReverify(!!json.reverify);
		} catch (err) {
			console.error(err);
			setMessage("Network or server error.");
			setIsSuccess(false);
		}
	};


	const handleReverify = async () => {
		try {
			const res = await fetch(import.meta.env.VITE_AUTH_BACKEND + "/v1/reVerify", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					email: form.email,
					redirect: form.redirect,
				}),
			});

			const json = await res.json();
			setMessage(json.message || "Unknown response.");
			setIsSuccess(json.type === "success");
		} catch (err) {
			console.error(err);
			setMessage("Network or server error.");
			setIsSuccess(false);
		}
	};

	return (
		<div style={{ maxWidth: "500px", margin: "auto" }}>
			<h2>Register New User</h2>
			<form onSubmit={handleSubmit}>
				<label>Full Name:</label><br />
				<input type="text" id="fullName" value={form.fullName} onChange={handleChange} required />
				<br /><br />

				<label>Email:</label><br />
				<input type="email" id="email" value={form.email} onChange={handleChange} required />
				<br /><br />

				<label>Password:</label><br />
				<input type="password" id="pass" value={form.pass} onChange={handleChange} required />
				<br /><br />

				<label>Confirm Password:</label><br />
				<input type="password" id="confirmPass" value={form.confirmPass} onChange={handleChange} required />
				<br /><br />

				<button type="submit">Register</button>
			</form>

			{message && (
				<p style={{ color: isSuccess ? "green" : "red" }}>{message}</p>
			)}

			{showReverify && (
				<button onClick={handleReverify} style={{ marginTop: "10px" }}>
					Resend Verification Email
				</button>
			)}

			<p style={{ marginTop: "1rem" }}>
				Already have an account?{" "}
				<Link to={`/login?redirect=${encodeURIComponent(form.redirect)}`} style={{ color: "blue", textDecoration: "underline" }}>
					Login here
				</Link>
			</p>
		</div>
	);
}
