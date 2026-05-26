import { AlertTriangle, ArrowDownCircle, Boxes, ShieldAlert } from 'lucide-react';

function Field({ label, required, hint, error, children }) {
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

function ProductContextCard({ icon: Icon, title, description, tone }) {
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

function formatDate(dateValue) {
  if (!dateValue) {
    return 'Sin vencimiento';
  }

  const [year, month, day] = String(dateValue).split('-').map(Number);

  if (!year || !month || !day) {
    return dateValue;
  }

  return new Intl.DateTimeFormat('es-SV', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(year, month - 1, day));
}

function InventoryExitForm({
  form,
  products,
  laboratories,
  selectedProduct,
  selectedLaboratory,
  batchOptions,
  selectedBatch,
  selectedProductStock,
  stockLoading,
  stockMessage,
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

  const productLocation = selectedProduct?.locationName?.trim() || 'Ubicacion no definida';
  const laboratoryLabel = selectedLaboratory?.label || 'Laboratorio no definido';

  return (
    <form className="space-y-8" onSubmit={handleSubmit(onSubmit)}>
      <div className="grid gap-5 lg:grid-cols-2">
        <Field label="Insumo" required error={errors.productId?.message}>
          <select
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            defaultValue=""
            aria-invalid={Boolean(errors.productId)}
            {...register('productId')}
          >
            <option value="" disabled>
              Seleccione un insumo
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
          required
          hint="Seleccione el laboratorio desde donde se descargara el inventario."
          error={errors.laboratoryId?.message}
        >
          <select
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
        <div className="rounded-[30px] border border-brand-ink/[0.06] bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-5">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
                Insumo seleccionado
              </p>
              <h3 className="mt-3 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
                {selectedProduct.name}
              </h3>
              <p className="mt-2 text-sm leading-7 text-copy">
                Codigo {selectedProduct.code}. Unidad base: {selectedProduct.unit}. Stock disponible
                en el laboratorio: {selectedProductStock} {selectedProduct.unit}.
              </p>
              <p className="mt-2 text-sm font-semibold text-copy">
                Origen visible: {laboratoryLabel} / {productLocation}
              </p>
            </div>

            <div className="rounded-[20px] bg-brand-ink px-4 py-3 text-sm font-bold text-white">
              Stock minimo: {selectedProduct.minimumStock} {selectedProduct.unit}
            </div>
          </div>

          <div className="mt-5 grid gap-4 md:grid-cols-2">
            <ProductContextCard
              icon={Boxes}
              title="Stock trazable"
              description="La salida reduce existencias y conserva la trazabilidad por lote del movimiento original."
              tone="bg-brand-teal-soft text-brand-teal"
            />
            <ProductContextCard
              icon={ShieldAlert}
              title="Reversion independiente"
              description="Si se comete un error, la correccion debe hacerse desde el flujo de reversion y no editando esta salida."
              tone="bg-[#fff3dd] text-[#d28a19]"
            />
          </div>
        </div>
      ) : null}

      <div className="grid gap-5 lg:grid-cols-4">
        <Field
          label="Lote disponible"
          required
          hint={
            !selectedLaboratory
              ? 'Seleccione un laboratorio para consultar lotes disponibles.'
              : !selectedProduct
                ? 'Seleccione un insumo para cargar los lotes con stock.'
                : selectedBatch
                  ? `Disponible: ${selectedBatch.quantityAvailable} ${selectedBatch.unit}. Vence: ${formatDate(selectedBatch.expirationDate)}.`
                  : 'Seleccione el lote o existencia disponible que desea descargar.'
          }
          error={errors.selectedBatchKey?.message}
        >
          <select
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10 disabled:cursor-not-allowed disabled:opacity-70"
            disabled={!selectedProduct || !selectedLaboratory || stockLoading || !batchOptions.length}
            defaultValue=""
            aria-invalid={Boolean(errors.selectedBatchKey)}
            {...register('selectedBatchKey')}
          >
            <option value="" disabled>
              {stockLoading ? 'Cargando lotes...' : 'Seleccione un lote'}
            </option>
            {batchOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </Field>

        <Field label="Cantidad a descargar" required error={errors.quantity?.message}>
          <input
            type="number"
            min="0.01"
            step="0.01"
            placeholder="0"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.quantity)}
            {...register('quantity')}
          />
        </Field>

        <Field
          label="Unidad de medida"
          required
          hint="La salida se registra en la unidad base configurada para el insumo."
        >
          <select
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            {...register('unitLabel')}
          >
            <option value={selectedProduct?.unit ?? ''}>{selectedProduct?.unit ?? 'Seleccione un insumo'}</option>
          </select>
        </Field>

        <Field
          label="Observacion por linea"
          hint="Use este campo para documentar el destino o motivo puntual de la descarga."
          error={errors.lineObservation?.message}
        >
          <input
            type="text"
            placeholder="Ej. Consumo en practica de laboratorio"
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.lineObservation)}
            {...register('lineObservation')}
          />
        </Field>
      </div>

      <div className="grid gap-5 lg:grid-cols-[minmax(0,0.34fr)_minmax(0,0.66fr)]">
        <div className="rounded-[24px] border border-brand-ink/[0.06] bg-surface-2/60 p-4">
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            Disponibilidad actual
          </p>
          <div className="mt-4 space-y-3 text-sm font-semibold text-copy">
            <p>
              <span className="font-extrabold text-brand-ink">Insumo:</span> {selectedProductStock}{' '}
              {selectedProduct?.unit ?? 'unidades'}
            </p>
            <p>
              <span className="font-extrabold text-brand-ink">Lote seleccionado:</span>{' '}
              {selectedBatch
                ? `${selectedBatch.quantityAvailable} ${selectedBatch.unit}`
                : 'Seleccione un lote'}
            </p>
            <p>
              <span className="font-extrabold text-brand-ink">Vencimiento:</span>{' '}
              {selectedBatch ? formatDate(selectedBatch.expirationDate) : 'No aplica'}
            </p>
          </div>
        </div>

        <Field
          label="Observacion general"
          error={errors.observations?.message}
        >
          <textarea
            rows={4}
            placeholder="Motivo general del descargo, referencia interna o contexto del movimiento."
            className="w-full rounded-[22px] border border-transparent bg-surface-2 px-4 py-3.5 text-sm text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            aria-invalid={Boolean(errors.observations)}
            {...register('observations')}
          />
        </Field>
      </div>

      {stockMessage ? (
        <div className="rounded-[24px] border border-[#fff1d2] bg-[#fff8e8] px-4 py-3.5 text-sm font-semibold text-[#9a6a0a]">
          {stockMessage}
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
          className="inline-flex items-center justify-center gap-2 rounded-full bg-brand-ink px-6 py-3 text-sm font-extrabold text-white shadow-[0_16px_30px_rgba(23,61,44,0.18)] transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-70"
        >
          <ArrowDownCircle className="h-4 w-4" strokeWidth={2.3} />
          {submitLabel}
        </button>
      </div>
    </form>
  );
}

export default InventoryExitForm;
