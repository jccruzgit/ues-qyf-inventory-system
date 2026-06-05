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

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function translateRecipeMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    'Manufactured product id is required':
      'Debe seleccionar un producto a elaborar para la formula.',
    'Recipe code is required': 'El codigo de la formula es obligatorio.',
    'Recipe name is required': 'El nombre de la formula es obligatorio.',
    'Recipe code must not exceed 50 characters': 'El codigo no debe exceder 50 caracteres.',
    'Recipe name must not exceed 150 characters': 'El nombre no debe exceder 150 caracteres.',
    'Recipe description must not exceed 500 characters':
      'La descripcion no debe exceder 500 caracteres.',
    'Product id is required': 'Debe seleccionar un insumo.',
    'Unit of measure id is required': 'La unidad de medida es obligatoria.',
    'Quantity is required': 'La cantidad es obligatoria.',
    'Quantity must be greater than 0': 'La cantidad debe ser mayor que cero.',
    'Validation failed': 'Los datos de la formula no son validos.',
    'Access denied': 'No tiene permisos para gestionar formulas.',
    'An unexpected error occurred': 'Ocurrio un error inesperado al procesar la formula.',
  };

  if (exactMessages[normalizedMessage]) {
    return exactMessages[normalizedMessage];
  }

  if (normalizedMessage.startsWith('Recipe code already exists')) {
    return 'Ya existe una formula con ese codigo.';
  }

  if (normalizedMessage.startsWith('Manufactured product not found with id:')) {
    return 'El producto a elaborar seleccionado ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Recipe not found with id:')) {
    return 'La formula seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Recipe item not found with id:')) {
    return 'El insumo seleccionado ya no forma parte de la formula.';
  }

  if (normalizedMessage.startsWith('Recipe already contains product id:')) {
    return 'Ese insumo ya forma parte de la formula.';
  }

  if (normalizedMessage.startsWith('Product not found with id:')) {
    return 'El insumo seleccionado ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Unit of measure not found with id:')) {
    return 'La unidad de medida seleccionada ya no esta disponible.';
  }

  if (normalizedMessage.startsWith('Recipe item unit must match the product base unit')) {
    return 'La unidad de medida debe coincidir con la unidad base del insumo.';
  }

  return normalizedMessage;
}

function adaptRecipeItemFromApi(item) {
  return {
    id: item?.id ?? null,
    itemOrder: item?.itemOrder ?? null,
    productId: item?.productId ?? null,
    productCode: normalizeText(item?.productCode, 'SIN-CODIGO'),
    productName: normalizeText(item?.productName, 'Insumo sin nombre'),
    unitOfMeasureId: item?.unitOfMeasureId ?? null,
    unitOfMeasureName: normalizeText(item?.unitOfMeasureName, 'Unidad no definida'),
    unitOfMeasureSymbol: normalizeText(item?.unitOfMeasureSymbol),
    quantity: toNumber(item?.quantity),
    observations: normalizeText(item?.observations),
    locationName: normalizeText(item?.locationName, 'Ubicacion no definida'),
    raw: item,
  };
}

function adaptRecipeFromApi(item) {
  const items = Array.isArray(item?.items) ? item.items.map(adaptRecipeItemFromApi) : [];

  return {
    id: item?.id ?? null,
    manufacturedProductId: item?.manufacturedProductId ?? null,
    manufacturedProductCode: normalizeText(item?.manufacturedProductCode, 'SIN-CODIGO'),
    manufacturedProductName: normalizeText(
      item?.manufacturedProductName,
      'Producto a elaborar sin nombre',
    ),
    manufacturedProductGroupCode: normalizeText(item?.manufacturedProductGroupCode ?? item?.groupCode, ''),
    manufacturedProductCycle: normalizeText(item?.manufacturedProductCycle ?? item?.cycle, ''),
    manufacturedProductLotNumber: normalizeText(item?.manufacturedProductLotNumber ?? item?.lotNumber, ''),
    code: normalizeText(item?.code, 'SIN-CODIGO'),
    name: normalizeText(item?.name, 'Formula sin nombre'),
    description: normalizeText(item?.description),
    active: item?.active !== false,
    createdAt: normalizeText(item?.createdAt),
    updatedAt: normalizeText(item?.updatedAt),
    deletedAt: normalizeText(item?.deletedAt),
    deletedById: item?.deletedById ?? null,
    items,
    raw: item,
  };
}

