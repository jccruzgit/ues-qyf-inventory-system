import api from '../lib/api';

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

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function translateProductionRunMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Recipe id is required': 'Debe seleccionar una formula.',
    'Laboratory id is required': 'Debe seleccionar un laboratorio.',
    'Group name must not exceed 150 characters':
      'El nombre del grupo no debe exceder 150 caracteres.',
    'Recipe must contain at least one item before creating a production run':
      'La formula debe tener al menos un insumo antes de crear una elaboracion.',
    'Production run has already been confirmed':
      'Esta elaboracion ya fue confirmada.',
    'At least one production run item is required':
      'Debe registrar al menos un insumo con cantidad real para confirmar la elaboracion.',
    'All recipe items must be informed when confirming actual quantities':
      'Debe registrar la cantidad real de cada insumo antes de confirmar la elaboracion.',
    'Access denied': 'No tiene permisos para registrar elaboraciones.',
    'An unexpected error occurred': 'Ocurrio un error inesperado al procesar la elaboracion.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('Production run not found with id:')) {
    return 'La elaboracion seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Recipe not found with id:')) {
    return 'La formula seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Laboratory not found with id:')) {
    return 'El laboratorio seleccionado ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('You do not have access')) {
    return 'No tiene acceso al laboratorio seleccionado.';
  }

  if (normalizedMessage.startsWith('Insufficient stock to confirm production run for:')) {
    const shortageSummary = normalizedMessage.replace(
      'Insufficient stock to confirm production run for:',
      '',
    ).trim();
    return shortageSummary
      ? `No hay stock suficiente para confirmar la elaboracion. Revise: ${shortageSummary}.`
      : 'No hay stock suficiente para confirmar la elaboracion.';
  }

  if (normalizedMessage.startsWith('Actual quantity for product ')) {
    return normalizedMessage
      .replace('Actual quantity for product ', 'La cantidad real del insumo ')
      .replace(' exceeds the maximum allowed variation of 10%', ' supera la variacion maxima permitida del 10%');
  }

  if (normalizedMessage.startsWith('Allocation quantities do not match the actual quantity for product ')) {
    return normalizedMessage.replace(
      'Allocation quantities do not match the actual quantity for product ',
      'Las asignaciones por lote no coinciden con la cantidad real del insumo ',
    );
  }

  if (normalizedMessage.startsWith('Recipe item cannot be repeated in the same production run confirmation')) {
    return 'No se puede repetir el mismo insumo dentro de la confirmacion de elaboracion.';
  }

  return normalizedMessage;
}

function adaptAllocation(item) {
  return {
    productBatchId: item?.productBatchId ?? null,
    batchCode: normalizeText(item?.batchCode, 'Sin lote'),
    expirationDate: normalizeText(item?.expirationDate),
    availableQuantity: toNumber(item?.availableQuantity),
    suggestedQuantity: toNumber(item?.suggestedQuantity),
    raw: item,
  };
}

function adaptProductionRunItem(item) {
  return {
    recipeItemId: item?.recipeItemId ?? null,
    productId: item?.productId ?? null,
    productCode: normalizeText(item?.productCode, 'SIN-CODIGO'),
    productName: normalizeText(item?.productName, 'Insumo sin nombre'),
    locationName: normalizeText(item?.locationName, 'Ubicacion no definida'),
    unitOfMeasureId: item?.unitOfMeasureId ?? null,
    unitOfMeasureName: normalizeText(item?.unitOfMeasureName, 'Unidad no definida'),
    unitOfMeasureSymbol: normalizeText(item?.unitOfMeasureSymbol),
    requiredQuantity: toNumber(item?.requiredQuantity),
    actualQuantity: item?.actualQuantity == null ? null : toNumber(item?.actualQuantity),
    minimumAllowedQuantity:
      item?.minimumAllowedQuantity == null ? null : toNumber(item?.minimumAllowedQuantity),
    maximumAllowedQuantity:
      item?.maximumAllowedQuantity == null ? null : toNumber(item?.maximumAllowedQuantity),
    variationPercentage: item?.variationPercentage == null ? null : toNumber(item?.variationPercentage),
    maximumVariationPercentage:
      item?.maximumVariationPercentage == null
        ? null
        : toNumber(item?.maximumVariationPercentage),
    withinAllowedVariation:
      item?.withinAllowedVariation == null ? null : Boolean(item?.withinAllowedVariation),
    totalAvailableQuantity: toNumber(item?.totalAvailableQuantity),
    stockSufficient: Boolean(item?.stockSufficient),
    observations: normalizeText(item?.observations),
    suggestedAllocations: Array.isArray(item?.suggestedAllocations)
      ? item.suggestedAllocations.map(adaptAllocation)
      : [],
    raw: item,
  };
}

