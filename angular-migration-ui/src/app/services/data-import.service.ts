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