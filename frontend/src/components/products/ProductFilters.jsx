import { ChevronDown, Snowflake } from 'lucide-react';

export function FilterSelect({ label, value, options, onChange }) {
  return (
    <label className="min-w-0 flex-1">
      <span className="mb-2 block text-[0.68rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft">
        {label}
      </span>
      <div className="relative">
        <select
          value={value}
          onChange={(event) => onChange(event.target.value)}
          className="w-full appearance-none rounded-full border border-transparent bg-white px-4 py-3.5 pr-11 text-sm font-bold text-brand-ink shadow-[0_12px_28px_rgba(23,61,44,0.06)] outline-none transition focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10"
        >
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-copy-soft" />
      </div>
    </label>
  );
}

export function StorageToggle({ value, onChange, label = 'Condicion de almacenamiento', options }) {
  const toggleOptions = options ?? [
    { value: 'all', label: 'Todas' },
    { value: 'cold', label: 'Refrigerado' },
    { value: 'ambient', label: 'Ambiente' },
  ];

  return (
    <div className="min-w-0">
      <span className="mb-2 block text-[0.68rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft">
        {label}
      </span>
      <div className="inline-flex flex-wrap gap-2 rounded-full bg-white p-1.5 shadow-[0_12px_28px_rgba(23,61,44,0.06)]">
        {toggleOptions.map((option) => {
          const isActive = option.value === value;

          return (
            <button
              key={option.value}
              type="button"
              onClick={() => onChange(option.value)}
              className={`inline-flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-extrabold transition ${
                isActive
                  ? 'bg-brand-ink text-white shadow-[0_10px_22px_rgba(23,61,44,0.18)]'
                  : 'text-copy hover:bg-surface-2 hover:text-brand-ink'
              }`}
            >
              <Snowflake className="h-4 w-4" />
              {option.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function ProductFilters({
  category,
  laboratory,
  storageCondition,
  categoryOptions,
  laboratoryOptions,
  onCategoryChange,
  onLaboratoryChange,
  onStorageChange,
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] xl:items-end">
      <FilterSelect
        label="Filtrar por categoria"
        value={category}
        options={categoryOptions}
        onChange={onCategoryChange}
      />
      <FilterSelect
        label="Ubicacion de laboratorio"
        value={laboratory}
        options={laboratoryOptions}
        onChange={onLaboratoryChange}
      />
      <StorageToggle value={storageCondition} onChange={onStorageChange} />
    </div>
  );
}

export default ProductFilters;
