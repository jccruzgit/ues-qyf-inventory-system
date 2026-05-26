import { z } from 'zod';

function parseRequiredNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export const recipeFormSchema = z.object({
  manufacturedProductId: z.preprocess(
    parseRequiredNumber,
    z
      .number({ required_error: 'Seleccione un producto elaborado.' })
      .positive('Seleccione un producto elaborado.'),
  ),
  name: z
    .string()
    .trim()
    .min(1, 'El nombre de la receta es obligatorio.')
    .max(150, 'El nombre no debe exceder 150 caracteres.'),
  code: z
    .string()
    .trim()
    .min(1, 'El codigo de la receta es obligatorio.')
    .max(50, 'El codigo no debe exceder 50 caracteres.'),
  description: z
    .string()
    .trim()
    .max(500, 'La descripcion no debe exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
  active: z.boolean().default(true),
});

export const recipeItemFormSchema = z.object({
  productId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione un insumo.' }).positive('Seleccione un insumo.'),
  ),
  unitOfMeasureId: z.preprocess(
    parseRequiredNumber,
    z
      .number({ required_error: 'La unidad de medida es obligatoria.' })
      .positive('La unidad de medida es obligatoria.'),
  ),
  quantity: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Ingrese una cantidad.' }).positive('La cantidad debe ser mayor que cero.'),
  ),
  observations: z
    .string()
    .trim()
    .max(500, 'Las observaciones no deben exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
});
