import { z } from 'zod';

function parseRequiredNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : undefined;
}

export const inventoryExitSchema = z
  .object({
    productId: z.preprocess(
      parseRequiredNumber,
      z.number({ required_error: 'Seleccione un producto.' }).positive('Seleccione un producto.'),
    ),
    laboratoryId: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'Seleccione un laboratorio.' })
        .positive('Seleccione un laboratorio.'),
    ),
    selectedBatchKey: z.string().optional().or(z.literal('')),
    quantity: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'Ingrese la cantidad a descargar.' })
        .positive('La cantidad a descargar debe ser mayor que 0.'),
    ),
    unitLabel: z.string().optional(),
    observations: z
      .string()
      .trim()
      .max(500, 'Las observaciones generales no deben exceder 500 caracteres.')
      .optional()
      .or(z.literal('')),
    lineObservation: z
      .string()
      .trim()
      .max(500, 'La observacion por linea no debe exceder 500 caracteres.')
      .optional()
      .or(z.literal('')),
    availableQuantity: z.preprocess(parseRequiredNumber, z.number().min(0).optional()),
    requiresBatchSelection: z.boolean().default(false),
  })
  .superRefine((values, context) => {
    if (values.requiresBatchSelection && !values.selectedBatchKey?.trim()) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['selectedBatchKey'],
        message: 'Seleccione un lote disponible.',
      });
    }

    if (
      typeof values.availableQuantity === 'number' &&
      typeof values.quantity === 'number' &&
      values.quantity > values.availableQuantity
    ) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['quantity'],
        message: 'La cantidad a descargar no puede exceder el stock disponible.',
      });
    }
  });
