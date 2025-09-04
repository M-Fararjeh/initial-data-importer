import { Component, OnInit } from '@angular/core';
import { InternalMigrationService, InternalApprovalDetail, PaginatedResponse, ImportResponse } from '../../services/internal-migration.service';

@Component({
  selector: 'app-internal-approval-details',
  templateUrl: './internal-approval-details.component.html',
  styleUrls: ['./internal-approval-details.component.css']
})
export class InternalApprovalDetailsComponent implements OnInit {
  Math = Math; // Expose Math to template

  approvalDetails: InternalApprovalDetail[] = [];
  isLoading = false;
  error: string | null = null;
  selectedItems: Set<string> = new Set();

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;
  hasNext = false;
  hasPrevious = false;

  // Filters
  statusFilter = 'all';
  stepFilter = 'all';
  searchFilter = '';

  statusOptions = [
    { value: 'all', label: 'All Statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'ERROR', label: 'Error' }
  ];

  stepOptions = [
    { value: 'all', label: 'All Steps' },
    { value: 'APPROVE_CORRESPONDENCE', label: 'Approve Correspondence' },
    { value: 'REGISTER_WITH_REFERENCE', label: 'Register with Reference' },
    { value: 'SEND_CORRESPONDENCE', label: 'Send Correspondence' },
    { value: 'SET_OWNER', label: 'Set Owner' },
    { value: 'COMPLETED', label: 'Completed' }
  ];

  constructor(private internalMigrationService: InternalMigrationService) {}

  ngOnInit(): void {
    this.loadApprovalDetails();
  }

  loadApprovalDetails(): void {
    this.isLoading = true;
    this.error = null;

    this.internalMigrationService.getApprovalDetails(
      this.currentPage, 
      this.pageSize, 
      this.statusFilter, 
      this.stepFilter, 
      this.searchFilter
    ).subscribe({
      next: (response: PaginatedResponse<InternalApprovalDetail>) => {
        this.approvalDetails = response.content || [];
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.hasNext = response.hasNext;
        this.hasPrevious = response.hasPrevious;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading internal approval details:', error);
        this.error = 'Failed to load approval details';
        this.isLoading = false;
      }
    });
  }

  executeApprovalForSelected(): void {
    if (this.selectedItems.size === 0) {
      alert('Please select at least one correspondence to execute.');
      return;
    }

    this.isLoading = true;
    const correspondenceGuids = Array.from(this.selectedItems);

    this.internalMigrationService.executeApprovalForSpecific(correspondenceGuids).subscribe({
      next: (response: ImportResponse) => {
        console.log('Internal approval for selected completed:', response);
        this.selectedItems.clear();
        this.loadApprovalDetails();
      },
      error: (error) => {
        console.error('Error executing internal approval for selected:', error);
        this.error = 'Failed to execute approval for selected correspondences';
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.selectedItems.clear();
    this.loadApprovalDetails();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.selectedItems.clear();
    this.loadApprovalDetails();
  }

  toggleSelection(correspondenceGuid: string): void {
    if (this.selectedItems.has(correspondenceGuid)) {
      this.selectedItems.delete(correspondenceGuid);
    } else {
      this.selectedItems.add(correspondenceGuid);
    }
  }

  toggleSelectAll(): void {
    if (this.selectedItems.size === this.approvalDetails.length) {
      this.selectedItems.clear();
    } else {
      this.selectedItems.clear();
      this.approvalDetails.forEach(detail => {
        this.selectedItems.add(detail.correspondenceGuid);
      });
    }
  }

  isSelected(correspondenceGuid: string): boolean {
    return this.selectedItems.has(correspondenceGuid);
  }

  isAllSelected(): boolean {
    return this.approvalDetails.length > 0 && this.selectedItems.size === this.approvalDetails.length;
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'completed': return 'status-completed';
      case 'pending': return 'status-pending';
      case 'error': return 'status-error';
      default: return 'status-unknown';
    }
  }

  getStepClass(step: string): string {
    switch (step?.toLowerCase()) {
      case 'completed': return 'step-completed';
      case 'approve_correspondence': return 'step-approve';
      case 'register_with_reference': return 'step-register';
      case 'send_correspondence': return 'step-send';
      case 'set_owner': return 'step-owner';
      default: return 'step-unknown';
    }
  }

  getPaginationArray(): number[] {
    const pages: number[] = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }
}