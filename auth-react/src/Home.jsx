export default function Home() {
  return (
    <main style={{
      textAlign: "center",
      marginTop: "5rem",
      fontFamily: "sans-serif",
    }}>
      <h1 style={{ fontSize: "2rem" }}>Hriday.Tech Authentication</h1>
      <p>This is the central authentication service for Hriday.Tech apps.</p>
      <p>To learn more or contact the developer:</p>
      <a
        href="https://hriday.tech"
        target="_blank"
        rel="noopener noreferrer"
        style={{
          display: "inline-block",
          marginTop: "1rem",
          padding: "0.5rem 1rem",
          backgroundColor: "#333",
          color: "#fff",
          textDecoration: "none",
          borderRadius: "4px"
        }}
      >
        Visit My Portfolio
      </a>
    </main>
  );
}
