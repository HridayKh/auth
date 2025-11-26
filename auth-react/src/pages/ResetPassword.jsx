import { useSearchParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { resetPassword } from "@/api/userCreation.js";
import { withPrefix } from "@/main.jsx";

export default function ResetPassword() {
	const [searchParams] = useSearchParams();
	const [password, setPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [result, setResult] = useState(null); // { type, message, reverify }
	const navigate = useNavigate();

	async function handleSubmit(e) {
		e.preventDefault();
		setResult(null);
		try {
			if (password !== confirmPassword) {
				setResult({ type: "error", message: "Passwords do not match" });
				return;
			}
			const res = await resetPassword({ pass: password, token: searchParams.get("token") || "" });
			setResult(res);
			if (res && res.type === "success") {
				setTimeout(() => navigate(withPrefix("/login" + window.location.search + "&type=success&msg=Password changed successfully"), { replace: true }), 1000);
			}
		} catch (err) {
			setResult({ type: "error", message: err.message || "Unknown error" });
		}
	}

	return (
		<main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">
			<form
				className="bg-gray-900 p-6 rounded shadow-md w-full max-w-sm border border-gray-800"
				onSubmit={handleSubmit}
				autoComplete="on"
			>
				<h1 className="mb-4 text-2xl font-bold text-center">Reset Forgotten Password</h1>
				<div className="mb-3">
					<label htmlFor="password" className="form-label text-white">Password</label>
					<input
						id="password"
						type="password"
						className="form-control bg-dark text-white border-secondary"
						value={password}
						onChange={e => setPassword(e.target.value)}
						required
					/>
				</div>
				<div className="mb-3">
					<label htmlFor="confirmPassword" className="form-label text-white">Confirm Password</label>
					<input
						id="confirmPassword"
						type="password"
						className="form-control bg-dark text-white border-secondary"
						value={confirmPassword}
						onChange={e => setConfirmPassword(e.target.value)}
						required
					/>
				</div>
				<button
					type="submit"
					className="btn btn-primary w-full mb-2"
				>
					Reset Password
				</button>
				{result && (
					<div className={`alert mt-3 ${result.type === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{result.type}</strong>: {result.message}
					</div>
				)}
			</form>
		</main>
	);
}