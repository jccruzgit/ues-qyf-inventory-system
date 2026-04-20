import api from '../lib/api';

function toNumber(value, fallback = 0) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function normalizeStorageCondition(value) {
  const normalizedValue = String(value ?? '').trim().toLowerCase();

  if (!normalizedValue) {
    return {
      key: 'ambient',
      label: 'Ambiente',
      raw: '',
    };
  }

  if (
    normalizedValue.includes('cold') ||
    normalizedValue.includes('refrig') ||
    normalizedValue.includes('frio') ||
    normalizedValue.includes('2-8')
  ) {
    return {
      key: 'cold',
      label: 'Refrigerado',
      raw: value,
    };
  }

  return {
    key: 'ambient',
    label: 'Ambiente',
    raw: value,
  };
}

function inferRisk(productDto, normalizedStorage) {
  const searchableText = [
    productDto?.name,
    productDto?.description,
    productDto?.observations,
    productDto?.storageCondition,
    productDto?.categoryName,
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();

  if (
    searchableText.includes('corros') ||
    searchableText.includes('acid') ||
    searchableText.includes('acido')
  ) {
    return 'Corrosivo';
  }

  if (normalizedStorage.key === 'cold' || productDto?.requiresExpiration || productDto?.requiresBatchControl) {
    return 'Sensible';
  }

  return 'Bajo riesgo';
}

function deriveMaxStock(currentStock, minimumStock) {
  // Temporary adapter: the backend exposes current and minimum stock, but not
  // a target/max stock value for the UI progress bar yet.
  return Math.max(currentStock, minimumStock * 2, 1);
}

function deriveType(categoryName) {
  // Temporary adapter: until the backend exposes an explicit product type,
  // the category is used as the closest available classification.
  return categoryName?.trim() || 'Sin clasificacion';
}

export function adaptProductFromApi(productDto) {
  const currentStock = toNumber(productDto?.currentStock);
  const minimumStock = toNumber(productDto?.minimumStock);
  const storageCondition = normalizeStorageCondition(productDto?.storageCondition);

  return {
    id: productDto?.id,
    name: productDto?.name?.trim() || 'Producto sin nombre',
    code: productDto?.code?.trim() || 'SIN-CODIGO',
    type: deriveType(productDto?.categoryName),
    category: productDto?.categoryName?.trim() || 'Sin categoria',
    risk: inferRisk(productDto, storageCondition),
    stock: currentStock,
    maxStock: deriveMaxStock(currentStock, minimumStock),
    minimumStock,
    unit: productDto?.baseUnitName?.trim() || productDto?.baseUnitSymbol?.trim() || 'Unidades',
    laboratory: productDto?.locationName?.trim() || 'No asignado',
    storageCondition: storageCondition.key,
    storageConditionLabel: storageCondition.label,
    description: productDto?.description?.trim() || '',
    observations: productDto?.observations?.trim() || '',
    raw: productDto,
  };
}

function extractCollectionPayload(body) {
  const payload = body?.data ?? body;

  if (Array.isArray(payload)) {
    return {
      items: payload,
      totalItems: payload.length,
      pagination: null,
    };
  }

  if (Array.isArray(payload?.content)) {
    return {
      items: payload.content,
      totalItems: payload.totalElements ?? payload.content.length,
      pagination: {
        page: payload.number ?? 0,
        totalPages: payload.totalPages ?? 1,
        pageSize: payload.size ?? payload.content.length,
      },
    };
  }

  if (Array.isArray(payload?.items)) {
    return {
      items: payload.items,
      totalItems: payload.totalItems ?? payload.total ?? payload.items.length,
      pagination: payload.pagination ?? null,
    };
  }

  return {
    items: [],
    totalItems: 0,
    pagination: null,
  };
}

export async function fetchProducts() {
  const response = await api.get('/products');
  const body = response.data;

  if (body?.success === false) {
    throw new Error(body?.message ?? 'No se pudieron recuperar los productos.');
  }

  const collection = extractCollectionPayload(body);

  return {
    items: collection.items.map(adaptProductFromApi),
    totalItems: collection.totalItems,
    pagination: collection.pagination,
  };
}

export function getProductsErrorMessage(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (error?.response?.data?.error) {
    return error.response.data.error;
  }

  if (error?.message) {
    return error.message;
  }

  return 'No fue posible cargar los productos.';
}
