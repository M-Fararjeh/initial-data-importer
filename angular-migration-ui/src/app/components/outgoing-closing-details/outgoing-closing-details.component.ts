import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse } from '../../services/outgoing-migration.service';

export interface OutgoingClosingMigration {
  id: number;
  correspondenceGuid: string;
  isNeedToClose: boolean;
  closingStatus: string;
  closingError?: string;
  createdDocumentId?: string;
  retryCount: number;
  lastModifiedDate: string;
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  correspondenceLastModifiedDate?: string;
  creationUserName?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-outgoing-closing-details',
  templateUrl: './outgoing-closing-details.component.html',
  styleUrls: ['./outgoing-closing-details.component.css']
})
export class OutgoingClosingDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  closings: OutgoingClosingMigration[] = [];
  filteredClosings: OutgoingClosingMigration[] = [];
  isLoading = false;
  selectedClosings: OutgoingClosingMigration[] = [];
  allSelected = false;
  needToCloseCount = 0;
  
  // Filters
  statusFilter = 'all';
  needToCloseFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  totalElements = 0;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingClosingDetailsComponent initialized');
    this.loadClosingMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadClosingMigrations(): void {
    console.log('Loading outgoing closing phase migrations...');
    this.isLoading = true;
    
    this.migrationService.getOutgoingClosingMigrations(this.currentPage - 1, this.pageSize, this.statusFilter, this.needToCloseFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Outgoing closing migrations loaded:', response);
          this.closings = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.needToCloseCount = response.needToCloseCount || 0;
          
          this.filteredClosings = this.closings;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading outgoing closing migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    this.currentPage = 1;
    this.loadClosingMigrations();
  }
  
  onStatusFilterChange(): void {
    this.applyFilters();
  }
  
  onNeedToCloseFilterChange(): void {
    this.applyFilters();
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  clearFilters(): void {
    this.statusFilter = 'all';
    this.needToCloseFilter = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.loadClosingMigrations();
  }
  
  toggleSelection(closing: OutgoingClosingMigration): void {
    closing.selected = !closing.selected;
    this.updateSelectedClosings();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.filteredClosings.forEach(c => c.selected = this.allSelected);
    this.updateSelectedClosings();
  }
  
  updateSelectedClosings(): void {
    this.selectedClosings = this.closings.filter(c => c.selected);
    this.allSelected = this.filteredClosings.length > 0 && 
                      this.filteredClosings.every(c => c.selected);
  }
  
  clearSelection(): void {
    this.closings.forEach(c => c.selected = false);
    this.selectedClosings = [];
    this.allSelected = false;
  }
  
  executeClosingForSelected(): void {
    if (this.selectedClosings.length === 0) {
      alert('Please select at least one correspondence to execute closing.');
      return;
    }
    
    // Filter only those that need to be closed
    const needToCloseSelected = this.selectedClosings.filter(c => c.isNeedToClose);
    
    if (needToCloseSelected.length === 0) {
      alert('None of the selected correspondences need to be closed.');
      return;
    }
    
    if (!confirm(`Execute outgoing closing for ${needToCloseSelected.length} selected correspondences that need to be closed?`)) {
      return;
    }
    
    console.log('Executing outgoing closing for selected records:', needToCloseSelected);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingClosingForSpecific(
      needToCloseSelected.map(c => c.correspondenceGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing closing execution completed:', response);
        this.isLoading = false;
        this.loadClosingMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Outgoing closing completed successfully for ${response.successfulImports} correspondences.`);
        } else {
          alert(`Outgoing closing completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing outgoing closing:', error);
        this.isLoading = false;
        alert('Error executing outgoing closing. Please check the logs.');
      }
    });
  }
  
  executeClosingForSingle(closing: OutgoingClosingMigration): void {
    if (!closing.isNeedToClose) {
      alert('This outgoing correspondence does not need to be closed.');
      return;
    }
    
    if (!confirm(`Execute outgoing closing for correspondence: ${closing.correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Executing outgoing closing for single record:', closing);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingClosingForSpecific([closing.correspondenceGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single outgoing closing execution completed:', response);
          this.isLoading = false;
          this.loadClosingMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Outgoing closing completed successfully.');
          } else {
            alert(`Outgoing closing failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single outgoing closing:', error);
          this.isLoading = false;
          alert('Error executing outgoing closing. Please check the logs.');
        }
      });
  }
  
  retryFailedClosing(closing: OutgoingClosingMigration): void {
    if (!confirm(`Retry outgoing closing for correspondence: ${closing.correspondenceGuid}?`)) {
      return;
    }
    
    this.executeClosingForSingle(closing);
  }
  
  trackByGuid(index: number, item: OutgoingClosingMigration): string {
    return item.correspondenceGuid;
  }
  
  truncateText(text: string, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
  
  getStatusIcon(status: string): string {
    switch (status) {
      case 'COMPLETED':
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
      case 'COMPLETED':
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
      this.loadClosingMigrations();
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