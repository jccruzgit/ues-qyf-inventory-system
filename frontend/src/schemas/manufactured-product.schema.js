import { z } from 'zod';

export const manufacturedProductFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'El nombre del producto a elaborar es obligatorio.')
    .max(150, 'El nombre no debe exceder 150 caracteres.'),
  code: z
    .string()
    .trim()
    .min(1, 'El codigo del producto a elaborar es obligatorio.')
    .max(50, 'El codigo no debe exceder 50 caracteres.'),
  groupCode: z
    .string()
    .trim()
    .min(1, 'El grupo es obligatorio.')
    .max(50, 'El grupo no debe exceder 50 caracteres.'),
  cycle: z
    .string()
    .trim()
    .min(1, 'El ciclo es obligatorio.')
    .max(50, 'El ciclo no debe exceder 50 caracteres.'),
  lotNumber: z
    .string()
    .trim()
    .min(1, 'El numero de lote es obligatorio.')
    .max(50, 'El numero de lote no debe exceder 50 caracteres.'),
  description: z
    .string()
    .trim()
    .max(500, 'La descripcion no debe exceder 500 caracteres.')
    .optional()
    .or(z.literal('')),
  active: z.boolean().default(true),
});
