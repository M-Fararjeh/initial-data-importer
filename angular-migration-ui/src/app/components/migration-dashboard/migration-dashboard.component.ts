import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil, Observable } from 'rxjs';
import { MigrationService, ImportResponse, MigrationStatistics } from '../../services/migration.service';

export interface Phase {
  id: string;
  name: string;
  description: string;
  status: 'pending' | 'running' | 'completed' | 'error';
  order: number;
  count: number;
  lastResult?: ImportResponse;
}

@Component({
  selector: 'app-migration-dashboard',
  templateUrl: './migration-dashboard.component.html',
  styleUrls: ['./migration-dashboard.component.css']
})
export class MigrationDashboardComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  phases: Phase[] = [
    {
      id: 'prepare-data',
      name: 'Prepare Data',
      description: 'Select and prepare incoming correspondences for migration',
      status: 'pending',
      order: 1,
      count: 0
    },
    {
      id: 'creation',
      name: 'Creation',
      description: 'Create correspondences in destination system with attachments',
      status: 'pending',
      order: 2,
      count: 0
    },
    {
      id: 'assignment',
      name: 'Assignment',
      description: 'Assign correspondences to users and departments',
      status: 'pending',
      order: 3,
      count: 0
    },
    {
      id: 'business-log',
      name: 'Business Log',
      description: 'Process business logic and workflows',
      status: 'pending',
      order: 4,
      count: 0
    },
    {
      id: 'comment',
      name: 'Comment',
      description: 'Process comments and annotations',
      status: 'pending',
      order: 5,
      count: 0
    },
    {
      id: 'closing',
      name: 'Closing',
      description: 'Close correspondences that need to be archived',
      status: 'pending',
      order: 6,
      count: 0
    }
  ];
  
  statistics: MigrationStatistics | null = null;
  isLoading = false;
  
  constructor(private migrationService: MigrationService) {}
  
  ngOnInit(): void {
    this.loadStatistics();
    
    // Subscribe to statistics updates
    this.migrationService.statistics$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        if (stats) {
          this.statistics = stats;
          this.updatePhaseStatistics(stats);
        }
      });
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadStatistics(): void {
    this.migrationService.refreshStatistics();
  }
  
  updatePhaseStatistics(stats: MigrationStatistics): void {
    const phaseMap: { [key: string]: keyof MigrationStatistics } = {
      'prepare-data': 'prepareData',
      'creation': 'creation',
      'assignment': 'assignment',
      'business-log': 'businessLog',
      'comment': 'comment',
      'closing': 'closing'
    };
    
    this.phases.forEach(phase => {
      const statKey = phaseMap[phase.id];
      if (statKey && stats[statKey] !== undefined) {
        phase.count = stats[statKey] as number;
        
        // Update status based on count
        if (phase.count > 0) {
          phase.status = 'pending';
        } else {
          phase.status = 'completed';
        }
      }
    });
  }
  
  executePhase(phase: Phase): void {
    if (this.isLoading || phase.status === 'running') {
      return;
    }
    
    this.isLoading = true;
    phase.status = 'running';
    
    let operation: Observable<ImportResponse>;
    
    switch (phase.id) {
      case 'prepare-data':
        operation = this.migrationService.prepareData();
        break;
      case 'creation':
        operation = this.migrationService.executeCreation();
        break;
      case 'assignment':
        operation = this.migrationService.executeAssignment();
        break;
      case 'business-log':
        operation = this.migrationService.executeBusinessLog();
        break;
      case 'comment':
        operation = this.migrationService.executeComment();
        break;
      case 'closing':
        operation = this.migrationService.executeClosing();
        break;
      default:
        this.isLoading = false;
        phase.status = 'error';
        return;
    }
    
    operation.subscribe({
      next: (response: ImportResponse) => {
        this.isLoading = false;
        phase.lastResult = response;
        
        if (response.status === 'SUCCESS') {
          phase.status = 'completed';
        } else if (response.status === 'PARTIAL_SUCCESS') {
          phase.status = 'completed';
        } else {
          phase.status = 'error';
        }
        
        this.loadStatistics();
      },
      error: (error: any) => {
        this.isLoading = false;
        phase.status = 'error';
        console.error(`Error executing phase ${phase.name}:`, error);
      }
    });
  }
  
  retryFailed(): void {
    if (this.isLoading) {
      return;
    }
    
    this.isLoading = true;
    
    this.migrationService.retryFailed().subscribe({
      next: (response: ImportResponse) => {
        this.isLoading = false;
        this.loadStatistics();
        console.log('Retry completed:', response);
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error retrying failed migrations:', error);
      }
    });
  }
  
  canExecutePhase(phase: Phase): boolean {
    if (this.isLoading || phase.status === 'running') {
      return false;
    }
    
    // Check if previous phases are completed
    const previousPhases = this.phases.filter(p => p.order < phase.order);
    return previousPhases.every(p => p.status === 'completed') && phase.count > 0;
  }
  
  getPhaseStatusClass(phase: Phase): string {
    switch (phase.status) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'running':
        return 'bg-blue-100 text-blue-800';
      case 'error':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getPhaseStatusIcon(phase: Phase): string {
    switch (phase.status) {
      case 'completed':
        return '✓';
      case 'running':
        return '⟳';
      case 'error':
        return '✗';
      default:
        return '○';
    }
  }
}