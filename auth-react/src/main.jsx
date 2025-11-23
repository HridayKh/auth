import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './index.css';

import Home from "./Home.jsx";
import { AuthProvider } from "./AuthContext.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Profile from "./pages/Profile.jsx";
import Logout from "./pages/Logout.jsx";
import Sessions from './pages/Sessions';
import ChangePass from './pages/ChangePass';
import ResetPassword from './pages/ResetPassword';
import { PROD } from './vars';

const routePrefix = import.meta.env.DEV ? '' : (PROD ? '' : '/auth');
function withPrefix(path) {
	if (!routePrefix) return path;
	if (path === '/') return routePrefix + '/';
	return `${routePrefix}${path.startsWith('/') ? '' : '/'}${path}`;
}

createRoot(document.getElementById('root')).render(
	<StrictMode><AuthProvider><Router><Routes>
		<Route path={withPrefix('/')} element={<Home />} />
		<Route path={withPrefix('/login')} element={<Login />} />
		<Route path={withPrefix('/register')} element={<Register />} />
		<Route path={withPrefix('/profile')} element={<Profile />} />
		<Route path={withPrefix('/logout')} element={<Logout />} />
		<Route path={withPrefix('/sessions')} element={<Sessions />} />
		<Route path={withPrefix('/change-password')} element={<ChangePass />} />
		<Route path={withPrefix('/password-reset')} element={<ResetPassword />} />
		<Route path={withPrefix('*')} element={<h1> NotFound Page </h1>} />
	</Routes></Router></AuthProvider></StrictMode>
);