import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
// Get route prefix from environment variable (e.g., VITE_ROUTE_PREFIX)
let routePrefix = '';
if (import.meta.env.DEV) {
	routePrefix = '';
} else {
	const isProd = import.meta.env.VITE_PROD === "yes";
	routePrefix = isProd ? '' : '/auth';
}
function withPrefix(path) {
	if (!routePrefix) return path;
	if (path === '/') return routePrefix + '/';
	return `${routePrefix}${path.startsWith('/') ? '' : '/'}${path}`;
}
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './index.css';
import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

import Home from "./Home.jsx";
import NotFound, { Unimplemented } from "./NotFound.jsx";
import { AuthProvider } from "./AuthContext.jsx";

import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Profile from "./pages/Profile.jsx";
import Logout from "./pages/Logout.jsx";
import Sessions from './pages/Sessions';
import ChangePass from './pages/ChangePass';
import ResetPassword from './pages/ResetPassword';


createRoot(document.getElementById('root')).render(
	<StrictMode>
		<AuthProvider>
			<Router>
				<Routes>
					<Route path={withPrefix('/')} element={<Home />} />
					<Route path={withPrefix('/login')} element={<Login />} />
					<Route path={withPrefix('/register')} element={<Register />} />
					<Route path={withPrefix('/profile')} element={<Profile />} />
					<Route path={withPrefix('/logout')} element={<Logout />} />
					<Route path={withPrefix('/sessions')} element={<Sessions />} />
					<Route path={withPrefix('/change-password')} element={<ChangePass />} />
					<Route path={withPrefix('/password-reset')} element={<ResetPassword />} />
					<Route path={withPrefix('*')} element={<NotFound />} />
				</Routes>

				{/* Route debugger: logs current route and the route map. Visible in dev or when VITE_SHOW_ROUTE_DEBUG=yes */}
				<RouteDebugger />
			</Router>
		</AuthProvider>
	</StrictMode>
);

// --- Route definitions for debugging/logging ---
const routeDefinitions = [
	{ path: withPrefix('/'), name: 'Home' },
	{ path: withPrefix('/login'), name: 'Login' },
	{ path: withPrefix('/register'), name: 'Register' },
	{ path: withPrefix('/profile'), name: 'Profile' },
	{ path: withPrefix('/logout'), name: 'Logout' },
	{ path: withPrefix('/sessions'), name: 'Sessions' },
	{ path: withPrefix('/change-password'), name: 'ChangePass' },
	{ path: withPrefix('/password-reset'), name: 'ResetPassword' },
	{ path: withPrefix('*'), name: 'NotFound' }
];

function RouteDebugger() {
	const location = useLocation();
	// Always visible and always log (for production debugging as requested)
	const [visible, setVisible] = useState(true);

	useEffect(() => {
		console.groupCollapsed('Route Debugger');
		console.log('Current path:', location.pathname + location.search + location.hash);
		console.log('Route map:', routeDefinitions);
		console.groupEnd();
	}, [location]);

	if (!visible) return null;

	return (
		<div style={{ position: 'fixed', right: 8, bottom: 8, zIndex: 9999, background: 'rgba(0,0,0,0.7)', color: '#fff', padding: '8px 12px', borderRadius: 6, fontSize: 12 }}>
			<div style={{ marginBottom: 6 }}>Current: <strong>{location.pathname}</strong></div>
			<details style={{ color: '#ddd' }}>
				<summary style={{ cursor: 'pointer' }}>Route map ({routeDefinitions.length})</summary>
				<pre style={{ whiteSpace: 'pre-wrap', color: '#ddd', maxHeight: 200, overflow: 'auto' }}>{JSON.stringify(routeDefinitions, null, 2)}</pre>
			</details>
			<div style={{ marginTop: 6 }}>
				<button onClick={() => setVisible(false)} style={{ background: '#333', color: '#fff', border: 'none', padding: '4px 8px', borderRadius: 4 }}>Hide</button>
			</div>
		</div>
	);
}
