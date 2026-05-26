import { z } from 'zod';

export const manufacturedProductFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'El nombre del producto elaborado es obligatorio.')
    .max(150, 'El nombre no debe exceder 150 caracteres.'),
  code: z
    .string()
    .trim()
    .min(1, 'El codigo del producto elaborado es obligatorio.')
    .max(50, 'El codigo no debe exceder 50 caracteres.'),
  description: z
    .string()
    .trim()
    .max(500, 'La descripcion no debe exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
  active: z.boolean().default(true),
});
