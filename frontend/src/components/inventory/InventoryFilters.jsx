import { RotateCcw } from 'lucide-react';
import { FilterSelect, StorageToggle } from '../products/ProductFilters';

function InventoryFilters({
  laboratory,
  category,
  storageCondition,
  stockState,
  laboratoryOptions,
  categoryOptions,
  stockStateOptions,
  onLaboratoryChange,
  onCategoryChange,
  onStorageChange,
  onStockStateChange,
  onReset,
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto_minmax(0,1fr)_auto] xl:items-end">
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
