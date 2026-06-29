import { LogIn, Store } from 'lucide-react';
import { authenticate, demoUsers } from '../auth/demoUsers.js';

export function LoginPage({ credentials, error, onChange, onLogin }) {
  function submit(event) {
    event.preventDefault();
    const user = authenticate(credentials.username, credentials.password);
    onLogin(user);
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-brand">
          <div className="logo-mark">
            <Store size={24} />
          </div>
          <div>
            <p className="eyebrow">Store Operations</p>
            <h1>StoreApp</h1>
          </div>
        </div>

        <form className="login-form" onSubmit={submit}>
          <label>
            Username
            <input value={credentials.username} onChange={(event) => onChange({ username: event.target.value })} autoFocus />
          </label>
          <label>
            Password
            <input type="password" value={credentials.password} onChange={(event) => onChange({ password: event.target.value })} />
          </label>
          {error && <div className="notice error">{error}</div>}
          <button className="primary" type="submit">
            <LogIn size={18} />
            Sign in
          </button>
        </form>

        <div className="demo-users">
          {demoUsers.map((user) => (
            <button
              key={user.username}
              type="button"
              onClick={() => onChange({ username: user.username, password: user.password })}
            >
              <strong>{user.username}</strong>
              <span>{user.role}</span>
            </button>
          ))}
        </div>
      </section>
    </main>
  );
}
