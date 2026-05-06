import { useEffect, useMemo, useState } from 'react';
import {
  AlertTriangle,
  ArrowDownCircle,
  Boxes,
  CalendarClock,
  PackagePlus,
  Search,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  batchOperationalStateOptions,
  fetchProductBatches,
  getProductBatchesErrorMessage,
} from '../../services/batchesService';

const defaultFilters = {
  search: '',
  laboratory: 'all',
  state: 'all',
};

const quantityFormatter = new Intl.NumberFormat('es-SV', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 4,
});

const currencyFormatter = new Intl.NumberFormat('es-SV', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
});

function formatDate(dateValue) {
  if (!dateValue) {
    return 'Sin fecha registrada';
  }

  const date = new Date(`${dateValue}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return 'Fecha no disponible';
  }

  return new Intl.DateTimeFormat('es-SV', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
}

function formatQuantity(value) {
  return quantityFormatter.format(Number(value) || 0);
}

function formatUnitPrice(batch) {
  if (batch.unitPrice == null) {
    return 'No disponible';
  }

  return `${currencyFormatter.format(batch.unitPrice)} / ${batch.priceUnitLabel}`;
}

function getExpirationCopy(batch) {
  if (!batch.expirationDate) {
    return 'Sin vencimiento registrado';
  }

  if (batch.operationalState.key === 'agotado') {
    return 'Lote sin existencias';
  }

  if (batch.daysUntilExpiration == null) {
    return 'Fecha no disponible';
  }

  if (batch.daysUntilExpiration < 0) {
    return `Vencido hace ${Math.abs(batch.daysUntilExpiration)} dia(s)`;
  }

  if (batch.daysUntilExpiration === 0) {
    return 'Vence hoy';
  }

  return `Vence en ${batch.daysUntilExpiration} dia(s)`;
}

function BatchesLoadingState() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <Card
          key={index}
          className="animate-pulse bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]"
        >
          <div className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="space-y-2">
                <div className="h-3 w-24 rounded-full bg-surface-2" />
                <div className="h-5 w-56 rounded-full bg-surface-2" />
              </div>
              <div className="h-8 w-32 rounded-full bg-surface-2" />
            </div>
            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
              {Array.from({ length: 8 }).map((__, cellIndex) => (
                <div key={cellIndex} className="h-20 rounded-[24px] bg-surface-2" />
              ))}
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}

function BatchesErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudieron cargar los lotes
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

function BatchesEmptyState({ isFiltered, onReset }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
          <Boxes className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          {isFiltered ? 'No hay lotes para esos filtros' : 'Sin lotes registrados'}
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">
          {isFiltered
            ? 'Ajuste la busqueda, el laboratorio o el estado para ampliar el resultado.'
            : 'Cuando registre entradas con control por lote, esta seccion mostrara el detalle real de stock, vencimiento, precio unitario y observaciones.'}
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
            Registrar entrada
          </Link>
        )}
      </div>
    </Card>
  );
}

function FilterSelect({ label, value, options, onChange }) {
  return (
    <label className="space-y-2">
      <span className="text-xs font-extrabold uppercase tracking-[0.22em] text-copy-soft">
        {label}
      </span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-[22px] border border-brand-ink/[0.08] bg-white px-4 py-3 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/30 focus:ring-4 focus:ring-brand-teal/10"
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

function InfoCell({ label, value, emphasize = false }) {
  return (
    <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white/90 px-4 py-3">
      <p className="text-[11px] font-extrabold uppercase tracking-[0.22em] text-copy-soft">{label}</p>
      <p className={`mt-2 text-sm ${emphasize ? 'font-extrabold text-brand-ink' : 'font-semibold text-copy'}`}>
        {value}
      </p>
    </div>
  );
}

function BatchCard({ batch }) {
  return (
    <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
      <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            {batch.laboratoryCode ? `${batch.laboratoryCode} · ` : ''}
            {batch.laboratoryName}
          </p>
          <h3 className="mt-2 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
            {batch.productName}
          </h3>
          <p className="mt-2 text-sm font-semibold text-copy">
            Codigo producto: <span className="text-brand-ink">{batch.productCode}</span>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Badge variant="navy">Lote {batch.batchCode}</Badge>
          <Badge variant={batch.operationalState.variant}>{batch.operationalState.label}</Badge>
        </div>
      </div>

      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <InfoCell label="Disponible" value={`${formatQuantity(batch.quantityAvailable)} ${batch.unitLabel}`} emphasize />
        <InfoCell label="Vencimiento" value={formatDate(batch.expirationDate)} />
        <InfoCell label="Seguimiento" value={getExpirationCopy(batch)} />
        <InfoCell label="Precio unitario" value={formatUnitPrice(batch)} />
        <InfoCell label="Laboratorio" value={batch.laboratoryName} />
        <InfoCell label="Ubicacion" value={batch.locationName || 'No disponible'} />
        <InfoCell label="Unidad" value={batch.unitLabel} />
        <InfoCell label="Observaciones" value={batch.notes || 'Sin observaciones'} />
      </div>
    </Card>
  );
}

function BatchesPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [batches, setBatches] = useState([]);
  const [filters, setFilters] = useState(defaultFilters);

  const loadBatches = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchProductBatches();
      setBatches(response);
    } catch (requestError) {
      setBatches([]);
      setError(getProductBatchesErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBatches();
  }, []);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const laboratoryOptions = useMemo(() => {
    const options = batches
      .map((batch) => ({
        value: String(batch.laboratoryId),
        label: batch.laboratoryName,
      }))
      .filter((option, index, collection) => {
        return (
          option.value &&
          collection.findIndex((candidate) => candidate.value === option.value) === index
        );
      })
      .sort((left, right) => left.label.localeCompare(right.label));

    return [{ value: 'all', label: 'Todos los laboratorios' }, ...options];
  }, [batches]);

  const filteredBatches = useMemo(() => {
    const normalizedSearch = filters.search.trim().toLowerCase();

    return batches.filter((batch) => {
      const searchableText = [batch.productName, batch.productCode, batch.batchCode]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      const matchesSearch = !normalizedSearch || searchableText.includes(normalizedSearch);
      const matchesLaboratory =
        filters.laboratory === 'all' || String(batch.laboratoryId) === filters.laboratory;
      const matchesState =
        filters.state === 'all' || batch.operationalState.key === filters.state;

      return matchesSearch && matchesLaboratory && matchesState;
    });
  }, [batches, filters.laboratory, filters.search, filters.state]);

  const visibleCount = filteredBatches.length;
  const expiringCount = filteredBatches.filter((batch) => batch.operationalState.key === 'proximo').length;
  const expiredCount = filteredBatches.filter((batch) => batch.operationalState.key === 'vencido').length;
  const exhaustedCount = filteredBatches.filter((batch) => batch.operationalState.key === 'agotado').length;
  const isFiltered = Object.values(filters).some((value) => value && value !== 'all');

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Lotes"
        subtitle="Consulta lotes reales por producto y laboratorio con stock disponible, vencimiento, precio unitario y observaciones."
        action={
          <div className="flex flex-wrap items-center justify-end gap-3">
            <Link
              to="/inventory/exits/new"
              className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink shadow-[0_12px_24px_rgba(23,61,44,0.08)] transition hover:-translate-y-0.5 hover:border-brand-teal/30 hover:text-brand-teal"
            >
              <ArrowDownCircle className="h-4 w-4" strokeWidth={2.4} />
              Registrar salida
            </Link>
            <Link
              to="/inventory/entries/new"
              className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
            >
              <PackagePlus className="h-4 w-4" strokeWidth={2.4} />
              Nueva entrada
            </Link>
          </div>
        }
      />

      {notice ? (
        <div className="rounded-[24px] border border-[#d2e6d8] bg-[#eef6f0] px-4 py-3 text-sm font-semibold text-[#2d7a49]">
          {notice}
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Lotes visibles"
          value={visibleCount}
          icon={Boxes}
          meta="Consulta actual"
          metaVariant="teal"
          accent="bg-brand-teal-soft text-brand-teal"
        />
        <StatCard
          title="Proximos a vencer"
          value={expiringCount}
          icon={CalendarClock}
          meta="30 dias"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
        />
        <StatCard
          title="Vencidos"
          value={expiredCount}
          icon={AlertTriangle}
          meta="Atencion"
          metaVariant="danger"
          accent="bg-[#fdebec] text-[#d53a43]"
        />
        <StatCard
          title="Agotados"
          value={exhaustedCount}
          icon={Search}
          meta="Sin stock"
          metaVariant="warning"
          accent="bg-surface-2 text-copy"
        />
      </section>

      <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
              Filtros operativos
            </p>
            <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
              Vista detallada por lote
            </h3>
          </div>

          <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-3 text-sm font-semibold text-copy">
            {batches.length} lote(s) cargado(s) desde el backend para esta sesion.
          </div>
        </div>

        <div className="grid gap-4 xl:grid-cols-[minmax(0,1.3fr)_minmax(220px,0.7fr)_minmax(220px,0.7fr)_auto]">
          <label className="space-y-2">
            <span className="text-xs font-extrabold uppercase tracking-[0.22em] text-copy-soft">
              Buscar producto o lote
            </span>
            <div className="relative">
              <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-copy-soft" />
              <input
                type="search"
                value={filters.search}
                onChange={(event) =>
                  setFilters((currentFilters) => ({
                    ...currentFilters,
                    search: event.target.value,
                  }))
                }
                placeholder="Ej. Acetona o LOT-2026-001"
                className="w-full rounded-[22px] border border-brand-ink/[0.08] bg-white py-3 pl-11 pr-4 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/30 focus:ring-4 focus:ring-brand-teal/10"
              />
            </div>
          </label>

          <FilterSelect
            label="Laboratorio"
            value={filters.laboratory}
            options={laboratoryOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, laboratory: value }))
            }
          />

          <FilterSelect
            label="Estado"
            value={filters.state}
            options={batchOperationalStateOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, state: value }))
            }
          />

          <div className="flex items-end">
            <button
              type="button"
              onClick={() => setFilters(defaultFilters)}
              className="w-full rounded-[22px] border border-brand-ink/[0.08] bg-white px-4 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
            >
              Limpiar
            </button>
          </div>
        </div>
      </Card>

      {loading ? (
        <BatchesLoadingState />
      ) : error ? (
        <BatchesErrorState message={error} onRetry={loadBatches} />
      ) : filteredBatches.length ? (
        <section className="space-y-4">
          {filteredBatches.map((batch) => (
            <BatchCard key={batch.id} batch={batch} />
          ))}
        </section>
      ) : (
        <BatchesEmptyState
          isFiltered={isFiltered}
          onReset={() => setFilters(defaultFilters)}
        />
      )}
    </div>
  );
}

export default BatchesPage;
