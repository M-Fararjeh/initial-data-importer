import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface InternalMigrationStatistics {
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

export interface InternalCreationDetail {
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

export interface InternalApprovalDetail {
  id: number;
  correspondenceGuid: string;
  createdDocumentId?: string;
  approvalStatus: string;
  approvalStep: string;
  approvalError?: string;
  retryCount: number;
  lastModifiedDate: string;
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  creationUserName?: string;
}

export interface InternalAssignmentDetail {
  transactionGuid: string;
  correspondenceGuid: string;
  fromUserName?: string;
  toUserName?: string;
  actionDate?: string;
  decisionGuid?: string;
  notes?: string;
  migrateStatus: string;
  retryCount: number;
  lastModifiedDate: string;
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  createdDocumentId?: string;
  creationUserName?: string;
  departmentCode?: string;
  selected?: boolean;
}

export interface InternalBusinessLogDetail {
  transactionGuid: string;
  correspondenceGuid: string;
  actionId?: number;
  actionEnglishName?: string;
  actionLocalName?: string;
  actionDate?: string;
  fromUserName?: string;
  notes?: string;
  migrateStatus: string;
  retryCount: number;
  lastModifiedDate: string;
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  createdDocumentId?: string;
}

export interface InternalClosingDetail {
  id: number;
  correspondenceGuid: string;
  isNeedToClose: boolean;
  closingStatus: string;
  closingError?: string;
  createdDocumentId?: string;
  retryCount: number;
  lastModifiedDate: string;
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  correspondenceLastModifiedDate?: string;
  creationUserName?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  needToCloseCount?: number;
}

export interface ImportResponse {
  status: string;
  message: string;
  totalRecords: number;
  successfulImports: number;
  failedImports: number;
  errors: string[];
}

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
export class InternalMigrationService {
  private baseUrl = `${environment.apiBaseUrl}/api/internal-migration`;

  constructor(private http: HttpClient) {}

  // Phase execution methods
  prepareData(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/prepare-data`, {});
  }

  executeCreation(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation`, {});
  }

  executeCreationForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation/execute-specific`, {
      correspondenceGuids
    });
  }

  executeAssignment(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment`, {});
  }

  executeAssignmentForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment/execute-specific`, {
      transactionGuids
    });
  }

  executeApproval(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/approval`, {});
  }

  executeApprovalForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/approval/execute-specific`, {
      correspondenceGuids
    });
  }

  executeBusinessLog(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log`, {});
  }

  executeBusinessLogForSpecific(transactionGuids: string[]): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log/execute-specific`, {
      transactionGuids
    });
  }

  executeClosing(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing`, {});
  }

  executeClosingForSpecific(correspondenceGuids: string[]): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing/execute-specific`, {
      correspondenceGuids
    });
  }

  retryFailed(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/retry-failed`, {});
  }

  // Statistics and details methods
  getStatistics(): Observable<InternalMigrationStatistics> {
    return this.http.get<InternalMigrationStatistics>(`${this.baseUrl}/statistics`);
  }

  getCreationDetails(): Observable<{ content: InternalCreationDetail[], totalElements: number }> {
    return this.http.get<{ content: InternalCreationDetail[], totalElements: number }>(`${this.baseUrl}/creation/details`);
  }

  getCreationStatistics(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/creation/statistics`);
  }

  getAssignmentDetails(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<PaginatedResponse<InternalAssignmentDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('status', status)
      .set('search', search);

    return this.http.get<PaginatedResponse<InternalAssignmentDetail>>(`${this.baseUrl}/assignment/details`, { params });
  }

  getApprovalDetails(page: number = 0, size: number = 20, status: string = 'all', step: string = 'all', search: string = ''): Observable<PaginatedResponse<InternalApprovalDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('status', status)
      .set('step', step)
      .set('search', search);

    return this.http.get<PaginatedResponse<InternalApprovalDetail>>(`${this.baseUrl}/approval/details`, { params });
  }

  getBusinessLogDetails(page: number = 0, size: number = 20, status: string = 'all', search: string = ''): Observable<PaginatedResponse<InternalBusinessLogDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('status', status)
      .set('search', search);

    return this.http.get<PaginatedResponse<InternalBusinessLogDetail>>(`${this.baseUrl}/business-log/details`, { params });
  }

  getClosingDetails(page: number = 0, size: number = 20, status: string = 'all', needToClose: string = 'all', search: string = ''): Observable<PaginatedResponse<InternalClosingDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('status', status)
      .set('needToClose', needToClose)
      .set('search', search);

    return this.http.get<PaginatedResponse<InternalClosingDetail>>(`${this.baseUrl}/closing/details`, { params });
  }
}