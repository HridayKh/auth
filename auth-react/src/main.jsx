import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
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

const routePrefix = import.meta.env.DEV ? '' : import.meta.env.VITE_PROD === "yes" ? '' : '/auth';
function withPrefix(path) {
	if (!routePrefix) return path;
	if (path === '/') return routePrefix + '/';
	return `${routePrefix}${path.startsWith('/') ? '' : '/'}${path}`;
}

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
				<RouteDebugger />
			</Router>
		</AuthProvider>
	</StrictMode>
);

// --- Route definitions for debugging/logging ---
const routeDefinitions = [
	{ path: withPrefix('/'), name: 'Home' },
	{ path: withPrefix('/login'), name: 'Login' },
	{ path: withPrefix('*'), name: 'NotFound' }
];

function RouteDebugger() {
	const location = useLocation();
	useEffect(() => {
		console.groupCollapsed('Route Debugger');
		console.log('Current path:', location.pathname + location.search + location.hash);
		console.log('Route map:', routeDefinitions);
		console.log('VITE_AUTH_BACKEND:', import.meta.env.VITE_AUTH_BACKEND);
		console.groupEnd();
	}, [location]);
}
