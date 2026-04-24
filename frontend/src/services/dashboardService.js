import api from '../lib/api';

function extractItemPayload(body) {
  return body?.data ?? body;
}

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

export async function fetchDashboardSummary() {
  const response = await api.get('/dashboard/summary');
  const payload = extractItemPayload(response.data) ?? {};

  return {
    totalActiveProducts: toNumber(payload.totalActiveProducts),
    lowStockProducts: toNumber(payload.lowStockProducts),
    expiringBatches: toNumber(payload.expiringBatches),
    accessibleLaboratories: toNumber(payload.accessibleLaboratories),
    movementsLastSevenDays: toNumber(payload.movementsLastSevenDays),
    movementSeries: Array.isArray(payload.movementSeries)
      ? payload.movementSeries.map((point) => ({
          date: point.date ?? '',
          dayLabel: point.dayLabel ?? '',
          entryQuantity: toNumber(point.entryQuantity),
          exitQuantity: toNumber(point.exitQuantity),
          entryMovements: toNumber(point.entryMovements),
          exitMovements: toNumber(point.exitMovements),
        }))
      : [],
    recentMovements: Array.isArray(payload.recentMovements)
      ? payload.recentMovements.map((movement) => ({
          id: movement.id,
          movementType: movement.movementType ?? 'ENTRY',
          laboratoryId: movement.laboratoryId,
          laboratoryName: movement.laboratoryName?.trim() || 'Laboratorio no definido',
          performedByUsername: movement.performedByUsername?.trim() || 'Sistema',
          performedAt: movement.performedAt ?? '',
          primaryProductName: movement.primaryProductName?.trim() || '',
          lineCount: toNumber(movement.lineCount),
          totalQuantity: toNumber(movement.totalQuantity),
        }))
      : [],
    inventoryByLaboratory: Array.isArray(payload.inventoryByLaboratory)
      ? payload.inventoryByLaboratory.map((item) => ({
          laboratoryId: item.laboratoryId,
          laboratoryCode: item.laboratoryCode?.trim() || '',
          laboratoryName: item.laboratoryName?.trim() || 'Laboratorio no definido',
          visibleProducts: toNumber(item.visibleProducts),
          lowStockProducts: toNumber(item.lowStockProducts),
          expiringBatches: toNumber(item.expiringBatches),
          quantityAvailable: toNumber(item.quantityAvailable),
        }))
      : [],
  };
}

export function getDashboardErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar el dashboard.';
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

  return 'No fue posible cargar el resumen operativo.';
}
