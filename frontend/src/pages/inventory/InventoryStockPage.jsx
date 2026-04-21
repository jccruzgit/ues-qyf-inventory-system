import { useEffect, useState } from 'react';
import {
  AlertOctagon,
  ArchiveX,
  Boxes,
  CalendarClock,
  PackagePlus,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  buildInventoryFilterOptions,
  buildInventoryOverview,
} from '../../adapters/inventoryStock.adapter';
import InventoryFilters from '../../components/inventory/InventoryFilters';
import InventoryOverviewItem from '../../components/inventory/InventoryOverviewItem';
import {
  InventoryEmptyState,
  InventoryErrorState,
  InventoryLoadingState,
} from '../../components/inventory/InventoryPageStates';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import StatCard from '../../components/ui/StatCard';
import {
  fetchInventoryCatalogs,
  fetchInventoryStock,
  getInventoryCatalogsErrorMessage,
  getInventoryStockErrorMessage,
} from '../../services/inventoryService';

const defaultFilters = {
  laboratory: 'all',
  category: 'all',
  storageCondition: 'all',
  stockState: 'all',
};

function InventoryStockPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [products, setProducts] = useState([]);
  const [stockItems, setStockItems] = useState([]);
  const [filters, setFilters] = useState(defaultFilters);
  const [expandedItemId, setExpandedItemId] = useState(null);

  const loadInventory = async () => {
    setLoading(true);
    setError('');

    try {
      const [catalogsResponse, stockResponse] = await Promise.all([
        fetchInventoryCatalogs(),
        fetchInventoryStock(),
      ]);

      setProducts(catalogsResponse.products);
      setStockItems(stockResponse);
    } catch (requestError) {
      const message =
        requestError?.response?.config?.url === '/inventory-stock'
          ? getInventoryStockErrorMessage(requestError)
          : getInventoryCatalogsErrorMessage(requestError);

      setProducts([]);
      setStockItems([]);
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const inventoryItems = buildInventoryOverview(products, stockItems);
  const filterOptions = buildInventoryFilterOptions(inventoryItems);
  const filteredItems = inventoryItems.filter((item) => {
    const matchesLaboratory =
      filters.laboratory === 'all' || item.laboratoryName === filters.laboratory;
    const matchesCategory = filters.category === 'all' || item.category === filters.category;
    const matchesStorage =
      filters.storageCondition === 'all' || item.storageCondition === filters.storageCondition;
    const matchesStockState =
      filters.stockState === 'all' || item.stockState.key === filters.stockState;

    return matchesLaboratory && matchesCategory && matchesStorage && matchesStockState;
  });

  const activeBatchCount = filteredItems.reduce((total, item) => total + item.activeBatchCount, 0);
  const criticalCount = filteredItems.filter((item) => item.stockState.key === 'critical').length;
  const lowCount = filteredItems.filter((item) => item.stockState.key === 'low').length;
  const expiringSoonCount = filteredItems.filter((item) => item.expiringSoonCount > 0).length;
  const isFiltered = Object.values(filters).some((value) => value !== 'all');

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Niveles de stock"
        subtitle="Consulta inventario real por producto, laboratorio y lote para anticipar faltantes y vencimientos."
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
        <div className="rounded-[24px] border border-[#d7f0e1] bg-[#eef9f2] px-4 py-3 text-sm font-semibold text-[#2fa36b]">
          {notice}
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Productos visibles"
          value={filteredItems.length}
          icon={Boxes}
          meta="Vista actual"
          metaVariant="navy"
          accent="bg-[#e7efff] text-brand-ink"
        />
        <StatCard
          title="Stock critico"
          value={criticalCount}
          icon={AlertOctagon}
          meta="Prioridad"
          metaVariant="danger"
          accent="bg-[#fdebec] text-[#d53a43]"
        />
        <StatCard
          title="Stock bajo"
          value={lowCount}
          icon={ArchiveX}
          meta="Seguimiento"
          metaVariant="warning"
          accent="bg-[#fff3dd] text-[#d28a19]"
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
              Filtros operativos
            </p>
            <h3 className="mt-2 text-xl font-extrabold tracking-[-0.04em] text-brand-ink">
              Vista consolidada por producto y laboratorio
            </h3>
          </div>

          <div className="rounded-[24px] border border-brand-ink/[0.06] bg-white px-4 py-3 text-sm font-semibold text-copy">
            {activeBatchCount} lote(s) activo(s) visible(s). Esta base queda lista para integrar
            alertas por stock y vencimiento.
          </div>
        </div>

        <InventoryFilters
          laboratory={filters.laboratory}
          category={filters.category}
          storageCondition={filters.storageCondition}
          stockState={filters.stockState}
          laboratoryOptions={filterOptions.laboratoryOptions}
          categoryOptions={filterOptions.categoryOptions}
          stockStateOptions={filterOptions.stockStateOptions}
          onLaboratoryChange={(value) =>
            setFilters((currentFilters) => ({ ...currentFilters, laboratory: value }))
          }
          onCategoryChange={(value) =>
            setFilters((currentFilters) => ({ ...currentFilters, category: value }))
          }
          onStorageChange={(value) =>
            setFilters((currentFilters) => ({ ...currentFilters, storageCondition: value }))
          }
          onStockStateChange={(value) =>
            setFilters((currentFilters) => ({ ...currentFilters, stockState: value }))
          }
          onReset={() => setFilters(defaultFilters)}
        />
      </Card>

      {loading ? (
        <InventoryLoadingState />
      ) : error ? (
        <InventoryErrorState message={error} onRetry={loadInventory} />
      ) : filteredItems.length ? (
        <section className="space-y-4">
          {filteredItems.map((item) => (
            <InventoryOverviewItem
              key={item.id}
              item={item}
              isExpanded={expandedItemId === item.id}
              onToggle={() =>
                setExpandedItemId((currentId) => (currentId === item.id ? null : item.id))
              }
            />
          ))}
        </section>
      ) : (
        <InventoryEmptyState isFiltered={isFiltered} onReset={() => setFilters(defaultFilters)} />
      )}
    </div>
  );
}

export default InventoryStockPage;
