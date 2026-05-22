import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  AlertTriangle,
  ArrowDownCircle,
  BookOpenCheck,
  Boxes,
  CircleAlert,
  FlaskConical,
  Link2,
  PackageCheck,
  RefreshCcw,
} from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { FilterSelect } from '../../components/products/ProductFilters';
import { fetchInventoryCatalogs, getInventoryCatalogsErrorMessage } from '../../services/inventoryService';
import { fetchManufacturedProducts, getManufacturedProductsErrorMessage } from '../../services/manufacturedProductsService';
import { fetchRecipes, getRecipesErrorMessage } from '../../services/recipesService';
import {
  confirmProductionRun,
  createProductionRun,
  getProductionRunErrorMessage,
} from '../../services/productionRunsService';
import { productionRunFormSchema } from '../../schemas/production-run.schema';

const defaultValues = {
  manufacturedProductId: '',
  recipeId: '',
  laboratoryId: '',
  groupName: '',
  notes: '',
};

function Field({ label, required, error, children, hint }) {
  return (
    <label className="block min-w-0">
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

function PreviewDataCell({ label, value, compact = false }) {
  return (
    <div className={`rounded-[18px] bg-surface-2/65 px-4 py-2.5 ${compact ? '' : 'h-full'}`}>
      <p className="text-[11px] font-extrabold uppercase tracking-[0.18em] text-copy-soft">
        {label}
      </p>
      <p className="mt-2 text-sm font-semibold leading-6 text-copy">{value}</p>
    </div>
  );
}

function formatQuantity(value) {
  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  }).format(Number(value) || 0);
}

function formatDate(value) {
  if (!value) {
    return 'Sin vencimiento';
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
  }).format(parsedDate);
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

