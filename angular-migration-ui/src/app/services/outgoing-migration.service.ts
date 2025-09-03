import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';

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
  approval: number;
  businessLog: number;
  comment: number;
  closing: number;
  completed: number;
  failed: number;
  inProgress: number;
}

export interface OutgoingCreationMigration {
  id: number;
  correspondenceGuid: string;
  currentPhase: string;
  phaseStatus: string;
  creationStep: string;
  creationStatus: string;
  creationError?: string;
  createdDocumentId?: string;
  batchId?: string;
  retryCount: number;
  startedAt: string;
  lastModifiedDate: string;
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  creationUserName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OutgoingMigrationService {
  
  private baseUrl = 'http://localhost:8080/api/outgoing-migration';
  
  private statisticsSubject = new BehaviorSubject<MigrationStatistics | null>(null);
  public statistics$ = this.statisticsSubject.asObservable();
  
  constructor(private http: HttpClient) {
    console.log('OutgoingMigrationService initialized');
    // Initialize with default statistics
    this.statisticsSubject.next({
      prepareData: 0,
      creation: 0,
      assignment: 0,
      approval: 0,
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
    console.log('Calling outgoing prepareData API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/prepare-data`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing PrepareData response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing prepareData:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to prepare outgoing data: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeCreation(): Observable<ImportResponse> {
    console.log('Calling outgoing executeCreation API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteCreation response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeCreation:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing creation: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeAssignment(): Observable<ImportResponse> {
    console.log('Calling outgoing executeAssignment API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteAssignment response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeAssignment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing assignment: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeApproval(): Observable<ImportResponse> {
    console.log('Calling outgoing executeApproval API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/approval`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteApproval response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeApproval:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing approval: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeBusinessLog(): Observable<ImportResponse> {
    console.log('Calling outgoing executeBusinessLog API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteBusinessLog response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeBusinessLog:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing business log: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeComment(): Observable<ImportResponse> {
    console.log('Calling outgoing executeComment API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/comment`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteComment response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeComment:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing comment: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  executeClosing(): Observable<ImportResponse> {
    console.log('Calling outgoing executeClosing API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing ExecuteClosing response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing executeClosing:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing closing: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  retryFailed(): Observable<ImportResponse> {
    console.log('Calling outgoing retryFailed API');
    return this.http.post<ImportResponse>(`${this.baseUrl}/retry-failed`, {})
      .pipe(
        tap((response) => {
          console.log('Outgoing RetryFailed response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in outgoing retryFailed:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to retry failed outgoing migrations: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Creation phase specific methods
  getCreationMigrations(): Observable<OutgoingCreationMigration[]> {
    console.log('Calling getOutgoingCreationMigrations API');
    return this.http.get<any>(`${this.baseUrl}/creation/details`)
      .pipe(
        tap((response) => console.log('Outgoing creation migrations response:', response)),
        map((response: any) => {
          // Handle both array response and paginated response
          if (Array.isArray(response)) {
            return response;
          } else if (response && response.content) {
            return response.content;
          } else {
            return [];
          }
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing creation migrations:', error);
          return of([]);
        })
      );
  }
  
  executeCreationForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeOutgoingCreationForSpecific API with GUIDs:', correspondenceGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation/execute-specific`, {
      correspondenceGuids: correspondenceGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteOutgoingCreationForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeOutgoingCreationForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing creation for specific correspondences: ' + (error.message || 'Unknown error'),
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
    console.log('Calling getOutgoingStatistics API at:', `${this.baseUrl}/statistics`);
    return this.http.get<any>(`${this.baseUrl}/statistics`)
      .pipe(
        tap((stats) => console.log('Outgoing statistics response:', stats)),
        map((response: any) => {
          // Ensure all required fields are present
          return {
            prepareData: response.prepareData || 0,
            creation: response.creation || 0,
            assignment: response.assignment || 0,
            approval: response.approval || 0,
            businessLog: response.businessLog || 0,
            comment: response.comment || 0,
            closing: response.closing || 0,
            completed: response.completed || 0,
            failed: response.failed || 0,
            inProgress: response.inProgress || 0
          } as MigrationStatistics;
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing statistics:', error);
          return of({
            prepareData: 0,
            creation: 0,
            assignment: 0,
            approval: 0,
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
        console.log('Refreshed outgoing statistics:', stats);
        this.statisticsSubject.next(stats);
      },
      error: (error) => console.error('Error refreshing outgoing statistics:', error)
    });
  }
  
  // Creation statistics methods
  getCreationStatistics(): Observable<any> {
    console.log('Calling getOutgoingCreationStatistics API');
    return this.http.get<any>(`${this.baseUrl}/creation/statistics`)
      .pipe(
        tap((stats) => console.log('Outgoing creation statistics response:', stats)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing creation statistics:', error);
          return of({
            total: 0,
            completed: 0,
            pending: 0,
            error: 0,
            stepStatistics: []
          });
        })
      );
  }
  
  trackByGuid(index: number, item: OutgoingCreationMigration): string {
    return item.correspondenceGuid;
  }
}