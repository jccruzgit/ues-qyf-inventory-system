import { useEffect, useMemo, useState } from 'react';
import {
  Activity,
  AlertTriangle,
  Beaker,
  Boxes,
  CalendarRange,
  FlaskConical,
  ShieldCheck,
} from 'lucide-react';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import ProgressBar from '../../components/ui/ProgressBar';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  fetchDashboardSummary,
  getDashboardErrorMessage,
} from '../../services/dashboardService';

const activityToneClasses = {
  teal: 'bg-brand-teal-soft text-brand-teal',
  success: 'bg-[#ebf8ef] text-[#57a763]',
  danger: 'bg-[#fdebec] text-[#d53a43]',
  neutral: 'bg-surface-2 text-copy-soft',
};

function formatInteger(value) {
  return new Intl.NumberFormat('es-SV', {
    maximumFractionDigits: 0,
  }).format(Number(value ?? 0));
}

function formatQuantity(value) {
  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0));
}

function formatRelativeTime(value) {
  if (!value) {
    return 'Sin fecha';
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return 'Sin fecha';
  }

  const differenceInMinutes = Math.round((Date.now() - parsedDate.getTime()) / 60000);

  if (differenceInMinutes < 1) {
    return 'Hace un momento';
  }

  if (differenceInMinutes < 60) {
    return `Hace ${differenceInMinutes} min`;
  }

  const differenceInHours = Math.round(differenceInMinutes / 60);

  if (differenceInHours < 24) {
    return `Hace ${differenceInHours} h`;
  }

  const differenceInDays = Math.round(differenceInHours / 24);
  return `Hace ${differenceInDays} dia${differenceInDays === 1 ? '' : 's'}`;
}

function getActivityTone(movementType) {
  return movementType === 'EXIT' ? 'danger' : 'teal';
}

function getActivityTitle(item) {
  if (item.lineCount > 1) {
    return item.movementType === 'EXIT'
      ? `Salida de ${item.lineCount} productos`
      : `Entrada de ${item.lineCount} productos`;
  }

  const productName = item.primaryProductName || 'producto';

  return item.movementType === 'EXIT'
    ? `Salida: ${productName}`
    : `Entrada: ${productName}`;
}

function getActivityDetail(item) {
  return `${item.performedByUsername} | ${item.laboratoryName} | ${formatQuantity(
    item.totalQuantity,
  )} unidades`;
}

function DashboardChart({ movementSeries }) {
  const maxValue = Math.max(
    ...movementSeries.flatMap((point) => [point.entryQuantity, point.exitQuantity]),
    0,
  );

  if (!movementSeries.length || maxValue === 0) {
    return (
      <div className="mt-8 rounded-[24px] border border-dashed border-brand-ink/[0.08] bg-surface-2/50 px-5 py-10 text-center text-sm font-semibold text-copy-soft">
        Aun no hay movimientos suficientes para construir la serie semanal.
      </div>
    );
  }

  return (
    <div className="mt-8">
      <div className="grid h-[290px] grid-cols-7 items-end gap-3 sm:gap-4">
        {movementSeries.map((point) => {
          const entryHeight =
            point.entryQuantity > 0 ? Math.max((point.entryQuantity / maxValue) * 100, 6) : 0;
          const exitHeight =
            point.exitQuantity > 0 ? Math.max((point.exitQuantity / maxValue) * 100, 6) : 0;

          return (
            <div
              key={`${point.date}-${point.dayLabel}`}
              className="flex h-full flex-col items-center justify-end gap-3"
            >
              <div className="flex h-full items-end gap-1.5 sm:gap-2">
                <div
                  className="w-3 rounded-full bg-[#dde4ef] transition hover:bg-[#c8d5ea] sm:w-4"
                  style={{ height: `${entryHeight}%` }}
                />
                <div
                  className="w-3 rounded-full bg-brand-teal transition hover:bg-[#0a787b] sm:w-4"
                  style={{ height: `${exitHeight}%` }}
                />
              </div>
              <span className="text-[0.68rem] font-extrabold tracking-[0.2em] text-copy-soft">
                {point.dayLabel}
              </span>
            </div>
          );
        })}
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-5 border-t border-brand-ink/[0.06] pt-4">
        <div className="flex items-center gap-2 text-xs font-bold text-copy">
          <span className="h-2.5 w-2.5 rounded-full bg-[#dde4ef]" />
          Ingresos
        </div>
        <div className="flex items-center gap-2 text-xs font-bold text-copy">
          <span className="h-2.5 w-2.5 rounded-full bg-brand-teal" />
          Salidas
        </div>
      </div>
    </div>
  );
}

