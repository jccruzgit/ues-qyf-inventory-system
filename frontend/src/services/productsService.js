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
    normalizedValue.includes('congel') ||
    normalizedValue.includes('freeze') ||
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
  const baseUnitName = productDto?.baseUnitName?.trim() || '';
  const baseUnitSymbol = productDto?.baseUnitSymbol?.trim() || '';

  return {
    id: productDto?.id,
    name: productDto?.name?.trim() || 'Insumo sin nombre',
    code: productDto?.code?.trim() || 'SIN-CODIGO',
    type: deriveType(productDto?.categoryName),
    category: productDto?.categoryName?.trim() || 'Sin categoria',
    risk: inferRisk(productDto, storageCondition),
    stock: currentStock,
    maxStock: deriveMaxStock(currentStock, minimumStock),
    minimumStock,
    baseUnitId: productDto?.baseUnitId ?? null,
    unit: baseUnitName || baseUnitSymbol || 'Unidades',
    unitName: baseUnitName || 'Unidades',
    unitSymbol: baseUnitSymbol,
    locationName: productDto?.locationName?.trim() || '',
    laboratory: productDto?.locationName?.trim() || 'No asignado',
    storageCondition: storageCondition.key,
    storageConditionLabel: storageCondition.label,
    description: productDto?.description?.trim() || '',
    observations: productDto?.observations?.trim() || '',
    requiresExpiration: Boolean(productDto?.requiresExpiration),
    requiresBatchControl: Boolean(productDto?.requiresBatchControl),
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

function extractItemPayload(body) {
  return body?.data ?? body;
}

function normalizeOptionalText(value) {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue ? normalizedValue : null;
}

function mapCategoryOption(category) {
  return {
    value: String(category.id),
    label: category.name,
    description: category.description ?? '',
    raw: category,
  };
}

function mapUnitOption(unit) {
  return {
    value: String(unit.id),
    label: unit.symbol ? `${unit.name} (${unit.symbol})` : unit.name,
    description: unit.type ?? '',
    raw: unit,
  };
}

function mapLocationOption(location) {
  return {
    value: String(location.id),
    label: location.name,
    description: location.description ?? '',
    raw: location,
  };
}

function translateValidationMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Product code is required': 'El codigo del insumo es obligatorio.',
    'Product name is required': 'El nombre del insumo es obligatorio.',
    'Category id is required': 'La categoria es obligatoria.',
    'Base unit id is required': 'La unidad base es obligatoria.',
    'Minimum stock is required': 'El stock minimo es obligatorio.',
    'Location id is required': 'La ubicacion es obligatoria.',
    'Current stock must be greater than or equal to 0': 'El stock actual debe ser mayor o igual a 0.',
    'Minimum stock must be greater than or equal to 0': 'El stock minimo debe ser mayor o igual a 0.',
    'Product code must not exceed 50 characters': 'El codigo no debe exceder 50 caracteres.',
    'Product name must not exceed 150 characters': 'El nombre no debe exceder 150 caracteres.',
    'Product description must not exceed 500 characters': 'La descripcion no debe exceder 500 caracteres.',
    'Storage condition must not exceed 120 characters': 'La condicion de almacenamiento no debe exceder 120 caracteres.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('Product code already exists')) {
    return 'Ya existe un insumo con ese codigo.';
  }

  if (normalizedMessage.startsWith('Category not found')) {
    return 'La categoria seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Unit of measure not found')) {
    return 'La unidad base seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Location not found')) {
    return 'La ubicacion seleccionada ya no esta disponible.';
  }

  if (normalizedMessage === 'Validation failed') {
    return 'Los datos del formulario no son validos.';
  }

  if (normalizedMessage === 'Access denied') {
    return 'No tiene permisos para registrar insumos.';
  }

  if (normalizedMessage === 'An unexpected error occurred') {
    return 'Ocurrio un error inesperado al guardar el insumo.';
  }

  return normalizedMessage;
}

export async function fetchProducts() {
  const response = await api.get('/products');
  const body = response.data;

  if (body?.success === false) {
    throw new Error(body?.message ?? 'No se pudieron recuperar los insumos.');
  }

  const collection = extractCollectionPayload(body);

  return {
    items: collection.items.map(adaptProductFromApi),
    totalItems: collection.totalItems,
    pagination: collection.pagination,
  };
}

export async function fetchProductCatalogs() {
  const [categoriesResponse, unitsResponse, locationsResponse] = await Promise.all([
    api.get('/categories'),
    api.get('/units'),
    api.get('/locations'),
  ]);

  const categories = extractCollectionPayload(categoriesResponse.data).items
    .filter((item) => item?.active !== false)
    .map(mapCategoryOption);

  const units = extractCollectionPayload(unitsResponse.data).items
    .filter((item) => item?.active !== false)
    .map(mapUnitOption);

  const locations = extractCollectionPayload(locationsResponse.data).items
    .filter((item) => item?.active !== false)
    .map(mapLocationOption);

  return {
    categories,
    units,
    locations,
  };
}

export async function createProduct(values) {
  const payload = {
    code: values.code.trim(),
    name: values.name.trim(),
    description: normalizeOptionalText(values.description),
    categoryId: values.categoryId,
    baseUnitId: values.baseUnitId,
    minimumStock: values.minimumStock,
    currentStock: values.currentStock ?? 0,
    locationId: values.locationId,
    observations: normalizeOptionalText(values.observations),
    storageCondition: normalizeOptionalText(values.storageCondition),
    requiresExpiration: values.requiresExpiration,
    requiresBatchControl: values.requiresBatchControl,
    active: values.active,
  };

  const response = await api.post('/products', payload);
  const body = response.data;

  if (body?.success === false) {
    throw new Error(translateValidationMessage(body?.message ?? 'No se pudo crear el insumo.'));
  }

  return adaptProductFromApi(extractItemPayload(body));
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

  return 'No fue posible cargar los insumos.';
}

export function getProductCatalogsErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar los catalogos requeridos.';
  }

  return getProductsErrorMessage(error);
}

export function getCreateProductErrorDetails(error) {
  const body = error?.response?.data;
  const fieldErrors = {};

  if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
    for (const [field, message] of Object.entries(body.data)) {
      fieldErrors[field] = translateValidationMessage(message);
    }
  }

  if (error?.response?.status === 401) {
    return {
      message: 'La sesion ha expirado. Inicie sesion nuevamente.',
      fieldErrors,
    };
  }

  if (error?.response?.status === 403) {
    return {
      message: 'No tiene permisos para registrar insumos.',
      fieldErrors,
    };
  }

  return {
    message: translateValidationMessage(body?.message ?? error?.message ?? 'No se pudo crear el insumo.'),
    fieldErrors,
  };
}
