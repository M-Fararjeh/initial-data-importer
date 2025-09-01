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
  isLoading = false;
  selectedAssignments: AssignmentMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  
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
    
    this.migrationService.getAssignmentMigrations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (assignments) => {
          console.log('Assignment migrations loaded:', assignments);
          this.assignments = assignments;
          this.applyFilters();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading assignment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    let filtered = [...this.assignments];
    
    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(a => a.migrateStatus === this.statusFilter);
    }
    
    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(a => 
        a.correspondenceGuid.toLowerCase().includes(term) ||
        a.transactionGuid.toLowerCase().includes(term) ||
        (a.correspondenceSubject && a.correspondenceSubject.toLowerCase().includes(term)) ||
        (a.correspondenceReferenceNo && a.correspondenceReferenceNo.toLowerCase().includes(term)) ||
        (a.fromUserName && a.fromUserName.toLowerCase().includes(term)) ||
        (a.toUserName && a.toUserName.toLowerCase().includes(term))
      );
    }
    
    this.filteredAssignments = filtered;
    this.updatePagination();
    this.clearSelection();
  }
  
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredAssignments.length / this.pageSize);
    if (this.currentPage > this.totalPages) {
      this.currentPage = 1;
    }
  }
  
  getPaginatedAssignments(): AssignmentMigration[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredAssignments.slice(startIndex, endIndex);
  }
  
  toggleSelection(assignment: AssignmentMigration): void {
    assignment.selected = !assignment.selected;
    this.updateSelectedAssignments();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.getPaginatedAssignments().forEach(a => a.selected = this.allSelected);
    this.updateSelectedAssignments();
  }
  
  updateSelectedAssignments(): void {
    this.selectedAssignments = this.assignments.filter(a => a.selected);
    const paginatedAssignments = this.getPaginatedAssignments();
    this.allSelected = paginatedAssignments.length > 0 && 
                      paginatedAssignments.every(a => a.selected);
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
  
  trackByGuid(index: number, item: AssignmentMigration): string {
    return item.transactionGuid;
  }
}