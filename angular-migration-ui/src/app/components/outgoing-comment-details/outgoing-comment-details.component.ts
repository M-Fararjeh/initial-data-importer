import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse } from '../../services/outgoing-migration.service';

export interface OutgoingCommentMigration {
  id: number;
  commentGuid: string;
  correspondenceGuid: string;
  commentCreationDate: string;
  comment: string;
  commentType: string;
  creationUserGuid: string;
  roleGuid: string;
  attachmentCaption: string;
  migrateStatus: string;
  createdDocumentId?: string;
  retryCount: number;
  lastModifiedDate: string;
  // Additional fields for display
  correspondenceSubject?: string;
  correspondenceReferenceNo?: string;
  selected?: boolean;
}

@Component({
  selector: 'app-outgoing-comment-details',
  templateUrl: './outgoing-comment-details.component.html',
  styleUrls: ['./outgoing-comment-details.component.css']
})
export class OutgoingCommentDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  comments: OutgoingCommentMigration[] = [];
  filteredComments: OutgoingCommentMigration[] = [];
  isLoading = false;
  selectedComments: OutgoingCommentMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  commentTypeFilter = 'all';
  searchTerm = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 20;
  totalPages = 1;
  totalElements = 0;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingCommentDetailsComponent initialized');
    this.loadCommentMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadCommentMigrations(): void {
    console.log('Loading outgoing comment phase migrations...');
    this.isLoading = true;
    
    this.migrationService.getOutgoingCommentMigrations(this.currentPage - 1, this.pageSize, this.statusFilter, this.commentTypeFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Outgoing comment migrations loaded:', response);
          this.comments = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          
          this.filteredComments = this.comments;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading outgoing comment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  applyFilters(): void {
    this.currentPage = 1;
    this.loadCommentMigrations();
  }
  
  onStatusFilterChange(): void {
    this.applyFilters();
  }
  
  onCommentTypeFilterChange(): void {
    this.applyFilters();
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  clearFilters(): void {
    this.statusFilter = 'all';
    this.commentTypeFilter = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.loadCommentMigrations();
  }
  
  toggleSelection(comment: OutgoingCommentMigration): void {
    comment.selected = !comment.selected;
    this.updateSelectedComments();
  }
  
  toggleAllSelection(): void {
    this.allSelected = !this.allSelected;
    this.filteredComments.forEach(c => c.selected = this.allSelected);
    this.updateSelectedComments();
  }
  
  updateSelectedComments(): void {
    this.selectedComments = this.comments.filter(c => c.selected);
    this.allSelected = this.filteredComments.length > 0 && 
                      this.filteredComments.every(c => c.selected);
  }
  
  clearSelection(): void {
    this.comments.forEach(c => c.selected = false);
    this.selectedComments = [];
    this.allSelected = false;
  }
  
  executeCommentForSelected(): void {
    if (this.selectedComments.length === 0) {
      alert('Please select at least one comment to execute.');
      return;
    }
    
    if (!confirm(`Execute outgoing comment for ${this.selectedComments.length} selected records?`)) {
      return;
    }
    
    console.log('Executing outgoing comment for selected records:', this.selectedComments);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingCommentForSpecific(
      this.selectedComments.map(c => c.commentGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing comment execution completed:', response);
        this.isLoading = false;
        this.loadCommentMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Outgoing comment completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Outgoing comment completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing outgoing comment:', error);
        this.isLoading = false;
        alert('Error executing outgoing comment. Please check the logs.');
      }
    });
  }
  
  executeCommentForSingle(comment: OutgoingCommentMigration): void {
    if (!confirm(`Execute outgoing comment for: ${comment.commentGuid}?`)) {
      return;
    }
    
    console.log('Executing outgoing comment for single record:', comment);
    this.isLoading = true;
    
    this.migrationService.executeOutgoingCommentForSpecific([comment.commentGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single outgoing comment execution completed:', response);
          this.isLoading = false;
          this.loadCommentMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Outgoing comment completed successfully.');
          } else {
            alert(`Outgoing comment failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single outgoing comment:', error);
          this.isLoading = false;
          alert('Error executing outgoing comment. Please check the logs.');
        }
      });
  }
  
  retryFailedComment(comment: OutgoingCommentMigration): void {
    if (!confirm(`Retry outgoing comment for: ${comment.commentGuid}?`)) {
      return;
    }
    
    this.executeCommentForSingle(comment);
  }
  
  trackByGuid(index: number, item: OutgoingCommentMigration): string {
    return item.commentGuid;
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
  
  getCommentTypeClass(commentType: string): string {
    if (!commentType) return 'comment-user';
    
    const lowerType = commentType.toLowerCase();
    
    if (lowerType.includes('system')) {
      return 'comment-system';
    } else if (lowerType.includes('admin')) {
      return 'comment-admin';
    } else {
      return 'comment-user';
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
      this.loadCommentMigrations();
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