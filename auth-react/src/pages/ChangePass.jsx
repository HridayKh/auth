import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext.jsx";
import { changePassword } from "@/api/userInfo.js";
import { withPrefix } from "@/main.jsx";

export default function ChangePass() {
	const { user, setUser, loading } = useAuth();
	const [searchParams] = useSearchParams();
	const [oldPassword, setOldPassword] = useState("");
	const [password, setPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [result, setResult] = useState(null); // { type, message, reverify }
	const navigate = useNavigate();
	const redirect = searchParams.get("redirect")?.trim() || "";

	useEffect(() => {
		if (!loading && user === null) {
			navigate(withPrefix("/login?redirect=" + encodeURIComponent(window.location.pathname + window.location.search)), { replace: true });
		}
	}, [user, loading, navigate]);

	if (loading) return <main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">Loading...</main>;
	if (user === null) return null;

	function goBackLink() {
		if (redirect && redirect.length > 0) return <Link to={redirect} className="btn btn-link mb-2">Go Back</Link>;
		return (<Link to={"/profile"} className="btn btn-link mb-2">Go Back</Link>);
	}

	async function handleSubmit(e) {
		e.preventDefault();
		setResult(null);
		try {
			if (password !== confirmPassword) {
				setResult({ type: "error", message: "Passwords do not match" });
				return;
			}
			const res = await changePassword({ old: oldPassword, newPass: password });
			setResult(res);
			if (res && res.type === "success") {
				setTimeout(() => navigate(withPrefix("/profile?type=success&msg=Password changed successfully" + (redirect ? "&redirect=" + encodeURIComponent(redirect) : "")), { replace: true }), 1000);
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
				{goBackLink()}
				<h1 className="mb-4 text-2xl font-bold text-center">Change Password</h1>
				<div className="mb-3">
					<label htmlFor="oldPassword" className="form-label text-white">Old Password</label>
					<input
						id="oldPassword"
						type="password"
						className="form-control bg-dark text-white border-secondary"
						value={oldPassword}
						onChange={e => setOldPassword(e.target.value)}
						required
					/>
				</div>
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
					disabled={loading}
				>
					{loading ? "Changing..." : "Change Password"}
				</button>
				{result && (
					<div className={`alert mt-3 ${result.type === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{result.type}</strong>: {result.message}
					</div>
				)}
				{result && result.data && result.data.reverify === true && (
					<button
						type="button"
						className="btn btn-warning w-full mt-2"
						onClick={handleResendVerification}
						disabled={loading || resend}
					>
						{resend ? "Verification Email Sent" : "Resend Verification Email"}
					</button>
				)}
			</form>
		</main>
	);
}