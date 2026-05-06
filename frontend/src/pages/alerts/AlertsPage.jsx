import { useEffect, useMemo, useState } from 'react';
import {
  AlertTriangle,
  BellRing,
  Boxes,
  CalendarClock,
  SearchX,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  alertSeverityOptions,
  alertStatusOptions,
  alertTypeOptions,
  fetchInventoryAlerts,
  getInventoryAlertsErrorMessage,
} from '../../services/alertsService';

const defaultFilters = {
  alertType: 'all',
  laboratory: 'all',
  severity: 'all',
  status: 'PENDIENTE',
};

const quantityFormatter = new Intl.NumberFormat('es-SV', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 4,
});

function formatDate(dateValue) {
  if (!dateValue) {
    return 'No aplica';
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

function formatRelativeDateTime(dateTimeValue) {
  if (!dateTimeValue) {
    return 'Sin fecha';
  }

  const parsedDate = new Date(dateTimeValue);

  if (Number.isNaN(parsedDate.getTime())) {
    return 'Sin fecha';
  }

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(parsedDate);
}

function formatQuantity(value) {
  if (value == null) {
    return 'No disponible';
  }

  return quantityFormatter.format(value);
}

function AlertsLoadingState() {
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
                <div className="h-3 w-28 rounded-full bg-surface-2" />
                <div className="h-5 w-64 rounded-full bg-surface-2" />
              </div>
              <div className="h-8 w-36 rounded-full bg-surface-2" />
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

function AlertsErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudieron cargar las alertas
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

function AlertsEmptyState({ isFiltered, onReset }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
          <SearchX className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          {isFiltered ? 'No hay alertas para esos filtros' : 'Sin alertas activas'}
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">
          {isFiltered
            ? 'Ajuste el tipo, laboratorio, prioridad o estado para ampliar el resultado.'
            : 'No se encontraron alertas relevantes para los laboratorios accesibles en este momento.'}
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
            to="/inventory"
            className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
          >
            Revisar inventario
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

function AlertCard({ alert }) {
  return (
    <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
      <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            {alert.laboratoryCode ? `${alert.laboratoryCode} · ` : ''}
            {alert.laboratoryName}
          </p>
          <h3 className="mt-2 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
            {alert.productName}
          </h3>
          <p className="mt-2 text-sm font-semibold text-copy">
            Codigo producto: <span className="text-brand-ink">{alert.productCode}</span>
            {alert.batchCode ? ` · Lote ${alert.batchCode}` : ''}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Badge variant={alert.alertTypeVariant}>{alert.alertTypeLabel}</Badge>
          <Badge variant={alert.severityVariant}>{alert.severityLabel}</Badge>
          <Badge variant={alert.statusVariant}>{alert.statusLabel}</Badge>
        </div>
      </div>

      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <InfoCell label="Stock actual" value={formatQuantity(alert.quantityAvailable)} emphasize />
        <InfoCell label="Stock minimo" value={formatQuantity(alert.minimumStock)} />
        <InfoCell label="Vencimiento" value={formatDate(alert.expirationDate)} />
        <InfoCell label="Ubicacion" value={alert.locationName || 'No disponible'} />
        <InfoCell label="Laboratorio" value={alert.laboratoryName} />
        <InfoCell label="Tipo de alerta" value={alert.alertTypeLabel} />
        <InfoCell label="Estado" value={alert.statusLabel} />
        <InfoCell label="Registrada" value={formatRelativeDateTime(alert.triggeredAt)} />
      </div>

      <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white/80 px-4 py-3">
        <p className="text-[11px] font-extrabold uppercase tracking-[0.22em] text-copy-soft">Detalle</p>
        <p className="mt-2 text-sm font-semibold leading-7 text-copy">{alert.message}</p>
      </div>
    </Card>
  );
}

function AlertsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [alerts, setAlerts] = useState([]);
  const [filters, setFilters] = useState(defaultFilters);

  const loadAlerts = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchInventoryAlerts({ pendingOnly: false });
      setAlerts(response);
    } catch (requestError) {
      setAlerts([]);
      setError(getInventoryAlertsErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts();
  }, []);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const laboratoryOptions = useMemo(() => {
    const options = alerts
      .map((alert) => ({
        value: String(alert.laboratoryId),
        label: alert.laboratoryName,
      }))
      .filter((option, index, collection) => {
        return (
          option.value &&
          collection.findIndex((candidate) => candidate.value === option.value) === index
        );
      })
      .sort((left, right) => left.label.localeCompare(right.label));

    return [{ value: 'all', label: 'Todos los laboratorios' }, ...options];
  }, [alerts]);

  const filteredAlerts = useMemo(() => {
    return alerts.filter((alert) => {
      const matchesType = filters.alertType === 'all' || alert.alertType === filters.alertType;
      const matchesLaboratory =
        filters.laboratory === 'all' || String(alert.laboratoryId) === filters.laboratory;
      const matchesSeverity = filters.severity === 'all' || alert.severity === filters.severity;
      const matchesStatus = filters.status === 'all' || alert.status === filters.status;

      return matchesType && matchesLaboratory && matchesSeverity && matchesStatus;
    });
  }, [alerts, filters.alertType, filters.laboratory, filters.severity, filters.status]);

  const visibleCount = filteredAlerts.length;
  const criticalCount = filteredAlerts.filter((alert) => alert.severity === 'CRITICA').length;
  const pendingCount = filteredAlerts.filter((alert) => alert.status === 'PENDIENTE').length;
  const expirationCount = filteredAlerts.filter((alert) =>
    ['EXPIRING_BATCH', 'EXPIRED_BATCH'].includes(alert.alertType),
  ).length;
  const isFiltered = Object.entries(filters).some(
    ([key, value]) => value !== defaultFilters[key] && value !== 'all',
  );

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Alertas"
        subtitle="Consulta alertas reales por stock, vencimiento y disponibilidad para priorizar acciones operativas."
        action={
          <div className="flex flex-wrap items-center justify-end gap-3">
            <Link
              to="/inventory"
              className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink shadow-[0_12px_24px_rgba(23,61,44,0.08)] transition hover:-translate-y-0.5 hover:border-brand-teal/30 hover:text-brand-teal"
            >
              <Boxes className="h-4 w-4" strokeWidth={2.4} />
              Revisar stock
            </Link>
            <Link
              to="/batches"
              className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
            >
              <BellRing className="h-4 w-4" strokeWidth={2.4} />
              Ver lotes
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
          title="Alertas visibles"
          value={visibleCount}
          icon={BellRing}
          meta="Consulta actual"
          metaVariant="teal"
          accent="bg-brand-teal-soft text-brand-teal"
        />
        <StatCard
          title="Criticas"
          value={criticalCount}
          icon={AlertTriangle}
          meta="Prioridad alta"
          metaVariant="danger"
          accent="bg-[#fdebec] text-[#d53a43]"
        />
        <StatCard
          title="Pendientes"
          value={pendingCount}
          icon={Boxes}
          meta="Requieren seguimiento"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
        />
        <StatCard
          title="De vencimiento"
          value={expirationCount}
          icon={CalendarClock}
          meta="Lotes comprometidos"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
        />
      </section>

      <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
              Filtros operativos
            </p>
            <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
              Vista consolidada de alertas
            </h3>
          </div>

          <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-3 text-sm font-semibold text-copy">
            {alerts.length} alerta(s) cargada(s) desde el backend para esta sesion.
          </div>
        </div>

        <div className="grid gap-4 xl:grid-cols-5">
          <FilterSelect
            label="Tipo"
            value={filters.alertType}
            options={alertTypeOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, alertType: value }))
            }
          />
          <FilterSelect
            label="Laboratorio"
            value={filters.laboratory}
            options={laboratoryOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, laboratory: value }))
            }
          />
          <FilterSelect
            label="Prioridad"
            value={filters.severity}
            options={alertSeverityOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, severity: value }))
            }
          />
          <FilterSelect
            label="Estado"
            value={filters.status}
            options={alertStatusOptions}
            onChange={(value) =>
              setFilters((currentFilters) => ({ ...currentFilters, status: value }))
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
        <AlertsLoadingState />
      ) : error ? (
        <AlertsErrorState message={error} onRetry={loadAlerts} />
      ) : filteredAlerts.length ? (
        <section className="space-y-4">
          {filteredAlerts.map((alert) => (
            <AlertCard key={alert.id} alert={alert} />
          ))}
        </section>
      ) : (
        <AlertsEmptyState
          isFiltered={isFiltered}
          onReset={() => setFilters(defaultFilters)}
        />
      )}
    </div>
  );
}

export default AlertsPage;
