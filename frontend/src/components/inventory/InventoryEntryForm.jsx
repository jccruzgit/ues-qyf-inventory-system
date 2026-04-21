import { AlertTriangle, PackageCheck, ShieldAlert } from 'lucide-react';

function Field({ label, htmlFor, required, hint, error, children }) {
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

function ProductRequirementCard({ icon: Icon, title, description, tone }) {
  return (
    <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white p-4">
      <div className={`inline-flex h-11 w-11 items-center justify-center rounded-2xl ${tone}`}>
        <Icon className="h-5 w-5" strokeWidth={2} />
      </div>
      <h3 className="mt-4 text-sm font-extrabold text-brand-ink">{title}</h3>
      <p className="mt-2 text-xs leading-6 text-copy-soft">{description}</p>
    </div>
  );
}

function InventoryEntryForm({
  form,
  products,
  laboratories,
  selectedProduct,
  onSubmit,
  onCancel,
  submitLabel,
  isSubmitting,
  serverMessage,
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = form;

  const productRequiresTracking =
    selectedProduct?.requiresBatchControl || selectedProduct?.requiresExpiration;

  return (
    <form className="space-y-8" onSubmit={handleSubmit(onSubmit)}>
      <div className="grid gap-5 lg:grid-cols-2">
        <Field
          label="Producto"
          htmlFor="productId"
          required
          error={errors.productId?.message}
        >
          <select
            id="productId"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            defaultValue=""
            aria-invalid={Boolean(errors.productId)}
            {...register('productId')}
          >
            <option value="" disabled>
              Seleccione un producto
            </option>
            {products.map((product) => (
              <option key={product.id} value={product.id}>
                {product.name} ({product.code})
              </option>
            ))}
          </select>
        </Field>

        <Field
          label="Laboratorio"
          htmlFor="laboratoryId"
          required
          error={errors.laboratoryId?.message}
        >
          <select
            id="laboratoryId"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            defaultValue=""
            aria-invalid={Boolean(errors.laboratoryId)}
            {...register('laboratoryId')}
          >
            <option value="" disabled>
              Seleccione un laboratorio
            </option>
            {laboratories.map((laboratory) => (
              <option key={laboratory.value} value={laboratory.value}>
                {laboratory.label}
              </option>
            ))}
          </select>
        </Field>
      </div>

      {selectedProduct ? (
        <div className="rounded-[30px] border border-brand-ink/[0.06] bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-5">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Producto seleccionado
              </p>
              <h3 className="mt-3 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                {selectedProduct.name}
              </h3>
              <p className="mt-2 text-sm leading-7 text-copy">
                Codigo {selectedProduct.code}. Unidad base: {selectedProduct.unit}. Stock actual:{' '}
                {selectedProduct.stock} {selectedProduct.unit}.
              </p>
            </div>

            <div className="rounded-[20px] bg-brand-ink px-4 py-3 text-sm font-bold text-white">
              Stock minimo: {selectedProduct.minimumStock} {selectedProduct.unit}
            </div>
          </div>

          <div className="mt-5 grid gap-4 md:grid-cols-2">
            <ProductRequirementCard
              icon={PackageCheck}
              title={
                selectedProduct.requiresBatchControl
                  ? 'Control por lotes habilitado'
                  : 'Lote opcional'
              }
              description={
                selectedProduct.requiresBatchControl
                  ? 'Debe registrar un numero de lote para preservar la trazabilidad del inventario.'
                  : 'Puede registrar un lote para mejorar la trazabilidad, aunque no es obligatorio.'
              }
              tone="bg-brand-teal-soft text-brand-teal"
            />
            <ProductRequirementCard
              icon={ShieldAlert}
              title={
                selectedProduct.requiresExpiration
                  ? 'Vencimiento obligatorio'
                  : 'Vencimiento opcional'
              }
              description={
                selectedProduct.requiresExpiration
                  ? 'Este producto exige fecha de vencimiento para activar alertas y seguimiento.'
                  : 'Puede registrar fecha de vencimiento si aplica para este lote.'
              }
              tone="bg-[#fff3dd] text-[#d28a19]"
            />
          </div>
        </div>
      ) : null}

      <div className="grid gap-5 lg:grid-cols-3">
        <Field label="Numero de lote" htmlFor="batchCode" error={errors.batchCode?.message}>
          <input
            id="batchCode"
            type="text"
            placeholder="Ej. LOTE-2026-001"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.batchCode)}
            {...register('batchCode')}
          />
        </Field>

        <Field
          label="Fecha de vencimiento"
          htmlFor="expirationDate"
          error={errors.expirationDate?.message}
        >
          <input
            id="expirationDate"
            type="date"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.expirationDate)}
            {...register('expirationDate')}
          />
        </Field>

        <Field
          label="Cantidad ingresada"
          htmlFor="quantity"
          required
          error={errors.quantity?.message}
        >
          <input
            id="quantity"
            type="number"
            min="0.01"
            step="0.01"
            placeholder="0"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.quantity)}
            {...register('quantity')}
          />
        </Field>
      </div>

      <div className="grid gap-5 lg:grid-cols-[minmax(0,0.36fr)_minmax(0,0.64fr)]">
        <Field
          label="Unidad"
          htmlFor="unitLabel"
          hint="La unidad se toma automaticamente de la configuracion del producto."
        >
          <input
            id="unitLabel"
            type="text"
            readOnly
            className="w-full cursor-not-allowed rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink/80 outline-none"
            {...register('unitLabel')}
          />
        </Field>

        <Field
          label="Observaciones"
          htmlFor="observations"
          error={errors.observations?.message}
        >
          <textarea
            id="observations"
            rows={4}
            placeholder="Notas internas sobre el ingreso, proveedor o condicion del lote."
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.observations)}
            {...register('observations')}
          />
        </Field>
      </div>

      {productRequiresTracking ? (
        <div className="rounded-[24px] border border-[#fff1d2] bg-[#fff8e8] px-4 py-3.5 text-sm font-semibold text-[#9a6a0a]">
          Este producto requiere seguimiento detallado por lote o vencimiento. Verifique que la
          informacion capturada coincida con la documentacion fisica del ingreso.
        </div>
      ) : null}

      {serverMessage ? (
        <div className="rounded-[24px] border border-[#fdebec] bg-[#fff4f5] px-4 py-3.5 text-sm font-semibold text-[#d53a43]">
          <div className="flex items-start gap-2">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{serverMessage}</span>
          </div>
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
          className="inline-flex items-center justify-center rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(14,47,103,0.18)] transition hover:bg-[#0b2551] disabled:cursor-not-allowed disabled:opacity-70"
        >
          {submitLabel}
        </button>
      </div>
    </form>
  );
}

export default InventoryEntryForm;
