import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { InternalMigrationService, InternalBusinessLogDetail, PaginatedResponse, ImportResponse } from '../../services/internal-migration.service';

@Component({
  selector: 'app-internal-business-log-details',
  templateUrl: './internal-business-log-details.component.html',
  styleUrls: ['./internal-business-log-details.component.css']
})
export class InternalBusinessLogDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  businessLogs: InternalBusinessLogDetail[] = [];
  filteredBusinessLogs: InternalBusinessLogDetail[] = [];
  isLoading = false;
  selectedBusinessLogs: InternalBusinessLogDetail[] = [];
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
    console.log('InternalBusinessLogDetailsComponent initialized');
    this.loadBusinessLogMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadBusinessLogMigrations(): void {
    console.log('Loading internal business log phase migrations...');
    this.isLoading = true;
    
    this.internalMigrationService.getBusinessLogDetails(0, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Internal business log migrations loaded:', response);
          this.businessLogs = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.hasNext = response.hasNext || false;
          this.hasPrevious = response.hasPrevious || false;
          this.currentPage = (response.currentPage || 0) + 1;
          
          this.filteredBusinessLogs = this.businessLogs;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading internal business log migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadPage(page: number): void {
    if (this.isLoading) return;
    
    console.log('Loading page:', page);
    this.isLoading = true;
    
    this.internalMigrationService.getBusinessLogDetails(page - 1, this.pageSize, this.statusFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.businessLogs = response.content || [];
          this.currentPage = page;
          this.filteredBusinessLogs = this.businessLogs;
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
      this.loadBusinessLogMigrations();
    }, 500);
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1;
    this.loadBusinessLogMigrations();
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  clearFilters(): void {
    this.statusFilter = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.loadBusinessLogMigrations();
  }
  
  toggleSelection(businessLog: InternalBusinessLogDetail): void {
    businessLog.selected = !businessLog.selected;
    this.updateSelectedBusinessLogs();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.filteredBusinessLogs.forEach(bl => bl.selected = this.allSelected);
    this.updateSelectedBusinessLogs();
  }
  
  updateSelectedBusinessLogs(): void {
    this.selectedBusinessLogs = this.businessLogs.filter(bl => bl.selected);
    this.allSelected = this.filteredBusinessLogs.length > 0 && 
                      this.filteredBusinessLogs.every(bl => bl.selected);
  }
  
  clearSelection(): void {
    this.businessLogs.forEach(bl => bl.selected = false);
    this.selectedBusinessLogs = [];
    this.allSelected = false;
  }
  
  executeBusinessLogForSelected(): void {
    if (this.selectedBusinessLogs.length === 0) {
      alert('Please select at least one business log to execute.');
      return;
    }
    
    if (!confirm(`Execute internal business log for ${this.selectedBusinessLogs.length} selected records?`)) {
      return;
    }
    
    console.log('Executing internal business log for selected records:', this.selectedBusinessLogs);
    this.isLoading = true;
    
    this.internalMigrationService.executeBusinessLogForSpecific(
      this.selectedBusinessLogs.map(bl => bl.transactionGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Internal business log execution completed:', response);
        this.isLoading = false;
        this.loadBusinessLogMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Internal business log completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Internal business log completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing internal business log:', error);
        this.isLoading = false;
        alert('Error executing internal business log. Please check the logs.');
      }
    });
  }
  
  executeBusinessLogForSingle(businessLog: InternalBusinessLogDetail): void {
    if (!confirm(`Execute internal business log for transaction: ${businessLog.transactionGuid}?`)) {
      return;
    }
    
    console.log('Executing internal business log for single record:', businessLog);
    this.isLoading = true;
    
    this.internalMigrationService.executeBusinessLogForSpecific([businessLog.transactionGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single internal business log execution completed:', response);
          this.isLoading = false;
          this.loadBusinessLogMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Internal business log completed successfully.');
          } else {
            alert(`Internal business log failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single internal business log:', error);
          this.isLoading = false;
          alert('Error executing internal business log. Please check the logs.');
        }
      });
  }
  
  retryFailedBusinessLog(businessLog: InternalBusinessLogDetail): void {
    if (!confirm(`Retry internal business log for transaction: ${businessLog.transactionGuid}?`)) {
      return;
    }
    
    this.executeBusinessLogForSingle(businessLog);
  }
  
  trackByGuid(index: number, item: InternalBusinessLogDetail): string {
    return item.transactionGuid;
  }
  
  truncateText(text: string | undefined, maxLength: number = 50): string {
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
  
  getEventTypeClass(eventName: string | undefined): string {
    if (!eventName) return 'bg-gray-100 text-gray-800';
    
    const lowerEventName = eventName.toLowerCase();
    
    if (lowerEventName.includes('register')) {
      return 'event-register';
    } else if (lowerEventName.includes('forward') || lowerEventName.includes('send')) {
      return 'event-forward';
    } else if (lowerEventName.includes('reply')) {
      return 'event-reply';
    } else if (lowerEventName.includes('close') || lowerEventName.includes('archive')) {
      return 'event-close';
    } else {
      return 'bg-blue-100 text-blue-800';
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