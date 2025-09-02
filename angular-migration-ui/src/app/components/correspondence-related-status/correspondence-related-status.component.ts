import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DataImportService, ImportResponse } from '../../services/data-import.service';

export interface CorrespondenceImportStatus {
  id: number;
  correspondenceGuid: string;
  overallStatus: string;
  attachmentsStatus: string;
  commentsStatus: string;
  copyTosStatus: string;
  currentDepartmentsStatus: string;
  currentPositionsStatus: string;
  currentUsersStatus: string;
  customFieldsStatus: string;
  linksStatus: string;
  sendTosStatus: string;
  transactionsStatus: string;
  attachmentsCount: number;
  commentsCount: number;
  copyTosCount: number;
  currentDepartmentsCount: number;
  currentPositionsCount: number;
  currentUsersCount: number;
  customFieldsCount: number;
  linksCount: number;
  sendTosCount: number;
  transactionsCount: number;
  totalEntitiesCount: number;
  successfulEntitiesCount: number;
  failedEntitiesCount: number;
  retryCount: number;
  startedAt: string;
  completedAt: string;
  lastModifiedDate: string;
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  selected?: boolean;
}

export interface EntityType {
  id: string;
  name: string;
  description: string;
  icon: string;
  endpoint: string;
}

@Component({
  selector: 'app-correspondence-related-status',
  templateUrl: './correspondence-related-status.component.html',
  styleUrls: ['./correspondence-related-status.component.css']
})
export class CorrespondenceRelatedStatusComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  correspondenceStatuses: CorrespondenceImportStatus[] = [];
  filteredStatuses: CorrespondenceImportStatus[] = [];
  isLoading = false;
  selectedCorrespondences: CorrespondenceImportStatus[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  
  // Entity types for individual import
  entityTypes: EntityType[] = [
    { id: 'attachments', name: 'Attachments', description: 'Document attachments and files', icon: '📎', endpoint: 'correspondence-attachments' },
    { id: 'comments', name: 'Comments', description: 'Comments and annotations', icon: '💬', endpoint: 'correspondence-comments' },
    { id: 'copy-tos', name: 'Copy Tos', description: 'Copy recipients', icon: '📧', endpoint: 'correspondence-copy-tos' },
    { id: 'current-departments', name: 'Current Departments', description: 'Current department assignments', icon: '🏢', endpoint: 'correspondence-current-departments' },
    { id: 'current-positions', name: 'Current Positions', description: 'Current position assignments', icon: '💼', endpoint: 'correspondence-current-positions' },
    { id: 'current-users', name: 'Current Users', description: 'Current user assignments', icon: '👤', endpoint: 'correspondence-current-users' },
    { id: 'custom-fields', name: 'Custom Fields', description: 'Custom field data', icon: '🏷️', endpoint: 'correspondence-custom-fields' },
    { id: 'links', name: 'Links', description: 'Document links and references', icon: '🔗', endpoint: 'correspondence-links' },
    { id: 'send-tos', name: 'Send Tos', description: 'Send recipients', icon: '📤', endpoint: 'correspondence-send-tos' },
    { id: 'transactions', name: 'Transactions', description: 'Workflow transactions', icon: '📊', endpoint: 'correspondence-transactions' }
  ];
  
  // Statistics
  statistics: any = null;
  
  constructor(private dataImportService: DataImportService) {}
  
  ngOnInit(): void {
    console.log('CorrespondenceRelatedStatusComponent initialized');
    this.loadCorrespondenceStatuses();
    this.loadStatistics();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadCorrespondenceStatuses(): void {
    console.log('Loading correspondence import statuses...');
    this.isLoading = true;
    
    this.dataImportService.getCorrespondenceImportStatuses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statuses) => {
          console.log('Correspondence statuses loaded:', statuses);
          this.correspondenceStatuses = statuses;
          this.applyFilters();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading correspondence statuses:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadStatistics(): void {
    console.log('Loading correspondence import statistics...');
    
    this.dataImportService.getCorrespondenceImportStatistics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stats) => {
          console.log('Correspondence import statistics loaded:', stats);
          this.statistics = stats;
        },
        error: (error) => {
          console.error('Error loading correspondence import statistics:', error);
        }
      });
  }
  
  applyFilters(): void {
    let filtered = [...this.correspondenceStatuses];
    
    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(status => status.overallStatus === this.statusFilter);
    }
    
    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(status => 
        status.correspondenceGuid.toLowerCase().includes(term) ||
        (status.correspondenceSubject && status.correspondenceSubject.toLowerCase().includes(term)) ||
        (status.correspondenceReferenceNo && status.correspondenceReferenceNo.toLowerCase().includes(term))
      );
    }
    
    this.filteredStatuses = filtered;
    this.updatePagination();
    this.clearSelection();
  }
  
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredStatuses.length / this.pageSize);
    if (this.currentPage > this.totalPages) {
      this.currentPage = 1;
    }
  }
  
  getPaginatedStatuses(): CorrespondenceImportStatus[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredStatuses.slice(startIndex, endIndex);
  }
  
  importAllCorrespondencesWithRelated(): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm('Import all correspondences with related data? This may take a long time for large datasets.')) {
      return;
    }
    
    console.log('Starting bulk import of all correspondences with related data');
    this.isLoading = true;
    
    this.dataImportService.importAllCorrespondencesWithRelatedTracked()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Bulk import completed:', response);
          this.isLoading = false;
          this.loadCorrespondenceStatuses();
          this.loadStatistics();
          
          if (response.status === 'SUCCESS') {
            alert(`Bulk import completed successfully: ${response.successfulImports} correspondences processed.`);
          } else {
            alert(`Bulk import completed with issues: ${response.successfulImports} success, ${response.failedImports} failed.`);
          }
        },
        error: (error) => {
          console.error('Error in bulk import:', error);
          this.isLoading = false;
          alert('Error in bulk import. Please check the logs.');
        }
      });
  }
  
  importRelatedDataForCorrespondence(correspondenceGuid: string): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm(`Import all related data for correspondence: ${correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Importing related data for correspondence:', correspondenceGuid);
    this.isLoading = true;
    
    this.dataImportService.importRelatedDataForCorrespondence(correspondenceGuid)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Related data import completed:', response);
          this.isLoading = false;
          this.loadCorrespondenceStatuses();
          this.loadStatistics();
          
          if (response.success) {
            alert('Related data imported successfully.');
          } else {
            alert(`Related data import failed: ${response.error || 'Unknown error'}`);
          }
        },
        error: (error) => {
          console.error('Error importing related data:', error);
          this.isLoading = false;
          alert('Error importing related data. Please check the logs.');
        }
      });
  }
  
  importSpecificEntityForCorrespondence(correspondenceGuid: string, entityType: EntityType): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm(`Import ${entityType.name} for correspondence: ${correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Importing specific entity for correspondence:', correspondenceGuid, entityType.name);
    this.isLoading = true;
    
    this.dataImportService.importSpecificCorrespondenceEntity(correspondenceGuid, entityType.endpoint)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Specific entity import completed:', response);
          this.isLoading = false;
          this.loadCorrespondenceStatuses();
          
          if (response.status === 'SUCCESS') {
            alert(`${entityType.name} imported successfully: ${response.successfulImports} records.`);
          } else {
            alert(`${entityType.name} import failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error importing specific entity:', error);
          this.isLoading = false;
          alert('Error importing entity. Please check the logs.');
        }
      });
  }
  
  retryFailedImports(): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm('Retry all failed correspondence imports?')) {
      return;
    }
    
    console.log('Retrying failed correspondence imports');
    this.isLoading = true;
    
    this.dataImportService.retryFailedCorrespondenceImports()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Retry completed:', response);
          this.isLoading = false;
          this.loadCorrespondenceStatuses();
          this.loadStatistics();
          
          if (response.status === 'SUCCESS') {
            alert(`Retry completed successfully: ${response.successfulImports} correspondences processed.`);
          } else {
            alert(`Retry completed with issues: ${response.successfulImports} success, ${response.failedImports} failed.`);
          }
        },
        error: (error) => {
          console.error('Error in retry:', error);
          this.isLoading = false;
          alert('Error in retry. Please check the logs.');
        }
      });
  }
  
  resetImportStatus(correspondenceGuid: string): void {
    if (this.isLoading) {
      return;
    }
    
    if (!confirm(`Reset import status for correspondence: ${correspondenceGuid}? This will allow re-importing all related data.`)) {
      return;
    }
    
    console.log('Resetting import status for correspondence:', correspondenceGuid);
    this.isLoading = true;
    
    this.dataImportService.resetCorrespondenceImportStatus(correspondenceGuid)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Reset completed:', response);
          this.isLoading = false;
          this.loadCorrespondenceStatuses();
          
          if (response.success) {
            alert('Import status reset successfully.');
          } else {
            alert(`Reset failed: ${response.error || 'Unknown error'}`);
          }
        },
        error: (error) => {
          console.error('Error resetting status:', error);
          this.isLoading = false;
          alert('Error resetting status. Please check the logs.');
        }
      });
  }
  
  toggleSelection(status: CorrespondenceImportStatus): void {
    status.selected = !status.selected;
    this.updateSelectedCorrespondences();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.getPaginatedStatuses().forEach(s => s.selected = this.allSelected);
    this.updateSelectedCorrespondences();
  }
  
  updateSelectedCorrespondences(): void {
    this.selectedCorrespondences = this.correspondenceStatuses.filter(s => s.selected);
    const paginatedStatuses = this.getPaginatedStatuses();
    this.allSelected = paginatedStatuses.length > 0 && 
                      paginatedStatuses.every(s => s.selected);
  }
  
  clearSelection(): void {
    this.correspondenceStatuses.forEach(s => s.selected = false);
    this.selectedCorrespondences = [];
    this.allSelected = false;
  }
  
  clearFilters(): void {
    this.statusFilter = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.applyFilters();
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }
  
  onSearchChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }
  
  // Utility methods
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
      case 'IN_PROGRESS':
        return '⏳';
      case 'PENDING':
        return '⭕';
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
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getEntityStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  }
  
  getCompletionPercentage(status: CorrespondenceImportStatus): number {
    if (status.totalEntitiesCount === 0) return 0;
    return Math.round((status.successfulEntitiesCount / status.totalEntitiesCount) * 100);
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
  
  trackByGuid(index: number, item: CorrespondenceImportStatus): string {
    return item.correspondenceGuid;
  }
  
  // Helper methods for template
  getEntityStatus(status: CorrespondenceImportStatus, entityId: string): string {
    switch (entityId) {
      case 'attachments': return status.attachmentsStatus;
      case 'comments': return status.commentsStatus;
      case 'copy-tos': return status.copyTosStatus;
      case 'current-departments': return status.currentDepartmentsStatus;
      case 'current-positions': return status.currentPositionsStatus;
      case 'current-users': return status.currentUsersStatus;
      case 'custom-fields': return status.customFieldsStatus;
      case 'links': return status.linksStatus;
      case 'send-tos': return status.sendTosStatus;
      case 'transactions': return status.transactionsStatus;
      default: return 'PENDING';
    }
  }
  
  getEntityCount(status: CorrespondenceImportStatus, entityId: string): number {
    switch (entityId) {
      case 'attachments': return status.attachmentsCount;
      case 'comments': return status.commentsCount;
      case 'copy-tos': return status.copyTosCount;
      case 'current-departments': return status.currentDepartmentsCount;
      case 'current-positions': return status.currentPositionsCount;
      case 'current-users': return status.currentUsersCount;
      case 'custom-fields': return status.customFieldsCount;
      case 'links': return status.linksCount;
      case 'send-tos': return status.sendTosCount;
      case 'transactions': return status.transactionsCount;
      default: return 0;
    }
  }
}