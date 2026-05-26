import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { FlaskConical, PackagePlus, PencilLine, RefreshCcw, Save, SearchX } from 'lucide-react';
import { useForm } from 'react-hook-form';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { manufacturedProductFormSchema } from '../../schemas/manufactured-product.schema';
import {
  createManufacturedProduct,
  fetchManufacturedProducts,
  getManufacturedProductMutationErrorDetails,
  getManufacturedProductsErrorMessage,
  updateManufacturedProduct,
} from '../../services/manufacturedProductsService';

const defaultValues = {
  code: '',
  name: '',
  description: '',
  active: true,
};

function Field({ label, required, error, children, hint }) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-extrabold tracking-tight text-brand-ink">
        {label}
        {required ? <span className="ml-1 text-[#d53a43]">*</span> : null}
      </span>
      {children}
      {hint ? <p className="mt-2 text-xs leading-5 text-copy-soft">{hint}</p> : null}
      {error ? <p className="mt-2 text-sm font-semibold text-[#d53a43]">{error}</p> : null}
    </label>
  );
}

function formatDateTime(value) {
  if (!value) {
    return 'Sin fecha';
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(parsedDate);
}

function ManufacturedProductsPage() {
  const [manufacturedProducts, setManufacturedProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [serverMessage, setServerMessage] = useState('');

  const form = useForm({
    resolver: zodResolver(manufacturedProductFormSchema),
    defaultValues,
  });

  const {
    register,
    handleSubmit,
    reset,
    setError: setFormError,
    formState: { errors, isSubmitting },
  } = form;

  const editingProduct = useMemo(
    () => manufacturedProducts.find((item) => item.id === editingId) ?? null,
    [editingId, manufacturedProducts],
  );

  const loadManufacturedProducts = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchManufacturedProducts();
      setManufacturedProducts(response);
    } catch (requestError) {
      setManufacturedProducts([]);
      setError(getManufacturedProductsErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadManufacturedProducts();
  }, []);

  const handleStartCreate = () => {
    setEditingId(null);
    setFeedback('');
    setServerMessage('');
    reset(defaultValues);
  };

  const handleStartEdit = (item) => {
    setEditingId(item.id);
    setFeedback('');
    setServerMessage('');
    reset({
      code: item.code,
      name: item.name,
      description: item.description,
      active: item.active,
    });
  };

  const handleSave = async (values) => {
    setServerMessage('');
    setFeedback('');

    try {
      const savedProduct = editingId
        ? await updateManufacturedProduct(editingId, values)
        : await createManufacturedProduct(values);

      setManufacturedProducts((currentProducts) => {
        if (editingId) {
          return currentProducts.map((item) => (item.id === savedProduct.id ? savedProduct : item));
        }

        return [...currentProducts, savedProduct].sort((left, right) => left.name.localeCompare(right.name));
      });

      setEditingId(savedProduct.id);
      reset({
        code: savedProduct.code,
        name: savedProduct.name,
        description: savedProduct.description,
        active: savedProduct.active,
      });
      setFeedback(
        editingId
          ? 'Producto elaborado actualizado correctamente.'
          : 'Producto elaborado creado correctamente.',
      );
    } catch (requestError) {
      const details = getManufacturedProductMutationErrorDetails(requestError);

      Object.entries(details.fieldErrors).forEach(([field, message]) => {
        setFormError(field, { type: 'server', message });
      });

      setServerMessage(details.message);
    }
  };

  const hasItems = manufacturedProducts.length > 0;

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Productos elaborados"
        subtitle="Administra los productos que se fabrican en el laboratorio y que luego se vinculan a recetas."
        action={
          <button
            type="button"
            onClick={handleStartCreate}
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
          >
            <PackagePlus className="h-4 w-4" strokeWidth={2.3} />
            Nuevo producto elaborado
          </button>
        }
      />

      {feedback ? (
        <div className="rounded-[24px] border border-[#d2e6d8] bg-[#eef6f0] px-4 py-3 text-sm font-semibold text-[#2d7a49]">
          {feedback}
        </div>
      ) : null}

      <div className="grid gap-6 xl:grid-cols-[minmax(320px,0.42fr)_minmax(0,0.58fr)]">
        <Card className="space-y-4 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Catalogo actual
              </p>
              <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                Productos elaborados registrados
              </h3>
            </div>

            <button
              type="button"
              onClick={loadManufacturedProducts}
              className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
            >
              <RefreshCcw className="h-4 w-4" />
              Recargar
            </button>
          </div>

          {loading ? (
            <div className="space-y-3 animate-pulse">
              {Array.from({ length: 4 }).map((_, index) => (
                <div key={index} className="rounded-[24px] bg-white p-5">
                  <div className="h-4 w-24 rounded-full bg-surface-2" />
                  <div className="mt-3 h-5 w-2/3 rounded-full bg-surface-2" />
                  <div className="mt-3 h-3 w-full rounded-full bg-surface-2" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-4 text-sm font-semibold text-[#d53a43]">
              {error}
            </div>
          ) : hasItems ? (
            <div className="space-y-3">
              {manufacturedProducts
                .slice()
                .sort((left, right) => left.name.localeCompare(right.name))
                .map((item) => {
                  const isSelected = editingId === item.id;

                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => handleStartEdit(item)}
                      className={`w-full rounded-[24px] border px-5 py-4 text-left transition ${
                        isSelected
                          ? 'border-brand-teal/30 bg-[#f3faf6] shadow-[0_12px_26px_rgba(23,61,44,0.08)]'
                          : 'border-white/80 bg-white hover:border-brand-teal/20 hover:bg-[#fbfdfb]'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                            {item.code}
                          </p>
                          <h4 className="mt-2 text-lg font-extrabold tracking-[-0.03em] text-brand-ink">
                            {item.name}
                          </h4>
                        </div>

                        <span
                          className={`rounded-full px-3 py-1 text-xs font-extrabold ${
                            item.active
                              ? 'bg-[#e7f4eb] text-[#2d7a49]'
                              : 'bg-[#fdebec] text-[#d53a43]'
                          }`}
                        >
                          {item.active ? 'Activo' : 'Inactivo'}
                        </span>
                      </div>

                      <p className="mt-3 text-sm leading-6 text-copy">
                        {item.description || 'Sin descripcion registrada.'}
                      </p>
                      <p className="mt-3 text-xs font-semibold uppercase tracking-[0.14em] text-copy-soft">
                        Actualizado {formatDateTime(item.updatedAt)}
                      </p>
                    </button>
                  );
                })}
            </div>
          ) : (
            <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-8 text-center">
              <SearchX className="mx-auto h-8 w-8 text-copy-soft" strokeWidth={1.9} />
              <h3 className="mt-4 text-lg font-extrabold text-brand-ink">
                Aun no hay productos elaborados
              </h3>
              <p className="mt-2 text-sm leading-7 text-copy">
                Registra el primer producto elaborado para comenzar a definir recetas y descargos por elaboracion.
              </p>
            </div>
          )}
        </Card>

        <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-0">
          <div className="grid gap-0 lg:grid-cols-[minmax(220px,0.28fr)_minmax(0,0.72fr)]">
            <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#163826_0%,_#1e5d38_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
              <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
                {editingProduct ? (
                  <PencilLine className="h-5 w-5" strokeWidth={2.1} />
                ) : (
                  <FlaskConical className="h-5 w-5" strokeWidth={2.1} />
                )}
              </div>
              <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
                {editingProduct ? 'Editar producto elaborado' : 'Nuevo producto elaborado'}
              </h2>
              <p className="mt-4 text-sm leading-7 text-white/72">
                Define el producto final que se fabrica con una receta. Este catalogo no reemplaza a los insumos del inventario actual.
              </p>
              <div className="mt-8 space-y-3 text-sm text-white/78">
                <p>1. Registra codigo, nombre y descripcion operativa.</p>
                <p>2. Mantiene el estado activo para recetas vigentes.</p>
                <p>3. Luego crea una receta y sus insumos asociados.</p>
              </div>
            </aside>

            <div className="p-6 sm:p-8 lg:p-10">
              <form className="space-y-6" onSubmit={handleSubmit(handleSave)}>
                <Field label="Codigo" required error={errors.code?.message}>
                  <input
                    type="text"
                    placeholder="Ej. ELAB-JAB-001"
                    className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                    {...register('code')}
                  />
                </Field>

                <Field label="Nombre" required error={errors.name?.message}>
                  <input
                    type="text"
                    placeholder="Ej. Jabon liquido"
                    className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                    {...register('name')}
                  />
                </Field>

                <Field
                  label="Descripcion"
                  error={errors.description?.message}
                  hint="Describe el producto final o su uso academico."
                >
                  <textarea
                    rows={5}
                    placeholder="Describe el producto elaborado y su referencia dentro del laboratorio."
                    className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                    {...register('description')}
                  />
                </Field>

                <label className="flex items-start gap-3 rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                  <input
                    type="checkbox"
                    className="mt-1 h-4 w-4 rounded border-brand-ink/20 text-brand-teal focus:ring-brand-teal/20"
                    {...register('active')}
                  />
                  <span className="text-sm font-semibold leading-6 text-copy">
                    Mantener este producto elaborado activo para nuevas recetas y elaboraciones.
                  </span>
                </label>

                {serverMessage ? (
                  <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-3 text-sm font-semibold text-[#d53a43]">
                    {serverMessage}
                  </div>
                ) : null}

                <div className="grid gap-3 sm:grid-cols-2">
                  <button
                    type="button"
                    onClick={handleStartCreate}
                    className="inline-flex w-full items-center justify-center rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                  >
                    Limpiar
                  </button>
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    <Save className="h-4 w-4" strokeWidth={2.1} />
                    {isSubmitting ? 'Guardando...' : 'Guardar'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}

export default ManufacturedProductsPage;
