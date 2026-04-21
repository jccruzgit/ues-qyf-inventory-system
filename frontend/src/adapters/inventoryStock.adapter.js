const STOCK_STATE_ORDER = {
  critical: 0,
  low: 1,
  complete: 2,
};

const stockStateMeta = {
  critical: {
    key: 'critical',
    label: 'Stock critico',
    badgeVariant: 'danger',
    progressTone: 'danger',
    toneClassName: 'text-[#d53a43]',
    accentClassName: 'bg-[#fdebec] text-[#d53a43]',
  },
  low: {
    key: 'low',
    label: 'Stock bajo',
    badgeVariant: 'warning',
    progressTone: 'warning',
    toneClassName: 'text-[#d28a19]',
    accentClassName: 'bg-[#fff3dd] text-[#d28a19]',
  },
  complete: {
    key: 'complete',
    label: 'Stock completo',
    badgeVariant: 'success',
    progressTone: 'success',
    toneClassName: 'text-[#2fa36b]',
    accentClassName: 'bg-[#e5f7ef] text-[#249b66]',
  },
};

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function compareByExpiration(left, right) {
  const leftDays = typeof left.daysUntilExpiration === 'number' ? left.daysUntilExpiration : Number.POSITIVE_INFINITY;
  const rightDays = typeof right.daysUntilExpiration === 'number' ? right.daysUntilExpiration : Number.POSITIVE_INFINITY;

  if (leftDays !== rightDays) {
    return leftDays - rightDays;
  }

  return String(left.batchCode).localeCompare(String(right.batchCode));
}

function buildBatchItem(stockItem, unit) {
  const quantityAvailable = toNumber(stockItem.quantityAvailable);
  const daysUntilExpiration = stockItem.daysUntilExpiration;
  const hasExpiration = typeof daysUntilExpiration === 'number';
  const isExpired = hasExpiration && daysUntilExpiration < 0;
  const expiresSoon = hasExpiration && daysUntilExpiration >= 0 && daysUntilExpiration <= 30;

  return {
    id: stockItem.id,
    productBatchId: stockItem.productBatchId,
    batchCode: stockItem.batchCode || 'Sin lote',
    quantityAvailable,
    unit,
    expirationDate: stockItem.expirationDate || '',
    daysUntilExpiration,
    isExpired,
    expiresSoon,
    hasExpiration,
  };
}

export function resolveInventoryStockState(quantityAvailable, minimumStock) {
  const available = Math.max(toNumber(quantityAvailable), 0);
  const minimum = Math.max(toNumber(minimumStock), 0);

  if (available <= 0) {
    return stockStateMeta.critical;
  }

  if (minimum <= 0) {
    return stockStateMeta.complete;
  }

  if (available <= minimum * 0.5) {
    return stockStateMeta.critical;
  }

  if (available <= minimum) {
    return stockStateMeta.low;
  }

  return stockStateMeta.complete;
}

