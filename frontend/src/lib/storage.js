const AUTH_STORAGE_KEY = 'qyf.inventory.auth';

function getBrowserStorages() {
  if (typeof window === 'undefined') {
    return [];
  }

  return [window.localStorage, window.sessionStorage];
}

export function getStoredSession() {
  for (const storage of getBrowserStorages()) {
    const rawValue = storage.getItem(AUTH_STORAGE_KEY);

    if (!rawValue) {
      continue;
    }

    try {
      return JSON.parse(rawValue);
    } catch {
      storage.removeItem(AUTH_STORAGE_KEY);
    }
  }

  return null;
}

export function saveStoredSession(session, rememberSession) {
  clearStoredSession();

  if (typeof window === 'undefined') {
    return;
  }

  const storage = rememberSession ? window.localStorage : window.sessionStorage;
  storage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
}

export function clearStoredSession() {
  for (const storage of getBrowserStorages()) {
    storage.removeItem(AUTH_STORAGE_KEY);
  }
}