function adaptPrintableRecipeItem(item) {
  return {
    itemOrder: item?.itemOrder ?? null,
    productId: item?.productId ?? null,
    productCode: normalizeText(item?.productCode, 'SIN-CODIGO'),
    productName: normalizeText(item?.productName, 'Insumo sin nombre'),
    unitOfMeasureName: normalizeText(item?.unitOfMeasureName, 'Unidad no definida'),
    unitOfMeasureSymbol: normalizeText(item?.unitOfMeasureSymbol),
    theoreticalQuantity: toNumber(item?.theoreticalQuantity),
    observations: normalizeText(item?.observations),
    raw: item,
  };
}

function adaptRecipePrintableFromApi(item) {
  return {
    recipeId: item?.recipeId ?? null,
    recipeCode: normalizeText(item?.recipeCode, 'SIN-CODIGO'),
    recipeName: normalizeText(item?.recipeName, 'Formula sin nombre'),
    manufacturedProductId: item?.manufacturedProductId ?? null,
    manufacturedProductCode: normalizeText(item?.manufacturedProductCode, 'SIN-CODIGO'),
    manufacturedProductName: normalizeText(
      item?.manufacturedProductName,
      'Producto a elaborar sin nombre',
    ),
    groupCode: normalizeText(item?.groupCode),
    cycle: normalizeText(item?.cycle),
    lotNumber: normalizeText(item?.lotNumber),
    generatedAt: normalizeText(item?.generatedAt),
    items: Array.isArray(item?.items) ? item.items.map(adaptPrintableRecipeItem) : [],
    raw: item,
  };
}

export async function fetchRecipes() {
  const response = await api.get('/recipes');
  return extractCollectionPayload(response.data).map(adaptRecipeFromApi);
}

export async function fetchRecipeById(id) {
  const response = await api.get(`/recipes/${id}`);
  return adaptRecipeFromApi(extractItemPayload(response.data));
}

export async function fetchRecipePrintableById(id) {
  const response = await api.get(`/recipes/${id}/printable`);
  return adaptRecipePrintableFromApi(extractItemPayload(response.data));
}

export async function createRecipe(values) {
  const response = await api.post('/recipes', {
    manufacturedProductId: values.manufacturedProductId,
    code: values.code.trim(),
    name: values.name.trim(),
    description: normalizeOptionalText(values.description),
    active: values.active,
  });

  return adaptRecipeFromApi(extractItemPayload(response.data));
}

export async function updateRecipe(id, values) {
  const response = await api.put(`/recipes/${id}`, {
    manufacturedProductId: values.manufacturedProductId,
    code: values.code.trim(),
    name: values.name.trim(),
    description: normalizeOptionalText(values.description),
    active: values.active,
  });

  return adaptRecipeFromApi(extractItemPayload(response.data));
}

export async function addRecipeItem(recipeId, values) {
  const response = await api.post(`/recipes/${recipeId}/items`, {
    productId: values.productId,
    unitOfMeasureId: values.unitOfMeasureId,
    quantity: values.quantity,
    observations: normalizeOptionalText(values.observations),
  });

  return adaptRecipeFromApi(extractItemPayload(response.data));
}

export async function deleteRecipeItem(recipeId, itemId) {
  const response = await api.delete(`/recipes/${recipeId}/items/${itemId}`);
  return adaptRecipeFromApi(extractItemPayload(response.data));
}

export function getRecipesErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'La sesion ha expirado. Inicie sesion nuevamente.';
  }

  if (error?.response?.status === 403) {
    return 'No tiene permisos para consultar formulas.';
  }

  if (error?.response?.data?.message) {
    return translateRecipeMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return translateRecipeMessage(error.response.data.error);
  }

  if (error?.message) {
    return translateRecipeMessage(error.message);
  }

  return 'No fue posible cargar las formulas.';
}

export function getRecipeMutationErrorDetails(error) {
  const body = error?.response?.data;
  const fieldErrors = {};

  if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
    for (const [field, message] of Object.entries(body.data)) {
      fieldErrors[field] = translateRecipeMessage(message);
    }
  }

  return {
    message: getRecipesErrorMessage(error),
    fieldErrors,
  };
}
