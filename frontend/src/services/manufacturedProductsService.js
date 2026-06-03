import api from '../lib/api';

function extractCollectionPayload(body) {
  const payload = body?.data ?? body;

  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  if (Array.isArray(payload?.items)) {
    return payload.items;
  }

  return [];
}

function extractItemPayload(body) {
  return body?.data ?? body;
}

function normalizeOptionalText(value) {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue ? normalizedValue : null;
}

function normalizeText(value, fallback = '') {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue || fallback;
}

function translateManufacturedProductMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Manufactured product code is required': 'El codigo del producto a elaborar es obligatorio.',
    'Manufactured product name is required': 'El nombre del producto a elaborar es obligatorio.',
    'Manufactured product code must not exceed 50 characters':
      'El codigo no debe exceder 50 caracteres.',
    'Manufactured product name must not exceed 150 characters':
      'El nombre no debe exceder 150 caracteres.',
    'Manufactured product group code is required': 'El grupo es obligatorio.',
    'Manufactured product cycle is required': 'El ciclo es obligatorio.',
    'Manufactured product lot number is required': 'El numero de lote es obligatorio.',
    'Manufactured product group code must not exceed 50 characters':
      'El grupo no debe exceder 50 caracteres.',
    'Manufactured product cycle must not exceed 50 characters':
      'El ciclo no debe exceder 50 caracteres.',
    'Manufactured product lot number must not exceed 50 characters':
      'El numero de lote no debe exceder 50 caracteres.',
    'Manufactured product description must not exceed 500 characters':
      'La descripcion no debe exceder 500 caracteres.',
    'Validation failed': 'Los datos del producto a elaborar no son validos.',
    'Access denied': 'No tiene permisos para gestionar productos a elaborar.',
    'An unexpected error occurred':
      'Ocurrio un error inesperado al procesar el producto a elaborar.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('Manufactured product code already exists')) {
    return 'Ya existe un producto a elaborar con ese codigo.';
  }

  if (normalizedMessage.startsWith('Manufactured product not found with id:')) {
    return 'El producto a elaborar seleccionado ya no esta disponible.';
  }

  return normalizedMessage;
}

function adaptManufacturedProductFromApi(item) {
  return {
    id: item?.id ?? null,
    code: normalizeText(item?.code, 'SIN-CODIGO'),
    name: normalizeText(item?.name, 'Producto a elaborar sin nombre'),
    groupCode: normalizeText(item?.groupCode ?? item?.group, ''),
    cycle: normalizeText(item?.cycle, ''),
    lotNumber: normalizeText(item?.lotNumber ?? item?.batchNumber, ''),
    description: normalizeText(item?.description),
    active: item?.active !== false,
    createdAt: normalizeText(item?.createdAt),
    updatedAt: normalizeText(item?.updatedAt),
    deletedAt: normalizeText(item?.deletedAt),
    deletedById: item?.deletedById ?? null,
    raw: item,
  };
}

export async function fetchManufacturedProducts() {
  const response = await api.get('/manufactured-products');
  return extractCollectionPayload(response.data).map(adaptManufacturedProductFromApi);
}

export async function createManufacturedProduct(values) {
  const response = await api.post('/manufactured-products', {
    code: values.code.trim(),
    name: values.name.trim(),
    groupCode: values.groupCode.trim(),
    cycle: values.cycle.trim(),
    lotNumber: values.lotNumber.trim(),
    description: normalizeOptionalText(values.description),
    active: values.active,
  });

  return adaptManufacturedProductFromApi(extractItemPayload(response.data));
}

export async function updateManufacturedProduct(id, values) {
  const response = await api.put(`/manufactured-products/${id}`, {
    code: values.code.trim(),
    name: values.name.trim(),
    groupCode: values.groupCode.trim(),
    cycle: values.cycle.trim(),
    lotNumber: values.lotNumber.trim(),
    description: normalizeOptionalText(values.description),
    active: values.active,
  });

  return adaptManufacturedProductFromApi(extractItemPayload(response.data));
}

export function getManufacturedProductsErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar productos a elaborar.';
  }

  if (error?.response?.data?.message) {
    return translateManufacturedProductMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return translateManufacturedProductMessage(error.response.data.error);
  }

  if (error?.message) {
    return translateManufacturedProductMessage(error.message);
  }

  return 'No fue posible cargar los productos a elaborar.';
}

export function getManufacturedProductMutationErrorDetails(error) {
  const body = error?.response?.data;
  const fieldErrors = {};

  if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
    for (const [field, message] of Object.entries(body.data)) {
      fieldErrors[field] = translateManufacturedProductMessage(message);
    }
  }

  return {
    message: getManufacturedProductsErrorMessage(error),
    fieldErrors,
  };
}
