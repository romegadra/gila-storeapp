import { useState } from 'react';
import { History, LogOut, Menu, Store, UserRound } from 'lucide-react';

export function Header({ user, onOrderHistory, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const initials = user.name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  function openOrderHistory() {
    onOrderHistory();
    setMenuOpen(false);
  }

  return (
    <header className="app-header">
      <div className="brand">
        <div className="logo-mark">
          <Store size={22} />
        </div>
        <div>
          <p className="eyebrow">Store Operations</p>
          <h1>StoreApp</h1>
        </div>
      </div>

      <div className="header-actions">
        <div className="user-chip">
          <span className="avatar">{initials}</span>
          <span>{user.name}</span>
        </div>
        <div className="account-menu">
          <button
            className="icon-button"
            type="button"
            title="Account menu"
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <Menu size={18} />
          </button>
          {menuOpen && (
            <div className="menu-popover" role="menu">
              <div className="menu-user">
                <span className="avatar">{initials}</span>
                <div>
                  <strong>{user.name}</strong>
                  <span>{user.role}</span>
                </div>
              </div>
              <button type="button" role="menuitem">
                <UserRound size={16} />
                Profile
              </button>
              <button type="button" role="menuitem" onClick={openOrderHistory}>
                <History size={16} />
                Order history
              </button>
              <button className="menu-danger" type="button" role="menuitem" onClick={onLogout}>
                <LogOut size={16} />
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
