import api from '../lib/api';

const alertTypeMeta = {
  LOW_STOCK: {
    key: 'LOW_STOCK',
    label: 'Stock bajo',
    variant: 'warning',
  },
  OUT_OF_STOCK: {
    key: 'OUT_OF_STOCK',
    label: 'Sin stock',
    variant: 'danger',
  },
  EXPIRING_BATCH: {
    key: 'EXPIRING_BATCH',
    label: 'Proximo a vencer',
    variant: 'warning',
  },
  EXPIRED_BATCH: {
    key: 'EXPIRED_BATCH',
    label: 'Vencido',
    variant: 'danger',
  },
};

const severityMeta = {
  CRITICA: {
    key: 'CRITICA',
    label: 'Critica',
    variant: 'danger',
  },
  ALTA: {
    key: 'ALTA',
    label: 'Alta',
    variant: 'warning',
  },
  MEDIA: {
    key: 'MEDIA',
    label: 'Media',
    variant: 'teal',
  },
};

const statusMeta = {
  PENDIENTE: {
    key: 'PENDIENTE',
    label: 'Pendiente',
    variant: 'warning',
  },
  ATENDIDA: {
    key: 'ATENDIDA',
    label: 'Atendida',
    variant: 'success',
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

function normalizeText(value, fallback = '') {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue || fallback;
}

function resolveMeta(metaMap, key, fallbackLabel) {
  return (
    metaMap[normalizeText(key).toUpperCase()] ?? {
      key: normalizeText(key).toUpperCase() || fallbackLabel.toUpperCase(),
      label: fallbackLabel,
      variant: 'neutral',
    }
  );
}

export async function fetchInventoryAlerts(filters = {}) {
  const params = {};

  if (filters.laboratoryId) {
    params.laboratoryId = filters.laboratoryId;
  }

  if (filters.alertType) {
    params.alertType = filters.alertType;
  }

  if (typeof filters.pendingOnly === 'boolean') {
    params.pendingOnly = filters.pendingOnly;
  }

  const response = await api.get('/inventory-alerts', { params });
  const items = extractCollectionPayload(response.data);

  return items.map((item) => {
    const alertType = resolveMeta(alertTypeMeta, item.alertType, 'Alerta');
    const severity = resolveMeta(severityMeta, item.severity, 'Media');
    const status = resolveMeta(statusMeta, item.status, 'Pendiente');

    return {
      id: item.id,
      laboratoryId: item.laboratoryId ?? null,
      laboratoryCode: normalizeText(item.laboratoryCode),
      laboratoryName: normalizeText(item.laboratoryName, 'Laboratorio no definido'),
      productId: item.productId ?? null,
      productCode: normalizeText(item.productCode, 'SIN-CODIGO'),
      productName: normalizeText(item.productName, 'Producto sin nombre'),
      productBatchId: item.productBatchId ?? null,
      batchCode: normalizeText(item.batchCode),
      locationName: normalizeText(item.locationName),
      quantityAvailable: item.quantityAvailable == null ? null : toNumber(item.quantityAvailable, null),
      minimumStock: item.minimumStock == null ? null : toNumber(item.minimumStock, null),
      expirationDate: item.expirationDate || '',
      alertType: alertType.key,
      alertTypeLabel: alertType.label,
      alertTypeVariant: alertType.variant,
      severity: severity.key,
      severityLabel: severity.label,
      severityVariant: severity.variant,
      status: status.key,
      statusLabel: status.label,
      statusVariant: status.variant,
      message: normalizeText(item.message),
      triggeredAt: item.triggeredAt || '',
      acknowledgedAt: item.acknowledgedAt || '',
      raw: item,
    };
  });
}

export function getInventoryAlertsErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar alertas del inventario.';
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

  return 'No fue posible cargar las alertas del inventario.';
}

export const alertTypeOptions = [
  { value: 'all', label: 'Todos los tipos' },
  { value: 'LOW_STOCK', label: 'Stock bajo' },
  { value: 'OUT_OF_STOCK', label: 'Sin stock' },
  { value: 'EXPIRING_BATCH', label: 'Proximo a vencer' },
  { value: 'EXPIRED_BATCH', label: 'Vencido' },
];

export const alertSeverityOptions = [
  { value: 'all', label: 'Todas las prioridades' },
  { value: 'CRITICA', label: 'Critica' },
  { value: 'ALTA', label: 'Alta' },
  { value: 'MEDIA', label: 'Media' },
];

export const alertStatusOptions = [
  { value: 'all', label: 'Todos los estados' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'ATENDIDA', label: 'Atendida' },
];
