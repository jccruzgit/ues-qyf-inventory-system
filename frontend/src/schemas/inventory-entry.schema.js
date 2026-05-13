import { z } from 'zod';

function parseRequiredNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : undefined;
}

function isValidIsoDate(value) {
  if (!value) {
    return false;
  }

  const [year, month, day] = String(value).split('-').map(Number);

  if (!year || !month || !day) {
    return false;
  }

  const date = new Date(year, month - 1, day);

  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  );
}

export const inventoryEntrySchema = z
  .object({
    productId: z.preprocess(
      parseRequiredNumber,
      z.number({ required_error: 'Seleccione un insumo.' }).positive('Seleccione un insumo.'),
    ),
    laboratoryId: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'Seleccione un laboratorio.' })
        .positive('Seleccione un laboratorio.'),
    ),
    quantity: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'Ingrese la cantidad ingresada.' })
        .positive('La cantidad ingresada debe ser mayor que 0.'),
    ),
    unitPrice: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'Ingrese el precio por unidad de medida.' })
        .min(0, 'El precio por unidad de medida debe ser mayor o igual a 0.'),
    ),
    priceUnitId: z.preprocess(
      parseRequiredNumber,
      z
        .number({ required_error: 'La unidad del precio es obligatoria.' })
        .positive('La unidad del precio es obligatoria.'),
    ),
    priceUnitLabel: z.string().optional(),
    unitLabel: z.string().optional(),
    batchCode: z
      .string()
      .trim()
      .max(100, 'El numero de lote no debe exceder 100 caracteres.')
      .optional()
      .or(z.literal('')),
    expirationDate: z
      .string()
      .optional()
      .or(z.literal(''))
      .refine((value) => !value || isValidIsoDate(value), {
        message: 'Ingrese una fecha de vencimiento valida.',
      }),
    observations: z
      .string()
      .trim()
      .max(500, 'Las observaciones no deben exceder 500 caracteres.')
      .optional()
      .or(z.literal('')),
    requiresBatchControl: z.boolean().default(false),
    requiresExpiration: z.boolean().default(false),
  })
  .superRefine((values, context) => {
    if ((values.requiresBatchControl || values.requiresExpiration) && !values.batchCode?.trim()) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['batchCode'],
        message: 'El numero de lote es obligatorio para el insumo seleccionado.',
      });
    }

    if (values.requiresExpiration && !values.expirationDate) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['expirationDate'],
        message: 'La fecha de vencimiento es obligatoria para el insumo seleccionado.',
      });
    }

    if (values.unitPrice === undefined || values.unitPrice === null) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['unitPrice'],
        message: 'Ingrese el precio por unidad de medida.',
      });
    }

    if (!values.priceUnitId) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['priceUnitId'],
        message: 'La unidad del precio es obligatoria.',
      });
    }
  });
