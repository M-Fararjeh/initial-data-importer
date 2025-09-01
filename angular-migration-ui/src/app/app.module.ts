import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { MigrationDashboardComponent } from './components/migration-dashboard/migration-dashboard.component';
import { PhaseCardComponent } from './components/phase-card/phase-card.component';
import { StatisticsCardComponent } from './components/statistics-card/statistics-card.component';
import { CreationDetailsComponent } from './components/creation-details/creation-details.component';
import { AssignmentDetailsComponent } from './components/assignment-details/assignment-details.component';
import { MigrationService } from './services/migration.service';

const routes: Routes = [
  { path: '', component: MigrationDashboardComponent },
  { path: 'creation-details', component: CreationDetailsComponent },
  { path: 'assignment-details', component: AssignmentDetailsComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  declarations: [
    AppComponent,
    MigrationDashboardComponent,
    PhaseCardComponent,
    StatisticsCardComponent,
    CreationDetailsComponent
    AssignmentDetailsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    MigrationService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }