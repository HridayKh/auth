import React, { useState } from "react";


export default function Login(){


const [email, setEmail] = useState("");
  const [pass, setPass] = useState("");
  const [confirmPass, setConfirmPass] = useState("");
  const [error, setError] = useState(null);
  const [responseMsg, setResponseMsg] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setResponseMsg(null);

    if (pass !== confirmPass) {
      setError("Passwords do not match.");
      return;
    }

    try {
      const res = await fetch(`localhost:8080/auth/login?email=${encodeURIComponent(email)}&pass=${encodeURIComponent(pass)}`, {
        method: "POST",
      });

      const text = await res.text();
      if (!res.ok) throw new Error(text || "Something went wrong");

      setResponseMsg("Login successful!");
    } catch (err) {
      setError(err.message);
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

      <input
        type="password"
        placeholder="Confirm Password"
        required
        value={confirmPass}
        onChange={(e) => setConfirmPass(e.target.value)}
      />

      <button type="submit">Login</button>
    </form>

  </>);

};
