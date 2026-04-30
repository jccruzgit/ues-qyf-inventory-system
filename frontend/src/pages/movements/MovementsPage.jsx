import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, ArrowDownUp, Boxes, PackagePlus, Rows3 } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  buildMovementProductOptions,
  buildMovementRows,
  sortMovementRowsByDate,
} from '../../adapters/movements.adapter';
import MovementFilters from '../../components/movements/MovementFilters';
import MovementTable from '../../components/movements/MovementTable';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import { useAuth } from '../../hooks/useAuth';
import {
  fetchInventoryCatalogs,
  getInventoryCatalogsErrorMessage,
} from '../../services/inventoryService';
import {
  fetchInventoryMovements,
  getInventoryMovementActionErrorMessage,
  getInventoryMovementsErrorMessage,
  reverseInventoryMovement,
} from '../../services/movementsService';

const reverseRoles = new Set(['ADMIN', 'INVENTORY_MANAGER', 'LAB_TECHNICIAN']);

const defaultFilters = {
  dateFrom: '',
  dateTo: '',
  movementType: 'all',
  productId: 'all',
  laboratoryId: 'all',
};

function getInitialFilters(state) {
  return {
    ...defaultFilters,
    productId: state?.prefill?.productId ? String(state.prefill.productId) : 'all',
    laboratoryId: state?.prefill?.laboratoryId ? String(state.prefill.laboratoryId) : 'all',
  };
}

function MovementsLoadingState() {
  return (
    <Card className="overflow-hidden p-0">
      <div className="animate-pulse">
        <div className="flex flex-col gap-4 border-b border-brand-ink/[0.06] px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <div className="h-3 w-32 rounded-full bg-surface-2" />
            <div className="mt-3 h-4 w-52 rounded-full bg-surface-2" />
          </div>
          <div className="h-10 w-56 rounded-full bg-surface-2" />
        </div>
        <div className="space-y-0">
          {Array.from({ length: 6 }).map((_, index) => (
            <div
              key={index}
              className="grid grid-cols-[1.1fr_0.7fr_1fr_1.3fr_0.9fr_0.6fr_0.6fr_0.8fr_0.8fr_1fr_0.8fr_1.2fr_1.2fr_1fr] gap-4 border-b border-brand-ink/[0.06] px-6 py-4"
            >
              {Array.from({ length: 14 }).map((__, cellIndex) => (
                <div key={cellIndex} className="h-4 rounded-full bg-surface-2" />
              ))}
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
}

function MovementsErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudo cargar el historial
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">{message}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
        >
          Reintentar
        </button>
      </div>
    </Card>
  );
}

function MovementsEmptyState({ isFiltered, onReset }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
          <Rows3 className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No hay movimientos registrados
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">
          {isFiltered
            ? 'No se encontraron movimientos para los filtros seleccionados. Ajuste fechas, tipo, producto o laboratorio.'
            : 'Cuando se registren entradas o salidas, el historial completo aparecera aqui para auditoria y reportes.'}
        </p>
        {isFiltered ? (
          <button
            type="button"
            onClick={onReset}
            className="mt-7 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
          >
            Limpiar filtros
          </button>
        ) : (
          <Link
            to="/inventory/entries/new"
            className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
          >
            <PackagePlus className="h-4 w-4" />
            Registrar primera entrada
          </Link>
        )}
      </div>
    </Card>
  );
}

function ReverseMovementModal({
  movement,
  reason,
  error,
  submitting,
  onReasonChange,
  onClose,
  onConfirm,
}) {
  if (!movement) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-brand-ink/45 px-4 py-6 backdrop-blur-sm">
      <div className="w-full max-w-2xl rounded-[32px] bg-white p-6 shadow-[0_28px_80px_rgba(23,61,44,0.28)] sm:p-8">
        <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
          Reversion trazable
        </p>
        <h3 className="mt-3 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          Confirmar reversion del movimiento #{movement.movementId}
        </h3>
        <p className="mt-3 text-sm leading-7 text-copy">
          Esta accion no elimina el movimiento original. Creara un movimiento compensatorio
          para mantener la trazabilidad.
        </p>
        <div className="mt-5 rounded-[24px] border border-[#f7d7b5] bg-[#fff7eb] px-4 py-3 text-sm font-semibold leading-6 text-[#915b10]">
          Tipo original: {movement.movementTypeLabel}. Producto de referencia: {movement.productName}.
        </div>

        <label className="mt-6 block">
          <span className="text-sm font-extrabold text-brand-ink">Motivo de la reversion</span>
          <textarea
            value={reason}
            onChange={(event) => onReasonChange(event.target.value)}
            rows={4}
            placeholder="Explique por que este movimiento debe revertirse."
            className="mt-3 w-full rounded-[24px] border border-brand-ink/[0.08] bg-white px-4 py-3 text-sm text-brand-ink outline-none transition focus:border-brand-teal/30 focus:ring-4 focus:ring-brand-teal/10"
          />
        </label>

        {error ? (
          <div className="mt-4 rounded-[20px] border border-[#f4c7cb] bg-[#fff3f4] px-4 py-3 text-sm font-semibold text-[#b73945]">
            {error}
          </div>
        ) : null}

        <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="inline-flex items-center justify-center rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal disabled:cursor-not-allowed disabled:opacity-45"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={submitting}
            className="inline-flex items-center justify-center rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong disabled:cursor-not-allowed disabled:opacity-45"
          >
            {submitting ? 'Procesando...' : 'Crear reversion'}
          </button>
        </div>
      </div>
    </div>
  );
}

