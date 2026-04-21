import { useEffect, useState } from 'react';
import {
  AlertTriangle,
  ArchiveX,
  ArrowUpRight,
  Boxes,
  CalendarClock,
  Layers3,
  PackagePlus,
  RefreshCcw,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  fetchInventoryCatalogs,
  fetchInventoryStock,
  getInventoryCatalogsErrorMessage,
  getInventoryStockErrorMessage,
} from '../../services/inventoryService';

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

function InventoryLoadingState() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <Card key={index} className="animate-pulse rounded-[28px] px-5 py-5 sm:px-6">
          <div className="grid gap-4 lg:grid-cols-[minmax(240px,1.4fr)_minmax(160px,0.8fr)_minmax(150px,0.72fr)_minmax(160px,0.74fr)_minmax(140px,0.7fr)]">
            <div>
              <div className="h-5 w-2/3 rounded-full bg-surface-2" />
              <div className="mt-3 h-3 w-1/2 rounded-full bg-surface-2" />
            </div>
            <div className="h-9 rounded-full bg-surface-2" />
            <div className="h-9 rounded-full bg-surface-2" />
            <div className="h-9 rounded-full bg-surface-2" />
            <div className="h-9 rounded-full bg-surface-2" />
          </div>
        </Card>
      ))}
    </div>
  );
}

function InventoryErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f9fbff_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudo cargar el inventario
        </h3>
        <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">{message}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
        >
          <RefreshCcw className="h-4 w-4" />
          Reintentar
        </button>
      </div>
    </Card>
  );
}

function getStatusBadge(item) {
  if (item.expired) {
    return { label: 'Vencido', variant: 'danger' };
  }

  if (item.expiresSoon) {
    return { label: 'Vence pronto', variant: 'warning' };
  }

  if (item.lowStock) {
    return { label: 'Stock bajo', variant: 'danger' };
  }

  return { label: 'Disponible', variant: 'success' };
}

function InventoryStockPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [catalogs, setCatalogs] = useState({
    products: [],
    laboratories: [],
  });
  const [stockItems, setStockItems] = useState([]);
  const [catalogsLoading, setCatalogsLoading] = useState(true);
  const [stockLoading, setStockLoading] = useState(true);
  const [catalogsError, setCatalogsError] = useState('');
  const [stockError, setStockError] = useState('');
  const [notice, setNotice] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('all');
  const [selectedLaboratoryId, setSelectedLaboratoryId] = useState('all');

  const loadCatalogs = async () => {
    setCatalogsLoading(true);
    setCatalogsError('');

    try {
      const response = await fetchInventoryCatalogs();
      setCatalogs(response);
    } catch (error) {
      setCatalogs({ products: [], laboratories: [] });
      setCatalogsError(getInventoryCatalogsErrorMessage(error));
    } finally {
      setCatalogsLoading(false);
    }
  };

  const loadStock = async () => {
    setStockLoading(true);
    setStockError('');

    try {
      const response = await fetchInventoryStock({
        productId: selectedProductId === 'all' ? null : Number(selectedProductId),
        laboratoryId: selectedLaboratoryId === 'all' ? null : Number(selectedLaboratoryId),
      });
      setStockItems(response);
    } catch (error) {
      setStockItems([]);
      setStockError(getInventoryStockErrorMessage(error));
    } finally {
      setStockLoading(false);
    }
  };

  useEffect(() => {
    loadCatalogs();
  }, []);

  useEffect(() => {
    loadStock();
  }, [selectedLaboratoryId, selectedProductId]);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const productMap = new Map(catalogs.products.map((product) => [product.id, product]));
  const displayItems = stockItems.map((item) => ({
    ...item,
    unit: productMap.get(item.productId)?.unit ?? 'Unidades',
  }));

  const lowStockCount = displayItems.filter((item) => item.lowStock).length;
  const expiringSoonCount = displayItems.filter((item) => item.expiresSoon).length;
  const activeBatchCount = displayItems.filter((item) => item.productBatchId).length;

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Inventario por lote"
        subtitle="Consulta existencias reales por producto, laboratorio y vencimiento para mantener trazabilidad operativa."
        action={
          <Link
            to="/inventory/entries/new"
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(14,47,103,0.2)] transition hover:-translate-y-0.5 hover:bg-[#0b2551]"
          >
            <PackagePlus className="h-4 w-4" strokeWidth={2.4} />
            Registrar entrada
          </Link>
        }
      />

      {notice ? (
        <div className="rounded-[24px] border border-[#d7f0e1] bg-[#eef9f2] px-4 py-3 text-sm font-semibold text-[#2fa36b]">
          {notice}
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-3">
        <StatCard
          title="Registros en stock"
          value={displayItems.length}
          icon={Boxes}
          meta="Visibles"
          metaVariant="navy"
          accent="bg-[#e7efff] text-brand-ink"
        />
        <StatCard
          title="Stock bajo"
          value={lowStockCount}
          icon={ArchiveX}
          meta="Atencion"
          metaVariant="danger"
          accent="bg-[#fdebec] text-[#d53a43]"
        />
        <StatCard
          title="Vencen pronto"
          value={expiringSoonCount}
          icon={CalendarClock}
          meta="30 dias"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
        />
      </section>

      <Card className="space-y-5 bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)]">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
              Filtros
            </p>
            <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
              Vista consolidada de stock
            </h3>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <label className="block">
              <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.22em] text-copy-soft">
                Producto
              </span>
              <select
                value={selectedProductId}
                onChange={(event) => setSelectedProductId(event.target.value)}
                className="w-full rounded-[20px] border border-transparent bg-surface-2 px-4 py-3 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
              >
                <option value="all">Todos los productos</option>
                {catalogs.products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="block">
              <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.22em] text-copy-soft">
                Laboratorio
              </span>
              <select
                value={selectedLaboratoryId}
                onChange={(event) => setSelectedLaboratoryId(event.target.value)}
                className="w-full rounded-[20px] border border-transparent bg-surface-2 px-4 py-3 text-sm font-semibold text-brand-ink outline-none transition focus:border-brand-teal/25 focus:bg-white focus:ring-4 focus:ring-brand-teal/10"
              >
                <option value="all">Todos los laboratorios</option>
                {catalogs.laboratories.map((laboratory) => (
                  <option key={laboratory.value} value={laboratory.value}>
                    {laboratory.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>

        <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-3 text-sm font-semibold text-copy">
          Lotes activos detectados: {activeBatchCount}. Esta vista sirve como base para alertas
          por vencimiento y niveles bajos de stock.
        </div>
      </Card>

      {catalogsError ? (
        <InventoryErrorState message={catalogsError} onRetry={loadCatalogs} />
      ) : stockError ? (
        <InventoryErrorState message={stockError} onRetry={loadStock} />
      ) : catalogsLoading || stockLoading ? (
        <InventoryLoadingState />
      ) : displayItems.length ? (
        <section className="space-y-4">
          <div className="hidden grid-cols-[minmax(240px,1.4fr)_minmax(160px,0.8fr)_minmax(150px,0.72fr)_minmax(160px,0.74fr)_minmax(140px,0.7fr)] gap-4 px-6 text-[0.7rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft lg:grid">
            <span>Producto</span>
            <span>Laboratorio</span>
            <span>Lote</span>
            <span>Vencimiento</span>
            <span>Disponible</span>
          </div>

          <div className="space-y-4">
            {displayItems.map((item) => {
              const status = getStatusBadge(item);

              return (
                <Card
                  key={item.id}
                  className="rounded-[28px] px-5 py-5 transition hover:-translate-y-0.5 hover:shadow-[0_22px_40px_rgba(14,47,103,0.1)] sm:px-6"
                >
                  <div className="grid gap-4 lg:grid-cols-[minmax(240px,1.4fr)_minmax(160px,0.8fr)_minmax(150px,0.72fr)_minmax(160px,0.74fr)_minmax(140px,0.7fr)]">
                    <div className="min-w-0">
                      <div className="flex items-start gap-3">
                        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-brand-teal-soft text-brand-teal">
                          <Layers3 className="h-5 w-5" />
                        </div>
                        <div className="min-w-0">
                          <h3 className="truncate text-base font-extrabold text-brand-ink">
                            {item.productName}
                          </h3>
                          <p className="mt-1 text-sm font-semibold text-copy-soft">
                            {item.productCode}
                          </p>
                          <div className="mt-3">
                            <Badge variant={status.variant}>{status.label}</Badge>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div>
                      <p className="text-sm font-extrabold text-brand-ink">
                        {item.laboratoryName}
                      </p>
                      <p className="mt-1 text-sm font-semibold text-copy-soft">
                        {item.laboratoryCode || 'Sin codigo'}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm font-extrabold text-brand-ink">{item.batchCode}</p>
                      <p className="mt-1 text-sm font-semibold text-copy-soft">
                        {item.productBatchId ? 'Lote trazable' : 'Sin lote asociado'}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm font-extrabold text-brand-ink">
                        {formatDate(item.expirationDate)}
                      </p>
                      <p className="mt-1 text-sm font-semibold text-copy-soft">
                        {typeof item.daysUntilExpiration === 'number'
                          ? item.daysUntilExpiration >= 0
                            ? `${item.daysUntilExpiration} dias restantes`
                            : 'Fecha vencida'
                          : 'Sin control de vencimiento'}
                      </p>
                    </div>

                    <div>
                      <p className="text-lg font-extrabold tracking-tight text-brand-ink">
                        {item.quantityAvailable} {item.unit}
                      </p>
                      <p className="mt-1 text-sm font-semibold text-copy-soft">
                        Minimo: {item.minimumStock} {item.unit}
                      </p>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        </section>
      ) : (
        <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-8 sm:p-10">
          <div className="flex flex-col items-center justify-center text-center">
            <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
              <ArrowUpRight className="h-8 w-8" strokeWidth={1.9} />
            </div>
            <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
              No hay existencias registradas
            </h3>
            <p className="mt-3 max-w-[560px] text-base leading-8 text-copy">
              Registre la primera entrada de inventario para comenzar a visualizar stock por lote,
              vencimiento y niveles operativos.
            </p>
            <Link
              to="/inventory/entries/new"
              className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
            >
              <PackagePlus className="h-4 w-4" />
              Registrar primera entrada
            </Link>
          </div>
        </Card>
      )}
    </div>
  );
}

export default InventoryStockPage;
