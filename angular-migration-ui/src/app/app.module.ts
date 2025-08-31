import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { MigrationDashboardComponent } from './components/migration-dashboard/migration-dashboard.component';
import { PhaseCardComponent } from './components/phase-card/phase-card.component';
import { StatisticsCardComponent } from './components/statistics-card/statistics-card.component';
import { MigrationService } from './services/migration.service';

@NgModule({
  declarations: [
    AppComponent,
    MigrationDashboardComponent,
    PhaseCardComponent,
    StatisticsCardComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule
  ],
  providers: [
    MigrationService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }