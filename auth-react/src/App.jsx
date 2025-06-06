import React from "react";
import ReactDOM from "react-dom/client";
import Login from "./Login.jsx";

// Simple components
const Register = () => <h2>Register Form</h2>;
const UpdatePassword = () => <h2>Update Password Form</h2>;
const Logout = () => <h2>You have been logged out.</h2>;

const Message = ({ type, msg }) => {
  const style = {
    padding: "1em",
    marginBottom: "1em",
    border: `1px solid ${type === "error" ? "red" : "green"}`,
    color: type === "error" ? "red" : "green",
    backgroundColor: type === "error" ? "#ffe5e5" : "#e5ffe5",
  };
  return <div style={style}>{msg}</div>;
};

export default function App(){
  const params = new URLSearchParams(window.location.search);
  const site = params.get("site");
  const type = params.get("type");
  const msg = params.get("msg");

  const renderComponent = () => {
    switch (site) {
      case "login":
        return <Login />;
      case "register":
        return <Register />;
      case "update":
        return <UpdatePassword />;
      case "logout":
        return <Logout />;
      default:
        return <h2>Unknown site action.</h2>;
    }
  };

  return (
    <div style={{ maxWidth: "500px", margin: "2em auto", fontFamily: "Arial" }}>
      {["error", "success"].includes(type) && msg && (
        <Message type={type} msg={msg} />
      )}
      {renderComponent()}
    </div>
  );
};

