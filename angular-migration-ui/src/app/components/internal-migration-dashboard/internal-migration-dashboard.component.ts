import { Component, OnInit, OnDestroy } from '@angular/core';
import { InternalMigrationService, InternalMigrationStatistics, ImportResponse } from '../../services/internal-migration.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-internal-migration-dashboard',
  templateUrl: './internal-migration-dashboard.component.html',
  styleUrls: ['./internal-migration-dashboard.component.css']
})
export class InternalMigrationDashboardComponent implements OnInit, OnDestroy {
  statistics: InternalMigrationStatistics = {
    prepareData: 0,
    creation: 0,
    assignment: 0,
    approval: 0,
    businessLog: 0,
    closing: 0,
    completed: 0,
    failed: 0,
    inProgress: 0
  };

  isLoading = false;
  error: string | null = null;
  lastUpdated: Date = new Date();
  
  private refreshSubscription?: Subscription;
  private readonly REFRESH_INTERVAL = 30000; // 30 seconds

  phases = [
    {
      id: 'prepare-data',
      name: 'Prepare Data',
      description: 'Select and prepare internal correspondences for migration',
      order: 1,
      status: 'pending',
      count: 0,
      canExecute: true
    },
    {
      id: 'creation',
      name: 'Creation',
      description: 'Create internal correspondences with attachments',
      order: 2,
      status: 'pending',
      count: 0,
      canExecute: false
    },
    {
      id: 'assignment',
      name: 'Assignment',
      description: 'Create readonly assignments for internal correspondences',
      order: 3,
      status: 'pending',
      count: 0,
      canExecute: false
    },
    {
      id: 'approval',
      name: 'Approval',
      description: 'Approve, register and send internal correspondences',
      order: 4,
      status: 'pending',
      count: 0,
      canExecute: false
    },
    {
      id: 'business-log',
      name: 'Business Log',
      description: 'Process business logic and workflows',
      order: 5,
      status: 'pending',
      count: 0,
      canExecute: false
    },
    {
      id: 'closing',
      name: 'Closing',
      description: 'Close internal correspondences that need archiving',
      order: 6,
      status: 'pending',
      count: 0,
      canExecute: false
    }
  ];

  constructor(private internalMigrationService: InternalMigrationService) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  loadStatistics(): void {
    this.isLoading = true;
    this.error = null;

    this.internalMigrationService.getStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
        this.updatePhaseStatuses();
        this.lastUpdated = new Date();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading internal migration statistics:', error);
        this.error = 'Failed to load statistics';
        this.isLoading = false;
      }
    });
  }

  private updatePhaseStatuses(): void {
    // Update phase counts and determine execution availability
    this.phases.forEach(phase => {
      switch (phase.id) {
        case 'prepare-data':
          phase.count = this.statistics.prepareData;
          phase.canExecute = true;
          break;
        case 'creation':
          phase.count = this.statistics.creation;
          phase.canExecute = this.statistics.prepareData > 0;
          break;
        case 'assignment':
          phase.count = this.statistics.assignment;
          phase.canExecute = this.statistics.creation > 0;
          break;
        case 'approval':
          phase.count = this.statistics.approval;
          phase.canExecute = this.statistics.assignment > 0;
          break;
        case 'business-log':
          phase.count = this.statistics.businessLog;
          phase.canExecute = this.statistics.approval > 0;
          break;
        case 'closing':
          phase.count = this.statistics.closing;
          phase.canExecute = this.statistics.businessLog > 0;
          break;
      }
    });
  }

  executePhase(phaseId: string): void {
    this.isLoading = true;
    this.error = null;

    let executeObservable: Observable<ImportResponse>;

    switch (phaseId) {
      case 'prepare-data':
        executeObservable = this.internalMigrationService.prepareData();
        break;
      case 'creation':
        executeObservable = this.internalMigrationService.executeCreation();
        break;
      case 'assignment':
        executeObservable = this.internalMigrationService.executeAssignment();
        break;
      case 'approval':
        executeObservable = this.internalMigrationService.executeApproval();
        break;
      case 'business-log':
        executeObservable = this.internalMigrationService.executeBusinessLog();
        break;
      case 'closing':
        executeObservable = this.internalMigrationService.executeClosing();
        break;
      default:
        this.error = `Unknown phase: ${phaseId}`;
        this.isLoading = false;
        return;
    }

    executeObservable.subscribe({
      next: (response) => {
        console.log(`Internal Phase ${phaseId} completed:`, response);
        this.loadStatistics(); // Refresh statistics
      },
      error: (error) => {
        console.error(`Error executing internal phase ${phaseId}:`, error);
        this.error = `Failed to execute ${phaseId} phase`;
        this.isLoading = false;
      }
    });
  }

  retryFailed(): void {
    this.isLoading = true;
    this.error = null;

    this.internalMigrationService.retryFailed().subscribe({
      next: (response) => {
        console.log('Internal retry completed:', response);
        this.loadStatistics(); // Refresh statistics
      },
      error: (error) => {
        console.error('Error retrying failed internal migrations:', error);
        this.error = 'Failed to retry failed migrations';
        this.isLoading = false;
      }
    });
  }

  private startAutoRefresh(): void {
    this.refreshSubscription = interval(this.REFRESH_INTERVAL).subscribe(() => {
      if (!this.isLoading) {
        this.loadStatistics();
      }
    });
  }

  private stopAutoRefresh(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  getPhaseStatusClass(phase: any): string {
    if (phase.count > 0) {
      return 'completed';
    } else if (phase.canExecute) {
      return 'ready';
    } else {
      return 'pending';
    }
  }

  getPhaseStatusText(phase: any): string {
    if (phase.count > 0) {
      return `${phase.count} processed`;
    } else if (phase.canExecute) {
      return 'Ready to execute';
    } else {
      return 'Waiting for dependencies';
    }
  }
}