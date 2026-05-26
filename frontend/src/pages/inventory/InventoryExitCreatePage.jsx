import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { AlertTriangle, ArrowDownCircle, ArrowLeft } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import InventoryExitForm from '../../components/inventory/InventoryExitForm';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { inventoryExitSchema } from '../../schemas/inventory-exit.schema';
import {
  createInventoryExit,
  fetchInventoryCatalogs,
  fetchInventoryStock,
  getCreateInventoryExitErrorDetails,
  getInventoryCatalogsErrorMessage,
  getInventoryStockErrorMessage,
} from '../../services/inventoryService';

const defaultValues = {
  productId: '',
  laboratoryId: '',
  selectedBatchKey: '',
  quantity: '',
  unitLabel: '',
  observations: '',
  lineObservation: '',
  availableQuantity: 0,
  requiresBatchSelection: false,
};

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function buildBatchOption(stockItem, unitLabel) {
  const quantityAvailable = toNumber(stockItem.quantityAvailable);
  const batchLabel = stockItem.batchCode?.trim() || 'Sin lote';
  const expirationLabel = stockItem.expirationDate
    ? ` | vence ${stockItem.expirationDate}`
    : '';
  const value =
    stockItem.productBatchId != null
      ? `batch:${stockItem.productBatchId}`
      : `stock:${stockItem.id}`;

  return {
    value,
    label: `${batchLabel} | disponible ${quantityAvailable}${expirationLabel}`,
    productBatchId: stockItem.productBatchId ?? null,
    batchCode: batchLabel,
    quantityAvailable,
    unit: unitLabel || 'unidades',
    expirationDate: stockItem.expirationDate || '',
  };
}

function InventoryExitCreatePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [catalogs, setCatalogs] = useState({
    products: [],
    laboratories: [],
  });
  const [catalogsLoading, setCatalogsLoading] = useState(true);
  const [catalogsError, setCatalogsError] = useState('');
  const [stockItems, setStockItems] = useState([]);
  const [stockLoading, setStockLoading] = useState(false);
  const [stockError, setStockError] = useState('');
  const [serverMessage, setServerMessage] = useState('');

  const form = useForm({
    resolver: zodResolver(inventoryExitSchema),
    defaultValues,
  });

  const {
    watch,
    setError,
    setValue,
    clearErrors,
    formState: { isSubmitting },
  } = form;

  const selectedProductId = Number(watch('productId'));
  const selectedLaboratoryId = String(watch('laboratoryId') ?? '');
  const selectedBatchKey = String(watch('selectedBatchKey') ?? '');
  const selectedProduct = catalogs.products.find((product) => product.id === selectedProductId);
  const selectedLaboratory = catalogs.laboratories.find(
    (laboratory) => laboratory.value === selectedLaboratoryId,
  );

  const batchOptions = useMemo(() => {
    return stockItems
      .filter((item) => toNumber(item.quantityAvailable) > 0)
      .map((item) => buildBatchOption(item, selectedProduct?.unit))
      .sort((left, right) => left.label.localeCompare(right.label));
  }, [selectedProduct?.unit, stockItems]);

  const selectedBatch = batchOptions.find((option) => option.value === selectedBatchKey) ?? null;
  const selectedProductStock = stockItems.reduce(
    (total, item) => total + toNumber(item.quantityAvailable),
    0,
  );

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

  useEffect(() => {
    loadCatalogs();
  }, []);

  useEffect(() => {
    const prefill = location.state?.prefill;

    if (!prefill) {
      return;
    }

    if (prefill.productId) {
      setValue('productId', String(prefill.productId));
    }

    if (prefill.laboratoryId) {
      setValue('laboratoryId', String(prefill.laboratoryId));
    }
  }, [location.state, setValue]);

  useEffect(() => {
    setValue('unitLabel', selectedProduct?.unit ?? '');
  }, [selectedProduct, setValue]);

  useEffect(() => {
    setValue('selectedBatchKey', '');
    setValue('availableQuantity', 0);
    setValue('requiresBatchSelection', false);
    setStockItems([]);
    setStockError('');
    clearErrors(['selectedBatchKey', 'quantity']);

    if (!selectedProductId || !selectedLaboratoryId) {
      return;
    }

    let isActive = true;

    const loadStock = async () => {
      setStockLoading(true);

      try {
        const response = await fetchInventoryStock({
          productId: selectedProductId,
          laboratoryId: Number(selectedLaboratoryId),
        });

        if (!isActive) {
          return;
        }

        setStockItems(response);
        setValue(
          'requiresBatchSelection',
          response.some((item) => toNumber(item.quantityAvailable) > 0),
        );
      } catch (error) {
        if (!isActive) {
          return;
        }

        setStockItems([]);
        setStockError(getInventoryStockErrorMessage(error));
      } finally {
        if (isActive) {
          setStockLoading(false);
        }
      }
    };

    loadStock();

    return () => {
      isActive = false;
    };
  }, [clearErrors, selectedLaboratoryId, selectedProductId, setValue]);

  useEffect(() => {
    setValue('availableQuantity', selectedBatch?.quantityAvailable ?? 0);
  }, [selectedBatch, setValue]);

  const handleSubmit = async (values) => {
    setServerMessage('');

    if (!batchOptions.length) {
      setServerMessage(
        'No hay stock disponible para el insumo seleccionado en el laboratorio indicado.',
      );
      setError('selectedBatchKey', {
        type: 'manual',
        message: 'No hay lotes o existencias disponibles para descargar.',
      });
      return;
    }

    try {
      await createInventoryExit(values, selectedBatch);
      navigate('/inventory', {
        replace: true,
        state: { notice: 'Salida de inventario registrada correctamente.' },
      });
    } catch (error) {
      const details = getCreateInventoryExitErrorDetails(error);

      Object.entries(details.fieldErrors).forEach(([field, message]) => {
        setError(field, { type: 'server', message });
      });

      setServerMessage(details.message);
    }
  };

  const hasCatalogData = catalogs.products.length && catalogs.laboratories.length;
  const stockMessage = stockError
    ? stockError
    : selectedProduct && selectedLaboratory && !stockLoading && !batchOptions.length
      ? 'No hay lotes o existencias disponibles para registrar una salida de este insumo en el laboratorio seleccionado.'
      : '';

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <Link
          to="/inventory"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-copy transition hover:text-brand-ink"
        >
          <ArrowLeft className="h-4 w-4" />
          Volver a inventario
        </Link>

        <SectionHeader
          title="Registrar salida"
          subtitle="Registra descargos normales del inventario por laboratorio, insumo y lote disponible sin alterar el flujo de reversion."
        />
      </div>

      <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-0">
        <div className="grid gap-0 lg:grid-cols-[minmax(260px,0.34fr)_minmax(0,0.66fr)]">
          <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#163826_0%,_#1e5d38_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
              <ArrowDownCircle className="h-5 w-5" strokeWidth={2.2} />
            </div>
            <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
              Descargo por lote
            </h2>
            <p className="mt-4 text-sm leading-7 text-white/72">
              Este flujo registra una salida normal del inventario, reduce existencias y conserva
              la trazabilidad del movimiento para consultas y futuras reversiones.
            </p>

            <div className="mt-8 space-y-3 text-sm text-white/78">
              <p>1. Seleccione laboratorio, insumo y lote con existencias disponibles.</p>
              <p>2. Indique la cantidad a descargar y documente las observaciones necesarias.</p>
              <p>3. Guarde para actualizar stock y dejar el movimiento visible en historial.</p>
            </div>
          </aside>

          <div className="p-6 sm:p-8">
            {catalogsLoading ? (
              <div className="space-y-4 animate-pulse">
                <div className="h-5 w-44 rounded-full bg-surface-2" />
                <div className="grid gap-4 lg:grid-cols-2">
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                </div>
                <div className="h-36 rounded-[26px] bg-surface-2" />
                <div className="grid gap-4 lg:grid-cols-4">
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                </div>
              </div>
            ) : catalogsError ? (
              <div className="rounded-[28px] border border-[#fdebec] bg-[#fff4f5] p-6">
                <div className="flex items-start gap-3">
                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-[#fdebec] text-[#d53a43]">
                    <AlertTriangle className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="text-lg font-extrabold text-brand-ink">
                      No se pudieron cargar los catalogos
                    </h3>
                    <p className="mt-2 text-sm leading-7 text-copy">{catalogsError}</p>
                    <button
                      type="button"
                      onClick={loadCatalogs}
                      className="mt-4 inline-flex rounded-full bg-brand-ink px-5 py-2.5 text-sm font-extrabold text-white transition hover:bg-brand-ink-strong"
                    >
                      Reintentar
                    </button>
                  </div>
                </div>
              </div>
            ) : !hasCatalogData ? (
              <div className="rounded-[28px] border border-brand-ink/[0.06] bg-surface-2/70 p-6">
                <h3 className="text-lg font-extrabold text-brand-ink">
                  No hay catalogos suficientes para registrar salidas
                </h3>
                <p className="mt-2 text-sm leading-7 text-copy">
                  Verifique que existan insumos activos y laboratorios disponibles antes de
                  registrar una nueva salida de inventario.
                </p>
              </div>
            ) : (
              <InventoryExitForm
                form={form}
                products={catalogs.products}
                laboratories={catalogs.laboratories}
                selectedProduct={selectedProduct}
                selectedLaboratory={selectedLaboratory}
                batchOptions={batchOptions}
                selectedBatch={selectedBatch}
                selectedProductStock={selectedProductStock}
                stockLoading={stockLoading}
                stockMessage={stockMessage}
                onSubmit={handleSubmit}
                onCancel={() => navigate('/inventory')}
                submitLabel={isSubmitting ? 'Registrando salida...' : 'Registrar salida'}
                isSubmitting={isSubmitting}
                serverMessage={serverMessage}
              />
            )}
          </div>
        </div>
      </Card>
    </div>
  );
}

export default InventoryExitCreatePage;
