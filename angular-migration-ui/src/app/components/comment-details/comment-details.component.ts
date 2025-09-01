import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MigrationService, ImportResponse } from '../../services/migration.service';

export interface CommentMigration {
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
  selector: 'app-comment-details',
  templateUrl: './comment-details.component.html',
  styleUrls: ['./comment-details.component.css']
})
export class CommentDetailsComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  comments: CommentMigration[] = [];
  filteredComments: CommentMigration[] = [];
  allComments: CommentMigration[] = []; // Keep full dataset for client-side filtering
  isLoading = false;
  selectedComments: CommentMigration[] = [];
  allSelected = false;
  
  // Filters
  statusFilter = 'all';
  commentTypeFilter = 'all';
  searchTerm = '';
  
  // Search debounce
  private searchTimeout: any;
  
  // Server-side pagination
  currentPage = 1;
  pageSize = 50; // Increased for better performance
  totalPages = 1;
  totalElements = 0;
  hasNext = false;
  hasPrevious = false;
  
  constructor(private migrationService: MigrationService) {}
  
  ngOnInit(): void {
    console.log('CommentDetailsComponent initialized');
    this.loadCommentMigrations();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadCommentMigrations(): void {
    console.log('Loading comment phase migrations...');
    this.isLoading = true;
    
    // Load first page
    this.migrationService.getCommentMigrations(0, this.pageSize, this.statusFilter, this.commentTypeFilter, this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Comment migrations loaded:', response);
          this.comments = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.totalPages = response.totalPages || 1;
          this.hasNext = response.hasNext || false;
          this.hasPrevious = response.hasPrevious || false;
          this.currentPage = (response.currentPage || 0) + 1; // Convert to 1-based
          
          this.filteredComments = this.comments;
          this.clearSelection();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading comment migrations:', error);
          this.isLoading = false;
        }
      });
  }
  
  loadPage(page: number): void {
    if (this.isLoading) return;
    
    console.log('Loading page:', page);
    this.isLoading = true;
    
    this.migrationService.getCommentMigrations(page - 1, this.pageSize, this.statusFilter, this.commentTypeFilter, this.searchTerm) // Convert to 0-based
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.comments = response.content || [];
          this.currentPage = page;
          this.filteredComments = this.comments;
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
    // Debounce search to avoid too many API calls
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 1; // Reset to first page when filtering
      this.loadCommentMigrations();
    }, 500); // 500ms debounce
  }
  
  onStatusFilterChange(): void {
    this.currentPage = 1; // Reset to first page when filtering
    this.loadCommentMigrations();
  }
  
  onCommentTypeFilterChange(): void {
    this.currentPage = 1; // Reset to first page when filtering
    this.loadCommentMigrations();
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
  
  toggleSelection(comment: CommentMigration): void {
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
    
    if (!confirm(`Execute comment for ${this.selectedComments.length} selected records?`)) {
      return;
    }
    
    console.log('Executing comment for selected records:', this.selectedComments);
    this.isLoading = true;
    
    this.migrationService.executeCommentForSpecific(
      this.selectedComments.map(c => c.commentGuid)
    ).subscribe({
      next: (response: ImportResponse) => {
        console.log('Comment execution completed:', response);
        this.isLoading = false;
        this.loadCommentMigrations();
        this.clearSelection();
        
        if (response.status === 'SUCCESS') {
          alert(`Comment completed successfully for ${response.successfulImports} records.`);
        } else {
          alert(`Comment completed with ${response.successfulImports} successes and ${response.failedImports} failures.`);
        }
      },
      error: (error) => {
        console.error('Error executing comment:', error);
        this.isLoading = false;
        alert('Error executing comment. Please check the logs.');
      }
    });
  }
  
  executeCommentForSingle(comment: CommentMigration): void {
    if (!confirm(`Execute comment for: ${comment.commentGuid}?`)) {
      return;
    }
    
    console.log('Executing comment for single record:', comment);
    this.isLoading = true;
    
    this.migrationService.executeCommentForSpecific([comment.commentGuid])
      .subscribe({
        next: (response: ImportResponse) => {
          console.log('Single comment execution completed:', response);
          this.isLoading = false;
          this.loadCommentMigrations();
          
          if (response.status === 'SUCCESS') {
            alert('Comment completed successfully.');
          } else {
            alert(`Comment failed: ${response.message}`);
          }
        },
        error: (error) => {
          console.error('Error executing single comment:', error);
          this.isLoading = false;
          alert('Error executing comment. Please check the logs.');
        }
      });
  }
  
  retryFailedComment(comment: CommentMigration): void {
    if (!confirm(`Retry comment for: ${comment.commentGuid}?`)) {
      return;
    }
    
    this.executeCommentForSingle(comment);
  }
  
  trackByGuid(index: number, item: CommentMigration): string {
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