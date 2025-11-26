
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext.jsx";
import { unlinkGoogle, linkPassword } from "../api/userInfo.js";
import { AUTH_BACKEND } from "@/vars.js";
import { withPrefix } from "@/main.jsx";

export default function Profile() {
	const { user, setUser, loading } = useAuth();
	const [linking, setLinking] = useState(false);
	const [searchParams] = useSearchParams();
	const navigate = useNavigate();
	const redirect = searchParams.get("redirect")?.trim() || "";

	useEffect(() => {
		if (!loading) {
			if (user === null) {
				navigate(withPrefix("/login?redirect=" + encodeURIComponent(window.location.pathname + window.location.search)), { replace: true });
			}
		}
	}, [user, loading, redirect, navigate]);

	if (loading) return <main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">Loading...</main>;
	if (user === null) return null;


	async function handleGoogleUnlink() {
		if (!window.confirm("Are you sure you want to unlink your Google account?")) return;
		setLinking(true);
		try {
			const res = await unlinkGoogle();
			if (res && res.ok) {
				setUser({ ...user, accType: "password" });
			} else {
				alert(res && res.message ? res.message : "Failed to unlink Google account");
			}
		} catch (err) {
			alert(err.message || "Failed to unlink Google account");
		} finally {
			setLinking(false);
		}
	}


	async function handleAddPassword() {
		const password = window.prompt("Enter a new password for your account:");
		if (!password || password.length < 8) {
			alert("Password must be at least 8 characters.");
			return;
		}
		setLinking(true);
		try {
			const res = await linkPassword({ newPass: password });
			if (res && res.ok) {
				setUser({ ...user, accType: "both" });
				alert("Password added! You can now log in with email and password.");
			} else {
				alert(res && res.message ? res.message : "Failed to add password");
			}
		} catch (err) {
			alert(err.message || "Failed to add password");
		} finally {
			setLinking(false);
		}
	}

	function handleGoogleLink() {
		setLinking(true);
		const url = `${AUTH_BACKEND}/googleLoginInitiate?source=glink&redirect=${encodeURIComponent(window.location.href)}`;
		window.location.href = url;
	}

	function goBackLink() {
		if (redirect && redirect.length > 0) return <Link to={withPrefix(redirect)} className="btn btn-link mb-2">Go Back</Link>;
		return (<></>);
	}

	function formatDate(ts) {
		if (!ts) return "-";
		const n = Number(ts);
		if (!Number.isFinite(n) || n < 1000000000) return ts;
		const date = n < 1e12 ? new Date(n * 1000) : new Date(n);
		// yyyy/mm/dd hh:mm:ss.ms + timezone
		const pad = (v, l = 2) => v.toString().padStart(l, "0");
		const yyyy = date.getFullYear();
		const mm = pad(date.getMonth() + 1);
		const dd = pad(date.getDate());
		const hh = pad(date.getHours());
		const min = pad(date.getMinutes());
		const ss = pad(date.getSeconds());
		const tz = (() => {
			const offset = -date.getTimezoneOffset();
			const sign = offset >= 0 ? "+" : "-";
			const abs = Math.abs(offset);
			const h = pad(Math.floor(abs / 60));
			const m = pad(abs % 60);
			return `${sign}${h}:${m}`;
		})();
		return `${yyyy}/${mm}/${dd} ${hh}:${min}:${ss} GMT${tz}`;
	}
	// Show type/message from URL params if present, else from user object
	const urlType = searchParams.get("type");
	const urlMessage = searchParams.get("msg");
	const pageRedirect = redirect ? "?redirect=" + encodeURIComponent(redirect) : "";

	return (
		<main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">
			<div className="bg-gray-900 p-6 rounded shadow-md w-full max-w-lg border border-gray-800">
				<h1 className="mb-4 text-2xl font-bold text-center">Profile</h1>
				{goBackLink()}
				{(urlType && urlMessage) ? (
					<div className={`alert mt-3 ${urlType === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{urlType}</strong>: {urlMessage}
					</div>
				) : (user.type && user.message && (
					<div className={`alert mt-3 ${user.type === "success" ? "alert-success" : "alert-danger"}`} role="alert">
						<strong>{user.type}</strong>: {user.message}
					</div>
				))}
				<div className="mb-3 d-flex align-items-center gap-3" style={{ minHeight: 80 }}>
					{user.profile_pic && (
						<img
							src={user.profile_pic}
							alt="Profile"
							className="rounded-circle"
							style={{ width: 80, height: 80, objectFit: 'cover', flexShrink: 0 }}
							referrerPolicy="no-referrer"
						/>
					)}
					<div className="flex-grow-1">
						<div className="fw-bold">{user.full_name}</div>
						<div className="text-secondary">{user.email}</div>
						<div className={`badge bg-${user.is_verified ? 'success' : 'danger'} mt-2`}>
							{user.is_verified ? 'Verified' : 'Not Verified'}
						</div>
					</div>
				</div>
				<div className="mb-3">
					<strong>UUID:</strong> <i>{user.uuid}</i>
				</div>
				<div className="mb-3">
					<strong>Account Type:</strong> {user.accType}
				</div>
				<div className="mb-3">
					<strong>Created:</strong> {formatDate(user.created_at)}
				</div>
				<div className="mb-3">
					<strong>Last Password Change:</strong> {formatDate(user.updated_at)}
				</div>
				<div className="mb-3">
					<strong>Last Login:</strong> {user.last_login}
				</div>
				<div className="mb-3">
					<strong>Metadata:</strong> <pre className="bg-dark text-white p-2 rounded" style={{ fontSize: 12 }}>{JSON.stringify(user.metadata, null, 2)}</pre>
				</div>
				<div className="mb-3">
					<strong>Permissions:</strong> <pre className="bg-dark text-white p-2 rounded" style={{ fontSize: 12 }}>{JSON.stringify(user.permissions, null, 2)}</pre>
				</div>
				<div className="mb-3">
					{/* Google link/unlink logic */}
					{user.accType === "google" && (
						<button className="btn btn-secondary w-full mb-2" onClick={handleAddPassword} disabled={linking}>
							{linking ? "Adding..." : "Add Password"}
						</button>
					)}
					{user.accType === "password" && (
						<button className="btn btn-outline-primary w-full mb-2" onClick={handleGoogleLink} disabled={linking}>
							Link Google Account
						</button>
					)}
					{user.accType === "both" && (
						<button className="btn btn-secondary w-full mb-2" onClick={handleGoogleUnlink} disabled={linking}>
							{linking ? "Unlinking..." : "Unlink Google"}
						</button>
					)}
				</div>
				<div className="d-flex flex-column gap-2 mt-4">
					<Link to={withPrefix("/sessions?redirect=/profile" + pageRedirect)} className="btn btn-outline-light w-full">Manage Sessions</Link>
					<Link
						to={withPrefix("/change-password?redirect=/profile" + pageRedirect)}
						className={"btn btn-outline-light w-full" + (user.accType === "google" ? " disabled" : "")}
						tabIndex={user.accType === "google" ? -1 : 0}
						aria-disabled={user.accType === "google" ? "true" : undefined}
						onClick={e => { if (user.accType === "google") e.preventDefault(); }}
					>
						{user.accType === "google" ? " Change Password (link password first)" : "Change Password"}
					</Link>
					<Link to={withPrefix("/logout" + pageRedirect)} className="btn btn-outline-danger w-full">Logout</Link>
				</div>
			</div>
		</main>
	);
}