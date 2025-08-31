import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

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
  
  private baseUrl = 'http://localhost:8080/api/incoming-migration';
  
  private statisticsSubject = new BehaviorSubject<MigrationStatistics | null>(null);
  public statistics$ = this.statisticsSubject.asObservable();
  
  constructor(private http: HttpClient) {
    console.log('MigrationService initialized');
    // Initialize with default statistics
    this.statisticsSubject.next({
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
  }
  
  // Phase execution methods
  prepareData(): Observable<ImportResponse> {
    console.log('Calling prepareData API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/prepare-data`, {})
      .pipe(
        tap((response) => {
          console.log('PrepareData response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in prepareData:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to prepare data: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeCreation(): Observable<ImportResponse> {
    console.log('Calling executeCreation API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation`, {})
      .pipe(
        tap((response) => {
          console.log('ExecuteCreation response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeCreation:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute creation: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeAssignment(): Observable<ImportResponse> {
    console.log('Calling executeAssignment API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment`, {})
      .pipe(
        tap((response) => {
          console.log('ExecuteAssignment response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeAssignment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute assignment: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeBusinessLog(): Observable<ImportResponse> {
    console.log('Calling executeBusinessLog API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log`, {})
      .pipe(
        tap((response) => {
          console.log('ExecuteBusinessLog response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeBusinessLog:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute business log: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeComment(): Observable<ImportResponse> {
    console.log('Calling executeComment API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/comment`, {})
      .pipe(
        tap((response) => {
          console.log('ExecuteComment response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeComment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute comment: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeClosing(): Observable<ImportResponse> {
    console.log('Calling executeClosing API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing`, {})
      .pipe(
        tap((response) => {
          console.log('ExecuteClosing response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeClosing:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute closing: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  retryFailed(): Observable<ImportResponse> {
    console.log('Calling retryFailed API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/retry-failed`, {})
      .pipe(
        tap((response) => {
          console.log('RetryFailed response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in retryFailed:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to retry failed migrations: ' + (error.message || 'Unknown error'),
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
    console.log('Calling getStatistics API');
    return this.http.get<MigrationStatistics>(`${this.baseUrl}/statistics`)
      .pipe(
        tap((stats) => console.log('Statistics response:', stats)),
        catchError((error: HttpErrorResponse) => {
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
      next: (stats) => {
        console.log('Refreshed statistics:', stats);
        this.statisticsSubject.next(stats);
      },
      error: (error) => console.error('Error refreshing statistics:', error)
    });
  }
}