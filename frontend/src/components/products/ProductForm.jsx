function ProductField({ label, htmlFor, required, error, children, hint }) {
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

function ProductForm({
  form,
  onSubmit,
  categories,
  units,
  locations,
  submitLabel,
  onCancel,
  serverMessage,
  isSubmitting,
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = form;

  return (
    <form className="space-y-8" onSubmit={handleSubmit(onSubmit)}>
      <div className="grid gap-5 lg:grid-cols-2">
        <ProductField
          label="Nombre"
          htmlFor="name"
          required
          error={errors.name?.message}
        >
          <input
            id="name"
            type="text"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            placeholder="Ej. Acido clorhidrico 37%"
            aria-invalid={Boolean(errors.name)}
            {...register('name')}
          />
        </ProductField>

        <ProductField
          label="Codigo"
          htmlFor="code"
          required
          error={errors.code?.message}
        >
          <input
            id="code"
            type="text"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            placeholder="Ej. CHEM-1001-AX"
            aria-invalid={Boolean(errors.code)}
            {...register('code')}
          />
        </ProductField>
      </div>

      <ProductField
        label="Descripcion"
        htmlFor="description"
        error={errors.description?.message}
      >
        <textarea
          id="description"
          rows={4}
          className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
          placeholder="Describe el uso o caracteristicas del insumo."
          aria-invalid={Boolean(errors.description)}
          {...register('description')}
        />
      </ProductField>

      <div className="grid gap-5 lg:grid-cols-3">
        <ProductField
          label="Categoria"
          htmlFor="categoryId"
          required
          error={errors.categoryId?.message}
        >
          <select
            id="categoryId"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.categoryId)}
            defaultValue=""
            {...register('categoryId')}
          >
            <option value="" disabled>
              Seleccione una categoria
            </option>
            {categories.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </ProductField>

        <ProductField
          label="Unidad base"
          htmlFor="baseUnitId"
          required
          error={errors.baseUnitId?.message}
        >
          <select
            id="baseUnitId"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.baseUnitId)}
            defaultValue=""
            {...register('baseUnitId')}
          >
            <option value="" disabled>
              Seleccione una unidad
            </option>
            {units.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </ProductField>

        <ProductField
          label="Ubicacion"
          htmlFor="locationId"
          required
          error={errors.locationId?.message}
        >
          <select
            id="locationId"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.locationId)}
            defaultValue=""
            {...register('locationId')}
          >
            <option value="" disabled>
              Seleccione una ubicacion
            </option>
            {locations.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </ProductField>
      </div>

      <div className="grid gap-5 lg:grid-cols-3">
        <ProductField
          label="Stock minimo"
          htmlFor="minimumStock"
          required
          error={errors.minimumStock?.message}
        >
          <input
            id="minimumStock"
            type="number"
            min="0"
            step="0.01"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            placeholder="0"
            aria-invalid={Boolean(errors.minimumStock)}
            {...register('minimumStock')}
          />
        </ProductField>

        <ProductField
          label="Stock actual inicial"
          htmlFor="currentStock"
          error={errors.currentStock?.message}
          hint="Si no se indica, se enviara en 0."
        >
          <input
            id="currentStock"
            type="number"
            min="0"
            step="0.01"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            placeholder="0"
            aria-invalid={Boolean(errors.currentStock)}
            {...register('currentStock')}
          />
        </ProductField>

        <ProductField
          label="Condicion de almacenamiento"
          htmlFor="storageCondition"
          error={errors.storageCondition?.message}
        >
          <select
            id="storageCondition"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            defaultValue="Ambiente"
            aria-invalid={Boolean(errors.storageCondition)}
            {...register('storageCondition')}
          >
            <option value="Ambiente">Ambiente</option>
            <option value="Refrigerado">Refrigerado</option>
            <option value="Congelado">Congelado</option>
            <option value="Protegido de la luz">Protegido de la luz</option>
          </select>
        </ProductField>
      </div>

      <ProductField
        label="Observaciones"
        htmlFor="observations"
        error={errors.observations?.message}
      >
        <textarea
          id="observations"
          rows={4}
          className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
          placeholder="Notas internas, advertencias o recomendaciones de manejo."
          aria-invalid={Boolean(errors.observations)}
          {...register('observations')}
        />
      </ProductField>

      <div className="grid gap-4 rounded-[28px] border border-brand-ink/[0.06] bg-surface-2/70 p-5 sm:grid-cols-3">
        <label className="flex items-start gap-3 rounded-[20px] bg-white px-4 py-3.5">
          <input type="checkbox" className="mt-1 h-4 w-4 accent-brand-ink" {...register('requiresExpiration')} />
          <span>
            <span className="block text-sm font-extrabold text-brand-ink">Requiere expiracion</span>
            <span className="mt-1 block text-xs leading-5 text-copy-soft">
              Activa el seguimiento por fecha de vencimiento.
            </span>
          </span>
        </label>

        <label className="flex items-start gap-3 rounded-[20px] bg-white px-4 py-3.5">
          <input type="checkbox" className="mt-1 h-4 w-4 accent-brand-ink" {...register('requiresBatchControl')} />
          <span>
            <span className="block text-sm font-extrabold text-brand-ink">Control por lotes</span>
            <span className="mt-1 block text-xs leading-5 text-copy-soft">
              Mantiene trazabilidad de entradas y salidas por lote.
            </span>
          </span>
        </label>

        <label className="flex items-start gap-3 rounded-[20px] bg-white px-4 py-3.5">
          <input type="checkbox" className="mt-1 h-4 w-4 accent-brand-ink" {...register('active')} />
          <span>
            <span className="block text-sm font-extrabold text-brand-ink">Activo</span>
            <span className="mt-1 block text-xs leading-5 text-copy-soft">
              Deja disponible el insumo en el catalogo al guardarlo.
            </span>
          </span>
        </label>
      </div>

      {serverMessage ? (
        <div className="rounded-[22px] border border-[#fdebec] bg-[#fff4f5] px-4 py-3 text-sm font-semibold text-[#d53a43]">
          {serverMessage}
        </div>
      ) : null}

      <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <button
          type="button"
          onClick={onCancel}
          className="inline-flex items-center justify-center rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center justify-center rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
        >
          {submitLabel}
        </button>
      </div>
    </form>
  );
}

export default ProductForm;
