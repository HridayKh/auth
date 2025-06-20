import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Home from "./Home.jsx";
import NotFound from "./NotFound.jsx";
import RegisterPage from "./pages/Register.jsx";
import LoginPage from "./pages/Login.jsx";
import LogoutPage from "./pages/Logout.jsx";

function App() {
	return (
		<Router>
			<Routes>
				<Route path="/" element={<Home />} />
				<Route path="/register" element={<RegisterPage />} />
				<Route path="/login" element={<LoginPage />} />
				<Route path="/logout" element={<LogoutPage />} />
				<Route path="*" element={<NotFound />} />
			</Routes>
		</Router>
	);
}

export default App;
