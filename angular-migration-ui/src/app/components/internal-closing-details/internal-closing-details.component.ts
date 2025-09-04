import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { InternalMigrationService, InternalClosingDetail, PaginatedResponse, ImportResponse } from '../../services/internal-migration.service';

@Component({
  selector: 'app-internal-closing-details',
  templateUrl: './internal-closing-details.component.html',
  styleUrls: ['./internal-closing-details.component.css']
})
export class InternalClosingDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  closings: InternalClosingDetail[] = [];
  filteredClosings: InternalClosingDetail[] = [];
  isLoading = false;
  selectedClosings: InternalClosingDetail[] = [];
  allSelected = false;
  needToCloseCount = 0;
  
  // Filters
  statusFilter = 'all';
  needToCloseFilter = 'all';
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
    console.log('InternalClosingDetailsComponent initialized');
    this.loadClosingMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadClosingMigrations(): void {
    console.log('Loading internal closing phase migrations...');
    this.isLoading = true;
    
    this.internalMigrationService.getClosingDetails(0, this.pageSize, this.statusFilter, this.needToCloseFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Internal closing migrations loaded:', response);
          this.closings = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.hasNext = response.hasNext || false;
          this.hasPrevious = response.hasPrevious || false;
          this.currentPage = (response.currentPage || 0) + 1;
          this.needToCloseCount = response.needToCloseCount || 0;
          
          this.filteredClosings = this.closings;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading internal closing migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadPage(page: number): void {
    if (this.isLoading) return;
    
    console.log('Loading page:', page);
    this.isLoading = true;
    
    this.internalMigrationService.getClosingDetails(page - 1, this.pageSize, this.statusFilter, this.needToCloseFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.closings = response.content || [];
          this.currentPage = page;
          this.filteredClosings = this.closings;
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
      this.loadClosingMigrations();
    }, 500);
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1;
    this.loadClosingMigrations();
  }
  
  onNeedToCloseFilterChange(): void {
    this.currentPage = 1;
    this.loadClosingMigrations();
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
  
  toggleSelection(closing: InternalClosingDetail): void {
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
    
    if (!confirm(`Execute internal closing for ${needToCloseSelected.length} selected correspondences that need to be closed?`)) {
      return;
    }
    
    console.log('Executing internal closing for selected records:', needToCloseSelected);
    this.isLoading = true;
    
    this.internalMigrationService.executeClosingForSpecific(
      needToCloseSelected.map(c => c.correspondenceGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Internal closing execution completed:', response);
        this.isLoading = false;
        this.loadClosingMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Internal closing completed successfully for ${response.successfulImports} correspondences.`);
        } else {
          alert(`Internal closing completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing internal closing:', error);
        this.isLoading = false;
        alert('Error executing internal closing. Please check the logs.');
      }
    });
  }
  
  executeClosingForSingle(closing: InternalClosingDetail): void {
    if (!closing.isNeedToClose) {
      alert('This internal correspondence does not need to be closed.');
      return;
    }
    
    if (!confirm(`Execute internal closing for correspondence: ${closing.correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Executing internal closing for single record:', closing);
    this.isLoading = true;
    
    this.internalMigrationService.executeClosingForSpecific([closing.correspondenceGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single internal closing execution completed:', response);
          this.isLoading = false;
          this.loadClosingMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Internal closing completed successfully.');
          } else {
            alert(`Internal closing failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single internal closing:', error);
          this.isLoading = false;
          alert('Error executing internal closing. Please check the logs.');
        }
      });
  }
  
  retryFailedClosing(closing: InternalClosingDetail): void {
    if (!confirm(`Retry internal closing for correspondence: ${closing.correspondenceGuid}?`)) {
      return;
    }
    
    this.executeClosingForSingle(closing);
  }
  
  trackByGuid(index: number, item: InternalClosingDetail): string {
    return item.correspondenceGuid;
  }
  
  truncateText(text: string | undefined, maxLength: number = 50): string {
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