
import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { createSession } from "../api/sessions";
import { resendVerification, requestPasswordReset } from "../api/userCreation";
import { useAuth } from "../AuthContext.jsx";
import { AUTH_BACKEND } from "@/vars";
import { withPrefix } from "@/main";

export default function Login() {
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");

	const [loading, setLoading] = useState(false);

	const [result, setResult] = useState(null); // { type, message, reverify }

	const [resend, setResend] = useState(false);

	const [resetSent, setResetSent] = useState(false);
	const { user, loading: authLoading } = useAuth();
	const navigate = useNavigate();
	const [searchParams] = useSearchParams();
	const redirect = searchParams.get("redirect")?.trim() || withPrefix("/profile");

	useEffect(() => {
		if (!authLoading && user) {
			if (/^[a-zA-Z][a-zA-Z0-9+.-]*:\/\//.test(redirect)) {
				window.location.replace(redirect);
			} else {
				navigate(withPrefix(redirect.startsWith("/") ? redirect : "/" + redirect), { replace: true });
			}
		}
	}, [user, authLoading, navigate, redirect]);

	function handleGoogleLogin(e) {
		e.preventDefault();
		var redirectUrl = "";

		if (/^[a-zA-Z][a-zA-Z0-9+.-]*:\/\//.test(redirect)) {
			redirectUrl = redirect;
		} else {
			redirectUrl = window.location.origin + withPrefix(redirect.startsWith("/") ? redirect : "/" + redirect);
		}
		const url = `${AUTH_BACKEND}/googleLoginInitiate?source=login&redirect=${encodeURIComponent(redirectUrl)}`;

		window.location.href = url;
	}

	async function handleSubmit(e) {
		e.preventDefault();
		setLoading(true);
		setResult(null);
		setResend(false);
		setResetSent(false);
		try {
			const res = await createSession({ email, password });
			setResult(res);
			if (res && res.ok) {
				// Give AuthProvider a moment to update, then redirect
				setTimeout(() => {
					if (typeof redirect === "string" && redirect.length > 0) {
						if (/^(http|https):\/\//.test(redirect)) {
							window.location.replace(redirect);
						} else if (redirect.startsWith("/") || redirect.startsWith("%2F") || redirect.startsWith("%2f")) {
							window.location.assign(withPrefix(decodeURIComponent(redirect)));
						} else {
							navigate(withPrefix("/profile"), { replace: true });
						}
					} else {
						navigate(withPrefix("/profile"), { replace: true });
					}
				}, 300);
			}
		} catch (err) {
			setResult({ type: "error", message: err.message || "Unknown error" });
		} finally {
			setLoading(false);
		}
	}

	async function handleResendVerification() {
		setResend(false);
		if (!email) return;
		setLoading(true);
		try {
			const res = await resendVerification({ email, redirect: redirect || window.location.origin + withPrefix("/profile") });
			setResend(true);
			setResult({ type: res.type, message: res.message });
		} catch (err) {
			setResult({ type: "error", message: err.message || "Unknown error" });
		} finally {
			setLoading(false);
		}
	}

	async function handleForgotPassword() {
		setResetSent(false);
		if (!email) return;
		setLoading(true);
		try {
			const res = await requestPasswordReset({ email });
			setResetSent(true);
			setResult({ type: res.type, message: res.message });
		} catch (err) {
			setResult({ type: "error", message: err.message || "Unknown error" });
		} finally {
			setLoading(false);
		}
	}

	const urlType = searchParams.get("type");
	const urlMessage = searchParams.get("msg");
	return (
		<main className="flex flex-col items-center justify-center min-h-screen bg-dark">
			<form
				className="bg-gray-900 p-6 rounded shadow-md w-full max-w-sm border border-gray-800"
				onSubmit={handleSubmit}
				autoComplete="on"
			>
				<h1 className="mb-4 text-2xl font-bold text-center text-white">Login</h1>
				<div className="mb-3">
					<label htmlFor="email" className="form-label text-white">Email</label>
					<input
						id="email"
						type="email"
						className="form-control bg-dark text-white border-secondary"
						value={email}
						onChange={e => setEmail(e.target.value)}
						required
						autoFocus
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
				<button
					type="submit"
					className="btn btn-primary w-full mb-2"
					disabled={loading}
				>
					{loading ? "Logging in..." : "Login"}
				</button>
				<button
					type="button"
					className="btn btn-dark w-full mb-2"
					onClick={handleGoogleLogin}
					disabled={loading}
				>
					<span className="me-2" style={{ verticalAlign: "middle" }}>
						<svg width="20" height="20" viewBox="0 0 48 48" style={{ display: "inline" }}>
							<g>
								<path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path>
								<path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path>
								<path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path>
								<path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path>

							</g>
						</svg>
					</span>
					Login with Google
				</button>
				<Link to={withPrefix(`/register${redirect ? `?redirect=${encodeURIComponent(redirect)}` : ""}`)} className="btn btn-outline-light w-full mb-2">
					Register instead
				</Link>
				<button
					type="button"
					className="btn btn-link w-full text-info mb-2"
					style={{ textDecoration: "underline" }}
					onClick={handleForgotPassword}
					disabled={loading || !email}
				>
					{resetSent ? "Reset link sent!" : "Forgot password?"}
				</button>
				{(urlType && urlMessage) ? (
					<div className={`alert mt-3 ${urlType === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{urlType}</strong>: {urlMessage}
					</div>
				) : (result && (
					<div className={`alert mt-3 ${result.type === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{result.type}</strong>: {result.message}
					</div>
				))}
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