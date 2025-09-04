import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse } from '../../services/outgoing-migration.service';

export interface OutgoingApprovalMigration {
  id: number;
  correspondenceGuid: string;
  approvalStatus: string;
  approvalStep: string;
  approvalError?: string;
  createdDocumentId?: string;
  retryCount: number;
  lastModifiedDate: string;
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  creationUserName?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-outgoing-approval-details',
  templateUrl: './outgoing-approval-details.component.html',
  styleUrls: ['./outgoing-approval-details.component.css']
})
export class OutgoingApprovalDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  approvals: OutgoingApprovalMigration[] = [];
  filteredApprovals: OutgoingApprovalMigration[] = [];
  isLoading = false;
  selectedApprovals: OutgoingApprovalMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  stepFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  totalElements = 0;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingApprovalDetailsComponent initialized');
    this.loadApprovalMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadApprovalMigrations(): void {
    console.log('Loading outgoing approval phase migrations...');
    this.isLoading = true;
    
    this.migrationService.getOutgoingApprovalMigrations(this.currentPage - 1, this.pageSize, this.statusFilter, this.stepFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Outgoing approval migrations loaded:', response);
          this.approvals = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          
          this.filteredApprovals = this.approvals;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading outgoing approval migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    let filtered = [...this.approvals];
    
    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(a => a.approvalStatus === this.statusFilter);
    }
    
    // Step filter
    if (this.stepFilter !== 'all') {
      filtered = filtered.filter(a => a.approvalStep === this.stepFilter);
    }
    
    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(a => 
        a.correspondenceGuid.toLowerCase().includes(term) ||
        (a.correspondenceSubject && a.correspondenceSubject.toLowerCase().includes(term)) ||
        (a.correspondenceReferenceNo && a.correspondenceReferenceNo.toLowerCase().includes(term)) ||
        (a.creationUserName && a.creationUserName.toLowerCase().includes(term))
      );
    }
    
    this.filteredApprovals = filtered;
    this.updatePagination();
    this.clearSelection();
  }
  
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredApprovals.length / this.pageSize);
    if (this.currentPage > this.totalPages) {
      this.currentPage = 1;
    }
  }
  
  getPaginatedApprovals(): OutgoingApprovalMigration[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredApprovals.slice(startIndex, endIndex);
  }
  
  toggleSelection(approval: OutgoingApprovalMigration): void {
    approval.selected = !approval.selected;
    this.updateSelectedApprovals();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.getPaginatedApprovals().forEach(a => a.selected = this.allSelected);
    this.updateSelectedApprovals();
  }
  
  updateSelectedApprovals(): void {
    this.selectedApprovals = this.approvals.filter(a => a.selected);
    const paginatedApprovals = this.getPaginatedApprovals();
    this.allSelected = paginatedApprovals.length > 0 && 
                      paginatedApprovals.every(a => a.selected);
  }
  
  clearSelection(): void {
    this.approvals.forEach(a => a.selected = false);
    this.selectedApprovals = [];
    this.allSelected = false;
  }
  
  executeApprovalForSelected(): void {
    if (this.selectedApprovals.length === 0) {
      alert('Please select at least one correspondence to execute approval.');
      return;
    }
    
    if (!confirm(`Execute outgoing approval for ${this.selectedApprovals.length} selected correspondences?`)) {
      return;
    }
    
    console.log('Executing outgoing approval for selected records:', this.selectedApprovals);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingApprovalForSpecific(
      this.selectedApprovals.map(a => a.correspondenceGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing approval execution completed:', response);
        this.isLoading = false;
        this.loadApprovalMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Outgoing approval completed successfully for ${response.successfulImports} correspondences.`);
        } else {
          alert(`Outgoing approval completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing outgoing approval:', error);
        this.isLoading = false;
        alert('Error executing outgoing approval. Please check the logs.');
      }
    });
  }
  
  executeApprovalForSingle(approval: OutgoingApprovalMigration): void {
    if (!confirm(`Execute outgoing approval for correspondence: ${approval.correspondenceGuid}?`)) {
      return;
    }
    
    console.log('Executing outgoing approval for single record:', approval);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingApprovalForSpecific([approval.correspondenceGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single outgoing approval execution completed:', response);
          this.isLoading = false;
          this.loadApprovalMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Outgoing approval completed successfully.');
          } else {
            alert(`Outgoing approval failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single outgoing approval:', error);
          this.isLoading = false;
          alert('Error executing outgoing approval. Please check the logs.');
        }
      });
  }
  
  retryFailedApproval(approval: OutgoingApprovalMigration): void {
    if (!confirm(`Retry outgoing approval for correspondence: ${approval.correspondenceGuid}?`)) {
      return;
    }
    
    this.executeApprovalForSingle(approval);
  }
  
  trackByGuid(index: number, item: OutgoingApprovalMigration): string {
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
    const completedSteps = ['APPROVE_CORRESPONDENCE', 'REGISTER_WITH_REFERENCE'];
    
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
  
  getProgressPercentage(step: string): number {
    const steps = [
      'APPROVE_CORRESPONDENCE',
      'REGISTER_WITH_REFERENCE',
      'SEND_CORRESPONDENCE',
      'COMPLETED'
    ];
    
    const stepIndex = steps.indexOf(step);
    if (stepIndex === -1) return 0;
    
    return Math.round((stepIndex / (steps.length - 1)) * 100);
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
}