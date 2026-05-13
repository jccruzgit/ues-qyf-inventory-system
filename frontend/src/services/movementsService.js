import api from '../lib/api';
import { adaptInventoryMovementFromApi } from '../adapters/movements.adapter';

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

function normalizeMovementMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Access denied': 'No tiene permisos para consultar movimientos de inventario.',
    'An unexpected error occurred': 'Ocurrio un error inesperado al consultar movimientos.',
    'Reason is required': 'El motivo de la reversion es obligatorio.',
    'This movement has already been reversed':
      'Este movimiento ya tiene una reversion registrada.',
    'Reversal movements cannot be reversed again':
      'No se puede reversar un movimiento que ya es una reversion.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('You do not have access')) {
    return 'No tiene acceso al laboratorio seleccionado.';
  }

  return normalizedMessage;
}

export async function fetchInventoryMovements(filters = {}) {
  const params = {};

  if (filters.productId && filters.productId !== 'all') {
    params.productId = Number(filters.productId);
  }

  if (filters.laboratoryId && filters.laboratoryId !== 'all') {
    params.laboratoryId = Number(filters.laboratoryId);
  }

  if (filters.movementType && filters.movementType !== 'all') {
    params.movementType = filters.movementType;
  }

  if (filters.dateFrom) {
    params.dateFrom = filters.dateFrom;
  }

  if (filters.dateTo) {
    params.dateTo = filters.dateTo;
  }

  const response = await api.get('/inventory-movements', { params });
  const items = extractCollectionPayload(response.data);

  return items.map(adaptInventoryMovementFromApi);
}

export async function reverseInventoryMovement(movementId, reason) {
  const response = await api.post(`/inventory-movements/${movementId}/reverse`, {
    reason: String(reason ?? '').trim(),
  });

  return adaptInventoryMovementFromApi(response?.data?.data ?? response?.data);
}

export function getInventoryMovementsErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar movimientos de inventario.';
  }

  if (error?.response?.data?.message) {
    return normalizeMovementMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return normalizeMovementMessage(error.response.data.error);
  }

  if (error?.message) {
    return normalizeMovementMessage(error.message);
  }

  return 'No fue posible cargar el historial de movimientos.';
}

export function getInventoryMovementActionErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para reversar movimientos de inventario.';
  }

  if (error?.response?.data?.message) {
    return normalizeMovementMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return normalizeMovementMessage(error.response.data.error);
  }

  if (error?.message) {
    return normalizeMovementMessage(error.message);
  }

  return 'No fue posible reversar el movimiento seleccionado.';
}
