import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OutgoingMigrationService, ImportResponse, MigrationStatistics } from '../../services/outgoing-migration.service';

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
  selector: 'app-outgoing-migration-dashboard',
  templateUrl: './outgoing-migration-dashboard.component.html',
  styleUrls: ['./outgoing-migration-dashboard.component.css']
})
export class OutgoingMigrationDashboardComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  phases: Phase[] = [
    {
      id: 'prepare-data',
      name: 'Prepare Data',
      description: 'Select and prepare outgoing correspondences for migration',
      status: 'pending',
      order: 1,
      count: 0
    },
    {
      id: 'creation',
      name: 'Creation',
      description: 'Create outgoing correspondences in destination system with attachments',
      status: 'pending',
      order: 2,
      count: 0
    },
    {
      id: 'assignment',
      name: 'Assignment',
      description: 'Assign outgoing correspondences to users and departments',
      status: 'pending',
      order: 3,
      count: 0
    },
    {
      id: 'approval',
      name: 'Approval',
      description: 'Approve and register outgoing correspondences',
      status: 'pending',
      order: 4,
      count: 0
    },
    {
      id: 'business-log',
      name: 'Business Log',
      description: 'Process business logic and workflows',
      status: 'pending',
      order: 5,
      count: 0
    },
    {
      id: 'closing',
      name: 'Closing',
      description: 'Close outgoing correspondences that need archiving',
      status: 'pending',
      order: 6,
      count: 0
    }
  ];
  
  statistics: MigrationStatistics | null = null;
  isLoading = false;
  
  constructor(private migrationService: OutgoingMigrationService) {}
  
  ngOnInit(): void {
    console.log('OutgoingMigrationDashboardComponent initialized');
    this.loadStatistics();
    
    // Subscribe to statistics updates
    this.migrationService.statistics$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        console.log('Outgoing statistics updated:', stats);
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
    console.log('Loading outgoing statistics...');
    this.migrationService.refreshStatistics();
  }
  
  updatePhaseStatistics(stats: MigrationStatistics): void {
    const phaseMap: { [key: string]: keyof MigrationStatistics } = {
      'prepare-data': 'prepareData',
      'creation': 'creation',
      'assignment': 'assignment',
      'approval': 'approval',
      'business-log': 'businessLog',
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
    
    console.log('Executing outgoing phase:', phase.name);
    this.isLoading = true;
    phase.status = 'running';
    
    let operation;
    
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
      case 'approval':
        operation = this.migrationService.executeApproval();
        break;
      case 'business-log':
        operation = this.migrationService.executeBusinessLog();
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
        console.log('Outgoing phase completed:', phase.name, response);
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
        console.error(`Error executing outgoing phase ${phase.name}:`, error);
        this.isLoading = false;
        phase.status = 'error';
      }
    });
  }
  
  retryFailed(): void {
    if (this.isLoading) {
      return;
    }
    
    console.log('Retrying failed outgoing migrations...');
    this.isLoading = true;
    
    this.migrationService.retryFailed().subscribe({
      next: (response: ImportResponse) => {
        console.log('Outgoing retry completed:', response);
        this.isLoading = false;
        this.loadStatistics();
      },
      error: (error: any) => {
        console.error('Error retrying failed outgoing migrations:', error);
        this.isLoading = false;
      }
    });
  }
  
  canExecutePhase(phase: Phase): boolean {
    if (this.isLoading || phase.status === 'running') {
      return false;
    }
    
    // First phase can always be executed
    if (phase.order === 1) {
      return true;
    }
    
    // Check if previous phases are completed
    const previousPhases = this.phases.filter(p => p.order < phase.order);
    return previousPhases.every(p => p.status === 'completed');
  }
}