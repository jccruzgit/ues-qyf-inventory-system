import { startTransition, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { AtSign, Eye, EyeOff, LockKeyhole } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthShell from '../../layouts/AuthShell';
import { useAuth } from '../../hooks/useAuth';
import { loginSchema } from '../../schemas/auth.schema';
import { getAuthErrorMessage } from '../../services/auth.service';

const defaultValues = {
  email: '',
  password: '',
  rememberSession: false,
};

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState('');
  const sessionExpired = new URLSearchParams(location.search).get('reason') === 'session-expired';

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues,
  });

  const onSubmit = handleSubmit(async (values) => {
    setServerError('');

    try {
      await login(values);
      startTransition(() => {
        navigate('/dashboard', { replace: true });
      });
    } catch (error) {
      setServerError(getAuthErrorMessage(error));
    }
  });

  return (
    <AuthShell
      title="Bienvenido de nuevo"
      description="Ingresa tus credenciales institucionales para acceder al sistema."
    >
      <form className="space-y-6" onSubmit={onSubmit}>
        <div className="space-y-2">
          <label className="text-sm font-extrabold tracking-tight text-brand-ink" htmlFor="email">
            Email institucional
          </label>
          <div className="relative">
            <AtSign className="pointer-events-none absolute left-5 top-1/2 h-5 w-5 -translate-y-1/2 text-copy-soft" />
            <input
              id="email"
              type="text"
              autoComplete="username"
              placeholder="nombre@ues.edu.sv"
              className="w-full rounded-full border border-transparent bg-surface-2 py-4 pl-14 pr-5 text-brand-ink outline-none transition focus:border-brand-teal/30 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
              {...register('email')}
            />
          </div>
          {errors.email ? (
            <p className="text-sm font-medium text-rose-600">{errors.email.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between gap-4">
            <label
              className="text-sm font-extrabold tracking-tight text-brand-ink"
              htmlFor="password"
            >
              Clave de acceso
            </label>
            <Link
              to="/forgot-password"
              className="text-sm font-bold text-brand-teal transition hover:text-brand-ink"
            >
              Olvido su clave?
            </Link>
          </div>

          <div className="relative">
            <LockKeyhole className="pointer-events-none absolute left-5 top-1/2 h-5 w-5 -translate-y-1/2 text-copy-soft" />
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              placeholder="************"
              className="w-full rounded-full border border-transparent bg-surface-2 py-4 pl-14 pr-14 text-brand-ink outline-none transition focus:border-brand-teal/30 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
              {...register('password')}
            />
            <button
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              className="absolute right-4 top-1/2 inline-flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full text-copy-soft transition hover:bg-brand-ink/5 hover:text-brand-ink"
              aria-label={showPassword ? 'Ocultar clave' : 'Mostrar clave'}
            >
              {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
          </div>

          {errors.password ? (
            <p className="text-sm font-medium text-rose-600">{errors.password.message}</p>
          ) : null}
        </div>

        <label className="flex items-center gap-3 text-sm text-copy">
          <input
            type="checkbox"
            className="h-5 w-5 rounded-full border border-brand-ink/15 bg-surface-2 text-brand-ink accent-brand-ink"
            {...register('rememberSession')}
          />
          <span>Mantener sesion activa</span>
        </label>

        {sessionExpired ? (
          <div className="rounded-[24px] border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800">
            Tu sesion expiro. Ingresa nuevamente para continuar.
          </div>
        ) : null}

        {serverError ? (
          <div className="rounded-[24px] border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
            {serverError}
          </div>
        ) : null}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-full bg-brand-ink px-6 py-4 text-base font-extrabold text-white shadow-[0_18px_32px_rgba(23,61,44,0.22)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isSubmitting ? 'Validando acceso...' : 'Autorizar ingreso'}
        </button>

        <div className="space-y-4 pt-5 text-center">
          <p className="text-sm text-copy">Nuevo miembro de la facultad?</p>
          <Link
            to="/request-access"
            className="inline-flex min-w-[170px] items-center justify-center rounded-full border border-brand-ink/[0.12] bg-white px-6 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
          >
            Solicitar acceso
          </Link>
        </div>

        <div className="pt-6 text-center text-[0.68rem] font-bold uppercase tracking-[0.28em] text-copy-soft">
          Protocolo interno &nbsp;&nbsp; Seguridad institucional
        </div>
      </form>
    </AuthShell>
  );
}

export default LoginPage;
