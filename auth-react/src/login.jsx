import { useState } from 'react';

export function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [responseMsg, setResponseMsg] = useState('');
  const [error, setError] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const res = await fetch(`/api/login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`, {
        method: 'POST'
      });

      const data = await res.json();

      if (data.type === 'success') {
        setError(false);
        setResponseMsg(data.msg);
        // Optional: redirect or store session info
      } else {
        setError(true);
        setResponseMsg(data.msg);
      }
    } catch (err) {
      setError(true);
      setResponseMsg('An error occurred. Try again.');
    }
  };

  return (
    <div style={styles.container}>
      <form onSubmit={handleLogin} style={styles.form}>
        <h2>Login</h2>
        <input
          style={styles.input}
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          style={styles.input}
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button style={styles.button} type="submit">Login</button>
        {responseMsg && (
          <div style={{ color: error ? 'red' : 'green', marginTop: '10px' }}>{responseMsg}</div>
        )}
      </form>
    </div>
  );
}

const styles = {
  container: {
    height: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f4f4f4'
  },
  form: {
    backgroundColor: 'white',
    padding: '2rem',
    borderRadius: '8px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    width: '300px',
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem'
  },
  input: {
    padding: '10px',
    fontSize: '1rem',
    borderRadius: '4px',
    border: '1px solid #ccc'
  },
  button: {
    padding: '10px',
    backgroundColor: '#007bff',
    color: 'white',
    fontSize: '1rem',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  }
};
