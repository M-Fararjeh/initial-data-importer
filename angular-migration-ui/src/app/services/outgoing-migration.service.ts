import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

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
  
  private baseUrl = `${environment.apiBaseUrl}/data-import/api/outgoing-migration`;
  
  private statisticsSubject = new BehaviorSubject<MigrationStatistics | null>(null);
  public statistics$ = this.statisticsSubject.asObservable();
  
  constructor(private http: HttpClient) {
    console.log('OutgoingMigrationService initialized');
    console.log('Using API Base URL:', environment.apiBaseUrl);
    // Initialize with default statistics
    this.statisticsSubject.next({
      prepareData: 0,
      creation: 0,
      assignment: 0,
      approval: 0,
      businessLog: 0,
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
  
  // Assignment phase methods
  getOutgoingAssignmentMigrations(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getOutgoingAssignmentMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'search:', search);
    
    // Build query parameters
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    params.set('status', status);
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    
    return this.http.get<any>(`${this.baseUrl}/assignment/details?${params.toString()}`)
      .pipe(
        tap((assignments) => console.log('Outgoing assignment migrations response:', assignments)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing assignment migrations:', error);
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
            currentPage: page,
            pageSize: size,
            hasNext: false,
            hasPrevious: false,
            error: error.message
          });
        })
      );
  }
  
  executeOutgoingAssignmentForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeOutgoingAssignmentForSpecific API with GUIDs:', transactionGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment/execute-specific`, {
      transactionGuids: transactionGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteOutgoingAssignmentForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeOutgoingAssignmentForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing assignment for specific transactions: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Approval phase methods
  getOutgoingApprovalMigrations(page: number = 0, size: number = 20, status: string = 'all', step: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getOutgoingApprovalMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'step:', step, 'search:', search);
    
    // Build query parameters
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    params.set('status', status);
    params.set('step', step);
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    
    return this.http.get<any>(`${this.baseUrl}/approval/details?${params.toString()}`)
      .pipe(
        tap((approvals) => console.log('Outgoing approval migrations response:', approvals)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing approval migrations:', error);
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
            currentPage: page,
            pageSize: size,
            hasNext: false,
            hasPrevious: false,
            error: error.message
          });
        })
      );
  }
  
  executeOutgoingApprovalForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeOutgoingApprovalForSpecific API with GUIDs:', correspondenceGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/approval/execute-specific`, {
      correspondenceGuids: correspondenceGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteOutgoingApprovalForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeOutgoingApprovalForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing approval for specific correspondences: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Business log phase methods
  getOutgoingBusinessLogMigrations(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getOutgoingBusinessLogMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'search:', search);
    
    // Build query parameters
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    params.set('status', status);
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    
    return this.http.get<any>(`${this.baseUrl}/business-log/details?${params.toString()}`)
      .pipe(
        tap((businessLogs) => console.log('Outgoing business log migrations response:', businessLogs)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing business log migrations:', error);
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
            currentPage: page,
            pageSize: size,
            hasNext: false,
            hasPrevious: false,
            error: error.message
          });
        })
      );
  }
  
  executeOutgoingBusinessLogForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeOutgoingBusinessLogForSpecific API with GUIDs:', transactionGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log/execute-specific`, {
      transactionGuids: transactionGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteOutgoingBusinessLogForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeOutgoingBusinessLogForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing business log for specific transactions: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Closing phase methods
  getOutgoingClosingMigrations(page: number = 0, size: number = 20, status: string = 'all', needToClose: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getOutgoingClosingMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'needToClose:', needToClose, 'search:', search);
    
    // Build query parameters
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    params.set('status', status);
    params.set('needToClose', needToClose);
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    
    return this.http.get<any>(`${this.baseUrl}/closing/details?${params.toString()}`)
      .pipe(
        tap((closings) => console.log('Outgoing closing migrations response:', closings)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting outgoing closing migrations:', error);
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
            currentPage: page,
            pageSize: size,
            hasNext: false,
            hasPrevious: false,
            needToCloseCount: 0,
            error: error.message
          });
        })
      );
  }
  
  executeOutgoingClosingForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeOutgoingClosingForSpecific API with GUIDs:', correspondenceGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing/execute-specific`, {
      correspondenceGuids: correspondenceGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteOutgoingClosingForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeOutgoingClosingForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute outgoing closing for specific correspondences: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  trackByGuid(index: number, item: OutgoingCreationMigration): string {
    return item.correspondenceGuid;
  }
}