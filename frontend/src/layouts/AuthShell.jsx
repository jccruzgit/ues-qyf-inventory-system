import { Boxes, ShieldCheck } from 'lucide-react';
import BrandMark from '../components/ui/BrandMark';

const features = [
  {
    icon: Boxes,
    title: 'Trazabilidad inteligente',
    description: 'Monitorea existencias, reactivos y equipos con alertas preventivas.',
  },
  {
    icon: ShieldCheck,
    title: 'Seguridad primero',
    description: 'Centraliza controles de acceso, cumplimiento y resguardo documental.',
  },
];

function AuthShell({ title, description, children }) {
  return (
    <main className="relative min-h-screen overflow-hidden">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.8),_transparent_33%),radial-gradient(circle_at_bottom_right,_rgba(13,140,143,0.08),_transparent_25%)]" />

      <div className="relative mx-auto grid min-h-screen max-w-[1440px] gap-8 px-5 py-5 lg:grid-cols-[minmax(0,1.1fr)_minmax(430px,520px)] lg:items-center lg:px-10 xl:px-16">
        <section className="flex h-full flex-col justify-between rounded-[36px] px-3 py-6 lg:px-6 lg:py-8">
          <div className="max-w-[680px]">
            <div className="mb-10 flex items-center gap-4">
              <div className="relative">
                <div className="absolute -left-2 -top-3 h-[5.5rem] w-[5.5rem] rounded-[28px] border border-white/50 bg-white/[0.14] blur-[1px]" />
                <BrandMark className="relative z-10" label="Universidad de El Salvador" />
              </div>

              <div>
                <p className="text-base font-extrabold tracking-tight text-brand-ink sm:text-lg">
                  Facultad de Quimica y Farmacia
                </p>
                <p className="text-xs font-semibold uppercase tracking-[0.28em] text-copy-soft">
                  Universidad de El Salvador
                </p>
              </div>
            </div>

            <div className="space-y-6">
              <h1 className="max-w-[620px] text-balance text-4xl font-extrabold leading-[1.02] tracking-[-0.04em] text-brand-ink sm:text-5xl lg:text-[4.25rem]">
                Tu inventario completo,
                <span className="block text-brand-teal">digitalmente refinado.</span>
              </h1>

              <p className="max-w-[560px] text-lg leading-8 text-copy sm:text-xl">
                Administra reactivos, lotes, equipos y registros clinicos desde una sola
                plataforma, con trazabilidad clara para el ecosistema de laboratorio de la UES.
              </p>
            </div>

            <div className="mt-12 grid gap-4 sm:grid-cols-2">
              {features.map(({ icon: Icon, title: featureTitle, description: featureDescription }) => (
                <article
                  key={featureTitle}
                  className="glass-panel rounded-[30px] p-6 shadow-soft-lg"
                >
                  <div className="mb-5 inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-brand-teal-soft text-brand-teal">
                    <Icon className="h-5 w-5" strokeWidth={2.1} />
                  </div>
                  <h2 className="text-lg font-extrabold tracking-tight text-brand-ink">
                    {featureTitle}
                  </h2>
                  <p className="mt-2 text-sm leading-7 text-copy">{featureDescription}</p>
                </article>
              ))}
            </div>
          </div>

          <div className="mt-10 flex items-center gap-4 rounded-[28px] border border-white/[0.55] bg-white/[0.34] px-5 py-4 shadow-[0_10px_24px_rgba(14,47,103,0.08)] backdrop-blur-md sm:max-w-max">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-ink/10 text-xs font-bold uppercase tracking-[0.24em] text-brand-ink">
              UES
            </div>
            <div>
              <p className="text-[0.68rem] font-bold uppercase tracking-[0.3em] text-copy-soft">
                Plataforma institucional
              </p>
              <p className="text-xl font-extrabold tracking-tight text-brand-ink">
                Servicios Inventario Q&amp;F
              </p>
            </div>
          </div>
        </section>

        <section className="mx-auto flex w-full max-w-[520px] items-center justify-center">
          <div className="w-full rounded-[40px] border border-white/[0.75] bg-white/[0.92] p-7 shadow-soft-xl backdrop-blur-xl sm:p-10">
            <div className="mb-10 text-center">
              <h2 className="text-3xl font-extrabold tracking-tight text-brand-ink">{title}</h2>
              <p className="mx-auto mt-3 max-w-[320px] text-base leading-7 text-copy">
                {description}
              </p>
            </div>

            {children}
          </div>
        </section>
      </div>
    </main>
  );
}

export default AuthShell;
