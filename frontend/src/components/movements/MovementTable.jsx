import { ArrowDown, ArrowUp, ChevronsUpDown, FileClock, RotateCcw } from 'lucide-react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';

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

function formatNumber(value) {
  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(Number(value) || 0);
}

function formatPrice(value) {
  if (value === null || value === undefined) {
    return 'No aplica';
  }

  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  }).format(Number(value) || 0);
}

function SortButton({ direction, onToggle }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-3 py-2 text-xs font-extrabold uppercase tracking-[0.18em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
    >
      Orden fecha
      {direction === 'desc' ? (
        <ArrowDown className="h-4 w-4" />
      ) : direction === 'asc' ? (
        <ArrowUp className="h-4 w-4" />
      ) : (
        <ChevronsUpDown className="h-4 w-4" />
      )}
    </button>
  );
}

function PaginationButton({ children, disabled, onClick }) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className="inline-flex items-center justify-center rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal disabled:cursor-not-allowed disabled:opacity-45"
    >
      {children}
    </button>
  );
}

function MovementTable({
  rows,
  sortDirection,
  onToggleSort,
  page,
  pageSize,
  totalPages,
  totalItems,
  onPageChange,
  onPageSizeChange,
  onReverseRequest,
}) {
  return (
    <Card className="overflow-hidden p-0">
      <div className="flex flex-col gap-4 border-b border-brand-ink/[0.06] px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            Historial de movimientos
          </p>
          <p className="mt-1 text-sm font-semibold text-copy">
            {totalItems} registro(s) en la consulta actual
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <label className="inline-flex items-center gap-2 text-sm font-semibold text-copy">
            Filas por pagina
            <select
              value={pageSize}
              onChange={(event) => onPageSizeChange(Number(event.target.value))}
              className="rounded-full border border-transparent bg-surface-2 px-3 py-2 text-sm font-extrabold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
            >
              {[10, 20, 50].map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </label>

          <SortButton direction={sortDirection} onToggle={onToggleSort} />
        </div>
      </div>

      <div className="max-h-[68vh] overflow-x-auto overflow-y-auto hide-scrollbar">
        <table className="min-w-[1880px] border-separate border-spacing-0">
          <thead className="sticky top-0 z-10 bg-[#f8fbff]">
            <tr>
              {[
                'Fecha',
                'Tipo',
                'Estado',
                'Producto',
                'Lote',
                'Cantidad',
                'Unidad',
                'Precio unitario',
                'Unidad precio',
                'Laboratorio',
                'Usuario',
                'Observacion general',
                'Nota por linea',
                'Acciones',
              ].map((column) => (
                <th
                  key={column}
                  className="border-b border-brand-ink/[0.06] px-5 py-4 text-left text-[0.7rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft"
                >
                  {column}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="bg-white transition hover:bg-[#fbfdff]">
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  <div className="min-w-[150px]">
                    <p className="font-extrabold text-brand-ink">{formatDateTime(row.performedAt)}</p>
                    <p className="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-copy-soft">
                      ID {row.movementId}
                    </p>
                  </div>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top">
                  <Badge variant={row.movementTypeVariant}>{row.movementTypeLabel}</Badge>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top">
                  <div className="min-w-[180px]">
                    <Badge variant={row.correctionTypeVariant}>{row.correctionTypeLabel}</Badge>
                    {row.correctionType === 'REVERSAL' ? (
                      <p className="mt-2 text-xs font-semibold leading-5 text-copy-soft">
                        Relacionado con movimiento ID {row.relatedMovementId}.<br />
                        Motivo: {row.correctionReason}
                      </p>
                    ) : null}
                  </div>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top">
                  <div className="min-w-[210px]">
                    <p className="text-sm font-extrabold text-brand-ink">{row.productName}</p>
                    <p className="mt-1 text-sm font-semibold text-copy-soft">{row.productCode}</p>
                  </div>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  {row.batchCode}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-extrabold text-brand-ink">
                  {formatNumber(row.quantity)}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  {row.unit}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-extrabold text-brand-ink">
                  {formatPrice(row.unitPrice)}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  {row.unitPrice == null ? 'No aplica' : row.priceUnitLabel}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  {row.laboratoryName}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  {row.username}
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  <p className="max-w-[280px] leading-6">{row.movementObservation}</p>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top text-sm font-semibold text-copy">
                  <p className="max-w-[280px] leading-6">{row.lineObservation}</p>
                </td>
                <td className="border-b border-brand-ink/[0.06] px-5 py-4 align-top">
                  {row.isFirstLine ? (
                    row.canReverse ? (
                      <button
                        type="button"
                        onClick={() => onReverseRequest(row)}
                        className="inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-4 py-2 text-xs font-extrabold uppercase tracking-[0.14em] text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                      >
                        <RotateCcw className="h-4 w-4" />
                        Reversar movimiento
                      </button>
                    ) : (
                      <p className="max-w-[220px] text-xs font-semibold leading-5 text-copy-soft">
                        {row.reverseDisabledReason}
                      </p>
                    )
                  ) : (
                    <span className="text-xs font-semibold text-copy-soft">Usar la fila principal</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col gap-4 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div className="flex items-center gap-2 text-sm font-semibold text-copy">
          <FileClock className="h-4 w-4" />
          <span>
            Pagina {page} de {totalPages}
          </span>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <PaginationButton disabled={page <= 1} onClick={() => onPageChange(page - 1)}>
            Anterior
          </PaginationButton>

          {Array.from({ length: totalPages }).map((_, index) => {
            const pageNumber = index + 1;

            if (
              totalPages > 7 &&
              pageNumber !== 1 &&
              pageNumber !== totalPages &&
              Math.abs(pageNumber - page) > 1
            ) {
              if (pageNumber === 2 || pageNumber === totalPages - 1) {
                return (
                  <span key={`ellipsis-${pageNumber}`} className="px-1 text-copy-soft">
                    ...
                  </span>
                );
              }

              return null;
            }

            return (
              <button
                key={pageNumber}
                type="button"
                onClick={() => onPageChange(pageNumber)}
                className={`inline-flex h-10 w-10 items-center justify-center rounded-full text-sm font-extrabold transition ${
                  pageNumber === page
                    ? 'bg-brand-ink text-white shadow-[0_14px_30px_rgba(23,61,44,0.18)]'
                    : 'border border-brand-ink/[0.08] bg-white text-brand-ink hover:border-brand-teal/30 hover:text-brand-teal'
                }`}
              >
                {pageNumber}
              </button>
            );
          })}

          <PaginationButton
            disabled={page >= totalPages}
            onClick={() => onPageChange(page + 1)}
          >
            Siguiente
          </PaginationButton>
        </div>
      </div>
    </Card>
  );
}

export default MovementTable;
