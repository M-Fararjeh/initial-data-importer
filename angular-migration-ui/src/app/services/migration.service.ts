import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export interface ImportResponse {
  status: string;
  message: string;
  totalRecords: number;
  successfulImports: number;
  failedImports: number;
  errors: string[];
}

export interface MigrationStatistics {
  prepareData: number;
  creation: number;
  assignment: number;
  businessLog: number;
  comment: number;
  closing: number;
  completed: number;
  failed: number;
  inProgress: number;
}

@Injectable({
  providedIn: 'root'
})
export class MigrationService {
  
  private baseUrl = 'http://localhost:8080/data-import/api/incoming-migration';
  
  private statisticsSubject = new BehaviorSubject<MigrationStatistics | null>(null);
  public statistics$ = this.statisticsSubject.asObservable();
  
  constructor(private http: HttpClient) {
    // Initialize statistics on service creation
    this.refreshStatistics();
  }
  
  // Phase execution methods
  prepareData(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/prepare-data`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in prepareData:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to prepare data',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeCreation(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in executeCreation:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute creation',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeAssignment(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in executeAssignment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute assignment',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeBusinessLog(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in executeBusinessLog:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute business log',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeComment(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/comment`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in executeComment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute comment',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeClosing(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in executeClosing:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute closing',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  retryFailed(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/retry-failed`, {})
      .pipe(
        tap(() => this.refreshStatistics()),
        catchError(error => {
          console.error('Error in retryFailed:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to retry failed migrations',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Statistics methods
  getStatistics(): Observable<MigrationStatistics> {
    return this.http.get<MigrationStatistics>(`${this.baseUrl}/statistics`)
      .pipe(
        catchError(error => {
          console.error('Error getting statistics:', error);
          return of({
            prepareData: 0,
            creation: 0,
            assignment: 0,
            businessLog: 0,
            comment: 0,
            closing: 0,
            completed: 0,
            failed: 0,
            inProgress: 0
          });
        })
      );
  }
  
  refreshStatistics(): void {
    this.getStatistics().subscribe({
      next: (stats) => this.statisticsSubject.next(stats),
      error: (error) => console.error('Error refreshing statistics:', error)
    });
  }
}