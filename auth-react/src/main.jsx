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
			</Router>
		</AuthProvider>
	</StrictMode>
);