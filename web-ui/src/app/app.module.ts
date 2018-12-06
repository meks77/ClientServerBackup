import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { ClientListComponent } from './client-list/client-list.component';
import { NavigationbarComponent } from './navigationbar/navigationbar.component';
import { ImportantStatisticsComponent } from './important-statistics/important-statistics.component';
import { DiskUsageComponent } from './disk-usage/disk-usage.component';
import {ChartModule} from "primeng/chart";
import { ErrorListComponent } from './error-list/error-list.component';
import { HttpClientModule }    from '@angular/common/http';


@NgModule({
  declarations: [
    AppComponent,
    ClientListComponent,
    NavigationbarComponent,
    ImportantStatisticsComponent,
    DiskUsageComponent,
    ErrorListComponent
  ],
  imports: [
    BrowserModule,
    ChartModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
