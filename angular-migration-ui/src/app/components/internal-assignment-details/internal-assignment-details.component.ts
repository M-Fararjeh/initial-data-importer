import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { InternalMigrationService, InternalAssignmentDetail, PaginatedResponse, ImportResponse } from '../../services/internal-migration.service';

@Component({
  selector: 'app-internal-assignment-details',
  templateUrl: './internal-assignment-details.component.html',
  styleUrls: ['./internal-assignment-details.component.css']
})
export class InternalAssignmentDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  assignments: InternalAssignmentDetail[] = [];
  filteredAssignments: InternalAssignmentDetail[] = [];
  isLoading = false;
  selectedAssignments: InternalAssignmentDetail[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  searchTerm = '';
  
  // Search debounce
  private searchTimeout: any;
  
  // Server-side pagination
  currentPage = 1;
  pageSize = 50;
  totalPages = 1;
  totalElements = 0;
  hasNext = false;
  hasPrevious = false;
  
  constructor(private internalMigrationService: InternalMigrationService) {}
  
  ngOnInit(): void {
    console.log('InternalAssignmentDetailsComponent initialized');
    this.loadAssignmentMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadAssignmentMigrations(): void {
    console.log('Loading internal assignment phase migrations...');
    this.isLoading = true;
    
    this.internalMigrationService.getAssignmentDetails(0, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Internal assignment migrations loaded:', response);
          this.assignments = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.hasNext = response.hasNext || false;
          this.hasPrevious = response.hasPrevious || false;
          this.currentPage = (response.currentPage || 0) + 1;
          
          this.filteredAssignments = this.assignments;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading internal assignment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadPage(page: number): void {
    if (this.isLoading) return;
    
    console.log('Loading page:', page);
    this.isLoading = true;
    
    this.internalMigrationService.getAssignmentDetails(page - 1, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.assignments = response.content || [];
          this.currentPage = page;
          this.filteredAssignments = this.assignments;
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
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 1;
      this.loadAssignmentMigrations();
    }, 500);
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1;
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
  
  toggleSelection(assignment: InternalAssignmentDetail): void {
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
    
    if (!confirm(`Execute internal assignment for ${this.selectedAssignments.length} selected records?`)) {
      return;
    }
    
    console.log('Executing internal assignment for selected records:', this.selectedAssignments);
    this.isLoading = true;
    
    this.internalMigrationService.executeAssignmentForSpecific(
      this.selectedAssignments.map(a => a.transactionGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Internal assignment execution completed:', response);
        this.isLoading = false;
        this.loadAssignmentMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Internal assignment completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Internal assignment completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing internal assignment:', error);
        this.isLoading = false;
        alert('Error executing internal assignment. Please check the logs.');
      }
    });
  }
  
  executeAssignmentForSingle(assignment: InternalAssignmentDetail): void {
    if (!confirm(`Execute internal assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    console.log('Executing internal assignment for single record:', assignment);
    this.isLoading = true;
    
    this.internalMigrationService.executeAssignmentForSpecific([assignment.transactionGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single internal assignment execution completed:', response);
          this.isLoading = false;
          this.loadAssignmentMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Internal assignment completed successfully.');
          } else {
            alert(`Internal assignment failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single internal assignment:', error);
          this.isLoading = false;
          alert('Error executing internal assignment. Please check the logs.');
        }
      });
  }
  
  retryFailedAssignment(assignment: InternalAssignmentDetail): void {
    if (!confirm(`Retry internal assignment for transaction: ${assignment.transactionGuid}?`)) {
      return;
    }
    
    this.executeAssignmentForSingle(assignment);
  }
  
  trackByGuid(index: number, item: InternalAssignmentDetail): string {
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
  
  formatDate(dateString: string | undefined): string {
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