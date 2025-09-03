import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse } from '../../services/outgoing-migration.service';

export interface OutgoingAssignmentMigration {
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
  selector: 'app-outgoing-assignment-details',
  templateUrl: './outgoing-assignment-details.component.html',
  styleUrls: ['./outgoing-assignment-details.component.css']
})
export class OutgoingAssignmentDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  assignments: OutgoingAssignmentMigration[] = [];
  filteredAssignments: OutgoingAssignmentMigration[] = [];
  isLoading = false;
  selectedAssignments: OutgoingAssignmentMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  totalElements = 0;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingAssignmentDetailsComponent initialized');
    this.loadAssignmentMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadAssignmentMigrations(): void {
    console.log('Loading outgoing assignment phase migrations...');
    this.isLoading = true;
    
    // For now, use mock data - implement actual API call when backend is ready
    this.migrationService.getOutgoingAssignmentMigrations(this.currentPage - 1, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Outgoing assignment migrations loaded:', response);
          this.assignments = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          
          this.filteredAssignments = this.assignments;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading outgoing assignment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    this.currentPage = 1;
    this.loadAssignmentMigrations();
  }
  
  onStatusFilterChange(): void {
    this.applyFilters();
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
  
  toggleSelection(assignment: OutgoingAssignmentMigration): void {
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
    
    if (!confirm(`Execute outgoing assignment for ${this.selectedAssignments.length} selected records?`)) {
      return;
    }
    
    console.log('Executing outgoing assignment for selected records:', this.selectedAssignments);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingAssignmentForSpecific(
      this.selectedAssignments.map(a => a.transactionGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing assignment execution completed:', response);
        this.isLoading = false;
        this.loadAssignmentMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Outgoing assignment completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Outgoing assignment completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing outgoing assignment:', error);
        this.isLoading = false;
        alert('Error executing outgoing assignment. Please check the logs.');
      }
    });
  }
  
  executeAssignmentForSingle(assignment: OutgoingAssignmentMigration): void {
    if (!confirm(`Execute outgoing assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    console.log('Executing outgoing assignment for single record:', assignment);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingAssignmentForSpecific([assignment.transactionGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single outgoing assignment execution completed:', response);
          this.isLoading = false;
          this.loadAssignmentMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Outgoing assignment completed successfully.');
          } else {
            alert(`Outgoing assignment failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single outgoing assignment:', error);
          this.isLoading = false;
          alert('Error executing outgoing assignment. Please check the logs.');
        }
      });
  }
  
  retryFailedAssignment(assignment: OutgoingAssignmentMigration): void {
    if (!confirm(`Retry outgoing assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    this.executeAssignmentForSingle(assignment);
  }
  
  trackByGuid(index: number, item: OutgoingAssignmentMigration): string {
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
      this.currentPage = page;
      this.loadAssignmentMigrations();
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