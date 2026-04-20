import { z } from 'zod';

const institutionalEmailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const usernamePattern = /^[a-zA-Z0-9._-]+$/;

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Ingresa tu correo institucional o usuario.')
    .refine(
      (value) => institutionalEmailPattern.test(value) || usernamePattern.test(value),
      'Ingresa un correo institucional o usuario valido.',
    ),
  password: z.string().min(1, 'Ingresa tu clave de acceso.'),
  rememberSession: z.boolean().default(false),
});
