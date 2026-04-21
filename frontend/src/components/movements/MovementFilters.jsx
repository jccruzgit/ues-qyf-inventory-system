import { CalendarRange, RotateCcw } from 'lucide-react';
import { FilterSelect } from '../products/ProductFilters';

function DateField({ label, value, onChange, max, min }) {
  return (
    <label className="min-w-0 flex-1">
      <span className="mb-2 block text-[0.68rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft">
        {label}
      </span>
      <div className="relative">
        <input
          type="date"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          max={max}
          min={min}
          className="w-full rounded-full border border-transparent bg-white px-4 py-3.5 pr-11 text-sm font-bold text-brand-ink shadow-[0_12px_28px_rgba(14,47,103,0.06)] outline-none transition focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10"
        />
        <CalendarRange className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-copy-soft" />
      </div>
    </label>
  );
}

function MovementFilters({
  filters,
  productOptions,
  laboratoryOptions,
  onFilterChange,
  onReset,
}) {
  const movementTypeOptions = [
    { value: 'all', label: 'Todos los tipos' },
    { value: 'ENTRY', label: 'Entrada' },
    { value: 'EXIT', label: 'Salida' },
  ];

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,0.85fr)_minmax(0,1fr)_minmax(0,1fr)_auto] xl:items-end">
      <DateField
        label="Fecha desde"
        value={filters.dateFrom}
        max={filters.dateTo || undefined}
        onChange={(value) => onFilterChange('dateFrom', value)}
      />
      <DateField
        label="Fecha hasta"
        value={filters.dateTo}
        min={filters.dateFrom || undefined}
        onChange={(value) => onFilterChange('dateTo', value)}
      />
      <FilterSelect
        label="Tipo de movimiento"
        value={filters.movementType}
        options={movementTypeOptions}
        onChange={(value) => onFilterChange('movementType', value)}
      />
      <FilterSelect
        label="Producto"
        value={filters.productId}
        options={productOptions}
        onChange={(value) => onFilterChange('productId', value)}
      />
      <FilterSelect
        label="Laboratorio"
        value={filters.laboratoryId}
        options={laboratoryOptions}
        onChange={(value) => onFilterChange('laboratoryId', value)}
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

export default MovementFilters;
