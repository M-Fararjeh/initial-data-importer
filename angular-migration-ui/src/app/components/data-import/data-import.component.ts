import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DataImportService, ImportResponse } from '../../services/data-import.service';

export interface ImportEntity {
  id: string;
  name: string;
  description: string;
  icon: string;
  endpoint: string;
  status: 'pending' | 'importing' | 'completed' | 'error';
  lastResult?: ImportResponse;
  order: number;
}

export interface ImportLog {
  timestamp: Date;
  type: 'info' | 'success' | 'error' | 'warning';
  message: string;
  entity?: string;
}

export interface OverallProgress {
  total: number;
  completed: number;
  inProgress: number;
  failed: number;
}

@Component({
  selector: 'app-data-import',
  templateUrl: './data-import.component.html',
  styleUrls: ['./data-import.component.css']
})
export class DataImportComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  isLoading = false;
  currentOperation = '';
  loadingProgress = 0;
  
  importLogs: ImportLog[] = [];
  overallProgress: OverallProgress = {
    total: 0,
    completed: 0,
    inProgress: 0,
    failed: 0
  };
  
  basicEntities: ImportEntity[] = [
    {
      id: 'classifications',
      name: 'Classifications',
      description: 'Document classification categories and types',
      icon: '📂',
      endpoint: 'classifications',
      status: 'pending',
      order: 1
    },
    {
      id: 'contacts',
      name: 'Contacts',
      description: 'Contact information and external parties',
      icon: '👥',
      endpoint: 'contacts',
      status: 'pending',
      order: 2
    },
    {
      id: 'decisions',
      name: 'Decisions',
      description: 'Decision types and workflow actions',
      icon: '⚖️',
      endpoint: 'decisions',
      status: 'pending',
      order: 3
    },
    {
      id: 'departments',
      name: 'Departments',
      description: 'Organizational departments and units',
      icon: '🏢',
      endpoint: 'departments',
      status: 'pending',
      order: 4
    },
    {
      id: 'forms',
      name: 'Forms',
      description: 'Document forms and templates',
      icon: '📋',
      endpoint: 'forms',
      status: 'pending',
      order: 5
    },
    {
      id: 'form-types',
      name: 'Form Types',
      description: 'Form type definitions and categories',
      icon: '📝',
      endpoint: 'form-types',
      status: 'pending',
      order: 6
    },
    {
      id: 'importance',
      name: 'Importance',
      description: 'Document importance levels',
      icon: '⭐',
      endpoint: 'importance',
      status: 'pending',
      order: 7
    },
    {
      id: 'positions',
      name: 'Positions',
      description: 'Job positions and organizational roles',
      icon: '💼',
      endpoint: 'positions',
      status: 'pending',
      order: 8
    },
    {
      id: 'pos-roles',
      name: 'Position Roles',
      description: 'Position-role mappings and assignments',
      icon: '🔗',
      endpoint: 'pos-roles',
      status: 'pending',
      order: 9
    },
    {
      id: 'priority',
      name: 'Priority',
      description: 'Document priority levels and urgency',
      icon: '🚨',
      endpoint: 'priority',
      status: 'pending',
      order: 10
    },
    {
      id: 'roles',
      name: 'Roles',
      description: 'User roles and permissions',
      icon: '👤',
      endpoint: 'roles',
      status: 'pending',
      order: 11
    },
    {
      id: 'secrecy',
      name: 'Secrecy',
      description: 'Security classification levels',
      icon: '🔒',
      endpoint: 'secrecy',
      status: 'pending',
      order: 12
    },
    {
      id: 'user-positions',
      name: 'User Positions',
      description: 'User-position assignments and mappings',
      icon: '👨‍💼',
      endpoint: 'user-positions',
      status: 'pending',
      order: 13
    },
    {
      id: 'users',
      name: 'Users',
      description: 'System users and account information',
      icon: '👨‍👩‍👧‍👦',
      endpoint: 'users',
      status: 'pending',
      order: 14
    }
  ];
  
  correspondenceImport: ImportEntity = {
    id: 'correspondences',
    name: 'Correspondences',
    description: 'Main correspondence records',
    icon: '📄',
    endpoint: 'correspondences',
    status: 'pending',
    order: 15
  };
  
  relatedDataImport: ImportEntity = {
    id: 'all-correspondences-with-related',
    name: 'All Related Data',
    description: 'Attachments, comments, transactions, etc.',
    icon: '📦',
    endpoint: 'all-correspondences-with-related',
    status: 'pending',
    order: 16
  };
  
  constructor(private dataImportService: DataImportService) {}
  
  ngOnInit(): void {
    console.log('DataImportComponent initialized');
    this.addLog('info', 'Data Import page loaded');
    this.updateOverallProgress();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  importEntity(entity: ImportEntity): void {
    if (this.isLoading || entity.status === 'importing') {
      return;
    }
    
    console.log('Importing entity:', entity.name);
    this.isLoading = true;
    this.currentOperation = `Importing ${entity.name}...`;
    this.loadingProgress = 0;
    entity.status = 'importing';
    
    this.addLog('info', `Starting import of ${entity.name}`);
    
    // Simulate progress updates
    const progressInterval = setInterval(() => {
      if (this.loadingProgress < 90) {
        this.loadingProgress += Math.random() * 20;
      }
    }, 500);
    
    this.dataImportService.importEntity(entity.endpoint)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          clearInterval(progressInterval);
          this.loadingProgress = 100;
          
          console.log(`${entity.name} import completed:`, response);
          entity.lastResult = response;
          
          if (response.status === 'SUCCESS') {
            entity.status = 'completed';
            this.addLog('success', `${entity.name} imported successfully: ${response.successfulImports} records`);
          } else if (response.status === 'PARTIAL_SUCCESS') {
            entity.status = 'completed';
            this.addLog('warning', `${entity.name} partially imported: ${response.successfulImports} success, ${response.failedImports} failed`);
          } else {
            entity.status = 'error';
            this.addLog('error', `${entity.name} import failed: ${response.message}`);
          }
          
          this.isLoading = false;
          this.updateOverallProgress();
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error(`Error importing ${entity.name}:`, error);
          entity.status = 'error';
          entity.lastResult = {
            status: 'ERROR',
            message: error.message || 'Unknown error',
            totalRecords: 0,
            successfulImports: 0,
            failedImports: 0,
            errors: [error.message || 'Unknown error']
          };
          
          this.addLog('error', `${entity.name} import failed: ${error.message}`);
          this.isLoading = false;
          this.updateOverallProgress();
        }
      });
  }
  
  importAllBasicEntities(): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm('Import all basic entities? This will import all 14 entity types sequentially.')) {
      return;
    }
    
    console.log('Starting import of all basic entities');
    this.isLoading = true;
    this.currentOperation = 'Importing all basic entities...';
    this.loadingProgress = 0;
    
    this.addLog('info', 'Starting bulk import of all basic entities');
    
    this.dataImportService.importAllBasicEntities()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('All basic entities import completed:', response);
          
          // Update all entity statuses
          this.basicEntities.forEach(entity => {
            entity.status = 'completed';
            entity.lastResult = response; // Simplified - in real scenario, you'd want individual results
          });
          
          this.loadingProgress = 100;
          
          if (response.status === 'SUCCESS') {
            this.addLog('success', `All basic entities imported successfully: ${response.successfulImports} total records`);
          } else {
            this.addLog('warning', `Basic entities import completed with some issues: ${response.successfulImports} success, ${response.failedImports} failed`);
          }
          
          this.isLoading = false;
          this.updateOverallProgress();
        },
        error: (error) => {
          console.error('Error importing all basic entities:', error);
          this.addLog('error', `Bulk import failed: ${error.message}`);
          this.isLoading = false;
          this.updateOverallProgress();
        }
      });
  }
  
  importCorrespondences(): void {
    if (this.isLoading) {
      return;
    }
    
    console.log('Importing correspondences');
    this.isLoading = true;
    this.currentOperation = 'Importing correspondences...';
    this.loadingProgress = 0;
    this.correspondenceImport.status = 'importing';
    
    this.addLog('info', 'Starting correspondence import');
    
    const progressInterval = setInterval(() => {
      if (this.loadingProgress < 90) {
        this.loadingProgress += Math.random() * 15;
      }
    }, 1000);
    
    this.dataImportService.importCorrespondences()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          clearInterval(progressInterval);
          this.loadingProgress = 100;
          
          console.log('Correspondences import completed:', response);
          this.correspondenceImport.lastResult = response;
          
          if (response.status === 'SUCCESS') {
            this.correspondenceImport.status = 'completed';
            this.addLog('success', `Correspondences imported successfully: ${response.successfulImports} records`);
          } else if (response.status === 'PARTIAL_SUCCESS') {
            this.correspondenceImport.status = 'completed';
            this.addLog('warning', `Correspondences partially imported: ${response.successfulImports} success, ${response.failedImports} failed`);
          } else {
            this.correspondenceImport.status = 'error';
            this.addLog('error', `Correspondences import failed: ${response.message}`);
          }
          
          this.isLoading = false;
          this.updateOverallProgress();
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error('Error importing correspondences:', error);
          this.correspondenceImport.status = 'error';
          this.addLog('error', `Correspondences import failed: ${error.message}`);
          this.isLoading = false;
          this.updateOverallProgress();
        }
      });
  }
  
  importAllCorrespondencesWithRelated(): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm('Import all correspondences with related data? This may take a long time for large datasets.')) {
      return;
    }
    
    console.log('Importing all correspondences with related data');
    this.isLoading = true;
    this.currentOperation = 'Importing correspondences with related data...';
    this.loadingProgress = 0;
    this.relatedDataImport.status = 'importing';
    
    this.addLog('info', 'Starting bulk import of correspondences with related data');
    
    const progressInterval = setInterval(() => {
      if (this.loadingProgress < 85) {
        this.loadingProgress += Math.random() * 10;
      }
    }, 2000);
    
    this.dataImportService.importAllCorrespondencesWithRelated()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          clearInterval(progressInterval);
          this.loadingProgress = 100;
          
          console.log('All correspondences with related data import completed:', response);
          this.relatedDataImport.lastResult = response;
          
          if (response.status === 'SUCCESS') {
            this.relatedDataImport.status = 'completed';
            this.addLog('success', `All correspondence data imported successfully: ${response.successfulImports} total records`);
          } else if (response.status === 'PARTIAL_SUCCESS') {
            this.relatedDataImport.status = 'completed';
            this.addLog('warning', `Correspondence data partially imported: ${response.successfulImports} success, ${response.failedImports} failed`);
          } else {
            this.relatedDataImport.status = 'error';
            this.addLog('error', `Correspondence data import failed: ${response.message}`);
          }
          
          this.isLoading = false;
          this.updateOverallProgress();
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error('Error importing all correspondences with related data:', error);
          this.relatedDataImport.status = 'error';
          this.addLog('error', `Bulk correspondence import failed: ${error.message}`);
          this.isLoading = false;
          this.updateOverallProgress();
        }
      });
  }
  
  refreshAllStatus(): void {
    console.log('Refreshing all import status');
    this.addLog('info', 'Refreshing import status for all entities');
    this.updateOverallProgress();
  }
  
  updateOverallProgress(): void {
    const allEntities = [...this.basicEntities, this.correspondenceImport, this.relatedDataImport];
    
    this.overallProgress.total = allEntities.length;
    this.overallProgress.completed = allEntities.filter(e => e.status === 'completed').length;
    this.overallProgress.inProgress = allEntities.filter(e => e.status === 'importing').length;
    this.overallProgress.failed = allEntities.filter(e => e.status === 'error').length;
  }
  
  getOverallProgressPercentage(): number {
    if (this.overallProgress.total === 0) return 0;
    return Math.round((this.overallProgress.completed / this.overallProgress.total) * 100);
  }
  
  getCompletedBasicEntities(): number {
    return this.basicEntities.filter(e => e.status === 'completed').length;
  }
  
  addLog(type: 'info' | 'success' | 'error' | 'warning', message: string, entity?: string): void {
    this.importLogs.push({
      timestamp: new Date(),
      type: type,
      message: message,
      entity: entity
    });
    
    // Keep only last 50 log entries
    if (this.importLogs.length > 50) {
      this.importLogs = this.importLogs.slice(-50);
    }
  }
  
  clearLogs(): void {
    this.importLogs = [];
    this.addLog('info', 'Import log cleared');
  }
  
  // Styling helper methods
  getEntityCardClass(entity: ImportEntity): string {
    switch (entity.status) {
      case 'completed':
        return 'border-green-200 bg-green-50';
      case 'importing':
        return 'border-blue-200 bg-blue-50';
      case 'error':
        return 'border-red-200 bg-red-50';
      default:
        return 'border-gray-200 bg-white';
    }
  }
  
  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'importing':
        return 'bg-blue-100 text-blue-800';
      case 'error':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getEntityButtonClass(entity: ImportEntity): string {
    if (entity.status === 'importing' || this.isLoading) {
      return 'bg-gray-300 text-gray-500 cursor-not-allowed';
    }
    
    switch (entity.status) {
      case 'completed':
        return 'bg-green-600 text-white hover:bg-green-700';
      case 'error':
        return 'bg-red-600 text-white hover:bg-red-700';
      default:
        return 'bg-indigo-600 text-white hover:bg-indigo-700';
    }
  }
  
  getEntityButtonText(entity: ImportEntity): string {
    if (entity.status === 'importing') {
      return 'Importing...';
    } else if (entity.status === 'completed') {
      return 'Re-import';
    } else if (entity.status === 'error') {
      return 'Retry Import';
    } else {
      return 'Import';
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