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

export interface CreationMigration {
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
  
  // Creation phase specific methods
  getCreationMigrations(): Observable<CreationMigration[]> {
    console.log('Calling getCreationMigrations API');
    return this.http.get<CreationMigration[]>(`${this.baseUrl}/creation/details`)
      .pipe(
        tap((migrations) => console.log('Creation migrations response:', migrations)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting creation migrations:', error);
          return of([]);
        })
      );
  }
  
  executeCreationForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeCreationForSpecific API with GUIDs:', correspondenceGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation/execute-specific`, {
      correspondenceGuids: correspondenceGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteCreationForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeCreationForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute creation for specific correspondences: ' + (error.message || 'Unknown error'),
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
  
  getProgressPercentage(step: string): number {
    const steps = [
      'GET_DETAILS',
      'GET_ATTACHMENTS', 
      'UPLOAD_MAIN_ATTACHMENT',
      'CREATE_CORRESPONDENCE',
      'UPLOAD_OTHER_ATTACHMENTS',
      'CREATE_PHYSICAL_ATTACHMENT',
      'SET_READY_TO_REGISTER',
      'REGISTER_WITH_REFERENCE',
      'START_WORK',
      'SET_OWNER',
      'COMPLETED'
    ];
    
    const stepIndex = steps.indexOf(step);
    if (stepIndex === -1) return 0;
    
    return Math.round((stepIndex / (steps.length - 1)) * 100);
  }
  
  trackByGuid(index: number, item: CreationMigration): string {
    return item.correspondenceGuid;
  }
  
  // Assignment phase methods
  getAssignmentMigrations(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getAssignmentMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'search:', search);
    
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
        tap((assignments) => console.log('Assignment migrations response:', assignments)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting assignment migrations:', error);
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
  
  executeAssignmentForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeAssignmentForSpecific API with GUIDs:', transactionGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment/execute-specific`, {
      transactionGuids: transactionGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteAssignmentForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeAssignmentForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute assignment for specific transactions: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Business log phase methods
  getBusinessLogMigrations(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getBusinessLogMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'search:', search);
    
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
        tap((businessLogs) => console.log('Business log migrations response:', businessLogs)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting business log migrations:', error);
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
  
  executeBusinessLogForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeBusinessLogForSpecific API with GUIDs:', transactionGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log/execute-specific`, {
      transactionGuids: transactionGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteBusinessLogForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeBusinessLogForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute business log for specific transactions: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Comment phase methods
  getCommentMigrations(page: number = 0, size: number = 20, status: string = 'all', commentType: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getCommentMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'commentType:', commentType, 'search:', search);
    
    // Build query parameters
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    params.set('status', status);
    params.set('commentType', commentType);
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    
    return this.http.get<any>(`${this.baseUrl}/comment/details?${params.toString()}`)
      .pipe(
        tap((comments) => console.log('Comment migrations response:', comments)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting comment migrations:', error);
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
  
  executeCommentForSpecific(commentGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeCommentForSpecific API with GUIDs:', commentGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/comment/execute-specific`, {
      commentGuids: commentGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteCommentForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeCommentForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute comment for specific records: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
  
  // Closing phase methods
  getClosingMigrations(page: number = 0, size: number = 20, status: string = 'all', needToClose: string = 'all', search: string = ''): Observable<any> {
    console.log('Calling getClosingMigrations API with pagination and search - page:', page, 'size:', size, 'status:', status, 'needToClose:', needToClose, 'search:', search);
    
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
        tap((closings) => console.log('Closing migrations response:', closings)),
        catchError((error: HttpErrorResponse) => {
          console.error('Error getting closing migrations:', error);
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
  
  executeClosingForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    console.log('Calling executeClosingForSpecific API with GUIDs:', correspondenceGuids);
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing/execute-specific`, {
      correspondenceGuids: correspondenceGuids
    })
      .pipe(
        tap((response) => {
          console.log('ExecuteClosingForSpecific response:', response);
          this.refreshStatistics();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Error in executeClosingForSpecific:', error);
          return of({
            status: 'ERROR',
            message: 'Failed to execute closing for specific correspondences: ' + (error.message || 'Unknown error'),
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          });
        })
      );
  }
}