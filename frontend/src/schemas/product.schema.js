import { z } from 'zod';

function parseRequiredNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function parseOptionalNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export const productFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'El nombre del insumo es obligatorio.')
    .max(150, 'El nombre no debe exceder 150 caracteres.'),
  code: z
    .string()
    .trim()
    .min(1, 'El codigo del insumo es obligatorio.')
    .max(50, 'El codigo no debe exceder 50 caracteres.'),
  description: z
    .string()
    .trim()
    .max(500, 'La descripcion no debe exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
  categoryId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione una categoria.' }).positive('Seleccione una categoria.'),
  ),
  baseUnitId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione una unidad base.' }).positive('Seleccione una unidad base.'),
  ),
  minimumStock: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Ingrese el stock minimo.' }).min(0, 'El stock minimo debe ser mayor o igual a 0.'),
  ),
  currentStock: z.preprocess(
    parseOptionalNumber,
    z.number().min(0, 'El stock actual debe ser mayor o igual a 0.').default(0),
  ),
  locationId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione una ubicacion.' }).positive('Seleccione una ubicacion.'),
  ),
  storageCondition: z
    .string()
    .trim()
    .max(120, 'La condicion de almacenamiento no debe exceder 120 caracteres.')
    .optional()
    .or(z.literal('')),
  observations: z
    .string()
    .trim()
    .max(500, 'Las observaciones no deben exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
  requiresExpiration: z.boolean().default(false),
  requiresBatchControl: z.boolean().default(true),
  active: z.boolean().default(true),
});
