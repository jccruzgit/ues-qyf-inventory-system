import { useState } from 'react';
import { Box, Plus, SearchX } from 'lucide-react';
import ProductCard from '../../components/products/ProductCard';
import ProductFilters from '../../components/products/ProductFilters';
import ProductListItem from '../../components/products/ProductListItem';
import Card from '../../components/ui/Card';
import SectionHeader from '../../components/ui/SectionHeader';
import { productsMock } from '../../mocks/products';

const badgeVariantByType = {
  'Grado farmaceutico': 'teal',
  Consumible: 'neutral',
  Equipo: 'navy',
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

function getStockLabel(product) {
  const ratio = product.maxStock > 0 ? product.stock / product.maxStock : 0;

  if (ratio >= 1) {
    return 'Stock completo';
  }

  if (ratio <= 0.35) {
    return 'Stock bajo';
  }

  return 'Stock estable';
}

function ProductsPage() {
  const [category, setCategory] = useState('all');
  const [laboratory, setLaboratory] = useState('all');
  const [storageCondition, setStorageCondition] = useState('all');

  const categoryOptions = [
    { value: 'all', label: 'Todas las clasificaciones' },
    ...[...new Set(productsMock.map((product) => product.category))].map((option) => ({
      value: option,
      label: option,
    })),
  ];

  const laboratoryOptions = [
    { value: 'all', label: 'Todos los laboratorios' },
    ...[...new Set(productsMock.map((product) => product.laboratory))].map((option) => ({
      value: option,
      label: option,
    })),
  ];

  const filteredProducts = productsMock.filter((product) => {
    const matchesCategory = category === 'all' || product.category === category;
    const matchesLaboratory = laboratory === 'all' || product.laboratory === laboratory;
    const matchesStorage =
      storageCondition === 'all' || product.storageCondition === storageCondition;

    return matchesCategory && matchesLaboratory && matchesStorage;
  });

  return (
    <div className="space-y-6">
      <SectionHeader
        title="Productos"
        subtitle="Gestiona el catalogo operativo de insumos, equipos y reactivos del laboratorio."
        action={
          <button
            type="button"
            className="inline-flex items-center gap-2 rounded-full bg-brand-ink px-5 py-3 text-sm font-extrabold text-white shadow-[0_16px_32px_rgba(14,47,103,0.2)] transition hover:-translate-y-0.5 hover:bg-[#0b2551]"
          >
            <Plus className="h-4 w-4" strokeWidth={2.4} />
            Nuevo producto
          </button>
        }
      />

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
        <div className="hidden grid-cols-[minmax(280px,1.55fr)_minmax(160px,0.72fr)_minmax(170px,0.82fr)_minmax(220px,0.8fr)_44px] gap-4 px-6 text-[0.7rem] font-extrabold uppercase tracking-[0.24em] text-copy-soft lg:grid">
          <span>Producto y codigo</span>
          <span>Clasificacion</span>
          <span>Nivel de riesgo</span>
          <span>En stock</span>
          <span>Accion</span>
        </div>

        {filteredProducts.length ? (
          <>
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
              Mostrando {filteredProducts.length} de {productsMock.length} productos
            </div>
          </>
        ) : (
          <Card className="bg-[linear-gradient(135deg,_#ffffff_0%,_#f7fbff_100%)] p-8 sm:p-10">
            <div className="flex flex-col items-center justify-center text-center">
              <div className="flex h-[4.5rem] w-[4.5rem] items-center justify-center rounded-[28px] bg-surface-2 text-copy-soft">
                <SearchX className="h-8 w-8" strokeWidth={1.9} />
              </div>
              <h3 className="mt-6 text-2xl font-extrabold tracking-[-0.04em] text-brand-ink">
                No se encontraron productos
              </h3>
              <p className="mt-3 max-w-[520px] text-base leading-8 text-copy">
                Ajusta la categoria, el laboratorio o la condicion de almacenamiento para ver
                resultados disponibles en el catalogo.
              </p>
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
            </div>
          </Card>
        )}
      </section>
    </div>
  );
}

export default ProductsPage;
