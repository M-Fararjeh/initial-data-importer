import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { PhaseCardComponent } from './components/phase-card/phase-card.component';
import { StatisticsCardComponent } from './components/statistics-card/statistics-card.component';
import { SourceDataImportComponent } from './components/source-data-import/source-data-import.component';
import { DestinationSetupComponent } from './components/destination-setup/destination-setup.component';
import { MigrationDashboardComponent } from './components/migration-dashboard/migration-dashboard.component';
import { CreationDetailsComponent } from './components/creation-details/creation-details.component';
import { AssignmentDetailsComponent } from './components/assignment-details/assignment-details.component';
import { BusinessLogDetailsComponent } from './components/business-log-details/business-log-details.component';
import { CommentDetailsComponent } from './components/comment-details/comment-details.component';
import { ClosingDetailsComponent } from './components/closing-details/closing-details.component';
import { CorrespondenceRelatedStatusComponent } from './components/correspondence-related-status/correspondence-related-status.component';
import { MigrationService } from './services/migration.service';
import { DataImportService } from './services/data-import.service';

const routes: Routes = [
  { path: '', component: SourceDataImportComponent },
  { path: 'correspondence-related-status', component: CorrespondenceRelatedStatusComponent },
  { path: 'destination-setup', component: DestinationSetupComponent },
  { path: 'migration-dashboard', component: MigrationDashboardComponent },
  { path: 'creation-details', component: CreationDetailsComponent },
  { path: 'assignment-details', component: AssignmentDetailsComponent },
  { path: 'business-log-details', component: BusinessLogDetailsComponent },
  { path: 'comment-details', component: CommentDetailsComponent },
  { path: 'closing-details', component: ClosingDetailsComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  declarations: [
    AppComponent,
    SourceDataImportComponent,
    DestinationSetupComponent,
    MigrationDashboardComponent,
    PhaseCardComponent,
    StatisticsCardComponent,
    CreationDetailsComponent,
    AssignmentDetailsComponent,
    BusinessLogDetailsComponent,
    CommentDetailsComponent,
    ClosingDetailsComponent,
    CorrespondenceRelatedStatusComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    MigrationService,
    DataImportService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }