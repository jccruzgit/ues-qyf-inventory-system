import {
  Bell,
  LogOut,
  Menu,
  Search,
  Settings,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

function getInitials(fullName) {
  if (!fullName) {
    return 'QV';
  }

  return fullName
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((segment) => segment[0]?.toUpperCase())
    .join('');
}

function getRoleLabel(role) {
  if (!role) {
    return 'Jefatura de investigacion';
  }

  return role
    .toLowerCase()
    .split('_')
    .map((token) => token.charAt(0).toUpperCase() + token.slice(1))
    .join(' ');
}

function Topbar({ onOpenSidebar }) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const profile = {
    name: user?.fullName ?? 'Dr. Julian Vance',
    role: getRoleLabel(user?.role),
    initials: getInitials(user?.fullName),
  };

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  return (
    <header className="px-4 pt-4 sm:px-6 lg:px-8 lg:pt-6">
      <div className="flex items-center gap-3 rounded-[28px] border border-white/70 bg-white/75 px-4 py-3 shadow-[0_14px_32px_rgba(14,47,103,0.08)] backdrop-blur-xl sm:px-5">
        <button
          type="button"
          onClick={onOpenSidebar}
          className="inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-surface-2 text-brand-ink transition hover:bg-brand-ink hover:text-white lg:hidden"
          aria-label="Abrir barra lateral"
        >
          <Menu className="h-5 w-5" />
        </button>

        <div className="relative min-w-0 flex-1">
          <Search className="pointer-events-none absolute left-4 top-1/2 h-[1.125rem] w-[1.125rem] -translate-y-1/2 text-copy-soft" />
          <input
            type="search"
            placeholder="Buscar quimicos, lotes o viales..."
            className="w-full rounded-full border border-transparent bg-surface-2 py-3 pl-11 pr-4 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
          />
        </div>

        <div className="hidden items-center gap-2 sm:flex">
          <button
            type="button"
            className="inline-flex h-11 w-11 items-center justify-center rounded-2xl text-copy transition hover:bg-surface-2 hover:text-brand-ink"
            aria-label="Notificaciones"
          >
            <Bell className="h-5 w-5" />
          </button>
          <button
            type="button"
            className="inline-flex h-11 w-11 items-center justify-center rounded-2xl text-copy transition hover:bg-surface-2 hover:text-brand-ink"
            aria-label="Configuracion"
          >
            <Settings className="h-5 w-5" />
          </button>
        </div>

        <div className="hidden h-10 w-px bg-brand-ink/[0.08] lg:block" />

        <div className="hidden items-center gap-3 lg:flex">
          <div className="text-right">
            <p className="text-sm font-extrabold tracking-tight text-brand-ink">{profile.name}</p>
            <p className="text-xs font-semibold text-copy-soft">{profile.role}</p>
          </div>

          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-brand-ink text-sm font-extrabold text-white shadow-[0_10px_20px_rgba(14,47,103,0.24)]">
            {profile.initials}
          </div>
        </div>

        <button
          type="button"
          onClick={handleLogout}
          className="inline-flex h-11 items-center justify-center gap-2 rounded-2xl border border-brand-ink/[0.08] bg-white px-4 text-sm font-bold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
        >
          <LogOut className="h-4 w-4" />
          <span className="hidden sm:inline">Cerrar sesion</span>
        </button>
      </div>
    </header>
  );
}

export default Topbar;
