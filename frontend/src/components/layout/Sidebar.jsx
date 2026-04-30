import {
  Archive,
  BellRing,
  Boxes,
  ChevronRight,
  LayoutDashboard,
  PackageSearch,
  Plus,
  ShieldAlert,
  TriangleAlert,
  X,
} from 'lucide-react';
import { Link, NavLink } from 'react-router-dom';

const primaryItems = [
  { label: 'Panel', icon: LayoutDashboard, to: '/dashboard' },
  { label: 'Productos', icon: PackageSearch, to: '/products' },
  { label: 'Inventario', icon: Boxes, to: '/inventory' },
  { label: 'Movimientos', icon: ChevronRight, to: '/movements' },
  { label: 'Lotes', icon: BellRing, to: '/batches' },
  { label: 'Alertas', icon: TriangleAlert, to: '/alerts' },
];

const secondaryItems = [
  { label: 'Soporte', icon: ShieldAlert, to: '/support' },
  { label: 'Archivo', icon: Archive, to: '/archive' },
];

function SidebarIdentity() {
  return (
    <div className="min-w-0 flex-1 rounded-[26px] border border-white/80 bg-white/72 px-4 py-4 shadow-[0_12px_28px_rgba(23,61,44,0.08)] backdrop-blur-sm">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-brand-ink text-[0.72rem] font-extrabold uppercase tracking-[0.18em] text-white shadow-[0_12px_24px_rgba(23,61,44,0.18)]">
          Q&amp;F
        </div>

        <div className="min-w-0">
          <p className="truncate text-sm font-extrabold leading-tight tracking-tight text-brand-ink">
            Sistema de Inventario
          </p>
          <p className="mt-1 text-[0.62rem] font-bold uppercase tracking-[0.26em] text-copy-soft">
            Q&amp;F
          </p>
          <p className="truncate text-xs font-medium text-copy">Tecnologia Farmaceutica</p>
        </div>
      </div>
    </div>
  );
}

function NavItem({ item, onNavigate }) {
  const Icon = item.icon;

  return (
    <NavLink to={item.to} onClick={onNavigate}>
      {({ isActive }) => (
        <span
          className={`group flex items-center gap-3 rounded-2xl px-3.5 py-3 text-sm font-bold transition ${
            isActive
              ? 'bg-white text-brand-ink shadow-[0_10px_22px_rgba(23,61,44,0.1)] ring-1 ring-brand-teal/12'
              : 'text-copy hover:bg-white/[0.78] hover:text-brand-ink'
          }`}
        >
          <span
            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl transition ${
              isActive
                ? 'bg-brand-teal text-white shadow-[0_10px_18px_rgba(33,115,70,0.22)]'
                : 'bg-surface-2 text-copy-soft group-hover:bg-brand-teal-soft group-hover:text-brand-teal'
            }`}
          >
            <Icon className="h-4 w-4" strokeWidth={2.2} />
          </span>
          <span className="truncate">{item.label}</span>
        </span>
      )}
    </NavLink>
  );
}

function Sidebar({ mobileOpen, onClose }) {
  return (
    <>
      <div
        className={`fixed inset-0 z-30 bg-brand-ink/35 backdrop-blur-[2px] transition lg:hidden ${
          mobileOpen ? 'opacity-100' : 'pointer-events-none opacity-0'
        }`}
        onClick={onClose}
      />

      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-[288px] flex-col bg-[linear-gradient(180deg,_#edf4ee_0%,_#f6faf7_100%)] px-4 pb-5 pt-4 shadow-[18px_0_40px_rgba(23,61,44,0.08)] transition duration-300 lg:sticky lg:top-0 lg:z-10 lg:h-screen lg:translate-x-0 lg:border-r lg:border-white/60 lg:shadow-none ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="mb-6 flex items-start gap-3">
          <SidebarIdentity />

          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-copy transition hover:bg-white hover:text-brand-ink lg:hidden"
            aria-label="Cerrar barra lateral"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="hide-scrollbar flex-1 space-y-1.5 overflow-y-auto pr-1">
          {primaryItems.map((item) => (
            <NavItem key={item.to} item={item} onNavigate={onClose} />
          ))}
        </nav>

        <Link
          to="/inventory/entries/new"
          onClick={onClose}
          className="mt-5 inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-5 py-4 text-sm font-extrabold text-white shadow-[0_20px_36px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
        >
          <Plus className="h-4 w-4" strokeWidth={2.5} />
          Nueva entrada
        </Link>

        <div className="mt-5 space-y-1.5 border-t border-brand-ink/[0.06] pt-4">
          {secondaryItems.map((item) => (
            <NavItem key={item.to} item={item} onNavigate={onClose} />
          ))}
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
