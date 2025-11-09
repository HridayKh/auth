import { createContext, useContext, useEffect, useState } from "react";
import { getUserInfo } from "./api/userInfo";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
	const [user, setUser] = useState(null);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		getUserInfo().then(res => {
			if (res && res.ok && res.data) setUser(res.data);
			setLoading(false);
		}).catch(() => setLoading(false));
	}, []);

	return (
		<AuthContext.Provider value={{ user, setUser, loading }}>
			{children}
		</AuthContext.Provider>
	);
}

export function useAuth() {
	return useContext(AuthContext);
}