export function buildInventoryOverview(products, stockItems) {
  const productsById = new Map(products.map((product) => [product.id, product]));
  const groupedStock = new Map();

  for (const stockItem of stockItems) {
    const key = `${stockItem.productId}-${stockItem.laboratoryId ?? 'na'}`;
    const batchItem = buildBatchItem(stockItem, productsById.get(stockItem.productId)?.unit ?? 'Unidades');

    if (!groupedStock.has(key)) {
      groupedStock.set(key, {
        key,
        productId: stockItem.productId,
        laboratoryId: stockItem.laboratoryId ?? null,
        laboratoryName: stockItem.laboratoryName || 'Laboratorio no definido',
        laboratoryCode: stockItem.laboratoryCode || '',
        stockRows: [],
      });
    }

    groupedStock.get(key).stockRows.push({
      ...stockItem,
      batch: batchItem,
    });
  }

  const cards = [];
  const consumedProducts = new Set();

  for (const group of groupedStock.values()) {
    const product = productsById.get(group.productId);

    if (product) {
      consumedProducts.add(product.id);
    }

    const batches = group.stockRows
      .map((row) => row.batch)
      .sort(compareByExpiration);
    const quantityAvailable = group.stockRows.reduce(
      (total, row) => total + toNumber(row.quantityAvailable),
      0,
    );
    const minimumStock = product?.minimumStock ?? group.stockRows[0]?.minimumStock ?? 0;
    const stockState = resolveInventoryStockState(quantityAvailable, minimumStock);
    const nextExpiringBatch =
      batches.find((batch) => batch.expiresSoon) ??
      batches.find((batch) => batch.hasExpiration && !batch.isExpired) ??
      null;
    const expiredBatchCount = batches.filter((batch) => batch.isExpired).length;
    const expiringSoonCount = batches.filter((batch) => batch.expiresSoon).length;
    const activeBatchCount = batches.filter(
      (batch) => batch.quantityAvailable > 0 && batch.batchCode !== 'Sin lote',
    ).length;

    cards.push({
      id: group.key,
      productId: group.productId,
      productName: product?.name ?? group.stockRows[0]?.productName ?? 'Producto sin nombre',
      productCode: product?.code ?? group.stockRows[0]?.productCode ?? 'SIN-CODIGO',
      category: product?.category ?? 'Sin categoria',
      type: product?.type ?? 'Sin clasificacion',
      risk: product?.risk ?? 'Bajo riesgo',
      unit: product?.unit ?? group.stockRows[0]?.unit ?? 'Unidades',
      minimumStock: toNumber(minimumStock),
      quantityAvailable,
      requiresBatchControl: Boolean(product?.requiresBatchControl),
      requiresExpiration: Boolean(product?.requiresExpiration),
      storageCondition: product?.storageCondition ?? 'ambient',
      storageConditionLabel: product?.storageConditionLabel ?? 'Ambiente',
      description: product?.description ?? '',
      observations: product?.observations ?? '',
      laboratoryId: group.laboratoryId,
      laboratoryName: group.laboratoryName,
      laboratoryCode: group.laboratoryCode,
      stockState,
      batches,
      activeBatchCount,
      nextExpiringBatch,
      expiringSoonCount,
      expiredBatchCount,
    });
  }

  for (const product of products) {
    if (consumedProducts.has(product.id)) {
      continue;
    }

    const quantityAvailable = toNumber(product.stock);
    const minimumStock = toNumber(product.minimumStock);
    const stockState = resolveInventoryStockState(quantityAvailable, minimumStock);

    cards.push({
      id: `product-${product.id}`,
      productId: product.id,
      productName: product.name,
      productCode: product.code,
      category: product.category,
      type: product.type,
      risk: product.risk,
      unit: product.unit,
      minimumStock,
      quantityAvailable,
      requiresBatchControl: Boolean(product.requiresBatchControl),
      requiresExpiration: Boolean(product.requiresExpiration),
      storageCondition: product.storageCondition,
      storageConditionLabel: product.storageConditionLabel,
      description: product.description,
      observations: product.observations,
      laboratoryId: null,
      laboratoryName: product.laboratory,
      laboratoryCode: '',
      stockState,
      batches: [],
      activeBatchCount: 0,
      nextExpiringBatch: null,
      expiringSoonCount: 0,
      expiredBatchCount: 0,
    });
  }

  cards.sort((left, right) => {
    const stateDiff =
      STOCK_STATE_ORDER[left.stockState.key] - STOCK_STATE_ORDER[right.stockState.key];

    if (stateDiff !== 0) {
      return stateDiff;
    }

    const leftExpiration =
      typeof left.nextExpiringBatch?.daysUntilExpiration === 'number'
        ? left.nextExpiringBatch.daysUntilExpiration
        : Number.POSITIVE_INFINITY;
    const rightExpiration =
      typeof right.nextExpiringBatch?.daysUntilExpiration === 'number'
        ? right.nextExpiringBatch.daysUntilExpiration
        : Number.POSITIVE_INFINITY;

    if (leftExpiration !== rightExpiration) {
      return leftExpiration - rightExpiration;
    }

    return left.productName.localeCompare(right.productName);
  });

  return cards;
}

export function buildInventoryFilterOptions(cards) {
  const laboratoryOptions = [
    { value: 'all', label: 'Todos los laboratorios' },
    ...cards
      .map((card) => ({
        value: card.laboratoryName,
        label: card.laboratoryName,
      }))
      .filter((option, index, collection) => {
        return (
          option.label &&
          collection.findIndex((candidate) => candidate.value === option.value) === index
        );
      })
      .sort((left, right) => left.label.localeCompare(right.label)),
  ];

  const categoryOptions = [
    { value: 'all', label: 'Todas las categorias' },
    ...cards
      .map((card) => ({
        value: card.category,
        label: card.category,
      }))
      .filter((option, index, collection) => {
        return (
          option.label &&
          collection.findIndex((candidate) => candidate.value === option.value) === index
        );
      })
      .sort((left, right) => left.label.localeCompare(right.label)),
  ];

  return {
    laboratoryOptions,
    categoryOptions,
    stockStateOptions: [
      { value: 'all', label: 'Todos los estados' },
      { value: 'complete', label: 'Stock completo' },
      { value: 'low', label: 'Stock bajo' },
      { value: 'critical', label: 'Stock critico' },
    ],
  };
}
