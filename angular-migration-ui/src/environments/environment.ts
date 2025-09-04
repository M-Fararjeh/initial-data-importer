export const environment = {
  production: false,
  apiBaseUrl: (window as any)['env']?.['API_BASE_URL'] || 'http://localhost:8080/data-import'
};