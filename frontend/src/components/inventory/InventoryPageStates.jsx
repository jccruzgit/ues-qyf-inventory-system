import { AlertTriangle, PackagePlus, SearchX } from 'lucide-react';
import { Link } from 'react-router-dom';
import Card from '../ui/Card';

export function InventoryLoadingState() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <Card key={index} className="animate-pulse rounded-[28px] px-5 py-5 sm:px-6">
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1.4fr)_minmax(0,0.9fr)_minmax(0,1fr)_auto]">
            <div>
              <div className="h-5 w-2/3 rounded-full bg-surface-2" />
              <div className="mt-3 h-3 w-1/2 rounded-full bg-surface-2" />
              <div className="mt-4 h-2 rounded-full bg-surface-2" />
            </div>
            <div className="h-24 rounded-[24px] bg-surface-2" />
            <div className="h-24 rounded-[24px] bg-surface-2" />
            <div className="h-11 w-36 rounded-full bg-surface-2" />
          </div>
        </Card>
      ))}
    </div>
  );
}

export function InventoryErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudo cargar el inventario
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">{message}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
        >
          Reintentar
        </button>
      </div>
    </Card>
  );
}

export function InventoryEmptyState({ isFiltered, onReset }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
          <SearchX className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          {isFiltered ? 'No hay coincidencias para esos filtros' : 'No hay stock disponible'}
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">
          {isFiltered
            ? 'Ajuste laboratorio, categoria, almacenamiento o estado para encontrar registros disponibles.'
            : 'Registre la primera entrada de inventario para comenzar a visualizar stock real, lotes y vencimientos.'}
        </p>
        {isFiltered ? (
          <button
            type="button"
            onClick={onReset}
            className="mt-7 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
          >
            Limpiar filtros
          </button>
        ) : (
          <Link
            to="/inventory/entries/new"
            className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
          >
            <PackagePlus className="h-4 w-4" />
            Registrar primera entrada
          </Link>
        )}
      </div>
    </Card>
  );
}
