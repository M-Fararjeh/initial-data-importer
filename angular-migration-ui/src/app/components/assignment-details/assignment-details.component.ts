import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MigrationService, ImportResponse } from '../../services/migration.service';

export interface AssignmentMigration {
  id: number;
  correspondenceGuid: string;
  transactionGuid: string;
  fromUserName: string;
  toUserName: string;
  actionDate: string;
  decisionGuid: string;
  notes: string;
  migrateStatus: string;
  createdDocumentId?: string;
  retryCount: number;
  lastModifiedDate: string;
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  departmentCode?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-assignment-details',
  templateUrl: './assignment-details.component.html',
  styleUrls: ['./assignment-details.component.css']
})
export class AssignmentDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  assignments: AssignmentMigration[] = [];
  filteredAssignments: AssignmentMigration[] = [];
  allAssignments: AssignmentMigration[] = []; // Keep full dataset for client-side filtering
  isLoading = false;
  selectedAssignments: AssignmentMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  searchTerm = '';
  
  // Search debounce
  private searchTimeout: any;
  
  // Server-side pagination
  currentPage = 1;
  pageSize = 50; // Increased for better performance
  totalPages = 1;
  totalElements = 0;
  hasNext = false;
  hasPrevious = false;
  
  constructor(private migrationService: MigrationService) {}
  
  ngOnInit(): void {
    console.log('AssignmentDetailsComponent initialized');
    this.loadAssignmentMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadAssignmentMigrations(): void {
    console.log('Loading assignment phase migrations...');
    this.isLoading = true;
    
    // Load first page
    this.migrationService.getAssignmentMigrations(0, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Assignment migrations loaded:', response);
          this.assignments = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.hasNext = response.hasNext || false;
          this.hasPrevious = response.hasPrevious || false;
          this.currentPage = (response.currentPage || 0) + 1; // Convert to 1-based
          
          this.filteredAssignments = this.assignments;
          this.clearSelection();
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading assignment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadPage(page: number): void {
    if (this.isLoading) return;
    
    console.log('Loading page:', page);
    this.isLoading = true;
    
    this.migrationService.getAssignmentMigrations(page - 1, this.pageSize, this.statusFilter, this.searchTerm) // Convert to 0-based
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.assignments = response.content || [];
          this.currentPage = page;
          this.filteredAssignments = this.assignments;
          this.clearSelection();
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading page:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    // Debounce search to avoid too many API calls
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 1; // Reset to first page when filtering
      this.loadAssignmentMigrations();
    }, 500); // 500ms debounce
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1; // Reset to first page when filtering
    this.loadAssignmentMigrations();
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  clearFilters(): void {
    this.statusFilter = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.loadAssignmentMigrations();
  }
  
  toggleSelection(assignment: AssignmentMigration): void {
    assignment.selected = !assignment.selected;
    this.updateSelectedAssignments();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.filteredAssignments.forEach(a => a.selected = this.allSelected);
    this.updateSelectedAssignments();
  }
  
  updateSelectedAssignments(): void {
    this.selectedAssignments = this.assignments.filter(a => a.selected);
    this.allSelected = this.filteredAssignments.length > 0 && 
                      this.filteredAssignments.every(a => a.selected);
  }
  
  clearSelection(): void {
    this.assignments.forEach(a => a.selected = false);
    this.selectedAssignments = [];
    this.allSelected = false;
  }
  
  executeAssignmentForSelected(): void {
    if (this.selectedAssignments.length === 0) {
      alert('Please select at least one assignment to execute.');
      return;
    }
    
    if (!confirm(`Execute assignment for ${this.selectedAssignments.length} selected records?`)) {
      return;
    }
    
    console.log('Executing assignment for selected records:', this.selectedAssignments);
    this.isLoading = true;
    
    this.migrationService.executeAssignmentForSpecific(
      this.selectedAssignments.map(a => a.transactionGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Assignment execution completed:', response);
        this.isLoading = false;
        this.loadAssignmentMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Assignment completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Assignment completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing assignment:', error);
        this.isLoading = false;
        alert('Error executing assignment. Please check the logs.');
      }
    });
  }
  
  executeAssignmentForSingle(assignment: AssignmentMigration): void {
    if (!confirm(`Execute assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    console.log('Executing assignment for single record:', assignment);
    this.isLoading = true;
    
    this.migrationService.executeAssignmentForSpecific([assignment.transactionGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single assignment execution completed:', response);
          this.isLoading = false;
          this.loadAssignmentMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Assignment completed successfully.');
          } else {
            alert(`Assignment failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single assignment:', error);
          this.isLoading = false;
          alert('Error executing assignment. Please check the logs.');
        }
      });
  }
  
  retryFailedAssignment(assignment: AssignmentMigration): void {
    if (!confirm(`Retry assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    this.executeAssignmentForSingle(assignment);
  }
  
  trackByGuid(index: number, item: AssignmentMigration): string {
    return item.transactionGuid;
  }
  
  truncateText(text: string, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
  
  getStatusIcon(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return '✅';
      case 'FAILED':
        return '❌';
      case 'PENDING':
        return '⏳';
      default:
        return '⭕';
    }
  }
  
  getStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  }
  
  // Pagination methods
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.loadPage(page);
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
  
}