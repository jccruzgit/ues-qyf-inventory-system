import { useLocation } from 'react-router-dom';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';

const routeLabels = {
  '/products': 'Productos',
  '/inventory': 'Inventario',
  '/movements': 'Movimientos',
  '/batches': 'Lotes',
  '/alerts': 'Alertas',
  '/support': 'Soporte',
  '/archive': 'Archivo',
};

function toTitleCase(segment) {
  return segment
    .replace('/', '')
    .split('-')
    .filter(Boolean)
    .map((token) => token.charAt(0).toUpperCase() + token.slice(1))
    .join(' ');
}

function ModulePlaceholderPage() {
  const location = useLocation();
  const moduleName = routeLabels[location.pathname] ?? toTitleCase(location.pathname) ?? 'Modulo';
  const context = location.state?.context;

  return (
    <div className="space-y-6">
      <SectionHeader
        title={moduleName}
        subtitle="Este modulo base ya esta listo para la siguiente fase de implementacion."
      />

      <Card className="min-h-[360px] bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)]">
        <div className="flex h-full min-h-[300px] flex-col justify-between">
          <div>
            <p className="inline-flex rounded-full bg-brand-teal-soft px-3 py-1 text-xs font-extrabold text-brand-teal">
              Ruta preparada
            </p>
            <h2 className="mt-5 text-3xl font-extrabold tracking-[-0.04em] text-brand-ink">
              {moduleName} ya esta preparado para la siguiente etapa.
            </h2>
            <p className="mt-4 max-w-[620px] text-base leading-8 text-copy">
              El ruteo, el layout, el estado de navegacion y los componentes reutilizables ya
              estan en su lugar. Esta pagina puede evolucionar al modulo real sin rehacer el
              shell protegido.
            </p>
          </div>

          <div className="rounded-[26px] border border-brand-ink/[0.06] bg-surface-2 px-5 py-4 text-sm font-semibold text-copy">
            Siguiente paso sugerido: conectar tablas, filtros y consultas al backend cuando este
            modulo pase de placeholder a implementacion.
          </div>

          {context ? (
            <div className="rounded-[26px] border border-brand-teal/15 bg-brand-teal-soft/40 px-5 py-4 text-sm font-semibold text-copy">
              Contexto recibido: <span className="text-brand-ink">{context.title}</span>
              {context.description ? ` • ${context.description}` : ''}
            </div>
          ) : null}
        </div>
      </Card>
    </div>
  );
}

export default ModulePlaceholderPage;
