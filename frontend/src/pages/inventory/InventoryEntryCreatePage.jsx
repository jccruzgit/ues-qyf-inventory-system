import { useEffect, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, AlertTriangle, Boxes } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import InventoryEntryForm from '../../components/inventory/InventoryEntryForm';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { inventoryEntrySchema } from '../../schemas/inventory-entry.schema';
import {
  createInventoryEntry,
  fetchInventoryCatalogs,
  getCreateInventoryEntryErrorDetails,
  getInventoryCatalogsErrorMessage,
} from '../../services/inventoryService';

const defaultValues = {
  productId: '',
  laboratoryId: '',
  quantity: '',
  unitLabel: '',
  batchCode: '',
  expirationDate: '',
  observations: '',
  requiresBatchControl: false,
  requiresExpiration: false,
};

function InventoryEntryCreatePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [catalogs, setCatalogs] = useState({
    products: [],
    laboratories: [],
  });
  const [catalogsLoading, setCatalogsLoading] = useState(true);
  const [catalogsError, setCatalogsError] = useState('');
  const [serverMessage, setServerMessage] = useState('');

  const form = useForm({
    resolver: zodResolver(inventoryEntrySchema),
    defaultValues,
  });

  const {
    watch,
    setError,
    setValue,
    formState: { isSubmitting },
  } = form;

  const selectedProductId = Number(watch('productId'));
  const selectedProduct = catalogs.products.find((product) => product.id === selectedProductId);

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
    setValue('requiresBatchControl', Boolean(selectedProduct?.requiresBatchControl));
    setValue('requiresExpiration', Boolean(selectedProduct?.requiresExpiration));
  }, [selectedProduct, setValue]);

  const handleSubmit = async (values) => {
    setServerMessage('');

    try {
      await createInventoryEntry(values);
      navigate('/inventory', {
        replace: true,
        state: { notice: 'Entrada de inventario registrada correctamente.' },
      });
    } catch (error) {
      const details = getCreateInventoryEntryErrorDetails(error);

      Object.entries(details.fieldErrors).forEach(([field, message]) => {
        setError(field, { type: 'server', message });
      });

      setServerMessage(details.message);
    }
  };

  const hasCatalogData = catalogs.products.length && catalogs.laboratories.length;

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
          title="Nueva entrada"
          subtitle="Registra inventario real por lote y actualiza las existencias del laboratorio seleccionado."
        />
      </div>

      <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-0">
        <div className="grid gap-0 lg:grid-cols-[minmax(260px,0.34fr)_minmax(0,0.66fr)]">
          <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#0d2d63_0%,_#112b58_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
              <Boxes className="h-5 w-5" strokeWidth={2.2} />
            </div>
            <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
              Entrada por lote
            </h2>
            <p className="mt-4 text-sm leading-7 text-white/72">
              Este flujo actualiza el stock real y deja el lote listo para trazabilidad,
              vencimientos y futuras alertas operativas.
            </p>

            <div className="mt-8 space-y-3 text-sm text-white/78">
              <p>1. Seleccione un producto y el laboratorio destino.</p>
              <p>2. Registre lote, vencimiento y cantidad ingresada.</p>
              <p>3. Guarde para revisar el inventario consolidado por lote.</p>
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
                <div className="grid gap-4 lg:grid-cols-3">
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
                      className="mt-4 inline-flex rounded-full bg-brand-ink px-5 py-2.5 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
                    >
                      Reintentar
                    </button>
                  </div>
                </div>
              </div>
            ) : !hasCatalogData ? (
              <div className="rounded-[28px] border border-brand-ink/[0.06] bg-surface-2/70 p-6">
                <h3 className="text-lg font-extrabold text-brand-ink">
                  No hay catalogos suficientes para registrar entradas
                </h3>
                <p className="mt-2 text-sm leading-7 text-copy">
                  Verifique que existan productos activos y laboratorios disponibles antes de
                  registrar una nueva entrada de inventario.
                </p>
              </div>
            ) : (
              <InventoryEntryForm
                form={form}
                products={catalogs.products}
                laboratories={catalogs.laboratories}
                selectedProduct={selectedProduct}
                onSubmit={handleSubmit}
                onCancel={() => navigate('/inventory')}
                submitLabel={isSubmitting ? 'Registrando entrada...' : 'Registrar entrada'}
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

export default InventoryEntryCreatePage;
