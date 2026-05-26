import { useEffect, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, AlertTriangle, PackagePlus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import ProductForm from '../../components/products/ProductForm';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { productFormSchema } from '../../schemas/product.schema';
import {
  createProduct,
  fetchProductCatalogs,
  getCreateProductErrorDetails,
  getProductCatalogsErrorMessage,
} from '../../services/productsService';

const defaultValues = {
  name: '',
  code: '',
  description: '',
  categoryId: '',
  baseUnitId: '',
  minimumStock: '',
  currentStock: 0,
  locationId: '',
  storageCondition: 'Ambiente',
  observations: '',
  requiresExpiration: false,
  requiresBatchControl: true,
  active: true,
};

function ProductCreatePage() {
  const navigate = useNavigate();
  const [catalogs, setCatalogs] = useState({
    categories: [],
    units: [],
    locations: [],
  });
  const [catalogsLoading, setCatalogsLoading] = useState(true);
  const [catalogsError, setCatalogsError] = useState('');
  const [serverMessage, setServerMessage] = useState('');

  const form = useForm({
    resolver: zodResolver(productFormSchema),
    defaultValues,
  });

  const {
    setError,
    formState: { isSubmitting },
  } = form;

  const loadCatalogs = async () => {
    setCatalogsLoading(true);
    setCatalogsError('');

    try {
      const response = await fetchProductCatalogs();
      setCatalogs(response);
    } catch (error) {
      setCatalogs({ categories: [], units: [], locations: [] });
      setCatalogsError(getProductCatalogsErrorMessage(error));
    } finally {
      setCatalogsLoading(false);
    }
  };

  useEffect(() => {
    loadCatalogs();
  }, []);

  const handleSubmit = async (values) => {
    setServerMessage('');

    try {
      await createProduct(values);
      navigate('/products', {
        replace: true,
        state: { notice: 'Insumo creado correctamente.' },
      });
    } catch (error) {
      const details = getCreateProductErrorDetails(error);

      Object.entries(details.fieldErrors).forEach(([field, message]) => {
        setError(field, { type: 'server', message });
      });

      setServerMessage(details.message);
    }
  };

  const hasCatalogData =
    catalogs.categories.length && catalogs.units.length && catalogs.locations.length;

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <Link
          to="/products"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-copy transition hover:text-brand-ink"
        >
          <ArrowLeft className="h-4 w-4" />
          Volver a insumos
        </Link>

        <SectionHeader
          title="Nuevo insumo"
          subtitle="Registra un nuevo insumo real en el catalogo institucional."
        />
      </div>

      <Card className="overflow-hidden bg-[linear-gradient(135deg,_#ffffff_0%,_#f4f8f4_100%)] p-0">
        <div className="grid gap-0 lg:grid-cols-[minmax(260px,0.34fr)_minmax(0,0.66fr)]">
          <aside className="border-b border-brand-ink/[0.06] bg-[linear-gradient(160deg,_#163826_0%,_#1e5d38_100%)] p-6 text-white lg:border-b-0 lg:border-r lg:border-white/10 lg:p-8">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/12">
              <PackagePlus className="h-5 w-5" strokeWidth={2.2} />
            </div>
            <h2 className="mt-8 text-3xl font-extrabold tracking-[-0.05em]">
              Alta de insumo
            </h2>
            <p className="mt-4 text-sm leading-7 text-white/72">
              Completa los datos requeridos para crear un insumo real y dejarlo disponible
              en el listado principal.
            </p>

            <div className="mt-8 space-y-3 text-sm text-white/78">
              <p>1. Selecciona categoria, unidad y ubicacion.</p>
              <p>2. Define stock minimo y condicion de almacenamiento.</p>
              <p>3. Guarda para volver al catalogo actualizado.</p>
            </div>
          </aside>

          <div className="p-6 sm:p-8">
            {catalogsLoading ? (
              <div className="space-y-4 animate-pulse">
                <div className="h-5 w-40 rounded-full bg-surface-2" />
                <div className="grid gap-4 lg:grid-cols-2">
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                  <div className="h-14 rounded-[22px] bg-surface-2" />
                </div>
                <div className="h-32 rounded-[22px] bg-surface-2" />
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
                  Faltan catalogos para registrar insumos
                </h3>
                <p className="mt-2 text-sm leading-7 text-copy">
                  Verifica que existan categorias, unidades base y ubicaciones activas en el
                  backend antes de crear un nuevo insumo.
                </p>
              </div>
            ) : (
              <ProductForm
                form={form}
                onSubmit={handleSubmit}
                categories={catalogs.categories}
                units={catalogs.units}
                locations={catalogs.locations}
                submitLabel={isSubmitting ? 'Guardando insumo...' : 'Guardar insumo'}
                isSubmitting={isSubmitting}
                onCancel={() => navigate('/products')}
                serverMessage={serverMessage}
              />
            )}
          </div>
        </div>
      </Card>
    </div>
  );
}

export default ProductCreatePage;
