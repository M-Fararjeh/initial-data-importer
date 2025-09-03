import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse } from '../../services/outgoing-migration.service';

export interface OutgoingCreationStatistics {
  total: number;
  completed: number;
  pending: number;
  error: number;
}

export interface OutgoingStepStatistics {
  step: string;
  count: number;
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
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  creationUserName?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-outgoing-creation-details',
  templateUrl: './outgoing-creation-details.component.html',
  styleUrls: ['./outgoing-creation-details.component.css']
})
export class OutgoingCreationDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  migrations: OutgoingCreationMigration[] = [];
  filteredMigrations: OutgoingCreationMigration[] = [];
  isLoading = false;
  selectedMigrations: OutgoingCreationMigration[] = [];
  allSelected = false;
  statistics: OutgoingCreationStatistics | null = null;
  stepStatistics: OutgoingStepStatistics[] = [];
  
  // Filters
  statusFilter = 'all';
  stepFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingCreationDetailsComponent initialized');
    this.loadCreationMigrations();
    this.loadStatistics();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadCreationMigrations(): void {
    console.log('Loading outgoing creation phase migrations...');
    this.isLoading = true;
    
    this.migrationService.getCreationMigrations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (migrations) => {
          console.log('Outgoing creation migrations loaded:', migrations);
          this.migrations = migrations;
          this.calculateStatistics();
          this.applyFilters();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading outgoing creation migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadStatistics(): void {
    console.log('Loading outgoing creation statistics...');
    
    this.migrationService.getCreationStatistics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stats) => {
          console.log('Outgoing creation statistics loaded:', stats);
          this.statistics = stats;
        },
        error: (error) => {
          console.error('Error loading outgoing creation statistics:', error);
          // Calculate from local data if API fails
          this.calculateStatistics();
        }
      });
  }
  
  calculateStatistics(): void {
    if (this.migrations.length === 0) {
      this.statistics = { total: 0, completed: 0, pending: 0, error: 0 };
      this.stepStatistics = [];
      return;
    }
    
    // Calculate status statistics
    const total = this.migrations.length;
    const completed = this.migrations.filter(m => m.creationStatus === 'COMPLETED').length;
    const pending = this.migrations.filter(m => m.creationStatus === 'PENDING').length;
    const error = this.migrations.filter(m => m.creationStatus === 'ERROR').length;
    
    this.statistics = { total, completed, pending, error };
    
    // Calculate step statistics
    const stepCounts = new Map<string, number>();
    this.migrations.forEach(migration => {
      const step = migration.creationStep || 'UNKNOWN';
      stepCounts.set(step, (stepCounts.get(step) || 0) + 1);
    });
    
    this.stepStatistics = Array.from(stepCounts.entries())
      .map(([step, count]) => ({ step, count }))
      .sort((a, b) => this.getStepOrder(a.step) - this.getStepOrder(b.step));
  }
  
  getStepDisplayName(step: string): string {
    const stepNames: { [key: string]: string } = {
      'GET_DETAILS': 'Get Details',
      'UPLOAD_MAIN_ATTACHMENT': 'Upload Main',
      'CREATE_CORRESPONDENCE': 'Create Corr.',
      'UPLOAD_OTHER_ATTACHMENTS': 'Upload Others',
      'CREATE_PHYSICAL_ATTACHMENT': 'Physical Attach.',
      'COMPLETED': 'Completed'
    };
    return stepNames[step] || step;
  }
  
  getStepOrder(step: string): number {
    const stepOrder: { [key: string]: number } = {
      'GET_DETAILS': 1,
      'UPLOAD_MAIN_ATTACHMENT': 2,
      'CREATE_CORRESPONDENCE': 3,
      'UPLOAD_OTHER_ATTACHMENTS': 4,
      'CREATE_PHYSICAL_ATTACHMENT': 5,
      'COMPLETED': 6
    };
    return stepOrder[step] || 999;
  }
  
  getStepPercentage(count: number): number {
    if (!this.statistics || this.statistics.total === 0) return 0;
    return Math.round((count / this.statistics.total) * 100);
  }
  
  applyFilters(): void {
    let filtered = [...this.migrations];
    
    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(m => m.creationStatus === this.statusFilter);
    }
    
    // Step filter
    if (this.stepFilter !== 'all') {
      filtered = filtered.filter(m => m.creationStep === this.stepFilter);
    }
    
    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(m => 
        m.correspondenceGuid.toLowerCase().includes(term) ||
        (m.correspondenceSubject && m.correspondenceSubject.toLowerCase().includes(term)) ||
        (m.correspondenceReferenceNo && m.correspondenceReferenceNo.toLowerCase().includes(term)) ||
        (m.creationUserName && m.creationUserName.toLowerCase().includes(term))
      );
    }
    
    this.filteredMigrations = filtered;
    this.updatePagination();
    this.clearSelection();
  }
  
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredMigrations.length / this.pageSize);
    if (this.currentPage > this.totalPages) {
      this.currentPage = 1;
    }
  }
  
  getPaginatedMigrations(): OutgoingCreationMigration[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredMigrations.slice(startIndex, endIndex);
  }
  
  toggleSelection(migration: OutgoingCreationMigration): void {
    migration.selected = !migration.selected;
    this.updateSelectedMigrations();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.getPaginatedMigrations().forEach(m => m.selected = this.allSelected);
    this.updateSelectedMigrations();
  }
  
  updateSelectedMigrations(): void {
    this.selectedMigrations = this.migrations.filter(m => m.selected);
    const paginatedMigrations = this.getPaginatedMigrations();
    this.allSelected = paginatedMigrations.length > 0 && 
                      paginatedMigrations.every(m => m.selected);
  }
  
  clearSelection(): void {
    this.migrations.forEach(m => m.selected = false);
    this.selectedMigrations = [];
    this.allSelected = false;
  }
  
  executeCreationForSelected(): void {
    if (this.selectedMigrations.length === 0) {
      alert('Please select at least one correspondence to execute creation.');
      return;
    }
    
    if (!confirm(`Execute outgoing creation for ${this.selectedMigrations.length} selected correspondences?`)) {
      return;
    }
    
    console.log('Executing outgoing creation for selected migrations:', this.selectedMigrations);
    this.isLoading = true;
    
    this.migrationService.executeCreationForSpecific(
      this.selectedMigrations.map(m => m.correspondenceGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing creation execution completed:', response);
        this.isLoading = false;
        this.loadCreationMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Outgoing creation completed successfully for ${response.successfulImports} correspondences.`);
        } else {
          alert(`Outgoing creation completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing outgoing creation:', error);
        this.isLoading = false;
        alert('Error executing outgoing creation. Please check the logs.');
      }
    });
  }
  
  executeCreationForSingle(migration: OutgoingCreationMigration): void {
    if (!confirm(`Execute outgoing creation for correspondence: ${migration.correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Executing outgoing creation for single migration:', migration);
    this.isLoading = true;
    
    this.migrationService.executeCreationForSpecific([migration.correspondenceGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single outgoing creation execution completed:', response);
          this.isLoading = false;
          this.loadCreationMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Outgoing creation completed successfully.');
          } else {
            alert(`Outgoing creation failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single outgoing creation:', error);
          this.isLoading = false;
          alert('Error executing outgoing creation. Please check the logs.');
        }
      });
  }
  
  retryFailedCreation(migration: OutgoingCreationMigration): void {
    if (!confirm(`Retry outgoing creation for correspondence: ${migration.correspondenceGuid}?`)) {
      return;
    }
    
    this.executeCreationForSingle(migration);
  }
  
  getStatusIcon(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return '✅';
      case 'ERROR':
        return '❌';
      case 'PENDING':
        return '⏳';
      default:
        return '⭕';
    }
  }
  
  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'ERROR':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getStepClass(step: string): string {
    const completedSteps = ['GET_DETAILS', 'UPLOAD_MAIN_ATTACHMENT', 'CREATE_CORRESPONDENCE'];
    
    if (step === 'COMPLETED') {
      return 'bg-green-100 text-green-800';
    } else if (completedSteps.includes(step)) {
      return 'bg-blue-100 text-blue-800';
    } else {
      return 'bg-gray-100 text-gray-800';
    }
  }
  
  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  }
  
  truncateText(text: string, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
  
  // Pagination methods
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }
  
  getPaginationPages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);
    
    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }
  
  trackByGuid(index: number, item: OutgoingCreationMigration): string {
    return item.correspondenceGuid;
  }
  
  getProgressPercentage(step: string): number {
    const steps = [
      'GET_DETAILS',
      'UPLOAD_MAIN_ATTACHMENT',
      'CREATE_CORRESPONDENCE',
      'UPLOAD_OTHER_ATTACHMENTS',
      'CREATE_PHYSICAL_ATTACHMENT',
      'COMPLETED'
    ];
    
    const stepIndex = steps.indexOf(step);
    if (stepIndex === -1) return 0;
    
    return Math.round((stepIndex / (steps.length - 1)) * 100);
  }
}