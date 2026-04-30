import api from '../lib/api';
import { fetchProducts } from './productsService';

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

function mapLaboratoryOption(laboratory) {
  return {
    value: String(laboratory.id),
    label: laboratory.name,
    description: laboratory.code ?? '',
    raw: laboratory,
  };
}

function parseDateToLocal(dateValue) {
  if (!dateValue) {
    return null;
  }

  const [year, month, day] = String(dateValue).split('-').map(Number);

  if (!year || !month || !day) {
    return null;
  }

  return new Date(year, month - 1, day);
}

function getDaysUntil(dateValue) {
  const targetDate = parseDateToLocal(dateValue);

  if (!targetDate) {
    return null;
  }

  const today = new Date();
  const todayAtMidnight = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const millisecondsPerDay = 1000 * 60 * 60 * 24;

  return Math.round((targetDate.getTime() - todayAtMidnight.getTime()) / millisecondsPerDay);
}

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function translateInventoryMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Movement type is required': 'El tipo de movimiento es obligatorio.',
    'Laboratory id is required': 'El laboratorio es obligatorio.',
    'At least one movement line is required': 'Debe registrar al menos una linea de movimiento.',
    'Product id is required': 'El producto es obligatorio.',
    'Quantity is required': 'La cantidad ingresada es obligatoria.',
    'Quantity must be greater than 0': 'La cantidad ingresada debe ser mayor que 0.',
    'Unit price must be greater than or equal to 0': 'El precio por unidad de medida debe ser mayor o igual a 0.',
    'Validation failed': 'Los datos del formulario no son validos.',
    'Constraint violation': 'Los datos enviados no cumplen las reglas requeridas.',
    'Access denied': 'No tiene permisos para registrar entradas de inventario.',
    'An unexpected error occurred': 'Ocurrio un error inesperado al registrar la entrada.',
    'Malformed request body': 'La solicitud enviada no tiene un formato valido.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('Laboratory not found with id:')) {
    return 'El laboratorio seleccionado ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Product not found with id:')) {
    return 'El producto seleccionado ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Batch is required for product')) {
    return 'El numero de lote es obligatorio para el producto seleccionado.';
  }

  if (normalizedMessage.startsWith('Expiration date is required for product')) {
    return 'La fecha de vencimiento es obligatoria para el producto seleccionado.';
  }

  if (normalizedMessage.startsWith('Unit price is required for entry movements')) {
    return 'El precio por unidad de medida es obligatorio para entradas.';
  }

  if (normalizedMessage.startsWith('Price unit id is required when unit price is informed')) {
    return 'La unidad del precio es obligatoria cuando se informa el precio.';
  }

  if (normalizedMessage.startsWith('Price unit must match the product base unit')) {
    return 'La unidad del precio debe coincidir con la unidad base del producto.';
  }

  if (normalizedMessage.startsWith('Expiration date does not match the existing batch data')) {
    return 'La fecha de vencimiento no coincide con la registrada para ese lote.';
  }

  if (normalizedMessage.startsWith('Unit of measure not found with id:')) {
    return 'La unidad del precio seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Product batch does not belong to the selected laboratory')) {
    return 'El lote indicado no pertenece al laboratorio seleccionado.';
  }

  if (normalizedMessage.startsWith('Product batch does not belong to the selected product')) {
    return 'El lote indicado no pertenece al producto seleccionado.';
  }

  if (normalizedMessage.startsWith('A product cannot be repeated in the same movement')) {
    return 'No se puede repetir el mismo producto dentro del mismo movimiento.';
  }

  if (normalizedMessage.startsWith('Insufficient stock for')) {
    return 'No hay existencias suficientes para completar el movimiento.';
  }

  if (normalizedMessage.startsWith('You do not have access')) {
    return 'No tiene acceso al laboratorio seleccionado.';
  }

  return normalizedMessage;
}

function mapFieldErrorPath(fieldPath) {
  const normalizedPath = String(fieldPath ?? '').trim();

  const fieldMap = {
    laboratoryId: 'laboratoryId',
    observation: 'observations',
    'lines[0].productId': 'productId',
    'lines[0].quantity': 'quantity',
    'lines[0].unitPrice': 'unitPrice',
    'lines[0].priceUnitId': 'priceUnitId',
    'lines[0].batchCode': 'batchCode',
    'lines[0].expirationDate': 'expirationDate',
    'lines[0].lineNotes': 'observations',
  };

  return fieldMap[normalizedPath] ?? normalizedPath;
}

