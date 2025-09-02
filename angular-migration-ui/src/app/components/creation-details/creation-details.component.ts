import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MigrationService, ImportResponse } from '../../services/migration.service';

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
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  creationUserName?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-creation-details',
  templateUrl: './creation-details.component.html',
  styleUrls: ['./creation-details.component.css']
})
export class CreationDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  migrations: CreationMigration[] = [];
  filteredMigrations: CreationMigration[] = [];
  isLoading = false;
  selectedMigrations: CreationMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  stepFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  
  constructor(private migrationService: MigrationService) {}
  
  ngOnInit(): void {
    console.log('CreationDetailsComponent initialized');
    this.loadCreationMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadCreationMigrations(): void {
    console.log('Loading creation phase migrations...');
    this.isLoading = true;
    
    this.migrationService.getCreationMigrations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (migrations) => {
          console.log('Creation migrations loaded:', migrations);
          this.migrations = migrations;
          this.applyFilters();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading creation migrations:', error);
          this.isLoading = false;
        }
      });
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
  
  getPaginatedMigrations(): CreationMigration[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredMigrations.slice(startIndex, endIndex);
  }
  
  toggleSelection(migration: CreationMigration): void {
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
    
    if (!confirm(`Execute creation for ${this.selectedMigrations.length} selected correspondences?`)) {
      return;
    }
    
    console.log('Executing creation for selected migrations:', this.selectedMigrations);
    this.isLoading = true;
    
    this.migrationService.executeCreationForSpecific(
      this.selectedMigrations.map(m => m.correspondenceGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Creation execution completed:', response);
        this.isLoading = false;
        this.loadCreationMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Creation completed successfully for ${response.successfulImports} correspondences.`);
        } else {
          alert(`Creation completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing creation:', error);
        this.isLoading = false;
        alert('Error executing creation. Please check the logs.');
      }
    });
  }
  
  executeCreationForSingle(migration: CreationMigration): void {
    if (!confirm(`Execute creation for correspondence: ${migration.correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Executing creation for single migration:', migration);
    this.isLoading = true;
    
    this.migrationService.executeCreationForSpecific([migration.correspondenceGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single creation execution completed:', response);
          this.isLoading = false;
          this.loadCreationMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Creation completed successfully.');
          } else {
            alert(`Creation failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single creation:', error);
          this.isLoading = false;
          alert('Error executing creation. Please check the logs.');
        }
      });
  }
  
  retryFailedCreation(migration: CreationMigration): void {
    if (!confirm(`Retry creation for correspondence: ${migration.correspondenceGuid}?`)) {
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
    const completedSteps = ['GET_DETAILS', 'GET_ATTACHMENTS', 'UPLOAD_MAIN_ATTACHMENT', 
                           'CREATE_CORRESPONDENCE', 'UPLOAD_OTHER_ATTACHMENTS'];
    
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
  
  trackByGuid(index: number, item: CreationMigration): string {
    return item.correspondenceGuid;
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
}