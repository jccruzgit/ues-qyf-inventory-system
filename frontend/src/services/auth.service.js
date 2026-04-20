import api from '../lib/api';

export async function loginRequest({ email, password }) {
  const response = await api.post('/auth/login', {
    username: email.trim(),
    password,
  });

  const body = response.data;

  if (!body?.success || !body?.data?.token) {
    throw new Error(body?.message ?? 'No se pudo iniciar sesion.');
  }

  return body.data;
}

export function getAuthErrorMessage(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (error?.response?.data?.error) {
    return error.response.data.error;
  }

  if (error?.message) {
    return error.message;
  }

  return 'No fue posible autenticar la sesion. Verifica tus credenciales.';
}
