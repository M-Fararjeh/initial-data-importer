import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DataImportService, ImportResponse } from '../../services/data-import.service';

export interface SetupEntity {
  id: string;
  name: string;
  description: string;
  icon: string;
  endpoint: string;
  isImporting: boolean;
  hasError: boolean;
  lastResult?: ImportResponse;
  order: number;
}

export interface SetupLog {
  timestamp: Date;
  type: 'info' | 'success' | 'error' | 'warning';
  message: string;
  entity?: string;
}

@Component({
  selector: 'app-destination-setup',
  templateUrl: './destination-setup.component.html',
  styleUrls: ['./destination-setup.component.css']
})
export class DestinationSetupComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  isLoading = false;
  currentOperation = '';
  loadingProgress = 0;
  
  setupLogs: SetupLog[] = [];
  
  setupEntities: SetupEntity[] = [
    {
      id: 'external-agencies',
      name: 'External Agencies',
      description: 'Import external agency data to destination system',
      icon: '🏛️',
      endpoint: 'external-agencies',
      isImporting: false,
      hasError: false,
      order: 1
    },
    {
      id: 'users-to-destination',
      name: 'Users & Roles',
      description: 'Create users in destination system and assign roles',
      icon: '👥',
      endpoint: 'users-to-destination',
      isImporting: false,
      hasError: false,
      order: 2
    }
  ];
  
  constructor(private dataImportService: DataImportService) {}
  
  ngOnInit(): void {
    console.log('DestinationSetupComponent initialized');
    this.addLog('info', 'Destination Setup page loaded');
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  importExternalAgencies(): void {
    if (this.isLoading) {
      return;
    }
    
    const entity = this.setupEntities.find(e => e.id === 'external-agencies')!;
    
    console.log('Importing external agencies');
    this.isLoading = true;
    this.currentOperation = 'Importing external agencies...';
    this.loadingProgress = 0;
    entity.isImporting = true;
    entity.hasError = false;
    
    this.addLog('info', 'Starting external agencies import to destination system');
    
    const progressInterval = setInterval(() => {
      if (this.loadingProgress < 90) {
        this.loadingProgress += Math.random() * 20;
      }
    }, 500);
    
    this.dataImportService.importExternalAgencies()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          clearInterval(progressInterval);
          this.loadingProgress = 100;
          
          console.log('External agencies import completed:', response);
          entity.lastResult = response;
          entity.isImporting = false;
          
          if (response.status === 'SUCCESS') {
            entity.hasError = false;
            this.addLog('success', `External agencies imported successfully: ${response.successfulImports} records`);
          } else if (response.status === 'PARTIAL_SUCCESS') {
            entity.hasError = false;
            this.addLog('warning', `External agencies partially imported: ${response.successfulImports} success, ${response.failedImports} failed`);
          } else {
            entity.hasError = true;
            this.addLog('error', `External agencies import failed: ${response.message}`);
          }
          
          this.isLoading = false;
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error('Error importing external agencies:', error);
          entity.isImporting = false;
          entity.hasError = true;
          entity.lastResult = {
            status: 'ERROR',
            message: error.message || 'Unknown error',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          };
          
          this.addLog('error', `External agencies import failed: ${error.message}`);
          this.isLoading = false;
        }
      });
  }
  
  importUsersToDestination(): void {
    if (this.isLoading) {
      return;
    }
    
    const entity = this.setupEntities.find(e => e.id === 'users-to-destination')!;
    
    console.log('Importing users to destination system');
    this.isLoading = true;
    this.currentOperation = 'Creating users and assigning roles...';
    this.loadingProgress = 0;
    entity.isImporting = true;
    entity.hasError = false;
    
    this.addLog('info', 'Starting user creation and role assignment in destination system');
    
    const progressInterval = setInterval(() => {
      if (this.loadingProgress < 90) {
        this.loadingProgress += Math.random() * 15;
      }
    }, 1000);
    
    this.dataImportService.importUsersToDestination()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          clearInterval(progressInterval);
          this.loadingProgress = 100;
          
          console.log('Users to destination import completed:', response);
          entity.lastResult = response;
          entity.isImporting = false;
          
          if (response.status === 'SUCCESS') {
            entity.hasError = false;
            this.addLog('success', `Users created successfully: ${response.successfulImports} users with roles assigned`);
          } else if (response.status === 'PARTIAL_SUCCESS') {
            entity.hasError = false;
            this.addLog('warning', `Users partially created: ${response.successfulImports} success, ${response.failedImports} failed`);
          } else {
            entity.hasError = true;
            this.addLog('error', `User creation failed: ${response.message}`);
          }
          
          this.isLoading = false;
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error('Error importing users to destination:', error);
          entity.isImporting = false;
          entity.hasError = true;
          entity.lastResult = {
            status: 'ERROR',
            message: error.message || 'Unknown error',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          };
          
          this.addLog('error', `User creation failed: ${error.message}`);
          this.isLoading = false;
        }
      });
  }
  
  addLog(type: 'info' | 'success' | 'error' | 'warning', message: string, entity?: string): void {
    this.setupLogs.push({
      timestamp: new Date(),
      type: type,
      message: message,
      entity: entity
    });
    
    // Keep only last 50 log entries
    if (this.setupLogs.length > 50) {
      this.setupLogs = this.setupLogs.slice(-50);
    }
  }
  
  clearLogs(): void {
    this.setupLogs = [];
  }
  
  getCompletedSetupEntities(): number {
    return this.setupEntities.filter(entity => 
      entity.lastResult && (entity.lastResult.status === 'SUCCESS' || entity.lastResult.status === 'PARTIAL_SUCCESS')
    ).length;
  }
  
  // Styling helper methods
  getEntityCardClass(entity: SetupEntity): string {
    if (entity.hasError) {
      return 'border-red-200 bg-red-50';
    } else if (entity.isImporting) {
      return 'border-blue-200 bg-blue-50 status-importing';
    } else if (entity.lastResult && (entity.lastResult.status === 'SUCCESS' || entity.lastResult.status === 'PARTIAL_SUCCESS')) {
      return 'border-green-200 bg-green-50';
    } else {
      return 'border-gray-200 bg-white';
    }
  }
  
  getEntityButtonClass(entity: SetupEntity): string {
    if (entity.isImporting || this.isLoading) {
      return 'bg-gray-300 text-gray-500 cursor-not-allowed';
    }
    
    if (entity.hasError) {
      return 'bg-red-600 text-white hover:bg-red-700';
    } else if (entity.lastResult && (entity.lastResult.status === 'SUCCESS' || entity.lastResult.status === 'PARTIAL_SUCCESS')) {
      return 'bg-green-600 text-white hover:bg-green-700';
    } else {
      return 'bg-indigo-600 text-white hover:bg-indigo-700';
    }
  }
  
  getEntityButtonText(entity: SetupEntity): string {
    if (entity.isImporting) {
      return 'Processing...';
    } else if (entity.hasError) {
      return 'Retry Setup';
    } else if (entity.lastResult && (entity.lastResult.status === 'SUCCESS' || entity.lastResult.status === 'PARTIAL_SUCCESS')) {
      return 'Re-run Setup';
    } else {
      return 'Start Setup';
    }
  }
  
  getLogEntryClass(type: string): string {
    switch (type) {
      case 'success':
        return 'bg-green-50 border-l-4 border-green-400';
      case 'error':
        return 'bg-red-50 border-l-4 border-red-400';
      case 'warning':
        return 'bg-yellow-50 border-l-4 border-yellow-400';
      default:
        return 'bg-blue-50 border-l-4 border-blue-400';
    }
  }
  
  getLogTextClass(type: string): string {
    switch (type) {
      case 'success':
        return 'text-green-800';
      case 'error':
        return 'text-red-800';
      case 'warning':
        return 'text-yellow-800';
      default:
        return 'text-blue-800';
    }
  }
  
  getLogIcon(type: string): string {
    switch (type) {
      case 'success':
        return '✅';
      case 'error':
        return '❌';
      case 'warning':
        return '⚠️';
      default:
        return 'ℹ️';
    }
  }
  
  formatDate(date: Date): string {
    return date.toLocaleString();
  }
}