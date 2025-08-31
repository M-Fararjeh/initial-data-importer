import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-statistics-card',
  templateUrl: './statistics-card.component.html',
  styleUrls: ['./statistics-card.component.css']
})
export class StatisticsCardComponent {
  
  @Input() title!: string;
  @Input() value!: number;
  @Input() icon!: string;
  @Input() color: 'green' | 'blue' | 'red' | 'yellow' = 'blue';
  
  getColorClasses(): string {
    switch (this.color) {
      case 'green':
        return 'bg-green-50 border-green-200';
      case 'red':
        return 'bg-red-50 border-red-200';
      case 'yellow':
        return 'bg-yellow-50 border-yellow-200';
      default:
        return 'bg-blue-50 border-blue-200';
    }
  }
  
  getTextColorClass(): string {
    switch (this.color) {
      case 'green':
        return 'text-green-900';
      case 'red':
        return 'text-red-900';
      case 'yellow':
        return 'text-yellow-900';
      default:
        return 'text-blue-900';
    }
  }
  
  getSubtitleColorClass(): string {
    switch (this.color) {
      case 'green':
        return 'text-green-600';
      case 'red':
        return 'text-red-600';
      case 'yellow':
        return 'text-yellow-600';
      default:
        return 'text-blue-600';
    }
  }
}