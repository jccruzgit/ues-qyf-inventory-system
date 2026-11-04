import { RotateCcw, Search } from 'lucide-react';
import { FilterSelect, StorageToggle } from '../products/ProductFilters';

function InventoryFilters({
  search,
  laboratory,
  category,
  storageCondition,
  stockState,
  laboratoryOptions,
  categoryOptions,
  stockStateOptions,
  onSearchChange,
  onLaboratoryChange,
  onCategoryChange,
  onStorageChange,
  onStockStateChange,
  onReset,
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-2 2xl:grid-cols-[minmax(220px,1.25fr)_minmax(0,1fr)_minmax(0,1fr)_auto_minmax(0,1fr)_auto] 2xl:items-end">
      <label className="block">
        <span className="text-xs font-extrabold uppercase tracking-[0.16em] text-copy-soft">
          Buscar por nombre
        </span>
        <span className="mt-2 flex items-center gap-3 rounded-[20px] border border-transparent bg-white px-4 py-3.5 transition focus-within:border-brand-teal/25 focus-within:ring-4 focus-within:ring-brand-teal/10">
          <Search className="h-4 w-4 shrink-0 text-copy-soft" strokeWidth={2.2} />
          <input
            type="search"
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Ej. Acido citrico"
            className="min-w-0 flex-1 bg-transparent text-sm font-semibold text-brand-ink outline-none placeholder:text-copy-soft/70"
          />
        </span>
      </label>
      <FilterSelect
        label="Filtrar por laboratorio"
        value={laboratory}
        options={laboratoryOptions}
        onChange={onLaboratoryChange}
      />
      <FilterSelect
        label="Filtrar por categoria"
        value={category}
        options={categoryOptions}
        onChange={onCategoryChange}
      />
      <StorageToggle
        value={storageCondition}
        onChange={onStorageChange}
        label="Condicion de almacenamiento"
      />
      <FilterSelect
        label="Estado de stock"
        value={stockState}
        options={stockStateOptions}
        onChange={onStockStateChange}
      />
      <button
        type="button"
        onClick={onReset}
        className="inline-flex items-center justify-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3.5 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
      >
        <RotateCcw className="h-4 w-4" />
        Limpiar
      </button>
    </div>
  );
}

export default InventoryFilters;