export async function fetchInventoryCatalogs() {
  const [productsResponse, laboratoriesResponse] = await Promise.all([
    fetchProducts(),
    api.get('/laboratories'),
  ]);

  const laboratories = extractCollectionPayload(laboratoriesResponse.data)
    .filter((laboratory) => laboratory?.active !== false)
    .map(mapLaboratoryOption)
    .sort((left, right) => left.label.localeCompare(right.label));

  const products = [...productsResponse.items].sort((left, right) =>
    left.name.localeCompare(right.name),
  );

  return {
    products,
    laboratories,
  };
}

export async function fetchInventoryStock(filters = {}) {
  const params = {};

  if (filters.productId) {
    params.productId = filters.productId;
  }

  if (filters.laboratoryId) {
    params.laboratoryId = filters.laboratoryId;
  }

  const response = await api.get('/inventory-stock', { params });
  const items = extractCollectionPayload(response.data);

  return items.map((item) => {
    const daysUntilExpiration = getDaysUntil(item.expirationDate);
    const quantityAvailable = toNumber(item.quantityAvailable);
    const minimumStock = toNumber(item.minimumStock);

    return {
      id: item.productBatchId ?? `${item.productId}-${item.laboratoryId}`,
      productId: item.productId,
      productCode: item.productCode?.trim() || 'SIN-CODIGO',
      productName: item.productName?.trim() || 'Producto sin nombre',
      laboratoryId: item.laboratoryId,
      laboratoryCode: item.laboratoryCode?.trim() || '',
      laboratoryName: item.laboratoryName?.trim() || 'Laboratorio no definido',
      productBatchId: item.productBatchId,
      batchCode: item.batchCode?.trim() || 'Sin lote',
      expirationDate: item.expirationDate || '',
      quantityAvailable,
      minimumStock,
      lowStock: Boolean(item.lowStock),
      expiresSoon:
        typeof daysUntilExpiration === 'number' &&
        daysUntilExpiration >= 0 &&
        daysUntilExpiration <= 30,
      expired: typeof daysUntilExpiration === 'number' && daysUntilExpiration < 0,
      daysUntilExpiration,
      raw: item,
    };
  });
}

export async function createInventoryEntry(values) {
  const payload = {
    movementType: 'ENTRY',
    laboratoryId: values.laboratoryId,
    observation: normalizeOptionalText(values.observations),
    lines: [
      {
        productId: values.productId,
        batchCode: normalizeOptionalText(values.batchCode),
        expirationDate: values.expirationDate || null,
        quantity: values.quantity,
        unitPrice: values.unitPrice,
        priceUnitId: values.priceUnitId,
        lineNotes: null,
      },
    ],
  };

  const response = await api.post('/inventory-movements', payload);
  const body = response.data;

  if (body?.success === false) {
    throw new Error(
      translateInventoryMessage(body?.message ?? 'No se pudo registrar la entrada de inventario.'),
    );
  }

  return extractItemPayload(body);
}

export function getInventoryCatalogsErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar catalogos de inventario.';
  }

  return getInventoryStockErrorMessage(error);
}

export function getInventoryStockErrorMessage(error) {
  if (error?.response?.data?.message) {
    return translateInventoryMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return translateInventoryMessage(error.response.data.error);
  }

  if (error?.message) {
    return translateInventoryMessage(error.message);
  }

  return 'No fue posible cargar el inventario actual.';
}

export function getCreateInventoryEntryErrorDetails(error) {
  const body = error?.response?.data;
  const fieldErrors = {};

  if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
    for (const [field, message] of Object.entries(body.data)) {
      fieldErrors[mapFieldErrorPath(field)] = translateInventoryMessage(message);
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
      message: 'No tiene permisos para registrar entradas de inventario.',
      fieldErrors,
    };
  }

  return {
    message: translateInventoryMessage(
      body?.message ?? error?.message ?? 'No se pudo registrar la entrada de inventario.',
    ),
    fieldErrors,
  };
}
