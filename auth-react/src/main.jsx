import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./Home.jsx";
import NotFound from "./NotFound.jsx";
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './index.css';


createRoot(document.getElementById('root')).render(
	<StrictMode>
		<Router>
			<Routes>
				<Route path="/" element={<Home />} />
				<Route path="*" element={<NotFound />} />
			</Routes>
		</Router>
	</StrictMode>,
);