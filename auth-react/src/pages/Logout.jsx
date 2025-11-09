

import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { deleteCurrentSession } from "../api/sessions";
import { useAuth } from "../AuthContext.jsx";

export default function Logout() {
	const { loading: authLoading } = useAuth();
	const navigate = useNavigate();
	const [searchParams] = useSearchParams();
	const redirect = searchParams.get("redirect");
	// Prevent double navigation
	let navigated = false;

	useEffect(() => {
		if (authLoading) return;
		deleteCurrentSession().then(() => {
			setTimeout(() => {
				if (navigated) return;
				navigated = true;
				if (typeof redirect === "string" && redirect.length > 0 && /^(http|https):\/\//.test(redirect)) {
					window.location.replace(redirect);
				} else {
					// Always go to /login after logout to avoid profile rerender
					navigate("/login", { replace: true });
				}
			}, 100);
		});
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [authLoading]);

	return (
		<div>Logging out...</div>
	);
}
