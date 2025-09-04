export const environment = {
  production: true,
  apiBaseUrl: (window as any)['env']?.['API_BASE_URL'] || 'http://localhost:8080'
};