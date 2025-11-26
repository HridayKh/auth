import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { deleteCurrentSession } from "../api/sessions";
import { useAuth } from "@/AuthContext";
import { withPrefix } from "@/main";

export default function Logout() {
	const { setUser } = useAuth();
	const [searchParams] = useSearchParams();
	const navigate = useNavigate();
	const redirect = searchParams.get("redirect")?.trim() || login";

	useEffect(() => {
		deleteCurrentSession().then(() => {
			setUser(null);
			setTimeout(() => {
				if (/^[a-zA-Z][a-zA-Z0-9+.-]*:\/\//.test(redirect)) {
					window.location.replace(redirect);
				} else {
					navigate(withPrefix(redirect), { replace: true });
				}
			}, 1000);
		});
	}, [setUser, navigate, redirect]);

	return (
		<div>Logging out...</div>
	);
}
