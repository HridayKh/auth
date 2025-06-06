import React, { useState } from "react";


export default function Login(){


  const [email, setEmail] = useState("");
  const [pass, setPass] = useState("");
  const [error, setError] = useState(null);
  const [responseMsg, setResponseMsg] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setResponseMsg(null);

    try {
      const res = await fetch(`http://localhost:8080/auth/login`, {
        method: "POST",
credentials: "include",
      body: JSON.stringify({ email, pass }),
      });
const data = await res.json();

    if (data.type === "error") {
      setError(data.message || "Login failed.");
    } else if (data.type === "success") {
      setResponseMsg(data.message || "Login successful!");
    } else {
      setError("Unexpected response from server.");
    }
    } catch (err) {
      setError("Network error or server not reachable!");
      console.log(data);
    }
  };

return(  <>
    <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "1em" }}>
      <h2>Login</h2>

      {error && <div style={{ color: "red" }}>{error}</div>}
      {responseMsg && <div style={{ color: "green" }}>{responseMsg}</div>}

      <input
        type="email"
        placeholder="Email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <input
        type="password"
        placeholder="Password"
        required
        value={pass}
        onChange={(e) => setPass(e.target.value)}
      />

      <button type="submit">Login</button>
    </form>

  </>);

};