function adaptProductionRunFromApi(item) {
  return {
    id: item?.id ?? null,
    status: normalizeText(item?.status, 'DRAFT'),
    recipeId: item?.recipeId ?? null,
    recipeCode: normalizeText(item?.recipeCode, 'SIN-CODIGO'),
    recipeName: normalizeText(item?.recipeName, 'Formula sin nombre'),
    manufacturedProductId: item?.manufacturedProductId ?? null,
    manufacturedProductCode: normalizeText(item?.manufacturedProductCode, 'SIN-CODIGO'),
    manufacturedProductName: normalizeText(
      item?.manufacturedProductName,
      'Producto a elaborar sin nombre',
    ),
    laboratoryId: item?.laboratoryId ?? null,
    laboratoryCode: normalizeText(item?.laboratoryCode),
    laboratoryName: normalizeText(item?.laboratoryName, 'Laboratorio no definido'),
    createdById: item?.createdById ?? null,
    createdByUsername: normalizeText(item?.createdByUsername, 'Sistema'),
    confirmedById: item?.confirmedById ?? null,
    confirmedByUsername: normalizeText(item?.confirmedByUsername),
    createdAt: normalizeText(item?.createdAt),
    confirmedAt: normalizeText(item?.confirmedAt),
    groupName: normalizeText(item?.groupName),
    notes: normalizeText(item?.notes),
    inventoryMovementId: item?.inventoryMovementId ?? null,
    readyToConfirm: Boolean(item?.readyToConfirm),
    items: Array.isArray(item?.items) ? item.items.map(adaptProductionRunItem) : [],
    raw: item,
  };
}

function adaptPrintableProductionRunAllocation(item) {
  return {
    movementLineId: item?.movementLineId ?? null,
    productBatchId: item?.productBatchId ?? null,
    batchCode: normalizeText(item?.batchCode, 'Sin lote'),
    expirationDate: normalizeText(item?.expirationDate),
    actualQuantity: toNumber(item?.actualQuantity),
    raw: item,
  };
}

function adaptPrintableProductionRunItem(item) {
  return {
    recipeItemId: item?.recipeItemId ?? null,
    itemOrder: item?.itemOrder ?? null,
    productId: item?.productId ?? null,
    productCode: normalizeText(item?.productCode, 'SIN-CODIGO'),
    productName: normalizeText(item?.productName, 'Insumo sin nombre'),
    unitOfMeasureName: normalizeText(item?.unitOfMeasureName, 'Unidad no definida'),
    unitOfMeasureSymbol: normalizeText(item?.unitOfMeasureSymbol),
    theoreticalQuantity: toNumber(item?.theoreticalQuantity),
    actualQuantity: item?.actualQuantity == null ? null : toNumber(item?.actualQuantity),
    observations: normalizeText(item?.observations),
    allocations: Array.isArray(item?.allocations)
      ? item.allocations.map(adaptPrintableProductionRunAllocation)
      : [],
    raw: item,
  };
}

function adaptProductionRunPrintableFromApi(item) {
  return {
    productionRunId: item?.productionRunId ?? null,
    status: normalizeText(item?.status, 'DRAFT'),
    controlMark: normalizeText(item?.controlMark),
    recipeId: item?.recipeId ?? null,
    recipeCode: normalizeText(item?.recipeCode, 'SIN-CODIGO'),
    recipeName: normalizeText(item?.recipeName, 'Formula sin nombre'),
    manufacturedProductId: item?.manufacturedProductId ?? null,
    manufacturedProductCode: normalizeText(item?.manufacturedProductCode, 'SIN-CODIGO'),
    manufacturedProductName: normalizeText(
      item?.manufacturedProductName,
      'Producto a elaborar sin nombre',
    ),
    groupName: normalizeText(item?.groupName),
    cycle: normalizeText(item?.cycle),
    lotNumber: normalizeText(item?.lotNumber),
    laboratoryId: item?.laboratoryId ?? null,
    laboratoryCode: normalizeText(item?.laboratoryCode),
    laboratoryName: normalizeText(item?.laboratoryName, 'Laboratorio no definido'),
    laboratoryDate: normalizeText(item?.laboratoryDate),
    generatedAt: normalizeText(item?.generatedAt),
    preparedByUsername: normalizeText(item?.preparedByUsername, 'Sistema'),
    confirmedByUsername: normalizeText(item?.confirmedByUsername),
    notes: normalizeText(item?.notes),
    items: Array.isArray(item?.items) ? item.items.map(adaptPrintableProductionRunItem) : [],
    raw: item,
  };
}

export async function createProductionRun(values) {
  const response = await api.post('/production-runs', {
    recipeId: values.recipeId,
    laboratoryId: values.laboratoryId,
    groupName: normalizeOptionalText(values.groupName),
    notes: normalizeOptionalText(values.notes),
  });

  return adaptProductionRunFromApi(extractItemPayload(response.data));
}

export async function confirmProductionRun(id, items) {
  const payload = Array.isArray(items) && items.length
    ? {
        items: items.map((item) => ({
          recipeItemId: item.recipeItemId,
          actualQuantity: item.actualQuantity,
        })),
      }
    : undefined;

  const response = await api.post(`/production-runs/${id}/confirm`, payload);
  return adaptProductionRunFromApi(extractItemPayload(response.data));
}

export async function fetchProductionRunById(id) {
  const response = await api.get(`/production-runs/${id}`);
  return adaptProductionRunFromApi(extractItemPayload(response.data));
}

export async function fetchProductionRunPrintableById(id) {
  const response = await api.get(`/production-runs/${id}/printable`);
  return adaptProductionRunPrintableFromApi(extractItemPayload(response.data));
}

export function getProductionRunErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para gestionar elaboraciones.';
  }

  if (error?.response?.data?.message) {
    return translateProductionRunMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return translateProductionRunMessage(error.response.data.error);
  }

  if (error?.message) {
    return translateProductionRunMessage(error.message);
  }

  return 'No fue posible procesar la elaboracion.';
}

