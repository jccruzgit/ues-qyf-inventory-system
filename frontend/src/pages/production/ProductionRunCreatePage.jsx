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
  Printer,
  RefreshCcw,
} from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { FilterSelect } from '../../components/products/ProductFilters';
import { fetchInventoryCatalogs, getInventoryCatalogsErrorMessage } from '../../services/inventoryService';
import { fetchManufacturedProducts, getManufacturedProductsErrorMessage } from '../../services/manufacturedProductsService';
import {
  fetchRecipePrintableById,
  fetchRecipes,
  getRecipesErrorMessage,
} from '../../services/recipesService';
import {
  confirmProductionRun,
  createProductionRun,
  fetchProductionRunPrintableById,
  getProductionRunErrorMessage,
} from '../../services/productionRunsService';
import { productionRunFormSchema } from '../../schemas/production-run.schema';
import {
  openPrintPreviewWindow,
  printProductionRunDocument,
  printRecipeDocument,
  renderPrintWindowError,
} from '../../utils/printDocuments';

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

function formatEditableQuantity(value) {
  if (value == null || value === '') {
    return '';
  }

  return String(value);
}

function parseQuantityInput(value) {
  const normalizedValue = String(value ?? '').trim();

  if (!normalizedValue) {
    return null;
  }

  const parsedValue = Number(normalizedValue);
  return Number.isFinite(parsedValue) ? parsedValue : null;
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
  const [actualQuantities, setActualQuantities] = useState({});
  const [preparing, setPreparing] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [printingRecipe, setPrintingRecipe] = useState(false);
  const [printingProductionRun, setPrintingProductionRun] = useState(false);

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
    setActualQuantities({});
    setServerMessage('');
    setFeedback('');
  }, [groupName, notes, selectedLaboratoryId, selectedManufacturedProductId, selectedRecipeId]);

  useEffect(() => {
    if (!previewRun) {
      setActualQuantities({});
      return;
    }

    setActualQuantities(
      Object.fromEntries(
        previewRun.items.map((item) => [
          item.recipeItemId,
          formatEditableQuantity(item.actualQuantity ?? item.requiredQuantity),
        ]),
      ),
    );
  }, [previewRun]);

  const previewItems = useMemo(() => {
    if (!previewRun) {
      return [];
    }

    return previewRun.items.map((item) => {
      const actualQuantityInput =
        actualQuantities[item.recipeItemId] ??
        formatEditableQuantity(item.actualQuantity ?? item.requiredQuantity);
      const parsedActualQuantity = parseQuantityInput(actualQuantityInput);
      const hasValidActualQuantity = parsedActualQuantity !== null && parsedActualQuantity >= 0;
      const minimumAllowedQuantity =
        item.minimumAllowedQuantity ?? item.requiredQuantity * 0.9;
      const maximumAllowedQuantity =
        item.maximumAllowedQuantity ?? item.requiredQuantity * 1.1;
      const exceedsAvailableStock =
        parsedActualQuantity !== null && parsedActualQuantity > item.totalAvailableQuantity;
      const withinAllowedVariation =
        parsedActualQuantity !== null &&
        parsedActualQuantity >= minimumAllowedQuantity &&
        parsedActualQuantity <= maximumAllowedQuantity;
      const variationPercentage =
        parsedActualQuantity === null || !item.requiredQuantity
          ? null
          : Math.abs(((parsedActualQuantity - item.requiredQuantity) / item.requiredQuantity) * 100);

      return {
        ...item,
        actualQuantityInput,
        parsedActualQuantity,
        hasValidActualQuantity,
        minimumAllowedQuantity,
        maximumAllowedQuantity,
        exceedsAvailableStock,
        withinAllowedVariation,
        variationPercentage,
      };
    });
  }, [actualQuantities, previewRun]);

  const quantityIssues = useMemo(
    () =>
      previewItems.filter(
        (item) =>
          !item.hasValidActualQuantity ||
          item.exceedsAvailableStock ||
          !item.withinAllowedVariation,
      ),
    [previewItems],
  );

  const canConfirmPreview =
    Boolean(previewRun) &&
    previewRun?.status !== 'CONFIRMED' &&
    previewItems.length > 0 &&
    quantityIssues.length === 0 &&
    previewItems.some((item) => (item.parsedActualQuantity ?? 0) > 0);

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

  const handleActualQuantityChange = (recipeItemId, value) => {
    setActualQuantities((currentState) => ({
      ...currentState,
      [recipeItemId]: value,
    }));
  };

  const handleConfirm = async () => {
    if (!previewRun) {
      return;
    }

    if (!previewItems.length) {
      setServerMessage('No hay insumos disponibles para confirmar esta elaboracion.');
      return;
    }

    if (previewItems.some((item) => !item.hasValidActualQuantity)) {
      setServerMessage('Ingrese una cantidad real valida para cada insumo antes de confirmar.');
      return;
    }

    if (previewItems.some((item) => item.exceedsAvailableStock)) {
      setServerMessage('La cantidad real no puede exceder el stock disponible de ningun insumo.');
      return;
    }

    if (previewItems.some((item) => !item.withinAllowedVariation)) {
      setServerMessage(
        'La cantidad real debe mantenerse dentro del rango permitido de variacion para todos los insumos.',
      );
      return;
    }

    if (!previewItems.some((item) => (item.parsedActualQuantity ?? 0) > 0)) {
      setServerMessage('Debe registrar al menos una cantidad real mayor que cero para confirmar.');
      return;
    }

    setConfirming(true);
    setServerMessage('');
    setFeedback('');

    try {
      const response = await confirmProductionRun(
        previewRun.id,
        previewItems.map((item) => ({
          recipeItemId: item.recipeItemId,
          actualQuantity: item.parsedActualQuantity,
        })),
      );
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

  const handlePrintRecipe = async () => {
    if (!previewRun?.recipeId) {
      return;
    }

    let printSession;
    setPrintingRecipe(true);
    setServerMessage('');

    try {
      printSession = openPrintPreviewWindow(`Formula ${previewRun.recipeCode || previewRun.recipeId}`);
      const printableRecipe = await fetchRecipePrintableById(previewRun.recipeId);
      printRecipeDocument(printableRecipe, printSession);
    } catch (requestError) {
      const message = getRecipesErrorMessage(requestError);
      renderPrintWindowError(
        printSession,
        `Formula ${previewRun.recipeCode || previewRun.recipeId}`,
        message,
      );
      setServerMessage(message);
    } finally {
      setPrintingRecipe(false);
    }
  };

  const handlePrintProductionRun = async () => {
    if (!previewRun?.id || previewRun.status !== 'CONFIRMED') {
      return;
    }

    let printSession;
    setPrintingProductionRun(true);
    setServerMessage('');

    try {
      printSession = openPrintPreviewWindow(`Descargo ${previewRun.recipeCode || previewRun.id}`);
      const printableProductionRun = await fetchProductionRunPrintableById(previewRun.id);
      printProductionRunDocument(printableProductionRun, printSession);
    } catch (requestError) {
      const message = getProductionRunErrorMessage(requestError);
      renderPrintWindowError(
        printSession,
        `Descargo ${previewRun.recipeCode || previewRun.id}`,
        message,
      );
      setServerMessage(message);
    } finally {
      setPrintingProductionRun(false);
    }
  };

  const handleReset = () => {
    reset(defaultValues);
    setPreviewRun(null);
    setActualQuantities({});
    setServerMessage('');
    setFeedback('');
  };

  const shortageItems = previewItems.filter(
    (item) =>
      item.parsedActualQuantity !== null && item.parsedActualQuantity > item.totalAvailableQuantity,
  );

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Elaboracion"
        subtitle="Prepara un descargo por formula, revisa los insumos sugeridos por lote y confirma la salida de inventario sin afectar el descargo individual."
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
                Descargo por formula
              </h2>
              <p className="mt-4 text-sm leading-7 text-white/72">
                Este flujo toma una formula, valida stock completo, permite registrar cantidades reales y crea una salida trazable con una linea por cada lote sugerido.
              </p>
              <div className="mt-8 space-y-3 text-sm text-white/78">
                <p>1. Selecciona producto a elaborar, formula y laboratorio.</p>
                <p>2. Prepara la elaboracion para revisar insumos, cantidades y lotes FEFO.</p>
                <p>3. Ajusta la cantidad real de cada insumo dentro del rango permitido.</p>
                <p>4. Confirma solo si toda la formula tiene disponibilidad suficiente.</p>
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
                    label="Producto a elaborar"
                    required
                    error={errors.manufacturedProductId?.message}
                  >
                    <select
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      defaultValue=""
                      {...register('manufacturedProductId')}
                    >
                      <option value="" disabled>
                        Seleccione un producto a elaborar
                      </option>
                      {manufacturedProducts
                        .filter((item) => item.active)
                        .sort((left, right) => left.name.localeCompare(right.name))
                        .map((item) => (
                          <option key={item.id} value={item.id}>
                            {item.name} ({item.code}){item.groupCode ? ` • ${item.groupCode}` : ''}
                          </option>
                        ))}
                    </select>
                  </Field>

                  <Field label="Formula" required error={errors.recipeId?.message}>
                    <select
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      defaultValue=""
                      {...register('recipeId')}
                      disabled={!selectedManufacturedProductId}
                    >
                      <option value="" disabled>
                        {selectedManufacturedProductId
                          ? 'Seleccione una formula'
                          : 'Seleccione primero un producto a elaborar'}
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
                      Esta formula contiene <span className="text-brand-ink">{selectedRecipe.items.length}</span> insumo(s) registrados.
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
                      Producto a elaborar
                    </p>
                    <h4 className="mt-2 text-lg font-extrabold text-brand-ink">
                      {previewRun.manufacturedProductName}
                    </h4>
                    <p className="mt-1 text-sm leading-6 text-copy">
                      Formula {previewRun.recipeCode} / {previewRun.recipeName}
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
                            {item.productName} ({item.productCode}): disponible {formatQuantity(item.totalAvailableQuantity)} y cantidad real actual {formatQuantity(item.parsedActualQuantity ?? item.requiredQuantity)}.
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="rounded-[24px] border border-[#fff1d2] bg-[#fff8e8] px-4 py-4 text-sm font-semibold text-[#9a6a0a]">
                  Cada insumo debe mantenerse dentro de la variacion maxima del 10% respecto a la formula teorica. Si la cantidad real supera ese rango, el backend bloqueara la confirmacion.
                </div>
              )}

              <div className="space-y-3">
                {previewItems.map((item) => (
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
                      <div className="rounded-[18px] bg-surface-2/65 px-4 py-2.5">
                        <p className="text-[11px] font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                          Cantidad real
                        </p>
                        <input
                          type="number"
                          min="0"
                          step="0.0001"
                          value={item.actualQuantityInput}
                          disabled={previewRun.status === 'CONFIRMED'}
                          onChange={(event) =>
                            handleActualQuantityChange(item.recipeItemId, event.target.value)
                          }
                          className={`mt-2 w-full rounded-[16px] border px-3 py-2 text-sm font-semibold outline-none transition ${
                            !item.hasValidActualQuantity || item.exceedsAvailableStock || !item.withinAllowedVariation
                              ? 'border-[#d53a43] bg-[#fff4f5] text-[#8a2530] focus:ring-4 focus:ring-[#f7cfd4]'
                              : 'border-transparent bg-white text-brand-ink focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10'
                          }`}
                        />
                        <p className="mt-2 text-xs font-semibold text-copy-soft">
                          Rango permitido: {formatQuantity(item.minimumAllowedQuantity)} - {formatQuantity(item.maximumAllowedQuantity)} {item.unitOfMeasureSymbol || item.unitOfMeasureName}
                        </p>
                      </div>
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
                        value={item.observations || 'Sin observaciones en la formula.'}
                      />
                    </div>

                    <div className="mt-3 grid gap-2.5 md:grid-cols-2 xl:grid-cols-3">
                      <PreviewDataCell
                        label="Variacion actual"
                        value={
                          item.variationPercentage == null
                            ? 'Pendiente'
                            : `${formatQuantity(item.variationPercentage)}%`
                        }
                      />
                      <PreviewDataCell
                        label="Maximo permitido"
                        value={`${formatQuantity(item.maximumVariationPercentage ?? 10)}%`}
                      />
                      <div
                        className={`rounded-[18px] px-4 py-2.5 ${
                          !item.hasValidActualQuantity || item.exceedsAvailableStock || !item.withinAllowedVariation
                            ? 'bg-[#fff4f5] text-[#b73945]'
                            : 'bg-[#eef6f0] text-[#2d7a49]'
                        }`}
                      >
                        <p className="text-[11px] font-extrabold uppercase tracking-[0.18em] opacity-70">
                          Estado de control
                        </p>
                        <p className="mt-2 text-sm font-extrabold">
                          {!item.hasValidActualQuantity
                            ? 'Cantidad invalida'
                            : item.exceedsAvailableStock
                              ? 'Supera stock disponible'
                              : item.withinAllowedVariation
                                ? 'Dentro de tolerancia'
                                : 'Fuera de tolerancia'}
                        </p>
                      </div>
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

              <div className="grid gap-3 xl:grid-cols-2">
                <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                  <div className="flex items-center gap-3">
                    <Printer className="h-5 w-5 text-brand-teal" />
                    <p className="text-sm font-extrabold text-brand-ink">
                      Impresion de formula
                    </p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-copy">
                    Genera una vista imprimible de la formula teorica con producto, grupo, ciclo, lote y materias primas.
                  </p>
                  <button
                    type="button"
                    onClick={handlePrintRecipe}
                    disabled={printingRecipe}
                    className="mt-4 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    <Printer className="h-4 w-4" />
                    {printingRecipe ? 'Preparando...' : 'Imprimir formula'}
                  </button>
                </div>

                <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                  <div className="flex items-center gap-3">
                    <Printer className="h-5 w-5 text-[#d28a19]" />
                    <p className="text-sm font-extrabold text-brand-ink">
                      Impresion de descargo
                    </p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-copy">
                    Genera la salida imprimible del descargo real con cantidades teoricas, cantidades reales y detalle por lote.
                  </p>
                  <button
                    type="button"
                    onClick={handlePrintProductionRun}
                    disabled={printingProductionRun || previewRun.status !== 'CONFIRMED'}
                    className="mt-4 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    <Printer className="h-4 w-4" />
                    {printingProductionRun
                      ? 'Preparando...'
                      : previewRun.status === 'CONFIRMED'
                        ? 'Imprimir descargo'
                        : 'Confirma para imprimir'}
                  </button>
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
                    !canConfirmPreview
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
                Selecciona una formula y prepara la elaboracion para revisar insumos, cantidades requeridas, lote sugerido y advertencias antes de confirmar.
              </p>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

export default ProductionRunCreatePage;