function DashboardLoadingState() {
  return (
    <div className="space-y-6">
      <section className="grid gap-4 xl:grid-cols-3">
        {Array.from({ length: 3 }).map((_, index) => (
          <Card key={index} className="animate-pulse p-6">
            <div className="h-12 w-12 rounded-2xl bg-surface-2" />
            <div className="mt-10 h-4 w-32 rounded-full bg-surface-2" />
            <div className="mt-4 h-10 w-28 rounded-full bg-surface-2" />
          </Card>
        ))}
      </section>

      <section className="grid gap-5 xl:grid-cols-[minmax(0,1.5fr)_340px]">
        <Card className="animate-pulse p-7">
          <div className="h-6 w-48 rounded-full bg-surface-2" />
          <div className="mt-3 h-4 w-72 rounded-full bg-surface-2" />
          <div className="mt-8 h-[290px] rounded-[24px] bg-surface-2" />
        </Card>

        <Card className="animate-pulse p-7">
          <div className="h-6 w-40 rounded-full bg-surface-2" />
          <div className="mt-6 space-y-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <div key={index} className="flex gap-4">
                <div className="h-10 w-10 rounded-full bg-surface-2" />
                <div className="flex-1">
                  <div className="h-4 w-40 rounded-full bg-surface-2" />
                  <div className="mt-3 h-3 w-52 rounded-full bg-surface-2" />
                </div>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}

function DashboardErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f9fbff_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudo cargar el dashboard
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">{message}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
        >
          Reintentar
        </button>
      </div>
    </Card>
  );
}

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadSummary = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchDashboardSummary();
      setSummary(response);
    } catch (requestError) {
      setSummary(null);
      setError(getDashboardErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSummary();
  }, []);

  const kpiCards = useMemo(() => {
    if (!summary) {
      return [];
    }

    return [
      {
        title: 'Productos activos',
        value: formatInteger(summary.totalActiveProducts),
        meta: `${formatInteger(summary.accessibleLaboratories)} laboratorios`,
        metaVariant: 'navy',
        icon: Beaker,
        accent: 'bg-[#e9f3ff] text-brand-ink',
        illustration: FlaskConical,
      },
      {
        title: 'Stock bajo',
        value: formatInteger(summary.lowStockProducts),
        meta: summary.lowStockProducts > 0 ? 'Requiere revision' : 'Controlado',
        metaVariant: summary.lowStockProducts > 0 ? 'danger' : 'success',
        icon: ShieldCheck,
        accent: 'bg-[#fdebec] text-[#d53a43]',
        illustration: Boxes,
      },
      {
        title: 'Lotes por vencer',
        value: formatInteger(summary.expiringBatches),
        meta: 'Proximos 30 dias',
        metaVariant: summary.expiringBatches > 0 ? 'warning' : 'success',
        icon: CalendarRange,
        accent: 'bg-[#ebf8ef] text-[#65a96e]',
        illustration: Beaker,
      },
    ];
  }, [summary]);

  const maxLaboratoryQuantity = useMemo(
    () =>
      Math.max(
        ...(summary?.inventoryByLaboratory?.map((item) => item.quantityAvailable) ?? []),
        1,
      ),
    [summary],
  );

  if (loading) {
    return <DashboardLoadingState />;
  }

  if (error) {
    return <DashboardErrorState message={error} onRetry={loadSummary} />;
  }

  if (!summary) {
    return null;
  }

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Resumen del laboratorio"
        subtitle="Estado en tiempo real del inventario de la Facultad de Quimica y Farmacia."
      />

      <section className="grid gap-4 xl:grid-cols-3">
        {kpiCards.map((card) => {
          const Illustration = card.illustration;

          return (
            <StatCard
              key={card.title}
              title={card.title}
              value={card.value}
              meta={card.meta}
              metaVariant={card.metaVariant}
              icon={card.icon}
              accent={card.accent}
              illustration={<Illustration className="h-24 w-24" strokeWidth={1.3} />}
            />
          );
        })}
      </section>

      <section className="grid gap-5 xl:grid-cols-[minmax(0,1.5fr)_340px]">
        <Card className="p-6 sm:p-7">
          <SectionHeader
            title="Movimientos de inventario"
            subtitle="Visualizacion semanal del ingreso y consumo de reactivos"
            action={
              <Badge variant="navy" className="px-4 py-2">
                Ultimos 7 dias
              </Badge>
            }
          />

          <DashboardChart movementSeries={summary.movementSeries} />
        </Card>

        <Card className="p-6 sm:p-7">
          <SectionHeader
            title="Actividad reciente"
            action={<Badge variant="teal">{formatInteger(summary.recentMovements.length)} eventos</Badge>}
          />

          <div className="mt-6 space-y-5">
            {summary.recentMovements.length ? (
              summary.recentMovements.map((item) => (
                <article key={item.id} className="flex items-start gap-4">
                  <div
                    className={`mt-1 flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${
                      activityToneClasses[getActivityTone(item.movementType)]
                    }`}
                  >
                    <Activity className="h-4 w-4" strokeWidth={2.3} />
                  </div>

                  <div className="min-w-0">
                    <h3 className="text-sm font-extrabold leading-6 text-brand-ink">
                      {getActivityTitle(item)}
                    </h3>
                    <p className="text-sm leading-6 text-copy">
                      {getActivityDetail(item)}{' '}
                      <span className="font-semibold text-copy-soft">
                        | {formatRelativeTime(item.performedAt)}
                      </span>
                    </p>
                  </div>
                </article>
              ))
            ) : (
              <div className="rounded-[24px] border border-dashed border-brand-ink/[0.08] bg-surface-2/50 px-5 py-8 text-center text-sm font-semibold text-copy-soft">
                Aun no hay movimientos recientes para mostrar.
              </div>
            )}
          </div>
        </Card>
      </section>

      <section className="grid gap-5 xl:grid-cols-[minmax(320px,0.78fr)_minmax(0,1.22fr)]">
        <Card className="overflow-hidden bg-[linear-gradient(160deg,_#0d2d63_0%,_#112b58_100%)] p-7 text-white">
          <div className="inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-white/12">
            <Boxes className="h-5 w-5" strokeWidth={2.2} />
          </div>

          <h2 className="mt-10 text-[1.7rem] font-extrabold tracking-[-0.04em] text-white">
            Cobertura operativa
          </h2>
          <p className="mt-4 max-w-[280px] text-sm leading-7 text-white/72">
            {`El dashboard consolida ${formatInteger(
              summary.movementsLastSevenDays,
            )} movimientos en los ultimos siete dias sobre ${formatInteger(
              summary.accessibleLaboratories,
            )} laboratorios accesibles.`}
          </p>

          <div className="mt-8 space-y-3 text-sm text-white/84">
            <div className="flex items-center justify-between gap-4">
              <span>Productos con stock bajo</span>
              <span className="font-extrabold">{formatInteger(summary.lowStockProducts)}</span>
            </div>
            <div className="flex items-center justify-between gap-4">
              <span>Lotes por vencer</span>
              <span className="font-extrabold">{formatInteger(summary.expiringBatches)}</span>
            </div>
            <div className="flex items-center justify-between gap-4">
              <span>Movimientos recientes</span>
              <span className="font-extrabold">{formatInteger(summary.recentMovements.length)}</span>
            </div>
          </div>
        </Card>

        <Card className="relative overflow-hidden bg-[linear-gradient(135deg,_#dff4f4_0%,_#dff2f7_100%)] p-7 sm:p-8">
          <div className="absolute -right-6 bottom-0 h-44 w-44 rounded-full bg-white/35 blur-2xl" />
          <div className="absolute right-5 top-5 flex h-14 w-14 items-center justify-center rounded-full bg-brand-ink text-white shadow-[0_14px_28px_rgba(14,47,103,0.18)]">
            <Boxes className="h-6 w-6" strokeWidth={2.1} />
          </div>

          <Badge variant="teal" className="gap-2 px-4 py-1.5">
            <CalendarRange className="h-3.5 w-3.5" />
            Inventario por laboratorio
          </Badge>

          <h2 className="mt-8 text-[1.7rem] font-extrabold tracking-[-0.04em] text-brand-ink">
            Distribucion actual
          </h2>
          <p className="mt-4 max-w-[520px] text-sm leading-7 text-copy">
            Revise la cobertura disponible, productos visibles y focos de atencion por cada
            laboratorio accesible en la demo.
          </p>

          <div className="mt-7 space-y-4">
            {summary.inventoryByLaboratory.length ? (
              summary.inventoryByLaboratory.map((item) => (
                <article
                  key={item.laboratoryId}
                  className="rounded-[24px] border border-white/70 bg-white/70 px-5 py-4 shadow-[0_12px_24px_rgba(14,47,103,0.06)] backdrop-blur"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <h3 className="text-sm font-extrabold text-brand-ink">{item.laboratoryName}</h3>
                      <p className="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-copy-soft">
                        {item.laboratoryCode || 'Sin codigo'}
                      </p>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      <Badge variant="navy">{formatInteger(item.visibleProducts)} productos</Badge>
                      <Badge variant={item.lowStockProducts > 0 ? 'warning' : 'success'}>
                        {formatInteger(item.lowStockProducts)} stock bajo
                      </Badge>
                      <Badge variant={item.expiringBatches > 0 ? 'danger' : 'neutral'}>
                        {formatInteger(item.expiringBatches)} por vencer
                      </Badge>
                    </div>
                  </div>

                  <div className="mt-4">
                    <div className="flex items-center justify-between gap-4 text-sm font-semibold text-copy">
                      <span>Existencia consolidada</span>
                      <span>{formatQuantity(item.quantityAvailable)} unidades</span>
                    </div>
                    <ProgressBar
                      value={item.quantityAvailable}
                      max={maxLaboratoryQuantity}
                      tone={item.lowStockProducts > 0 ? 'warning' : 'info'}
                      className="mt-3"
                    />
                  </div>
                </article>
              ))
            ) : (
              <div className="rounded-[24px] border border-dashed border-brand-ink/[0.08] bg-white/70 px-5 py-8 text-center text-sm font-semibold text-copy-soft">
                No hay laboratorios con inventario disponible para resumir.
              </div>
            )}
          </div>
        </Card>
      </section>
    </div>
  );
}

export default DashboardPage;
