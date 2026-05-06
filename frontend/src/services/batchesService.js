import api from '../lib/api';

const operationalStateMeta = {
  vigente: {
    key: 'vigente',
    label: 'Vigente',
    variant: 'success',
    helper: 'Disponible para uso.',
  },
  proximo: {
    key: 'proximo',
    label: 'Proximo a vencer',
    variant: 'warning',
    helper: 'Requiere seguimiento preventivo.',
  },
  vencido: {
    key: 'vencido',
    label: 'Vencido',
    variant: 'danger',
    helper: 'No debe utilizarse.',
  },
  agotado: {
    key: 'agotado',
    label: 'Agotado',
    variant: 'neutral',
    helper: 'Sin existencias disponibles.',
  },
};

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

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
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

function normalizeText(value, fallback = '') {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue || fallback;
}

function resolveOperationalState(batchStatus, quantityAvailable, daysUntilExpiration) {
  const normalizedBatchStatus = normalizeText(batchStatus).toUpperCase();

  if (quantityAvailable <= 0 || normalizedBatchStatus === 'EXHAUSTED') {
    return operationalStateMeta.agotado;
  }

  if (normalizedBatchStatus === 'QUARANTINED') {
    return {
      ...operationalStateMeta.vigente,
      label: 'Cuarentena',
      variant: 'warning',
      helper: 'Lote retenido para revision.',
    };
  }

  if (normalizedBatchStatus === 'EXPIRED') {
    return operationalStateMeta.vencido;
  }

  if (typeof daysUntilExpiration === 'number' && daysUntilExpiration < 0) {
    return operationalStateMeta.vencido;
  }

  if (typeof daysUntilExpiration === 'number' && daysUntilExpiration <= 30) {
    return operationalStateMeta.proximo;
  }

  return operationalStateMeta.vigente;
}

export async function fetchProductBatches(filters = {}) {
  const params = {};

  if (filters.productId) {
    params.productId = filters.productId;
  }

  if (filters.laboratoryId) {
    params.laboratoryId = filters.laboratoryId;
  }

  const response = await api.get('/product-batches/overview', { params });
  const items = extractCollectionPayload(response.data);

  return items.map((item) => {
    const quantityAvailable = toNumber(item.quantityAvailable);
    const daysUntilExpiration = getDaysUntil(item.expirationDate);
    const operationalState = resolveOperationalState(item.status, quantityAvailable, daysUntilExpiration);
    const unitName = normalizeText(item.unitName);
    const unitSymbol = normalizeText(item.unitSymbol);
    const priceUnitName = normalizeText(item.priceUnitName);
    const priceUnitSymbol = normalizeText(item.priceUnitSymbol);

    return {
      id: item.id,
      productId: item.productId,
      productCode: normalizeText(item.productCode, 'SIN-CODIGO'),
      productName: normalizeText(item.productName, 'Producto sin nombre'),
      laboratoryId: item.laboratoryId ?? null,
      laboratoryCode: normalizeText(item.laboratoryCode),
      laboratoryName: normalizeText(item.laboratoryName, 'Laboratorio no definido'),
      batchCode: normalizeText(item.batchCode, 'Sin lote'),
      locationName: normalizeText(item.locationName),
      quantityAvailable,
      unitName,
      unitSymbol,
      unitLabel: unitName || unitSymbol || 'Unidades',
      expirationDate: item.expirationDate || '',
      daysUntilExpiration,
      unitPrice: item.unitPrice == null ? null : toNumber(item.unitPrice, null),
      priceUnitName,
      priceUnitSymbol,
      priceUnitLabel: priceUnitName || priceUnitSymbol || unitName || unitSymbol || 'unidad',
      status: normalizeText(item.status, 'ACTIVE'),
      notes: normalizeText(item.notes),
      operationalState,
      raw: item,
    };
  });
}

export function getProductBatchesErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar los lotes del inventario.';
  }

  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (error?.response?.data?.error) {
    return error.response.data.error;
  }

  if (error?.message) {
    return error.message;
  }

  return 'No fue posible cargar los lotes del inventario.';
}

export const batchOperationalStateOptions = [
  { value: 'all', label: 'Todos los estados' },
  { value: 'vigente', label: 'Vigente' },
  { value: 'proximo', label: 'Proximo a vencer' },
  { value: 'vencido', label: 'Vencido' },
  { value: 'agotado', label: 'Agotado' },
];
