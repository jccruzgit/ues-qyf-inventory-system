import {
  Activity,
  Beaker,
  CalendarRange,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  complianceCard,
  kpiCards,
  movementSeries,
  optimizationActions,
  recentActivity,
} from '../../mocks/dashboard';

const activityToneClasses = {
  teal: 'bg-brand-teal-soft text-brand-teal',
  success: 'bg-[#ebf8ef] text-[#57a763]',
  danger: 'bg-[#fdebec] text-[#d53a43]',
  neutral: 'bg-surface-2 text-copy-soft',
};

function DashboardChart() {
  return (
    <div className="mt-8">
      <div className="grid h-[290px] grid-cols-7 items-end gap-3 sm:gap-4">
        {movementSeries.map((point) => (
          <div key={point.day} className="flex h-full flex-col items-center justify-end gap-3">
            <div className="flex h-full items-end gap-1.5 sm:gap-2">
              <div
                className="w-3 rounded-full bg-[#dde4ef] transition hover:bg-[#c8d5ea] sm:w-4"
                style={{ height: `${point.intake}%` }}
              />
              <div
                className="w-3 rounded-full bg-brand-teal transition hover:bg-[#0a787b] sm:w-4"
                style={{ height: `${point.usage}%` }}
              />
            </div>
            <span className="text-[0.68rem] font-extrabold tracking-[0.2em] text-copy-soft">
              {point.day}
            </span>
          </div>
        ))}
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-5 border-t border-brand-ink/[0.06] pt-4">
        <div className="flex items-center gap-2 text-xs font-bold text-copy">
          <span className="h-2.5 w-2.5 rounded-full bg-[#dde4ef]" />
          Volumen de ingreso
        </div>
        <div className="flex items-center gap-2 text-xs font-bold text-copy">
          <span className="h-2.5 w-2.5 rounded-full bg-brand-teal" />
          Tasa de uso
        </div>
      </div>
    </div>
  );
}

function DashboardPage() {
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
              <div className="inline-flex rounded-full bg-surface-2 p-1">
                <button
                  type="button"
                  className="rounded-full px-4 py-2 text-xs font-extrabold text-copy transition hover:text-brand-ink"
                >
                  Diario
                </button>
                <button
                  type="button"
                  className="rounded-full bg-brand-ink px-4 py-2 text-xs font-extrabold text-white shadow-[0_8px_18px_rgba(14,47,103,0.2)]"
                >
                  Semanal
                </button>
              </div>
            }
          />

          <DashboardChart />
        </Card>

        <Card className="p-6 sm:p-7">
          <SectionHeader
            title="Actividad reciente"
            action={
              <button
                type="button"
                className="text-sm font-extrabold text-brand-teal transition hover:text-brand-ink"
              >
                Ver todo
              </button>
            }
          />

          <div className="mt-6 space-y-5">
            {recentActivity.map((item) => (
              <article key={`${item.title}-${item.time}`} className="flex items-start gap-4">
                <div
                  className={`mt-1 flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${
                    activityToneClasses[item.tone]
                  }`}
                >
                  <Activity className="h-4 w-4" strokeWidth={2.3} />
                </div>

                <div className="min-w-0">
                  <h3 className="text-sm font-extrabold leading-6 text-brand-ink">{item.title}</h3>
                  <p className="text-sm leading-6 text-copy">
                    {item.detail} <span className="font-semibold text-copy-soft">| {item.time}</span>
                  </p>
                </div>
              </article>
            ))}
          </div>
        </Card>
      </section>

      <section className="grid gap-5 xl:grid-cols-[minmax(320px,0.78fr)_minmax(0,1.22fr)]">
        <Card className="overflow-hidden bg-[linear-gradient(160deg,_#0d2d63_0%,_#112b58_100%)] p-7 text-white">
          <div className="inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-white/12">
            <ShieldCheck className="h-5 w-5" strokeWidth={2.2} />
          </div>

          <h2 className="mt-10 text-[1.7rem] font-extrabold tracking-[-0.04em] text-white">
            {complianceCard.title}
          </h2>
          <p className="mt-4 max-w-[280px] text-sm leading-7 text-white/72">
            {complianceCard.description}
          </p>

          <button
            type="button"
            className="mt-8 inline-flex items-center gap-2 text-sm font-extrabold text-white transition hover:text-[#9fd8dc]"
          >
            {complianceCard.cta}
            <span aria-hidden="true">-&gt;</span>
          </button>
        </Card>

        <Card className="relative overflow-hidden bg-[linear-gradient(135deg,_#dff4f4_0%,_#dff2f7_100%)] p-7 sm:p-8">
          <div className="absolute -right-6 bottom-0 h-44 w-44 rounded-full bg-white/35 blur-2xl" />
          <div className="absolute right-5 top-5 flex h-14 w-14 items-center justify-center rounded-full bg-brand-ink text-white shadow-[0_14px_28px_rgba(14,47,103,0.18)]">
            <Beaker className="h-6 w-6" strokeWidth={2.1} />
          </div>

          <Badge variant="teal" className="gap-2 px-4 py-1.5">
            <CalendarRange className="h-3.5 w-3.5" />
            Pronostico inteligente
          </Badge>

          <h2 className="mt-8 text-[1.7rem] font-extrabold tracking-[-0.04em] text-brand-ink">
            Optimizacion de stock
          </h2>
          <p className="mt-4 max-w-[520px] text-sm leading-7 text-copy">
            El sistema sugiere reabastecer etanol y acetona segun el calendario actual de
            practicas y ensayos de laboratorio.
          </p>

          <div className="mt-7 flex flex-wrap items-center gap-3">
            {optimizationActions.map((action) => (
              <button
                key={action.label}
                type="button"
                className={
                  action.variant === 'primary'
                    ? 'rounded-full bg-brand-teal px-5 py-3 text-sm font-extrabold text-white shadow-[0_14px_28px_rgba(13,140,143,0.22)] transition hover:-translate-y-0.5 hover:bg-[#0b787b]'
                    : 'rounded-full border border-brand-teal/16 bg-white/65 px-5 py-3 text-sm font-extrabold text-brand-teal transition hover:bg-white'
                }
              >
                {action.label}
              </button>
            ))}
          </div>

          <div className="relative mt-10 flex justify-end">
            <div className="flex h-28 w-28 items-center justify-center rounded-full bg-white shadow-[0_18px_34px_rgba(90,186,190,0.25)] ring-8 ring-white/40">
              <Sparkles className="h-8 w-8 text-brand-teal" strokeWidth={2.1} />
            </div>
          </div>
        </Card>
      </section>
    </div>
  );
}

export default DashboardPage;
