import { MoreVertical } from 'lucide-react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';

function ProductCard({ product, typeVariant, riskVariant, stockTone, stockLabel }) {
  const ProductIcon = product.icon;

  return (
    <Card className="group p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-[0_22px_42px_rgba(14,47,103,0.12)]">
      <div className="flex items-start justify-between gap-4">
        <div className="flex min-w-0 gap-4">
          <div
            className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl ${product.iconTone}`}
          >
            <ProductIcon className="h-5 w-5" strokeWidth={2.2} />
          </div>

          <div className="min-w-0">
            <h3 className="text-lg font-extrabold leading-7 tracking-[-0.03em] text-brand-ink">
              {product.name}
            </h3>
            <p className="mt-1 text-sm font-semibold text-copy-soft">REF: {product.code}</p>
          </div>
        </div>

        <button
          type="button"
          className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-copy-soft transition hover:bg-surface-2 hover:text-brand-ink"
          aria-label={`Acciones de ${product.name}`}
        >
          <MoreVertical className="h-4 w-4" />
        </button>
      </div>

      <div className="mt-5 flex flex-wrap items-center gap-2.5">
        <Badge variant={typeVariant}>{product.type}</Badge>
        <Badge variant={riskVariant}>{product.risk}</Badge>
      </div>

      <div className="mt-6">
        <div className="mb-2 flex items-center justify-between gap-4">
          <p className="text-xs font-extrabold uppercase tracking-[0.24em] text-copy-soft">
            Stock disponible
          </p>
          <span className={`text-sm font-extrabold ${stockTone}`}>{stockLabel}</span>
        </div>
        <ProgressBar value={product.stock} max={product.maxStock} tone={product.stockState} />
        <p className="mt-3 text-sm font-semibold text-copy">
          {product.stock} / {product.maxStock} {product.unit}
        </p>
      </div>
    </Card>
  );
}

export default ProductCard;
