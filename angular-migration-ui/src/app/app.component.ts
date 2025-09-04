import { Component, OnInit } from '@angular/core';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm border-b">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between items-center py-6">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <h1 class="text-2xl font-bold text-gray-900">Data Import & Migration System</h1>
              </div>
              <nav class="ml-10 flex space-x-8">
                <a routerLink="/" 
                   routerLinkActive="text-indigo-600 border-indigo-500" 
                   [routerLinkActiveOptions]="{exact: true}"
                   class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200">
                  Source Data Import
                </a>
                <a routerLink="/destination-setup" 
                   routerLinkActive="text-indigo-600 border-indigo-500"
                   class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200">
                  Destination Setup
                </a>
                <a routerLink="/migration-dashboard" 
                   routerLinkActive="text-indigo-600 border-indigo-500"
                   class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200">
                  Migration Dashboard
                </a>
               <a routerLink="/outgoing-migration-dashboard" 
                  routerLinkActive="text-indigo-600 border-indigo-500"
                  class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200">
                  Outgoing Migration
                </a>
               <a routerLink="/internal-migration-dashboard" 
                  routerLinkActive="text-indigo-600 border-indigo-500"
                  class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200">
                  Internal Migration
               </a>
              </nav>
            </div>
            <div class="flex items-center space-x-4">
              <span class="text-sm text-gray-500">Data Import Service</span>
            </div>
          </div>
        </div>
      </header>
      
      <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: []
})
export class AppComponent implements OnInit {
  title = 'Data Import & Migration System';
  
  ngOnInit(): void {
    console.log('AppComponent initialized');
    console.log('Environment API Base URL:', (window as any)['env']?.['API_BASE_URL']);
    console.log('Resolved API Base URL:', environment.apiBaseUrl);
  }
}