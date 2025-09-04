import { Component, OnInit } from '@angular/core';
import { InternalMigrationService, InternalCreationDetail, ImportResponse } from '../../services/internal-migration.service';

@Component({
  selector: 'app-internal-creation-details',
  templateUrl: './internal-creation-details.component.html',
  styleUrls: ['./internal-creation-details.component.css']
})
export class InternalCreationDetailsComponent implements OnInit {
  Math = Math; // Expose Math to template

  creationDetails: InternalCreationDetail[] = [];
  statistics: any = {};
  isLoading = false;
  error: string | null = null;
  selectedItems: Set<string> = new Set();

  constructor(private internalMigrationService: InternalMigrationService) {}

  ngOnInit(): void {
    this.loadCreationDetails();
    this.loadCreationStatistics();
  }

  loadCreationDetails(): void {
    this.isLoading = true;
    this.error = null;

    this.internalMigrationService.getCreationDetails().subscribe({
      next: (response) => {
        this.creationDetails = response.content || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading internal creation details:', error);
        this.error = 'Failed to load creation details';
        this.isLoading = false;
      }
    });
  }

  loadCreationStatistics(): void {
    this.internalMigrationService.getCreationStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Error loading internal creation statistics:', error);
      }
    });
  }

  executeCreationForSelected(): void {
    if (this.selectedItems.size === 0) {
      alert('Please select at least one correspondence to execute.');
      return;
    }

    this.isLoading = true;
    const correspondenceGuids = Array.from(this.selectedItems);

    this.internalMigrationService.executeCreationForSpecific(correspondenceGuids).subscribe({
      next: (response: ImportResponse) => {
        console.log('Internal creation for selected completed:', response);
        this.selectedItems.clear();
        this.loadCreationDetails();
        this.loadCreationStatistics();
      },
      error: (error) => {
        console.error('Error executing internal creation for selected:', error);
        this.error = 'Failed to execute creation for selected correspondences';
        this.isLoading = false;
      }
    });
  }

  toggleSelection(correspondenceGuid: string): void {
    if (this.selectedItems.has(correspondenceGuid)) {
      this.selectedItems.delete(correspondenceGuid);
    } else {
      this.selectedItems.add(correspondenceGuid);
    }
  }

  toggleSelectAll(): void {
    if (this.selectedItems.size === this.creationDetails.length) {
      this.selectedItems.clear();
    } else {
      this.selectedItems.clear();
      this.creationDetails.forEach(detail => {
        this.selectedItems.add(detail.correspondenceGuid);
      });
    }
  }

  isSelected(correspondenceGuid: string): boolean {
    return this.selectedItems.has(correspondenceGuid);
  }

  isAllSelected(): boolean {
    return this.creationDetails.length > 0 && this.selectedItems.size === this.creationDetails.length;
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'completed': return 'status-completed';
      case 'pending': return 'status-pending';
      case 'error': return 'status-error';
      case 'in_progress': return 'status-in-progress';
      default: return 'status-unknown';
    }
  }

  getStepClass(step: string): string {
    switch (step?.toLowerCase()) {
      case 'completed': return 'step-completed';
      case 'get_details': return 'step-details';
      case 'upload_main_attachment': return 'step-upload';
      case 'create_correspondence': return 'step-create';
      case 'upload_other_attachments': return 'step-attachments';
      case 'create_physical_attachment': return 'step-physical';
      default: return 'step-unknown';
    }
  }
}