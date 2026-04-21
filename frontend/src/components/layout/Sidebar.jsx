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
import BrandMark from '../ui/BrandMark';

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

function NavItem({ item, onNavigate }) {
  const Icon = item.icon;

  return (
    <NavLink to={item.to} onClick={onNavigate}>
      {({ isActive }) => (
        <span
          className={`group flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold transition ${
            isActive
              ? 'bg-white text-brand-ink shadow-[0_10px_22px_rgba(15,40,85,0.08)]'
              : 'text-copy hover:bg-white/[0.72] hover:text-brand-ink'
          }`}
        >
          <span
            className={`flex h-8 w-8 items-center justify-center rounded-xl transition ${
              isActive
                ? 'bg-brand-ink text-white'
                : 'bg-surface-2 text-copy-soft group-hover:bg-brand-teal-soft group-hover:text-brand-teal'
            }`}
          >
            <Icon className="h-4 w-4" strokeWidth={2.2} />
          </span>
          <span>{item.label}</span>
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
        className={`fixed inset-y-0 left-0 z-40 flex w-[292px] flex-col bg-[#eff3f8] px-5 pb-6 pt-5 shadow-[18px_0_40px_rgba(14,47,103,0.08)] transition duration-300 lg:sticky lg:top-0 lg:z-10 lg:h-screen lg:translate-x-0 lg:border-r lg:border-white/60 lg:shadow-none ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="mb-8 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <BrandMark className="h-11 w-11 rounded-xl" iconClassName="h-[1.125rem] w-[1.125rem]" />
            <div>
              <p className="text-[1rem] font-extrabold tracking-tight text-brand-ink">
                Inventario Q&F
              </p>
              <p className="text-[0.62rem] font-bold uppercase tracking-[0.28em] text-copy-soft">
                Quimica &amp; Farmacia
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-10 w-10 items-center justify-center rounded-full text-copy transition hover:bg-white hover:text-brand-ink lg:hidden"
            aria-label="Cerrar barra lateral"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="hide-scrollbar space-y-2 overflow-y-auto">
          {primaryItems.map((item) => (
            <NavItem key={item.to} item={item} onNavigate={onClose} />
          ))}
        </nav>

        <Link
          to="/inventory/entries/new"
          onClick={onClose}
          className="mt-auto inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-5 py-4 text-sm font-extrabold text-white shadow-[0_20px_36px_rgba(14,47,103,0.22)] transition hover:-translate-y-0.5 hover:bg-[#0b2551]"
        >
          <Plus className="h-4 w-4" strokeWidth={2.5} />
          Nueva entrada
        </Link>

        <div className="mt-6 space-y-2 border-t border-brand-ink/[0.06] pt-5">
          {secondaryItems.map((item) => (
            <NavItem key={item.to} item={item} onNavigate={onClose} />
          ))}
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
