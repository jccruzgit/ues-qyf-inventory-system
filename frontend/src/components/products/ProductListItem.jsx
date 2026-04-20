import { MoreVertical } from 'lucide-react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';

function ProductListItem({
  product,
  typeVariant,
  riskVariant,
  stockTone,
  stockLabel,
}) {
  const ProductIcon = product.icon;

  return (
    <Card className="group rounded-[28px] px-5 py-4 transition duration-200 hover:-translate-y-0.5 hover:shadow-[0_22px_42px_rgba(14,47,103,0.1)] sm:px-6">
      <div className="grid items-center gap-4 lg:grid-cols-[minmax(280px,1.55fr)_minmax(160px,0.72fr)_minmax(170px,0.82fr)_minmax(220px,0.8fr)_44px]">
        <div className="flex min-w-0 items-center gap-4">
          <div
            className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl ${product.iconTone}`}
          >
            <ProductIcon className="h-5 w-5" strokeWidth={2.2} />
          </div>

          <div className="min-w-0">
            <h3 className="text-xl font-extrabold leading-7 tracking-[-0.035em] text-brand-ink">
              {product.name}
            </h3>
            <p className="mt-1 text-sm font-semibold text-copy-soft">REF: {product.code}</p>
          </div>
        </div>

        <div>
          <Badge variant={typeVariant}>{product.type}</Badge>
        </div>

        <div>
          <Badge variant={riskVariant}>{product.risk}</Badge>
        </div>

        <div className="min-w-0">
          <ProgressBar value={product.stock} max={product.maxStock} tone={product.stockState} />
          <div className="mt-2 flex items-center justify-between gap-3 text-sm font-extrabold">
            <span className="text-copy">
              {product.stock} / {product.maxStock} {product.unit}
            </span>
            <span className={stockTone}>{stockLabel}</span>
          </div>
        </div>

        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-full text-copy-soft transition hover:bg-surface-2 hover:text-brand-ink"
          aria-label={`Acciones de ${product.name}`}
        >
          <MoreVertical className="h-4 w-4" />
        </button>
      </div>
    </Card>
  );
}

export default ProductListItem;