function normalizeRole(role) {
  return String(role ?? '')
    .trim()
    .replace(/^ROLE_/, '');
}

function resolveReverseState(row, canManageReversals, reversedMovementIds) {
  if (!canManageReversals) {
    return {
      canReverse: false,
      reverseDisabledReason: 'Sin permisos para reversar.',
    };
  }

  if (row.correctionType === 'REVERSAL') {
    return {
      canReverse: false,
      reverseDisabledReason: 'Ya es una reversion.',
    };
  }

  if (reversedMovementIds.has(row.movementId)) {
    return {
      canReverse: false,
      reverseDisabledReason: 'Ya tiene reversion registrada.',
    };
  }

  return {
    canReverse: true,
    reverseDisabledReason: '',
  };
}

function MovementsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [catalogs, setCatalogs] = useState({
    products: [],
    laboratories: [],
  });
  const [movements, setMovements] = useState([]);
  const [filters, setFilters] = useState(() => getInitialFilters(location.state));
  const [sortDirection, setSortDirection] = useState('desc');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [catalogsLoading, setCatalogsLoading] = useState(true);
  const [catalogsReady, setCatalogsReady] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [reverseModalMovement, setReverseModalMovement] = useState(null);
  const [reverseReason, setReverseReason] = useState('');
  const [reverseError, setReverseError] = useState('');
  const [reversing, setReversing] = useState(false);

  const loadCatalogs = async () => {
    setCatalogsLoading(true);

    try {
      const response = await fetchInventoryCatalogs();
      setCatalogs(response);
      setCatalogsReady(true);
    } catch (requestError) {
      setCatalogs({ products: [], laboratories: [] });
      setCatalogsReady(false);
      throw new Error(getInventoryCatalogsErrorMessage(requestError));
    } finally {
      setCatalogsLoading(false);
    }
  };

  const loadMovements = async (activeFilters) => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchInventoryMovements(activeFilters);
      setMovements(response);
    } catch (requestError) {
      setMovements([]);
      setError(getInventoryMovementsErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let isMounted = true;

    const bootstrap = async () => {
      try {
        await loadCatalogs();
      } catch (catalogError) {
        if (isMounted) {
          setError(catalogError.message);
          setLoading(false);
        }
      }
    };

    bootstrap();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (catalogsReady) {
      loadMovements(filters);
    }
  }, [catalogsReady, filters]);

  useEffect(() => {
    if (location.state?.context) {
      setNotice(
        `Mostrando movimientos relacionados con ${location.state.context.title}. Ajuste filtros si necesita ampliar la consulta.`,
      );
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const productOptions = useMemo(
    () => buildMovementProductOptions(catalogs.products),
    [catalogs.products],
  );
  const canManageReversals = useMemo(
    () => reverseRoles.has(normalizeRole(user?.role)),
    [user?.role],
  );
  const reversedMovementIds = useMemo(
    () =>
      new Set(
        movements
          .map((movement) => movement.relatedMovementId)
          .filter((movementId) => movementId !== null && movementId !== undefined),
      ),
    [movements],
  );
  const laboratoryOptions = useMemo(
    () => [
      { value: 'all', label: 'Todos los laboratorios' },
      ...catalogs.laboratories,
    ],
    [catalogs.laboratories],
  );
  const movementRows = useMemo(() => {
    return buildMovementRows(movements, catalogs.products, catalogs.laboratories).map((row) => ({
      ...row,
      ...resolveReverseState(row, canManageReversals, reversedMovementIds),
    }));
  }, [canManageReversals, catalogs.laboratories, catalogs.products, movements, reversedMovementIds]);
  const sortedRows = useMemo(
    () => sortMovementRowsByDate(movementRows, sortDirection),
    [movementRows, sortDirection],
  );
  const totalPages = Math.max(1, Math.ceil(sortedRows.length / pageSize));
  const paginatedRows = sortedRows.slice((page - 1) * pageSize, page * pageSize);
  const isFiltered = Object.values(filters).some((value) => value && value !== 'all');
  const entryCount = movementRows.filter((row) => row.movementType === 'ENTRY').length;
  const exitCount = movementRows.filter((row) => row.movementType === 'EXIT').length;
  const laboratoryCount = new Set(movementRows.map((row) => row.laboratoryName)).size;

  useEffect(() => {
    setPage(1);
  }, [filters, pageSize, sortDirection]);

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const handleFilterChange = (field, value) => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [field]: value,
    }));
  };

  const handleReload = async () => {
    try {
      await loadCatalogs();
      await loadMovements(filters);
    } catch (catalogError) {
      setError(catalogError.message);
    }
  };

  const handleOpenReverseModal = (row) => {
    setReverseModalMovement(row);
    setReverseReason('');
    setReverseError('');
  };

  const handleCloseReverseModal = () => {
    setReverseModalMovement(null);
    setReverseReason('');
    setReverseError('');
  };

  const handleConfirmReverse = async () => {
    const normalizedReason = reverseReason.trim();

    if (!normalizedReason) {
      setReverseError('El motivo de la reversion es obligatorio.');
      return;
    }

    if (!reverseModalMovement) {
      return;
    }

    setReversing(true);
    setReverseError('');

    try {
      await reverseInventoryMovement(reverseModalMovement.movementId, normalizedReason);
      setFeedback({
        type: 'success',
        message: `Se registro la reversion del movimiento #${reverseModalMovement.movementId}.`,
      });
      handleCloseReverseModal();
      await loadMovements(filters);
    } catch (requestError) {
      setReverseError(getInventoryMovementActionErrorMessage(requestError));
    } finally {
      setReversing(false);
    }
  };

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Movimientos de inventario"
        subtitle="Revise entradas, salidas y reversiones por fecha, producto, lote, laboratorio, usuario y observaciones."
        action={
          <Link
            to="/inventory/entries/new"
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(23,61,44,0.2)] transition hover:-translate-y-0.5 hover:bg-brand-ink-strong"
          >
            <PackagePlus className="h-4 w-4" strokeWidth={2.4} />
            Nueva entrada
          </Link>
        }
      />

      {notice ? (
        <div className="rounded-[24px] border border-brand-teal/15 bg-brand-teal-soft/40 px-4 py-3 text-sm font-semibold text-copy">
          {notice}
        </div>
      ) : null}

      {feedback ? (
        <div
          className={`rounded-[24px] px-4 py-3 text-sm font-semibold ${
            feedback.type === 'success'
              ? 'border border-[#d2e6d8] bg-[#eef6f0] text-[#2d7a49]'
              : 'border border-[#f4c7cb] bg-[#fff3f4] text-[#b73945]'
          }`}
        >
          {feedback.message}
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Filas visibles"
          value={movementRows.length}
          icon={Rows3}
          meta="Consulta actual"
          metaVariant="teal"
          accent="bg-brand-teal-soft text-brand-teal"
        />
        <StatCard
          title="Entradas"
          value={entryCount}
          icon={PackagePlus}
          meta="Abastecimiento"
          metaVariant="success"
          accent="bg-[#e7f4eb] text-[#2d7a49]"
        />
        <StatCard
          title="Salidas"
          value={exitCount}
          icon={ArrowDownUp}
          meta="Consumo"
          metaVariant="danger"
          accent="bg-[#fdebec] text-[#d53a43]"
        />
        <StatCard
          title="Laboratorios"
          value={laboratoryCount}
          icon={Boxes}
          meta="Con actividad"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
        />
      </section>

      <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)]">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
              Filtros operativos
            </p>
            <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
              Consulta auditable de movimientos
            </h3>
          </div>

          <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-3 text-sm font-semibold text-copy">
            Base lista para exportaciones y reportes futuros.
          </div>
        </div>

        <MovementFilters
          filters={filters}
          productOptions={productOptions}
          laboratoryOptions={laboratoryOptions}
          onFilterChange={handleFilterChange}
          onReset={() => setFilters(defaultFilters)}
        />
      </Card>

      {catalogsLoading || loading ? (
        <MovementsLoadingState />
      ) : error ? (
        <MovementsErrorState message={error} onRetry={handleReload} />
      ) : movementRows.length ? (
        <MovementTable
          rows={paginatedRows}
          sortDirection={sortDirection}
          onToggleSort={() =>
            setSortDirection((currentDirection) =>
              currentDirection === 'desc' ? 'asc' : 'desc',
            )
          }
          page={page}
          pageSize={pageSize}
          totalPages={totalPages}
          totalItems={sortedRows.length}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
          onReverseRequest={handleOpenReverseModal}
        />
      ) : (
        <MovementsEmptyState isFiltered={isFiltered} onReset={() => setFilters(defaultFilters)} />
      )}

      <ReverseMovementModal
        movement={reverseModalMovement}
        reason={reverseReason}
        error={reverseError}
        submitting={reversing}
        onReasonChange={setReverseReason}
        onClose={handleCloseReverseModal}
        onConfirm={handleConfirmReverse}
      />
    </div>
  );
}

export default MovementsPage;

