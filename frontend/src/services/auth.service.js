import api from '../lib/api';

function translateAuthMessage(message) {
  const normalizedMessage = String(message ?? '').trim();

  const exactMessages = {
    Unauthorized: 'Usuario o clave incorrectos. Verifique sus credenciales.',
    'Invalid username or password': 'Usuario o clave incorrectos. Verifique sus credenciales.',
    'Bad credentials': 'Usuario o clave incorrectos. Verifique sus credenciales.',
    'Validation failed': 'Complete usuario y clave para iniciar sesion.',
    'An unexpected error occurred': 'Ocurrio un error inesperado al iniciar sesion.',
  };

  return exactMessages[normalizedMessage] ?? normalizedMessage;
}

export async function loginRequest({ email, password }) {
  const response = await api.post('/auth/login', {
    username: email.trim(),
    password,
  });

  const body = response.data;

  if (!body?.success || !body?.data?.token) {
    throw new Error(translateAuthMessage(body?.message ?? 'No se pudo iniciar sesion.'));
  }

  return body.data;
}

export function getAuthErrorMessage(error) {
  if (error?.response?.status === 401) {
    return 'Usuario o clave incorrectos. Verifique sus credenciales.';
  }

  if (error?.response?.data?.message) {
    return translateAuthMessage(error.response.data.message);
  }

  if (error?.response?.data?.error) {
    return translateAuthMessage(error.response.data.error);
  }

  if (error?.message) {
    return translateAuthMessage(error.message);
  }

  return 'No fue posible autenticar la sesion. Verifica tus credenciales.';
}
