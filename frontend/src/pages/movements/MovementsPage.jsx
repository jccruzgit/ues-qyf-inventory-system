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
import {
  fetchInventoryCatalogs,
  getInventoryCatalogsErrorMessage,
} from '../../services/inventoryService';
import {
  fetchInventoryMovements,
  getInventoryMovementsErrorMessage,
} from '../../services/movementsService';

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
              className="grid grid-cols-[1.1fr_0.7fr_1.3fr_0.9fr_0.6fr_0.6fr_1fr_0.8fr_1.4fr] gap-4 border-b border-brand-ink/[0.06] px-6 py-4"
            >
              {Array.from({ length: 9 }).map((__, cellIndex) => (
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
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f9fbff_100%)] p-8 sm:p-10">
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
          className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
        >
          Reintentar
        </button>
      </div>
    </Card>
  );
}

function MovementsEmptyState({ isFiltered, onReset }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-8 sm:p-10">
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
            className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
          >
            <PackagePlus className="h-4 w-4" />
            Registrar primera entrada
          </Link>
        )}
      </div>
    </Card>
  );
}

function MovementsPage() {
  const location = useLocation();
  const navigate = useNavigate();
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
  const laboratoryOptions = useMemo(
    () => [
      { value: 'all', label: 'Todos los laboratorios' },
      ...catalogs.laboratories,
    ],
    [catalogs.laboratories],
  );
  const movementRows = useMemo(
    () => buildMovementRows(movements, catalogs.products, catalogs.laboratories),
    [catalogs.laboratories, catalogs.products, movements],
  );
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

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Movimientos de inventario"
        subtitle="Revise entradas y salidas registradas por fecha, producto, lote, laboratorio y usuario."
        action={
          <Link
            to="/inventory/entries/new"
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(14,47,103,0.2)] transition hover:-translate-y-0.5 hover:bg-[#0b2551]"
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

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Filas visibles"
          value={movementRows.length}
          icon={Rows3}
          meta="Consulta actual"
          metaVariant="navy"
          accent="bg-[#e7efff] text-brand-ink"
        />
        <StatCard
          title="Entradas"
          value={entryCount}
          icon={PackagePlus}
          meta="Abastecimiento"
          metaVariant="success"
          accent="bg-[#e5f7ef] text-[#249b66]"
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

      <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)]">
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
        />
      ) : (
        <MovementsEmptyState isFiltered={isFiltered} onReset={() => setFilters(defaultFilters)} />
      )}
    </div>
  );
}

export default MovementsPage;
