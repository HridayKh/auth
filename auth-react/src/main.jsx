import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
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
					<Route path="/" element={<Home />} />
					<Route path="/login" element={<Login />} />
					<Route path="/register" element={<Register />} />
					<Route path="/profile" element={<Profile />} />
					<Route path="/logout" element={<Logout />} />
					<Route path="/sessions" element={<Sessions />} />
					<Route path="/change-password" element={<ChangePass />} />
					<Route path="/password-reset" element={<ResetPassword />} />
					<Route path="*" element={<NotFound />} />
				</Routes>
			</Router>
		</AuthProvider>
	</StrictMode>
);