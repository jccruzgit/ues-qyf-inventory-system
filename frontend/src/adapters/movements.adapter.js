function normalizeText(value, fallback = '') {
  const normalizedValue = String(value ?? '').trim();
  return normalizedValue || fallback;
}

function toNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

export function adaptInventoryMovementFromApi(movementDto) {
  return {
    id: movementDto?.id,
    movementType: normalizeText(movementDto?.movementType, 'ENTRY'),
    correctionType: normalizeText(movementDto?.correctionType, 'NORMAL'),
    relatedMovementId: movementDto?.relatedMovementId ?? null,
    correctionReason: normalizeText(movementDto?.correctionReason),
    laboratoryId: movementDto?.laboratoryId ?? null,
    performedById: movementDto?.performedById ?? null,
    performedByUsername: normalizeText(movementDto?.performedByUsername, 'Sistema'),
    performedAt: normalizeText(movementDto?.performedAt),
    observation: normalizeText(movementDto?.observation),
    attachmentDocumentId: movementDto?.attachmentDocumentId ?? null,
    lines: Array.isArray(movementDto?.lines) ? movementDto.lines : [],
    raw: movementDto,
  };
}

export function buildMovementRows(movements, products, laboratories) {
  const productsById = new Map(products.map((product) => [product.id, product]));
  const laboratoriesById = new Map(
    laboratories.map((laboratory) => [Number(laboratory.value), laboratory]),
  );

  return movements.flatMap((movement) => {
    const laboratory = laboratoriesById.get(Number(movement.laboratoryId));
    const baseRows = movement.lines.length ? movement.lines : [null];

    return baseRows.map((line, index) => {
      const product = productsById.get(line?.productId);

      return {
        id: `${movement.id}-${line?.id ?? index}`,
        movementId: movement.id,
        lineId: line?.id ?? null,
        performedAt: movement.performedAt,
        movementType: movement.movementType,
        movementTypeLabel: movement.movementType === 'EXIT' ? 'Salida' : 'Entrada',
        movementTypeVariant: movement.movementType === 'EXIT' ? 'danger' : 'success',
        correctionType: movement.correctionType,
        correctionTypeLabel:
          movement.correctionType === 'REVERSAL' ? 'Reversion' : 'Normal',
        correctionTypeVariant:
          movement.correctionType === 'REVERSAL' ? 'warning' : 'navy',
        relatedMovementId: movement.relatedMovementId,
        correctionReason: normalizeText(
          movement.correctionReason,
          movement.correctionType === 'REVERSAL' ? 'Sin motivo registrado' : 'Sin observaciones',
        ),
        productId: line?.productId ?? null,
        productName: normalizeText(line?.productName, product?.name ?? 'Producto no disponible'),
        productCode: normalizeText(line?.productCode, product?.code ?? 'SIN-CODIGO'),
        batchCode: normalizeText(line?.batchCode, 'Sin lote'),
        quantity: toNumber(line?.quantity),
        unit: normalizeText(product?.unitSymbol, product?.unit ?? 'Unidades'),
        unitPrice: line?.unitPrice == null ? null : toNumber(line.unitPrice),
        priceUnitLabel: normalizeText(
          line?.priceUnitSymbol,
          normalizeText(line?.priceUnitName, 'No aplica'),
        ),
        laboratoryId: movement.laboratoryId,
        laboratoryName: normalizeText(
          laboratory?.label,
          laboratory?.raw?.name ?? 'Laboratorio no definido',
        ),
        username: movement.performedByUsername,
        movementObservation: normalizeText(movement.observation, 'Sin observaciones'),
        lineObservation: normalizeText(line?.lineNotes, 'Sin observaciones'),
        isFirstLine: index === 0,
      };
    });
  });
}

export function sortMovementRowsByDate(rows, direction = 'desc') {
  const orderedRows = [...rows].sort((left, right) => {
    const leftValue = new Date(left.performedAt).getTime();
    const rightValue = new Date(right.performedAt).getTime();
    const fallbackLeft = Number.isFinite(leftValue) ? leftValue : 0;
    const fallbackRight = Number.isFinite(rightValue) ? rightValue : 0;

    if (fallbackLeft !== fallbackRight) {
      return direction === 'asc' ? fallbackLeft - fallbackRight : fallbackRight - fallbackLeft;
    }

    return direction === 'asc'
      ? left.movementId - right.movementId
      : right.movementId - left.movementId;
  });

  return orderedRows;
}

export function buildMovementProductOptions(products) {
  return [
    { value: 'all', label: 'Todos los productos' },
    ...products.map((product) => ({
      value: String(product.id),
      label: `${product.name} (${product.code})`,
    })),
  ];
}
