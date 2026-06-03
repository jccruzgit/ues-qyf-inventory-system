import { z } from 'zod';

function parseRequiredNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export const productionRunFormSchema = z.object({
  manufacturedProductId: z.preprocess(
    parseRequiredNumber,
    z
      .number({ required_error: 'Seleccione un producto a elaborar.' })
      .positive('Seleccione un producto a elaborar.'),
  ),
  recipeId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione una formula.' }).positive('Seleccione una formula.'),
  ),
  laboratoryId: z.preprocess(
    parseRequiredNumber,
    z.number({ required_error: 'Seleccione un laboratorio.' }).positive('Seleccione un laboratorio.'),
  ),
  groupName: z
    .string()
    .trim()
    .max(150, 'El nombre del grupo no debe exceder 150 caracteres.')
    .optional()
    .or(z.literal('')),
  notes: z
    .string()
    .trim()
    .max(500, 'Las observaciones no deben exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
});