function ProductionRunCreatePage() {
  const navigate = useNavigate();
  const [manufacturedProducts, setManufacturedProducts] = useState([]);
  const [recipes, setRecipes] = useState([]);
  const [laboratories, setLaboratories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [serverMessage, setServerMessage] = useState('');
  const [previewRun, setPreviewRun] = useState(null);
  const [preparing, setPreparing] = useState(false);
  const [confirming, setConfirming] = useState(false);

  const form = useForm({
    resolver: zodResolver(productionRunFormSchema),
    defaultValues,
  });

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    setError: setFormError,
    formState: { errors },
  } = form;

  const selectedManufacturedProductId = Number(watch('manufacturedProductId'));
  const selectedRecipeId = Number(watch('recipeId'));
  const selectedLaboratoryId = String(watch('laboratoryId') ?? '');
  const groupName = watch('groupName');
  const notes = watch('notes');

  const recipeOptions = useMemo(
    () =>
      recipes
        .filter((item) => item.active && item.manufacturedProductId === selectedManufacturedProductId)
        .sort((left, right) => left.name.localeCompare(right.name)),
    [recipes, selectedManufacturedProductId],
  );

  const selectedRecipe = useMemo(
    () => recipes.find((item) => item.id === selectedRecipeId) ?? null,
    [recipes, selectedRecipeId],
  );

  useEffect(() => {
    if (!selectedManufacturedProductId) {
      setValue('recipeId', '');
      return;
    }

    const stillValid = recipeOptions.some((item) => item.id === selectedRecipeId);

    if (!stillValid) {
      if (recipeOptions.length === 1) {
        setValue('recipeId', String(recipeOptions[0].id));
      } else {
        setValue('recipeId', '');
      }
    }
  }, [recipeOptions, selectedManufacturedProductId, selectedRecipeId, setValue]);

  const loadPageData = async () => {
    setLoading(true);
    setError('');

    try {
      const [manufacturedProductsResponse, recipesResponse, inventoryCatalogsResponse] = await Promise.all([
        fetchManufacturedProducts(),
        fetchRecipes(),
        fetchInventoryCatalogs(),
      ]);

      setManufacturedProducts(manufacturedProductsResponse);
      setRecipes(recipesResponse);
      setLaboratories(inventoryCatalogsResponse.laboratories);
    } catch (requestError) {
      setManufacturedProducts([]);
      setRecipes([]);
      setLaboratories([]);
      setError(
        requestError?.message ||
        getRecipesErrorMessage(requestError) ||
        getManufacturedProductsErrorMessage(requestError) ||
        getInventoryCatalogsErrorMessage(requestError) ||
        getProductionRunErrorMessage(requestError),
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPageData();
  }, []);

  useEffect(() => {
    setPreviewRun(null);
    setServerMessage('');
    setFeedback('');
  }, [groupName, notes, selectedLaboratoryId, selectedManufacturedProductId, selectedRecipeId]);

  const handlePrepare = async (values) => {
    setPreparing(true);
    setServerMessage('');
    setFeedback('');

    try {
      const response = await createProductionRun(values);
      setPreviewRun(response);
      setFeedback(
        response.readyToConfirm
          ? 'Previsualizacion generada. Revise los lotes sugeridos antes de confirmar.'
          : 'La elaboracion fue preparada, pero hay insumos con stock insuficiente.',
      );
    } catch (requestError) {
      const message = getProductionRunErrorMessage(requestError);
      setServerMessage(message);
      setFormError('recipeId', { type: 'server', message });
    } finally {
      setPreparing(false);
    }
  };

  const handleConfirm = async () => {
    if (!previewRun) {
      return;
    }

    setConfirming(true);
    setServerMessage('');
    setFeedback('');

    try {
      const response = await confirmProductionRun(previewRun.id);
      setPreviewRun(response);
      setFeedback(
        `Elaboracion confirmada correctamente. Se genero el movimiento de salida #${response.inventoryMovementId}.`,
      );
    } catch (requestError) {
      setServerMessage(getProductionRunErrorMessage(requestError));
    } finally {
      setConfirming(false);
    }
  };

  const handleReset = () => {
    reset(defaultValues);
    setPreviewRun(null);
    setServerMessage('');
    setFeedback('');
  };

  const shortageItems = previewRun?.items.filter((item) => !item.stockSufficient) ?? [];

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Elaboracion"
        subtitle="Prepara un descargo por receta, revisa los insumos sugeridos por lote y confirma la salida de inventario sin afectar el descargo individual."
        action={
          <Link
            to="/inventory/exits/new"
            className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink shadow-[0_12px_24px_rgba(23,61,44,0.08)] transition hover:-translate-y-0.5 hover:border-brand-teal/30 hover:text-brand-teal"
          >
            <ArrowDownCircle className="h-4 w-4" strokeWidth={2.2} />
            Descargo individual
          </Link>
        }
      />

      {feedback ? (
        <div className="rounded-[24px] border border-[#d2e6d8] bg-[#eef6f0] px-4 py-3 text-sm font-semibold text-[#2d7a49]">
          {feedback}
        </div>
      ) : null}

      <div className="space-y-6">
        <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-0">
          <div className="grid gap-0 xl:grid-cols-[minmax(220px,240px)_minmax(0,1fr)]">
            <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#163826_0%,_#1e5d38_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
              <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
                <BookOpenCheck className="h-5 w-5" strokeWidth={2.1} />
              </div>
              <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
                Descargo por receta
              </h2>
              <p className="mt-4 text-sm leading-7 text-white/72">
                Este flujo toma una receta, valida stock completo y crea una salida trazable con una linea por cada lote sugerido.
              </p>
              <div className="mt-8 space-y-3 text-sm text-white/78">
                <p>1. Selecciona producto elaborado, receta y laboratorio.</p>
                <p>2. Prepara la elaboracion para revisar insumos, cantidades y lotes FEFO.</p>
                <p>3. Confirma solo si toda la receta tiene disponibilidad suficiente.</p>
              </div>
            </aside>

            <div className="min-w-0 p-6 sm:p-8">
              {loading ? (
                <div className="max-w-[440px] space-y-4 animate-pulse">
                  <div className="h-5 w-44 rounded-full bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-24 rounded-[22px] bg-surface-2" />
                </div>
              ) : error ? (
                <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-4 text-sm font-semibold text-[#d53a43]">
                  {error}
                </div>
              ) : (
                <form className="max-w-[440px] space-y-6" onSubmit={handleSubmit(handlePrepare)}>
                  <Field
                    label="Producto elaborado"
                    required
                    error={errors.manufacturedProductId?.message}
                  >
                    <select
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      defaultValue=""
                      {...register('manufacturedProductId')}
                    >
                      <option value="" disabled>
                        Seleccione un producto elaborado
                      </option>
                      {manufacturedProducts
                        .filter((item) => item.active)
                        .sort((left, right) => left.name.localeCompare(right.name))
                        .map((item) => (
                          <option key={item.id} value={item.id}>
                            {item.name} ({item.code})
                          </option>
                        ))}
                    </select>
                  </Field>

                  <Field label="Receta" required error={errors.recipeId?.message}>
                    <select
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      defaultValue=""
                      {...register('recipeId')}
                      disabled={!selectedManufacturedProductId}
                    >
                      <option value="" disabled>
                        {selectedManufacturedProductId
                          ? 'Seleccione una receta'
                          : 'Seleccione primero un producto elaborado'}
                      </option>
                      {recipeOptions.map((item) => (
                        <option key={item.id} value={item.id}>
                          {item.name} ({item.code})
                        </option>
                      ))}
                    </select>
                  </Field>

                  <Field label="Laboratorio" required error={errors.laboratoryId?.message}>
                    <select
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      defaultValue=""
                      {...register('laboratoryId')}
                    >
                      <option value="" disabled>
                        Seleccione un laboratorio
                      </option>
                      {laboratories.map((item) => (
                        <option key={item.value} value={item.value}>
                          {item.label}
                        </option>
                      ))}
                    </select>
                  </Field>

                  <Field
                    label="Grupo o estudiante"
                    error={errors.groupName?.message}
                    hint="Opcional para conservar trazabilidad academica."
                  >
                    <input
                      type="text"
                      placeholder="Ej. Grupo 03 / Seccion B"
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      {...register('groupName')}
                    />
                  </Field>

                  <Field
                    label="Observaciones"
                    error={errors.notes?.message}
                    hint="Estas notas se adjuntan a la elaboracion y al movimiento generado."
                  >
                    <textarea
                      rows={4}
                      placeholder="Describe la practica, el contexto del descargo o cualquier detalle relevante."
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      {...register('notes')}
                    />
                  </Field>

                  {selectedRecipe ? (
                    <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-4 text-sm font-semibold text-copy">
                      Esta receta contiene <span className="text-brand-ink">{selectedRecipe.items.length}</span> insumo(s) registrados.
                    </div>
                  ) : null}

                  {serverMessage ? (
                    <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-3 text-sm font-semibold text-[#d53a43]">
                      {serverMessage}
                    </div>
                  ) : null}

                  <div className="grid gap-3 sm:grid-cols-2">
                    <button
                      type="button"
                      onClick={handleReset}
                      className="inline-flex items-center justify-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                    >
                      <RefreshCcw className="h-4 w-4" />
                      Reiniciar
                    </button>
                    <button
                      type="submit"
                      disabled={preparing}
                      className="inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
                    >
                      <FlaskConical className="h-4 w-4" />
                      {preparing ? 'Preparando...' : 'Preparar'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </Card>

        <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Previsualizacion
              </p>
              <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                Insumos, lotes sugeridos y advertencias
              </h3>
            </div>

            <span
              className={`rounded-full px-3 py-1 text-xs font-extrabold ${
                previewRun?.status === 'CONFIRMED'
                  ? 'bg-[#e7f4eb] text-[#2d7a49]'
                  : 'bg-brand-teal-soft text-brand-teal'
              }`}
            >
              {previewRun?.status === 'CONFIRMED' ? 'Confirmada' : 'Borrador'}
            </span>
          </div>

          {previewRun ? (
            <>
              <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <div className="min-w-0">
                    <p className="text-xs font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                      Producto elaborado
                    </p>
                    <h4 className="mt-2 text-lg font-extrabold text-brand-ink">
                      {previewRun.manufacturedProductName}
                    </h4>
                    <p className="mt-1 text-sm leading-6 text-copy">
                      Receta {previewRun.recipeCode} / {previewRun.recipeName}
                    </p>
                  </div>

                  <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4 xl:justify-items-stretch">
                    <PreviewDataCell
                      label="Laboratorio"
                      value={previewRun.laboratoryName}
                      compact
                    />
                    <PreviewDataCell
                      label="Creado por"
                      value={previewRun.createdByUsername}
                      compact
                    />
                    <PreviewDataCell
                      label="Fecha"
                      value={formatDateTime(previewRun.createdAt)}
                      compact
                    />
                    <PreviewDataCell
                      label="Grupo"
                      value={previewRun.groupName || 'Sin grupo asociado.'}
                      compact
                    />
                  </div>
                </div>

                <div className="mt-3 rounded-[18px] bg-surface-2/65 px-4 py-3">
                  <p className="text-[11px] font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                    Observaciones
                  </p>
                  <p className="mt-2 text-sm font-semibold leading-6 text-copy">
                    {previewRun.notes || 'Sin observaciones adicionales.'}
                  </p>
                </div>
              </div>

              {shortageItems.length ? (
                <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-4 text-sm font-semibold text-[#b73945]">
                  <div className="flex items-start gap-2">
                    <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
                    <div>
                      No hay stock suficiente para confirmar la elaboracion. Revise:
                      <ul className="mt-2 list-disc pl-5">
                        {shortageItems.map((item) => (
                          <li key={item.recipeItemId}>
                            {item.productName} ({item.productCode}): requiere {formatQuantity(item.requiredQuantity)} y solo hay {formatQuantity(item.totalAvailableQuantity)}.
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="rounded-[24px] border border-[#fff1d2] bg-[#fff8e8] px-4 py-4 text-sm font-semibold text-[#9a6a0a]">
                  Al confirmar se descargaran todos los insumos listados. No se realizaran descargas parciales si algun insumo falla.
                </div>
              )}

              <div className="space-y-3">
                {previewRun.items.map((item) => (
                  <div
                    key={item.recipeItemId}
                    className="rounded-[24px] border border-white/80 bg-white px-5 py-3"
                  >
                    <div className="flex flex-col gap-2.5 lg:flex-row lg:items-start lg:justify-between">
                      <div className="min-w-0">
                        <p className="text-xs font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                          Insumo requerido
                        </p>
                        <h4 className="mt-2 text-lg font-extrabold text-brand-ink">
                          {item.productName}
                        </h4>
                        <p className="mt-1 text-sm font-semibold text-copy">
                          {item.productCode}
                        </p>
                      </div>

                      <span
                        className={`rounded-full px-3 py-1 text-xs font-extrabold ${
                          item.stockSufficient
                            ? 'bg-[#e7f4eb] text-[#2d7a49]'
                            : 'bg-[#fdebec] text-[#d53a43]'
                        }`}
                      >
                        {item.stockSufficient ? 'Stock suficiente' : 'Stock insuficiente'}
                      </span>
                    </div>

                    <div className="mt-3 grid gap-2.5 md:grid-cols-2 xl:grid-cols-4">
                      <PreviewDataCell
                        label="Cantidad requerida"
                        value={`${formatQuantity(item.requiredQuantity)} ${item.unitOfMeasureSymbol || item.unitOfMeasureName}`}
                      />
                      <PreviewDataCell
                        label="Disponible total"
                        value={`${formatQuantity(item.totalAvailableQuantity)} ${item.unitOfMeasureSymbol || item.unitOfMeasureName}`}
                      />
                      <PreviewDataCell
                        label="Ubicacion"
                        value={item.locationName}
                      />
                      <PreviewDataCell
                        label="Observaciones"
                        value={item.observations || 'Sin observaciones en la receta.'}
                      />
                    </div>

                    <div className="mt-3 grid gap-2.5">
                      {item.suggestedAllocations.length ? (
                        <div className="overflow-hidden rounded-[20px] border border-brand-ink/[0.06] bg-surface-2/45">
                          <div className="hidden grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)_minmax(0,0.9fr)_minmax(0,0.9fr)] gap-3 border-b border-brand-ink/[0.06] px-4 py-2.5 text-[11px] font-extrabold uppercase tracking-[0.18em] text-copy-soft md:grid">
                            <span>Lote sugerido</span>
                            <span>Vencimiento</span>
                            <span>Disponible</span>
                            <span>Descargar</span>
                          </div>
                          <div className="divide-y divide-brand-ink/[0.06]">
                            {item.suggestedAllocations.map((allocation) => (
                              <div
                                key={`${item.recipeItemId}-${allocation.productBatchId ?? allocation.batchCode}`}
                                className="grid gap-2.5 px-4 py-2.5 md:grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)_minmax(0,0.9fr)_minmax(0,0.9fr)] md:items-center"
                              >
                                <PreviewDataCell
                                  label="Lote sugerido"
                                  value={allocation.batchCode}
                                  compact
                                />
                                <PreviewDataCell
                                  label="Vencimiento"
                                  value={formatDate(allocation.expirationDate)}
                                  compact
                                />
                                <PreviewDataCell
                                  label="Disponible"
                                  value={formatQuantity(allocation.availableQuantity)}
                                  compact
                                />
                                <PreviewDataCell
                                  label="Descargar"
                                  value={formatQuantity(allocation.suggestedQuantity)}
                                  compact
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      ) : (
                        <div className="rounded-[20px] bg-surface-2/65 px-4 py-3 text-sm font-semibold text-copy">
                          No se encontro un lote sugerido con disponibilidad para este insumo.
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              <div className="grid gap-4 xl:grid-cols-2">
                <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                  <div className="flex items-center gap-3">
                    <PackageCheck className="h-5 w-5 text-brand-teal" />
                    <p className="text-sm font-extrabold text-brand-ink">
                      Movimiento asociado
                    </p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-copy">
                    {previewRun.inventoryMovementId
                      ? `Se genero el movimiento #${previewRun.inventoryMovementId}.`
                      : 'Se generara un movimiento de salida al confirmar.'}
                  </p>
                  {previewRun.inventoryMovementId ? (
                    <button
                      type="button"
                      onClick={() =>
                        navigate('/movements', {
                          state: {
                            prefill: { laboratoryId: previewRun.laboratoryId },
                            context: {
                              title: `el movimiento #${previewRun.inventoryMovementId}`,
                              description: 'Revise el historial y use la fila principal para reversar si es necesario.',
                            },
                          },
                        })
                      }
                      className="mt-4 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                    >
                      <Link2 className="h-4 w-4" />
                      Ver movimientos
                    </button>
                  ) : null}
                </div>

                <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                  <div className="flex items-center gap-3">
                    <CircleAlert className="h-5 w-5 text-[#d28a19]" />
                    <p className="text-sm font-extrabold text-brand-ink">
                      Reversiones
                    </p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-copy">
                    El movimiento generado no se edita ni se elimina. Si hay un error, debe reversarse desde el historial y el stock volvera a su valor anterior.
                  </p>
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:max-w-[420px] lg:ml-auto">
                <button
                  type="button"
                  onClick={handleReset}
                  className="inline-flex items-center justify-center rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                >
                  Preparar otra elaboracion
                </button>
                <button
                  type="button"
                  disabled={
                    confirming ||
                    previewRun.status === 'CONFIRMED' ||
                    !previewRun.readyToConfirm
                  }
                  onClick={handleConfirm}
                  className="inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
                >
                  <Boxes className="h-4 w-4" />
                  {confirming
                    ? 'Confirmando...'
                    : previewRun.status === 'CONFIRMED'
                      ? 'Elaboracion confirmada'
                      : 'Confirmar elaboracion'}
                </button>
              </div>
            </>
          ) : (
            <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-10 text-center">
              <FlaskConical className="mx-auto h-8 w-8 text-copy-soft" strokeWidth={1.9} />
              <h3 className="mt-4 text-lg font-extrabold text-brand-ink">
                Aun no hay una elaboracion preparada
              </h3>
              <p className="mt-2 text-sm leading-7 text-copy">
                Selecciona una receta y prepara la elaboracion para revisar insumos, cantidades requeridas, lote sugerido y advertencias antes de confirmar.
              </p>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

export default ProductionRunCreatePage;
