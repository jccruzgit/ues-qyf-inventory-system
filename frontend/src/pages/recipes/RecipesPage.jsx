import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  BookOpenCheck,
  Eraser,
  FlaskConical,
  PackagePlus,
  RefreshCcw,
  Save,
  SearchX,
  Trash2,
} from 'lucide-react';
import { useForm } from 'react-hook-form';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { fetchProducts } from '../../services/productsService';
import { fetchManufacturedProducts, getManufacturedProductsErrorMessage } from '../../services/manufacturedProductsService';
import {
  addRecipeItem,
  createRecipe,
  deleteRecipeItem,
  fetchRecipes,
  getRecipeMutationErrorDetails,
  getRecipesErrorMessage,
  updateRecipe,
} from '../../services/recipesService';
import { recipeFormSchema, recipeItemFormSchema } from '../../schemas/recipe.schema';

const recipeDefaultValues = {
  manufacturedProductId: '',
  code: '',
  name: '',
  description: '',
  active: true,
};

const itemDefaultValues = {
  productId: '',
  unitOfMeasureId: '',
  quantity: '',
  observations: '',
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

function formatQuantity(value) {
  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  }).format(Number(value) || 0);
}

function RecipesPage() {
  const [recipes, setRecipes] = useState([]);
  const [manufacturedProducts, setManufacturedProducts] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [serverMessage, setServerMessage] = useState('');
  const [itemServerMessage, setItemServerMessage] = useState('');
  const [selectedRecipeId, setSelectedRecipeId] = useState(null);
  const [draftItems, setDraftItems] = useState([]);

  const recipeForm = useForm({
    resolver: zodResolver(recipeFormSchema),
    defaultValues: recipeDefaultValues,
  });
  const itemForm = useForm({
    resolver: zodResolver(recipeItemFormSchema),
    defaultValues: itemDefaultValues,
  });

  const {
    register,
    handleSubmit,
    reset,
    setError: setRecipeFieldError,
    watch,
    formState: { errors, isSubmitting },
  } = recipeForm;
  const {
    register: registerItem,
    handleSubmit: handleSubmitItem,
    reset: resetItem,
    setValue: setItemValue,
    setError: setItemFieldError,
    clearErrors: clearItemErrors,
    watch: watchItem,
    formState: { errors: itemErrors, isSubmitting: isSubmittingItem },
  } = itemForm;

  const selectedRecipe = useMemo(
    () => recipes.find((item) => item.id === selectedRecipeId) ?? null,
    [recipes, selectedRecipeId],
  );
  const currentItems = selectedRecipeId ? selectedRecipe?.items ?? [] : draftItems;
  const selectedManufacturedProductId = Number(watch('manufacturedProductId'));
  const selectedItemProductId = Number(watchItem('productId'));
  const selectedItemProduct = products.find((item) => item.id === selectedItemProductId) ?? null;

  const manufacturedProductOptions = useMemo(
    () =>
      manufacturedProducts
        .filter((item) => item.active)
        .sort((left, right) => left.name.localeCompare(right.name)),
    [manufacturedProducts],
  );

  const productOptions = useMemo(
    () => products.slice().sort((left, right) => left.name.localeCompare(right.name)),
    [products],
  );

  useEffect(() => {
    setItemValue('unitOfMeasureId', selectedItemProduct?.baseUnitId ? String(selectedItemProduct.baseUnitId) : '');
    clearItemErrors('unitOfMeasureId');
  }, [clearItemErrors, selectedItemProduct, setItemValue]);

  const loadPageData = async () => {
    setLoading(true);
    setError('');

    try {
      const [recipesResponse, manufacturedProductsResponse, productsResponse] = await Promise.all([
        fetchRecipes(),
        fetchManufacturedProducts(),
        fetchProducts(),
      ]);

      setRecipes(recipesResponse);
      setManufacturedProducts(manufacturedProductsResponse);
      setProducts(productsResponse.items);
    } catch (requestError) {
      setRecipes([]);
      setManufacturedProducts([]);
      setProducts([]);
      setError(
        getRecipesErrorMessage(requestError) ||
          getManufacturedProductsErrorMessage(requestError),
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPageData();
  }, []);

  const handleStartCreate = () => {
    setSelectedRecipeId(null);
    setDraftItems([]);
    setFeedback('');
    setServerMessage('');
    setItemServerMessage('');
    reset(recipeDefaultValues);
    resetItem(itemDefaultValues);
  };

  const handleSelectRecipe = (recipe) => {
    setSelectedRecipeId(recipe.id);
    setDraftItems([]);
    setFeedback('');
    setServerMessage('');
    setItemServerMessage('');
    reset({
      manufacturedProductId: String(recipe.manufacturedProductId),
      code: recipe.code,
      name: recipe.name,
      description: recipe.description,
      active: recipe.active,
    });
    resetItem(itemDefaultValues);
  };

  const handleAddItem = async (values) => {
    setItemServerMessage('');
    setFeedback('');

    if (currentItems.some((item) => item.productId === values.productId)) {
      setItemFieldError('productId', {
        type: 'manual',
        message: 'Ese insumo ya forma parte de la receta.',
      });
      return;
    }

    if (!selectedItemProduct) {
      setItemFieldError('productId', {
        type: 'manual',
        message: 'Seleccione un insumo valido.',
      });
      return;
    }

    const itemPayload = {
      productId: values.productId,
      unitOfMeasureId: values.unitOfMeasureId,
      quantity: values.quantity,
      observations: values.observations,
    };

    if (selectedRecipeId) {
      try {
        const updatedRecipe = await addRecipeItem(selectedRecipeId, itemPayload);
        setRecipes((currentRecipes) =>
          currentRecipes.map((item) => (item.id === updatedRecipe.id ? updatedRecipe : item)),
        );
        setFeedback('Insumo agregado a la receta.');
        resetItem(itemDefaultValues);
      } catch (requestError) {
        const details = getRecipeMutationErrorDetails(requestError);

        Object.entries(details.fieldErrors).forEach(([field, message]) => {
          setItemFieldError(field, { type: 'server', message });
        });

        setItemServerMessage(details.message);
      }

      return;
    }

    setDraftItems((currentDraftItems) => [
      ...currentDraftItems,
      {
        id: `draft-${Date.now()}`,
        itemOrder: currentDraftItems.length + 1,
        productId: selectedItemProduct.id,
        productCode: selectedItemProduct.code,
        productName: selectedItemProduct.name,
        unitOfMeasureId: selectedItemProduct.baseUnitId,
        unitOfMeasureName: selectedItemProduct.unitName,
        unitOfMeasureSymbol: selectedItemProduct.unitSymbol,
        quantity: values.quantity,
        observations: values.observations?.trim() || '',
        locationName: selectedItemProduct.locationName || 'Ubicacion no definida',
      },
    ]);
    setFeedback('Insumo agregado al borrador de receta.');
    resetItem(itemDefaultValues);
  };

  const handleRemoveItem = async (item) => {
    setItemServerMessage('');
    setFeedback('');

    if (!selectedRecipeId) {
      setDraftItems((currentDraftItems) =>
        currentDraftItems
          .filter((currentItem) => currentItem.id !== item.id)
          .map((currentItem, index) => ({ ...currentItem, itemOrder: index + 1 })),
      );
      return;
    }

    try {
      const updatedRecipe = await deleteRecipeItem(selectedRecipeId, item.id);
      setRecipes((currentRecipes) =>
        currentRecipes.map((currentRecipe) =>
          currentRecipe.id === updatedRecipe.id ? updatedRecipe : currentRecipe,
        ),
      );
      setFeedback('Insumo eliminado de la receta.');
    } catch (requestError) {
      setItemServerMessage(getRecipeMutationErrorDetails(requestError).message);
    }
  };

  const handleSaveRecipe = async (values) => {
    setServerMessage('');
    setFeedback('');

    if (!currentItems.length) {
      setServerMessage('La receta debe tener al menos un insumo antes de guardarse.');
      return;
    }

    if (selectedRecipeId) {
      try {
        const updatedRecipe = await updateRecipe(selectedRecipeId, values);
        setRecipes((currentRecipes) =>
          currentRecipes.map((item) => (item.id === updatedRecipe.id ? updatedRecipe : item)),
        );
        setFeedback('Receta actualizada correctamente.');
      } catch (requestError) {
        const details = getRecipeMutationErrorDetails(requestError);

        Object.entries(details.fieldErrors).forEach(([field, message]) => {
          setRecipeFieldError(field, { type: 'server', message });
        });

        setServerMessage(details.message);
      }

      return;
    }

    let createdRecipe = null;

    try {
      createdRecipe = await createRecipe(values);
      let latestRecipe = createdRecipe;

      for (const item of draftItems) {
        latestRecipe = await addRecipeItem(createdRecipe.id, {
          productId: item.productId,
          unitOfMeasureId: item.unitOfMeasureId,
          quantity: item.quantity,
          observations: item.observations,
        });
      }

      setRecipes((currentRecipes) =>
        [...currentRecipes, latestRecipe].sort((left, right) => left.name.localeCompare(right.name)),
      );
      setSelectedRecipeId(latestRecipe.id);
      setDraftItems([]);
      reset({
        manufacturedProductId: String(latestRecipe.manufacturedProductId),
        code: latestRecipe.code,
        name: latestRecipe.name,
        description: latestRecipe.description,
        active: latestRecipe.active,
      });
      setFeedback('Receta creada correctamente.');
    } catch (requestError) {
      const details = getRecipeMutationErrorDetails(requestError);

      Object.entries(details.fieldErrors).forEach(([field, message]) => {
        setRecipeFieldError(field, { type: 'server', message });
      });

      setServerMessage(
        createdRecipe
          ? 'La receta base fue creada, pero no se pudieron registrar todos los insumos. Revisa la receta y completa los faltantes.'
          : details.message,
      );
      await loadPageData();
    }
  };

  const recipesByManufacturedProduct = recipes.filter(
    (item) => item.manufacturedProductId === selectedManufacturedProductId,
  );

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Recetas"
        subtitle="Define la composicion de cada producto elaborado usando los insumos ya existentes en inventario."
        action={
          <button
            type="button"
            onClick={handleStartCreate}
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
          >
            <PackagePlus className="h-4 w-4" strokeWidth={2.3} />
            Nueva receta
          </button>
        }
      />

      {feedback ? (
        <div className="rounded-[24px] border border-[#d2e6d8] bg-[#eef6f0] px-4 py-3 text-sm font-semibold text-[#2d7a49]">
          {feedback}
        </div>
      ) : null}

      <div className="grid gap-6 xl:grid-cols-[minmax(340px,0.4fr)_minmax(0,0.6fr)]">
        <Card className="space-y-4 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Recetas registradas
              </p>
              <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                Catalogo de formulas
              </h3>
            </div>
            <button
              type="button"
              onClick={loadPageData}
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
                  <div className="h-4 w-20 rounded-full bg-surface-2" />
                  <div className="mt-3 h-5 w-3/4 rounded-full bg-surface-2" />
                  <div className="mt-3 h-3 w-full rounded-full bg-surface-2" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-4 text-sm font-semibold text-[#d53a43]">
              {error}
            </div>
          ) : recipes.length ? (
            <div className="space-y-3">
              {recipes
                .slice()
                .sort((left, right) => left.name.localeCompare(right.name))
                .map((recipe) => {
                  const isSelected = selectedRecipeId === recipe.id;

                  return (
                    <button
                      key={recipe.id}
                      type="button"
                      onClick={() => handleSelectRecipe(recipe)}
                      className={`w-full rounded-[24px] border px-5 py-4 text-left transition ${
                        isSelected
                          ? 'border-brand-teal/30 bg-[#f3faf6] shadow-[0_12px_26px_rgba(23,61,44,0.08)]'
                          : 'border-white/80 bg-white hover:border-brand-teal/20 hover:bg-[#fbfdfb]'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                            {recipe.code}
                          </p>
                          <h4 className="mt-2 text-lg font-extrabold tracking-[-0.03em] text-brand-ink">
                            {recipe.name}
                          </h4>
                          <p className="mt-1 text-sm font-semibold text-copy">
                            {recipe.manufacturedProductName}
                          </p>
                        </div>
                        <span className="rounded-full bg-[#e7f4eb] px-3 py-1 text-xs font-extrabold text-[#2d7a49]">
                          {recipe.items.length} insumo(s)
                        </span>
                      </div>
                      <p className="mt-3 text-sm leading-6 text-copy">
                        {recipe.description || 'Sin descripcion registrada.'}
                      </p>
                    </button>
                  );
                })}
            </div>
          ) : (
            <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-8 text-center">
              <SearchX className="mx-auto h-8 w-8 text-copy-soft" strokeWidth={1.9} />
              <h3 className="mt-4 text-lg font-extrabold text-brand-ink">Aun no hay recetas</h3>
              <p className="mt-2 text-sm leading-7 text-copy">
                Crea la primera receta para vincular un producto elaborado con sus insumos y cantidades requeridas.
              </p>
            </div>
          )}
        </Card>

        <div className="space-y-6">
          <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-0">
            <div className="grid gap-0 lg:grid-cols-[minmax(220px,0.28fr)_minmax(0,0.72fr)]">
              <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#163826_0%,_#1e5d38_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
                <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
                  <BookOpenCheck className="h-5 w-5" strokeWidth={2.1} />
                </div>
                <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
                  {selectedRecipe ? 'Editar receta' : 'Nueva receta'}
                </h2>
                <p className="mt-4 text-sm leading-7 text-white/72">
                  Cada receta describe los insumos y cantidades necesarias para elaborar un producto final.
                </p>
                <div className="mt-8 space-y-3 text-sm text-white/78">
                  <p>1. Selecciona el producto elaborado objetivo.</p>
                  <p>2. Agrega al menos un insumo con cantidad mayor que cero.</p>
                  <p>3. Guarda la receta para usarla en el modulo de elaboracion.</p>
                </div>
              </aside>

              <div className="p-6 sm:p-8 lg:p-10">
                <form className="space-y-6" onSubmit={handleSubmit(handleSaveRecipe)}>
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
                      {manufacturedProductOptions.map((item) => (
                        <option key={item.id} value={item.id}>
                          {item.name} ({item.code})
                        </option>
                      ))}
                    </select>
                  </Field>

                  <Field label="Codigo de receta" required error={errors.code?.message}>
                    <input
                      type="text"
                      placeholder="Ej. REC-JAB-001"
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      {...register('code')}
                    />
                  </Field>

                  <Field label="Nombre de receta" required error={errors.name?.message}>
                    <input
                      type="text"
                      placeholder="Ej. Formula base de jabon liquido"
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      {...register('name')}
                    />
                  </Field>

                  <label className="flex items-start gap-3 rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-4">
                    <input
                      type="checkbox"
                      className="mt-1 h-4 w-4 rounded border-brand-ink/20 text-brand-teal focus:ring-brand-teal/20"
                      {...register('active')}
                    />
                    <span className="text-sm font-semibold leading-6 text-copy">
                      Mantener la receta activa para nuevas elaboraciones.
                    </span>
                  </label>

                  <Field label="Descripcion" error={errors.description?.message}>
                    <textarea
                      rows={4}
                      placeholder="Describe el contexto de la receta o sus observaciones generales."
                      className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
                      {...register('description')}
                    />
                  </Field>

                  {recipesByManufacturedProduct.length > 1 ? (
                    <div className="rounded-[20px] border border-[#fff1d2] bg-[#fff8e8] px-4 py-3 text-sm font-semibold text-[#9a6a0a]">
                      Este producto elaborado ya tiene {recipesByManufacturedProduct.length} recetas registradas. Para esta primera version se recomienda mantener solo una receta activa.
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
                      onClick={handleStartCreate}
                      className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                    >
                      <Eraser className="h-4 w-4" />
                      Limpiar
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
                    >
                      <Save className="h-4 w-4" />
                      {isSubmitting ? 'Guardando...' : 'Guardar'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </Card>

          <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                  Detalle de insumos
                </p>
                <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                  Composicion de la receta
                </h3>
              </div>
              <span className="rounded-full bg-brand-teal-soft px-3 py-1 text-xs font-extrabold text-brand-teal">
                {currentItems.length} insumo(s)
              </span>
            </div>

            <form
              className="grid gap-4 lg:grid-cols-2 xl:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]"
              onSubmit={handleSubmitItem(handleAddItem)}
            >
              <Field label="Insumo" required error={itemErrors.productId?.message}>
                <select
                  className="w-full rounded-[22px] border border-transparent bg-white px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10"
                  defaultValue=""
                  {...registerItem('productId')}
                >
                  <option value="" disabled>
                    Seleccione un insumo
                  </option>
                  {productOptions.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name} ({item.code})
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                label="Unidad"
                required
                error={itemErrors.unitOfMeasureId?.message}
                hint="En este MVP se usa la unidad base actual del insumo."
              >
                <input
                  type="text"
                  readOnly
                  value={
                    selectedItemProduct
                      ? `${selectedItemProduct.unitName}${selectedItemProduct.unitSymbol ? ` (${selectedItemProduct.unitSymbol})` : ''}`
                      : ''
                  }
                  className="w-full cursor-not-allowed rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink/80 outline-none"
                />
                <input type="hidden" {...registerItem('unitOfMeasureId')} />
              </Field>

              <Field label="Cantidad" required error={itemErrors.quantity?.message}>
                <input
                  type="number"
                  min="0.0001"
                  step="0.0001"
                  placeholder="0"
                  className="w-full rounded-[22px] border border-transparent bg-white px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10"
                  {...registerItem('quantity')}
                />
              </Field>

              <Field label="Observaciones" error={itemErrors.observations?.message}>
                <input
                  type="text"
                  placeholder="Opcional"
                  className="w-full rounded-[22px] border border-transparent bg-white px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:ring-4 focus:ring-brand-teal/10"
                  {...registerItem('observations')}
                />
              </Field>

              <div className="flex items-end lg:col-span-2">
                <button
                  type="submit"
                  disabled={isSubmittingItem}
                  className="inline-flex h-[54px] w-full items-center justify-center gap-2 rounded-full bg-brand-ink px-5 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70 sm:w-auto sm:min-w-[160px]"
                >
                  <PackagePlus className="h-4 w-4" />
                  Agregar
                </button>
              </div>
            </form>

            {itemServerMessage ? (
              <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-3 text-sm font-semibold text-[#d53a43]">
                {itemServerMessage}
              </div>
            ) : null}

            {currentItems.length ? (
              <div className="space-y-3">
                {currentItems.map((item) => (
                  <div
                    key={item.id}
                    className="flex flex-col gap-4 rounded-[24px] border border-white/80 bg-white px-5 py-4 lg:flex-row lg:items-start lg:justify-between"
                  >
                    <div className="min-w-0">
                      <p className="text-xs font-extrabold uppercase tracking-[0.18em] text-copy-soft">
                        Insumo #{item.itemOrder}
                      </p>
                      <h4 className="mt-2 text-lg font-extrabold tracking-[-0.03em] text-brand-ink">
                        {item.productName}
                      </h4>
                      <p className="mt-1 text-sm font-semibold text-copy">
                        {item.productCode} • {formatQuantity(item.quantity)} {item.unitOfMeasureSymbol || item.unitOfMeasureName}
                      </p>
                      <p className="mt-2 text-sm leading-6 text-copy">
                        Ubicacion: {item.locationName}
                      </p>
                      <p className="mt-1 text-sm leading-6 text-copy">
                        {item.observations || 'Sin observaciones para esta linea.'}
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={() => handleRemoveItem(item)}
                      className="inline-flex items-center gap-2 rounded-full border border-[#f4c7cb] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-[#b73945] transition hover:bg-[#fff3f4]"
                    >
                      <Trash2 className="h-4 w-4" />
                      Quitar
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-5 py-8 text-center">
                <FlaskConical className="mx-auto h-8 w-8 text-copy-soft" strokeWidth={1.9} />
                <h3 className="mt-4 text-lg font-extrabold text-brand-ink">
                  La receta aun no tiene insumos
                </h3>
                <p className="mt-2 text-sm leading-7 text-copy">
                  Agrega al menos un insumo con cantidad mayor a cero antes de guardar la receta.
                </p>
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}

export default RecipesPage;
