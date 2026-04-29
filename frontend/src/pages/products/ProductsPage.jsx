import { useEffect, useState } from 'react';
import {
  AlertTriangle,
  Beaker,
  Box,
  FlaskConical,
  Plus,
  RefreshCcw,
  SearchX,
  ShieldPlus,
  Syringe,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ProductCard from '../../components/products/ProductCard';
import ProductFilters from '../../components/products/ProductFilters';
import ProductListItem from '../../components/products/ProductListItem';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { fetchProducts, getProductsErrorMessage } from '../../services/productsService';

const badgeVariantByType = {
  'Grado farmaceutico': 'teal',
  Consumible: 'neutral',
  Equipo: 'navy',
  Reactivos: 'teal',
  Proteccion: 'neutral',
  Farmaceuticos: 'teal',
  Equipos: 'navy',
};

const badgeVariantByRisk = {
  'Bajo riesgo': 'success',
  Corrosivo: 'danger',
  Sensible: 'warning',
};

const stockTextToneByState = {
  success: 'text-[#2fa36b]',
  danger: 'text-[#d53a43]',
  warning: 'text-[#d28a19]',
  info: 'text-brand-teal',
};

const categoryVisualMap = {
  Reactivos: {
    icon: FlaskConical,
    iconTone: 'bg-[#e7efff] text-brand-ink',
  },
  Proteccion: {
    icon: ShieldPlus,
    iconTone: 'bg-brand-teal-soft text-brand-teal',
  },
  Farmaceuticos: {
    icon: Syringe,
    iconTone: 'bg-[#e7f8ea] text-[#2fa36b]',
  },
  Equipos: {
    icon: Beaker,
    iconTone: 'bg-[#eef3fb] text-copy',
  },
};

function getStockLabel(product) {
  if (product.stock <= product.minimumStock) {
    return 'Stock bajo';
  }

  const ratio = product.maxStock > 0 ? product.stock / product.maxStock : 0;

  if (ratio >= 1) {
    return 'Stock completo';
  }

  if (ratio <= 0.55) {
    return 'Stock estable';
  }

  return 'Stock completo';
}

function getStockState(product) {
  if (product.stock <= product.minimumStock) {
    return 'danger';
  }

  if (product.stock <= product.minimumStock * 1.5) {
    return 'warning';
  }

  return 'success';
}

function enrichProduct(product) {
  const stockState = getStockState(product);

  return {
    ...product,
    stockState,
    icon: categoryVisualMap[product.category]?.icon ?? Box,
    iconTone:
      categoryVisualMap[product.category]?.iconTone ?? 'bg-[#eef8f8] text-brand-teal',
  };
}

function ProductsLoadingState() {
  return (
    <div className="space-y-4">
      <div className="hidden grid-cols-[minmax(280px,1.55fr)_minmax(160px,0.72fr)_minmax(170px,0.82fr)_minmax(220px,0.8fr)_44px] gap-4 px-6 text-[0.7rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft lg:grid">
        <span>Producto y codigo</span>
        <span>Clasificacion</span>
        <span>Nivel de riesgo</span>
        <span>En stock</span>
        <span>Accion</span>
      </div>

      {Array.from({ length: 4 }).map((_, index) => (
        <Card key={index} className="animate-pulse rounded-[28px] px-5 py-4 sm:px-6">
          <div className="grid items-center gap-4 lg:grid-cols-[minmax(280px,1.55fr)_minmax(160px,0.72fr)_minmax(170px,0.82fr)_minmax(220px,0.8fr)_44px]">
            <div className="flex items-center gap-4">
              <div className="h-12 w-12 rounded-2xl bg-surface-2" />
              <div className="min-w-0 flex-1">
                <div className="h-5 w-2/3 rounded-full bg-surface-2" />
                <div className="mt-3 h-3 w-1/3 rounded-full bg-surface-2" />
              </div>
            </div>
            <div className="h-8 w-32 rounded-full bg-surface-2" />
            <div className="h-8 w-32 rounded-full bg-surface-2" />
            <div>
              <div className="h-2 rounded-full bg-surface-2" />
              <div className="mt-3 h-3 w-3/4 rounded-full bg-surface-2" />
            </div>
            <div className="h-10 w-10 rounded-full bg-surface-2" />
          </div>
        </Card>
      ))}
    </div>
  );
}

function ProductsErrorState({ message, onRetry }) {
  return (
    <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f9fbff_100%)] p-8 sm:p-10">
      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-[#fdebec] text-[#d53a43]">
          <AlertTriangle className="h-8 w-8" strokeWidth={1.9} />
        </div>
        <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
          No se pudo cargar el catalogo
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

function ProductsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [totalProducts, setTotalProducts] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [category, setCategory] = useState('all');
  const [laboratory, setLaboratory] = useState('all');
  const [storageCondition, setStorageCondition] = useState('all');

  const loadProducts = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetchProducts();
      setProducts(response.items.map(enrichProduct));
      setTotalProducts(response.totalItems);
    } catch (requestError) {
      setProducts([]);
      setTotalProducts(0);
      setError(getProductsErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

  const categoryOptions = [
    { value: 'all', label: 'Todas las clasificaciones' },
    ...[...new Set(products.map((product) => product.category))].map((option) => ({
      value: option,
      label: option,
    })),
  ];

  const laboratoryOptions = [
    { value: 'all', label: 'Todos los laboratorios' },
    ...[...new Set(products.map((product) => product.laboratory))].map((option) => ({
      value: option,
      label: option,
    })),
  ];

  const filteredProducts = products.filter((product) => {
    const matchesCategory = category === 'all' || product.category === category;
    const matchesLaboratory = laboratory === 'all' || product.laboratory === laboratory;
    const matchesStorage =
      storageCondition === 'all' || product.storageCondition === storageCondition;

    return matchesCategory && matchesLaboratory && matchesStorage;
  });
  const hasRegisteredProducts = totalProducts > 0;

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Productos"
        subtitle="Gestiona el catalogo operativo de insumos, equipos y reactivos del laboratorio."
        action={
          <Link
            to="/products/new"
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(14,47,103,0.2)] transition hover:-translate-y-0.5 hover:bg-[#0b2551]"
          >
            <Plus className="h-4 w-4" strokeWidth={2.4} />
            Nuevo producto
          </Link>
        }
      />

      {notice ? (
        <div className="rounded-[24px] border border-[#d7f0e1] bg-[#eef9f2] px-4 py-3 text-sm font-semibold text-[#2fa36b]">
          {notice}
        </div>
      ) : null}

      <ProductFilters
        category={category}
        laboratory={laboratory}
        storageCondition={storageCondition}
        categoryOptions={categoryOptions}
        laboratoryOptions={laboratoryOptions}
        onCategoryChange={setCategory}
        onLaboratoryChange={setLaboratory}
        onStorageChange={setStorageCondition}
      />

      <section className="space-y-4">
        {loading ? (
          <ProductsLoadingState />
        ) : error ? (
          <ProductsErrorState message={error} onRetry={loadProducts} />
        ) : filteredProducts.length ? (
          <>
            <div className="hidden grid-cols-[minmax(280px,1.55fr)_minmax(160px,0.72fr)_minmax(170px,0.82fr)_minmax(220px,0.8fr)_44px] gap-4 px-6 text-[0.7rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft lg:grid">
              <span>Producto y codigo</span>
              <span>Clasificacion</span>
              <span>Nivel de riesgo</span>
              <span>En stock</span>
              <span>Accion</span>
            </div>

            <div className="hidden space-y-4 lg:block">
              {filteredProducts.map((product) => (
                <ProductListItem
                  key={product.id}
                  product={product}
                  typeVariant={badgeVariantByType[product.type] ?? 'neutral'}
                  riskVariant={badgeVariantByRisk[product.risk] ?? 'neutral'}
                  stockTone={stockTextToneByState[product.stockState] ?? 'text-copy'}
                  stockLabel={getStockLabel(product)}
                />
              ))}
            </div>

            <div className="grid gap-4 lg:hidden">
              {filteredProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  typeVariant={badgeVariantByType[product.type] ?? 'neutral'}
                  riskVariant={badgeVariantByRisk[product.risk] ?? 'neutral'}
                  stockTone={stockTextToneByState[product.stockState] ?? 'text-copy'}
                  stockLabel={getStockLabel(product)}
                />
              ))}
            </div>

            <div className="pt-2 text-sm font-semibold text-copy">
              Mostrando {filteredProducts.length} de {totalProducts} productos
            </div>
          </>
        ) : (
          <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-8 sm:p-10">
            <div className="flex flex-col items-center justify-center text-center">
              <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
                <SearchX className="h-8 w-8" strokeWidth={1.9} />
              </div>
              <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
                {hasRegisteredProducts
                  ? 'No se encontraron productos'
                  : 'Aun no hay productos registrados'}
              </h3>
              <p className="mt-3 max-w-[520px] text-base leading-8 text-copy">
                {hasRegisteredProducts
                  ? 'Ajusta la categoria, el laboratorio o la condicion de almacenamiento para ver resultados disponibles en el catalogo.'
                  : 'Crea el primer producto del catalogo para habilitar el flujo de entradas, stock y movimientos de la demo.'}
              </p>
              {hasRegisteredProducts ? (
                <button
                  type="button"
                  onClick={() => {
                    setCategory('all');
                    setLaboratory('all');
                    setStorageCondition('all');
                  }}
                  className="mt-7 inline-flex items-center gap-2 rounded-full border border-brand-ink/[0.08] bg-white px-5 py-3 text-sm font-extrabold text-brand-ink transition hover:border-brand-teal/30 hover:text-brand-teal"
                >
                  <Box className="h-4 w-4" />
                  Limpiar filtros
                </button>
              ) : (
                <Link
                  to="/products/new"
                  className="mt-7 inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white transition hover:bg-[#0b2551]"
                >
                  <Plus className="h-4 w-4" />
                  Crear primer producto
                </Link>
              )}
            </div>
          </Card>
        )}
      </section>
    </div>
  );
}

export default ProductsPage;
