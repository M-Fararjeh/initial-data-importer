import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

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
  
  constructor(private http: HttpClient) {}
  
  // Phase execution methods
  prepareData(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/prepare-data`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  executeCreation(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/creation`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  executeAssignment(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/assignment`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  executeBusinessLog(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/business-log`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  executeComment(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/comment`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  executeClosing(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/closing`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  retryFailed(): Observable<ImportResponse> {
    return this.http.post<ImportResponse>(`${this.baseUrl}/retry-failed`, {})
      .pipe(tap(() => this.refreshStatistics()));
  }
  
  // Statistics methods
  getStatistics(): Observable<MigrationStatistics> {
    return this.http.get<MigrationStatistics>(`${this.baseUrl}/statistics`);
  }
  
  refreshStatistics(): void {
    this.getStatistics().subscribe({
      next: (stats) => this.statisticsSubject.next(stats),
      error: (error) => console.error('Error refreshing statistics:', error)
    });
  }
  
  // Initialize statistics on service creation
  ngOnInit(): void {
    this.refreshStatistics();
  }
}