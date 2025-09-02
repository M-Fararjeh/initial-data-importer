import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';

export interface ImportResponse {
  status: string;
  message: string;
  totalRecords: number;
  successfulImports: number;
  failedImports: number;
  errors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DataImportService {
  
  private baseUrl = 'http://localhost:8080/api/data-import';
  
  constructor(private http: HttpClient) {
    console.log('DataImportService initialized');
  }
  
  // Basic entity imports
  importEntity(endpoint: string): Observable<ImportResponse> {
    console.log('Calling import API for endpoint:', endpoint);
    return this.http.post<ImportResponse>(`${this.baseUrl}/${endpoint}`, {})
      .pipe(
        tap((response) => console.log(`Import ${endpoint} response:`, response)),
        catchError((error: HttpErrorResponse) => {
          console.error(`Error importing ${endpoint}:`, error);
          return of({
            status: 'ERROR',
            message: `Failed to import ${endpoint}: ` + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Import all basic entities
  importAllBasicEntities(): Observable<ImportResponse> {
    console.log('Calling import all basic entities API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/basic-entities`, {})
      .pipe(
        tap((response) => console.log('Import all basic entities response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing all basic entities:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import all basic entities: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Import correspondences
  importCorrespondences(): Observable<ImportResponse> {
    console.log('Calling import correspondences API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/correspondences`, {})
      .pipe(
        tap((response) => console.log('Import correspondences response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing correspondences:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import correspondences: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Import all correspondences with related data
  importAllCorrespondencesWithRelated(): Observable<ImportResponse> {
    console.log('Calling import all correspondences with related data API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/all-correspondences-with-related`, {})
      .pipe(
        tap((response) => console.log('Import all correspondences with related data response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing all correspondences with related data:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import all correspondences with related data: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Get entity record count from database
  getEntityCount(endpoint: string): Observable<number> {
    console.log('Getting entity count for endpoint:', endpoint);
    return this.http.get<{count: number}>(`${this.baseUrl}/${endpoint}/count`)
      .pipe(
        map((response: {count: number}) => response.count || 0),
        tap((count) => console.log(`Entity ${endpoint} count:`, count)),
        catchError((error: HttpErrorResponse) => {
          console.error(`Error getting count for ${endpoint}:`, error);
          return of(0);
        })
      );
  }
  
  // Correspondence import status methods
  getCorrespondenceImportStatuses(): Observable<any[]> {
    console.log('Getting correspondence import statuses');
    return this.http.get<any[]>(`${this.baseUrl}/correspondence-import-status`)
      .pipe(
        tap((statuses) => console.log('Correspondence import statuses:', statuses)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting correspondence import statuses:', error);
          return of([]);
        })
      );
  }
  
  getCorrespondenceImportStatistics(): Observable<any> {
    console.log('Getting correspondence import statistics');
    return this.http.get<any>(`${this.baseUrl}/correspondence-import-statistics`)
      .pipe(
        tap((stats) => console.log('Correspondence import statistics:', stats)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting correspondence import statistics:', error);
          return of({
            total: 0,
            completed: 0,
            inProgress: 0,
            failed: 0,
            pending: 0
          });
        })
      );
  }
  
  importAllCorrespondencesWithRelatedTracked(): Observable<ImportResponse> {
    console.log('Calling import all correspondences with related data (tracked) API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/all-correspondences-with-related`, {})
      .pipe(
        tap((response) => console.log('Import all correspondences with related data (tracked) response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing all correspondences with related data (tracked):', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import all correspondences with related data: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  importRelatedDataForCorrespondence(correspondenceGuid: string): Observable<any> {
    console.log('Importing related data for correspondence:', correspondenceGuid);
    return this.http.post<any>(`http://localhost:8080/api/correspondence-import/correspondence/${correspondenceGuid}/related`, {})
      .pipe(
        tap((response) => console.log('Import related data for correspondence response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing related data for correspondence:', error);
          return of({
            success: false,
            error: error.message || 'Unknown error'
          });
        })
      );
  }
  
  importSpecificCorrespondenceEntity(correspondenceGuid: string, endpoint: string): Observable<ImportResponse> {
    console.log('Importing specific entity for correspondence:', correspondenceGuid, endpoint);
    return this.http.post<ImportResponse>(`${this.baseUrl}/${endpoint}/${correspondenceGuid}`, {})
      .pipe(
        tap((response) => console.log('Import specific entity response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing specific entity:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import entity: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  retryFailedCorrespondenceImports(): Observable<ImportResponse> {
    console.log('Retrying failed correspondence imports');
    return this.http.post<ImportResponse>(`${this.baseUrl}/all-correspondences-with-related`, {})
      .pipe(
        tap((response) => console.log('Retry failed correspondence imports response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error retrying failed correspondence imports:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to retry imports: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  resetCorrespondenceImportStatus(correspondenceGuid: string): Observable<any> {
    console.log('Resetting import status for correspondence:', correspondenceGuid);
    return this.http.post<any>(`http://localhost:8080/api/correspondence-import/correspondence/${correspondenceGuid}/reset`, {})
      .pipe(
        tap((response) => console.log('Reset import status response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error resetting import status:', error);
          return of({
            success: false,
            error: error.message || 'Unknown error'
          });
        })
      );
  }
  getCorrespondenceImportStatuses(): Observable<any[]> {
    console.log('Getting correspondence import statuses');
    return this.http.get<any[]>('http://localhost:8080/api/correspondence-import/status')
      .pipe(
        tap((statuses) => console.log('Correspondence import statuses:', statuses)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting correspondence import statuses:', error);
          return of([]);
        })
      );
  }
  
  getCorrespondenceImportStatistics(): Observable<any> {
    console.log('Getting correspondence import statistics');
    return this.http.get<any>('http://localhost:8080/api/correspondence-import/statistics')
      .pipe(
        tap((stats) => console.log('Correspondence import statistics:', stats)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting correspondence import statistics:', error);
          return of({
            total: 0,
            completed: 0,
            inProgress: 0,
            failed: 0,
            pending: 0
          });
        })
      );
  }
  
  importAllCorrespondencesWithRelatedTracked(): Observable<ImportResponse> {
    console.log('Calling import all correspondences with related data (tracked) API');
    return this.http.post<ImportResponse>('http://localhost:8080/api/correspondence-import/all-correspondences-with-related', {})
      .pipe(
        tap((response) => console.log('Import all correspondences with related data (tracked) response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing all correspondences with related data (tracked):', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import all correspondences with related data: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  importRelatedDataForCorrespondence(correspondenceGuid: string): Observable<any> {
    console.log('Importing related data for correspondence:', correspondenceGuid);
    return this.http.post<any>(`http://localhost:8080/api/correspondence-import/correspondence/${correspondenceGuid}/related`, {})
      .pipe(
        tap((response) => console.log('Import related data for correspondence response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing related data for correspondence:', error);
          return of({
            success: false,
            error: error.message || 'Unknown error'
          });
        })
      );
  }
  
  importSpecificCorrespondenceEntity(correspondenceGuid: string, endpoint: string): Observable<ImportResponse> {
    console.log('Importing specific entity for correspondence:', correspondenceGuid, endpoint);
    return this.http.post<ImportResponse>(`${this.baseUrl}/${endpoint}/${correspondenceGuid}`, {})
      .pipe(
        tap((response) => console.log('Import specific entity response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing specific entity:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import entity: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  retryFailedCorrespondenceImports(): Observable<ImportResponse> {
    console.log('Retrying failed correspondence imports');
    return this.http.post<ImportResponse>('http://localhost:8080/api/correspondence-import/retry-failed', {})
      .pipe(
        tap((response) => console.log('Retry failed correspondence imports response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error retrying failed correspondence imports:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to retry imports: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  resetCorrespondenceImportStatus(correspondenceGuid: string): Observable<any> {
    console.log('Resetting import status for correspondence:', correspondenceGuid);
    return this.http.post<any>(`http://localhost:8080/api/correspondence-import/reset/${correspondenceGuid}`, {})
      .pipe(
        tap((response) => console.log('Reset import status response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error resetting import status:', error);
          return of({
            success: false,
            error: error.message || 'Unknown error'
          });
        })
      );
  }
  
  // External agencies import
  importExternalAgencies(): Observable<ImportResponse> {
    console.log('Calling import external agencies API');
    return this.http.post<ImportResponse>('http://localhost:8080/api/import/external-agencies', {})
      .pipe(
        tap((response) => console.log('Import external agencies response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing external agencies:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import external agencies: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Users to destination import
  importUsersToDestination(): Observable<ImportResponse> {
    console.log('Calling import users to destination API');
    return this.http.post<ImportResponse>('http://localhost:8080/api/user-import/users-to-destination', {})
      .pipe(
        tap((response) => console.log('Import users to destination response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing users to destination:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import users to destination: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Import specific correspondence related data
  importCorrespondenceAttachments(docGuid: string): Observable<ImportResponse> {
    console.log('Calling import correspondence attachments API for doc:', docGuid);
    return this.http.post<ImportResponse>(`${this.baseUrl}/correspondence-attachments/${docGuid}`, {})
      .pipe(
        tap((response) => console.log('Import correspondence attachments response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing correspondence attachments:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import correspondence attachments: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  importCorrespondenceComments(docGuid: string): Observable<ImportResponse> {
    console.log('Calling import correspondence comments API for doc:', docGuid);
    return this.http.post<ImportResponse>(`${this.baseUrl}/correspondence-comments/${docGuid}`, {})
      .pipe(
        tap((response) => console.log('Import correspondence comments response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing correspondence comments:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import correspondence comments: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  importCorrespondenceTransactions(docGuid: string): Observable<ImportResponse> {
    console.log('Calling import correspondence transactions API for doc:', docGuid);
    return this.http.post<ImportResponse>(`${this.baseUrl}/correspondence-transactions/${docGuid}`, {})
      .pipe(
        tap((response) => console.log('Import correspondence transactions response:', response)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error importing correspondence transactions:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to import correspondence transactions: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
}