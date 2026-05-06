import {
  ArrowDownCircle,
  CalendarClock,
  ChevronDown,
  ChevronUp,
  Eye,
  PackagePlus,
  Rows3,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';

function formatDate(dateValue) {
  if (!dateValue) {
    return 'Sin vencimiento';
  }

  const [year, month, day] = String(dateValue).split('-').map(Number);

  if (!year || !month || !day) {
    return dateValue;
  }

  return new Intl.DateTimeFormat('es-SV', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(year, month - 1, day));
}

function getExpirationCopy(batch) {
  if (!batch?.hasExpiration) {
    return 'Sin control de vencimiento';
  }

  if (batch.isExpired) {
    return 'Vencido';
  }

  if (batch.expiresSoon) {
    return `${batch.daysUntilExpiration} dias para vencer`;
  }

  return `${batch.daysUntilExpiration} dias restantes`;
}

function QuickActionLink({ to, state, icon: Icon, children, variant = 'primary' }) {
  const className =
    variant === 'primary'
      ? 'bg-brand-ink text-white hover:bg-brand-ink-strong'
      : 'border border-brand-ink/[0.08] bg-white text-brand-ink hover:border-brand-teal/30 hover:text-brand-teal';

  return (
    <Link
      to={to}
      state={state}
      className={`inline-flex items-center justify-center gap-2 rounded-full px-4 py-2.5 text-sm font-extrabold transition ${className}`}
    >
      <Icon className="h-4 w-4" />
      {children}
    </Link>
  );
}

function InventoryOverviewItem({ item, isExpanded, onToggle }) {
  const progressTarget = Math.max(item.minimumStock * 2, item.quantityAvailable, 1);
  const nextExpirationBadgeVariant = item.nextExpiringBatch?.isExpired
    ? 'danger'
    : item.nextExpiringBatch?.expiresSoon
      ? 'warning'
      : 'neutral';

  return (
    <Card className="rounded-[28px] px-5 py-5 transition hover:-translate-y-0.5 hover:shadow-[0_22px_40px_rgba(23,61,44,0.1)] sm:px-6">
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1.4fr)_minmax(0,0.9fr)_minmax(0,1fr)_auto] xl:items-start">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant={item.stockState.badgeVariant}>{item.stockState.label}</Badge>
            <Badge variant="teal">{item.category}</Badge>
            <Badge variant="neutral">{item.storageConditionLabel}</Badge>
          </div>

          <div className="mt-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="min-w-0">
                <h3 className="text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                  {item.productName}
                </h3>
                <p className="mt-1 text-sm font-semibold text-copy-soft">
                  REF: {item.productCode} • {item.laboratoryName}
                </p>
              </div>

              <div
                className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-extrabold tracking-tight ${item.stockState.accentClassName}`}
              >
                Minimo {item.minimumStock} {item.unit}
              </div>
            </div>

            <div className="mt-5">
              <div className="mb-2 flex items-center justify-between gap-4">
                <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                  Stock actual
                </p>
                <span className={`text-sm font-extrabold ${item.stockState.toneClassName}`}>
                  {item.quantityAvailable} {item.unit}
                </span>
              </div>
              <ProgressBar
                value={item.quantityAvailable}
                max={progressTarget}
                tone={item.stockState.progressTone}
              />
              <p className="mt-3 text-sm font-semibold text-copy">
                Cobertura visual sobre un objetivo operativo de {progressTarget} {item.unit}
              </p>
            </div>
          </div>
        </div>

        <div className="rounded-[24px] border border-brand-ink/[0.06] bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-4">
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            Trazabilidad
          </p>
          <div className="mt-4 space-y-3 text-sm">
            <div>
              <p className="font-extrabold text-brand-ink">Lotes disponibles</p>
              <p className="mt-1 font-semibold text-copy">
                {item.activeBatchCount > 0 ? item.activeBatchCount : 'Sin lotes activos'}
              </p>
            </div>
            <div>
              <p className="font-extrabold text-brand-ink">Control requerido</p>
              <p className="mt-1 font-semibold text-copy">
                {item.requiresBatchControl || item.requiresExpiration
                  ? 'Seguimiento por lote o vencimiento'
                  : 'Sin control adicional'}
              </p>
            </div>
          </div>
        </div>

        <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white p-4">
          <div className="flex items-center gap-2 text-copy-soft">
            <CalendarClock className="h-4 w-4" />
            <p className="text-xs font-extrabold uppercase tracking-[0.24em]">
              Proximo vencimiento
            </p>
          </div>

          {item.nextExpiringBatch ? (
            <div className="mt-4">
              <Badge variant={nextExpirationBadgeVariant}>
                {getExpirationCopy(item.nextExpiringBatch)}
              </Badge>
              <p className="mt-3 text-base font-extrabold text-brand-ink">
                {item.nextExpiringBatch.batchCode}
              </p>
              <p className="mt-1 text-sm font-semibold text-copy">
                {formatDate(item.nextExpiringBatch.expirationDate)}
              </p>
              <p className="mt-3 text-xs font-bold uppercase tracking-[0.2em] text-copy-soft">
                {item.expiringSoonCount > 0
                  ? `${item.expiringSoonCount} lote(s) vence(n) pronto`
                  : item.expiredBatchCount > 0
                    ? `${item.expiredBatchCount} lote(s) vencido(s)`
                    : 'Sin alertas de vencimiento'}
              </p>
            </div>
          ) : (
            <div className="mt-4">
              <p className="text-base font-extrabold text-brand-ink">Sin vencimientos proximos</p>
              <p className="mt-2 text-sm font-semibold text-copy">
                Este producto no tiene lotes con fecha proxima o no requiere control de caducidad.
              </p>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3">
          <QuickActionLink
            to="/inventory/entries/new"
            state={{
              prefill: {
                productId: item.productId,
                laboratoryId: item.laboratoryId,
              },
            }}
            icon={PackagePlus}
          >
            Nueva entrada
          </QuickActionLink>
          <QuickActionLink
            to="/inventory/exits/new"
            state={{
              prefill: {
                productId: item.productId,
                laboratoryId: item.laboratoryId,
              },
            }}
            icon={ArrowDownCircle}
            variant="secondary"
          >
            Registrar salida
          </QuickActionLink>
          <QuickActionLink
            to="/movements"
            state={{
              prefill: {
                productId: item.productId,
                laboratoryId: item.laboratoryId,
              },
              context: {
                title: item.productName,
                description: `${item.productCode} • ${item.laboratoryName}`,
              },
            }}
            icon={Rows3}
            variant="secondary"
          >
            Ver movimientos
          </QuickActionLink>
          <button
            type="button"
            onClick={onToggle}
            className="inline-flex items-center justify-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2.5 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
          >
            <Eye className="h-4 w-4" />
            Ver detalle
            {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
          </button>
        </div>
      </div>

      {isExpanded ? (
        <div className="mt-6 grid gap-4 border-t border-brand-ink/[0.06] pt-6 lg:grid-cols-[minmax(0,0.42fr)_minmax(0,0.58fr)]">
          <div className="space-y-4">
            <div className="rounded-[24px] border border-brand-ink/[0.06] bg-surface-2/60 p-4">
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Resumen del producto
              </p>
              <div className="mt-4 space-y-3 text-sm font-semibold text-copy">
                <p>
                  <span className="font-extrabold text-brand-ink">Clasificacion:</span> {item.type}
                </p>
                <p>
                  <span className="font-extrabold text-brand-ink">Nivel de riesgo:</span> {item.risk}
                </p>
                <p>
                  <span className="font-extrabold text-brand-ink">Laboratorio:</span>{' '}
                  {item.laboratoryName}
                </p>
                <p>
                  <span className="font-extrabold text-brand-ink">Almacenamiento:</span>{' '}
                  {item.storageConditionLabel}
                </p>
              </div>
            </div>

            <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white p-4">
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Notas
              </p>
              <p className="mt-4 text-sm leading-7 text-copy">
                {item.description || item.observations || 'Sin notas registradas para este producto.'}
              </p>
            </div>
          </div>

          <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white p-4">
            <div className="flex items-center justify-between gap-3">
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Lotes disponibles
              </p>
              <Badge variant="navy">{item.batches.length}</Badge>
            </div>

            {item.batches.length ? (
              <div className="mt-4 space-y-3">
                {item.batches.map((batch) => (
                  <div
                    key={batch.id}
                    className="rounded-[22px] border border-brand-ink/[0.06] bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] px-4 py-3"
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <p className="text-sm font-extrabold text-brand-ink">{batch.batchCode}</p>
                        <p className="mt-1 text-sm font-semibold text-copy">
                          {batch.quantityAvailable} {batch.unit}
                        </p>
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        <Badge
                          variant={
                            batch.isExpired ? 'danger' : batch.expiresSoon ? 'warning' : 'neutral'
                          }
                        >
                          {getExpirationCopy(batch)}
                        </Badge>
                        <span className="text-sm font-semibold text-copy">
                          {formatDate(batch.expirationDate)}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="mt-4 rounded-[22px] border border-dashed border-brand-ink/[0.12] bg-surface-2/60 px-4 py-5 text-sm font-semibold text-copy">
                No hay lotes activos para este producto. La base queda preparada para alertas y
                trazabilidad cuando se registren nuevas entradas.
              </div>
            )}
          </div>
        </div>
      ) : null}
    </Card>
  );
}

export default InventoryOverviewItem;
