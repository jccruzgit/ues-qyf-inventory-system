import { ArrowLeft, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import AuthShell from '../../layouts/AuthShell';

const contentByMode = {
  'forgot-password': {
    title: 'Recuperacion de acceso',
    description: 'Gestiona la recuperacion de credenciales sin salir del portal institucional.',
    badge: 'Paso recomendado',
    heading: 'Solicita restablecimiento con el administrador del sistema.',
    copy:
      'Como primer entregable, la recuperacion se canaliza desde soporte institucional. En la siguiente fase puede conectarse a un flujo de correo o autoservicio.',
    primaryAction: {
      label: 'Volver al inicio de sesion',
      to: '/',
    },
  },
  'request-access': {
    title: 'Solicitud de acceso',
    description: 'Prepara el alta de nuevos usuarios sin romper la experiencia principal del login.',
    badge: 'Escalable',
    heading: 'Este espacio queda listo para el flujo de incorporacion de usuarios.',
    copy:
      'Puedes conectarlo despues a un formulario institucional, aprobacion por rol y asignacion de alcance. Por ahora se mantiene como punto de extension visible.',
    primaryAction: {
      label: 'Regresar al portal',
      to: '/',
    },
  },
};

function AuthSupportPage({ mode }) {
  const content = contentByMode[mode];

  return (
    <AuthShell title={content.title} description={content.description}>
      <div className="space-y-6">
        <div className="inline-flex rounded-full bg-brand-teal-soft px-4 py-2 text-xs font-extrabold uppercase tracking-[0.24em] text-brand-teal">
          {content.badge}
        </div>

        <div className="space-y-4 rounded-[32px] bg-surface-2 p-6">
          <h3 className="text-2xl font-extrabold tracking-tight text-brand-ink">
            {content.heading}
          </h3>
          <p className="text-base leading-8 text-copy">{content.copy}</p>
        </div>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Link
            to={content.primaryAction.to}
            className="inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3.5 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
          >
            <ArrowLeft className="h-4 w-4" />
            {content.primaryAction.label}
          </Link>

          <div className="inline-flex items-center justify-center gap-2 rounded-full border border-brand-ink/10 bg-white px-6 py-3.5 text-sm font-semibold text-copy">
            Integracion pendiente
            <ChevronRight className="h-4 w-4" />
          </div>
        </div>
      </div>
    </AuthShell>
  );
}

export default AuthSupportPage;
