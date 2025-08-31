import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface Phase {
  id: string;
  name: string;
  description: string;
  status: 'pending' | 'running' | 'completed' | 'error';
  order: number;
  count: number;
  lastResult?: any;
}

@Component({
  selector: 'app-phase-card',
  templateUrl: './phase-card.component.html',
  styleUrls: ['./phase-card.component.css']
})
export class PhaseCardComponent {
  
  @Input() phase!: Phase;
  @Input() canExecute = false;
  @Input() isLoading = false;
  @Output() execute = new EventEmitter<Phase>();
  
  onExecute(): void {
    if (this.canExecute && !this.isLoading) {
      this.execute.emit(this.phase);
    }
  }
  
  getStatusClass(): string {
    switch (this.phase.status) {
      case 'completed':
        return 'bg-green-50 border-green-200';
      case 'running':
        return 'bg-blue-50 border-blue-200 phase-running';
      case 'error':
        return 'bg-red-50 border-red-200';
      default:
        return 'bg-white border-gray-200';
    }
  }
  
  getStatusBadgeClass(): string {
    switch (this.phase.status) {
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
  
  getStatusIcon(): string {
    switch (this.phase.status) {
      case 'completed':
        return '✅';
      case 'running':
        return '⏳';
      case 'error':
        return '❌';
      default:
        return '⭕';
    }
  }
  
  getButtonClass(): string {
    if (!this.canExecute || this.isLoading) {
      return 'bg-gray-300 text-gray-500 cursor-not-allowed';
    }
    
    switch (this.phase.status) {
      case 'completed':
        return 'bg-green-600 text-white hover:bg-green-700';
      case 'error':
        return 'bg-red-600 text-white hover:bg-red-700';
      default:
        return 'bg-indigo-600 text-white hover:bg-indigo-700';
    }
  }
  
  getButtonText(): string {
    if (this.phase.status === 'running') {
      return 'Running...';
    } else if (this.phase.status === 'completed') {
      return 'Re-run Phase';
    } else if (this.phase.status === 'error') {
      return 'Retry Phase';
    } else {
      return 'Execute Phase';
    }
  }
}