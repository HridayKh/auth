import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext.jsx";
import { getSessions } from "../api/sessions.js";
import { deleteSession } from "../api/sessions.js";
import { withPrefix } from "@/main.jsx";

export default function Sessions() {
	const { user, setUser, loading } = useAuth();
	const [searchParams] = useSearchParams();
	const navigate = useNavigate();
	const [sessions, setSessions] = useState([]);
	const [sessionsLoading, setSessionsLoading] = useState(true);
	const [sessionsError, setSessionsError] = useState(null);
	const [deletingSessionId, setDeletingSessionId] = useState(null);
	const redirect = searchParams.get("redirect")?.trim() || "";

	useEffect(() => {
		if (!loading && user) {
			setSessionsLoading(true);
			getSessions().then(data => {
				setSessions(data.data.sessions || []);
				setSessionsError(null);
			}).catch(err => {
				setSessionsError(err.message || "Failed to fetch sessions");
			}).finally(() => setSessionsLoading(false));
		}
	}, [loading, user]);

	useEffect(() => {
		if (!loading && user === null) {
			navigate(withPrefix("/login?redirect=" + encodeURIComponent(window.location.pathname + window.location.search)), { replace: true });
		}
	}, [user, loading, navigate]);

	if (loading) return <main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">Loading...</main>;
	if (user === null) return null;

	async function handleDeleteSession(sessionId) {
		if (!window.confirm("Are you sure you want to delete this session?")) return;
		setDeletingSessionId(sessionId);
		try {
			await deleteSession(sessionId);
			setSessions(sessions => sessions.filter(sess => sess.session_id !== sessionId));
		} catch (err) {
			alert(err.message || "Failed to delete session");
		} finally {
			setDeletingSessionId(null);
		}
	}

	function goBackLink() {
		if (redirect && redirect.length > 0) return <Link to={withPrefix(redirect)} className="btn btn-link mb-2">Go Back</Link>;
		return (<Link to={withPrefix("/profile")} className="btn btn-link mb-2">Go Back</Link>);
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


	const urlType = searchParams.get("type");
	const urlMessage = searchParams.get("msg");

	return (
		<main className="flex flex-col items-center justify-center min-h-screen bg-dark text-white">
			<div className="bg-gray-900 p-6 rounded shadow-md w-full max-w-lg border border-gray-800">
				<h1 className="mb-4 text-2xl font-bold text-center">Sessions</h1>
				{goBackLink()}
				{sessionsLoading && <div>Loading sessions...</div>}
				{sessionsError && <div className="alert alert-danger">{sessionsError}</div>}
				{!sessionsLoading && !sessionsError && (
					<div className="mb-3">
						{sessions.length === 0 ? (
							<div>No active sessions found.</div>
						) : // session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent
							(
								<ul className="list-group">
									{sessions.map(sess => (
										<li key={sess.session_id} className="list-group-item bg-dark text-white">
											<div><strong>Session ID:</strong> {sess.session_id}</div>
											<div><strong>User UUID:</strong> {sess.user_uuid}</div>
											<div><strong>Created:</strong> {formatDate(sess.created_at)}</div>
											<div><strong>Last Accessed:</strong> {formatDate(sess.last_accessed_at)}</div>
											<div><strong>Expires At:</strong> {formatDate(sess.expires_at)}</div>
											<div><strong>User Agent:</strong> <span style={{ wordBreak: 'break-all' }}>{sess.user_agent}</span></div>
											<button
												className="btn btn-danger btn-sm mt-2"
												disabled={sess.is_current || deletingSessionId === sess.session_id}
												onClick={() => handleDeleteSession(sess.session_id)}
											>
												{deletingSessionId === sess.session_id ? "Deleting..." : sess.is_current ? "Can't delete current session" : "Delete Session"}
											</button>
										</li>
									))}
								</ul>
							)}
					</div>
				)}
			</div>
		</main>
	);
}