import {Component, ViewChild} from '@angular/core';
import {ImportantStatisticsComponent} from "./important-statistics/important-statistics.component";
import {ClientListComponent} from "./client-list/client-list.component";
import {NavigationbarComponent} from "./navigationbar/navigationbar.component";
import {ErrorListComponent} from "./error-list/error-list.component";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ClientServerBackup';

  @ViewChild(ImportantStatisticsComponent)
  importantStats: ImportantStatisticsComponent;

  @ViewChild(ClientListComponent)
  clientList: ClientListComponent;

  @ViewChild(NavigationbarComponent)
  navigationBar: NavigationbarComponent;

  @ViewChild(ErrorListComponent)
  errorList: ErrorListComponent;

  constructor() { }

  ngOnInit() {
    this.navigationBar.setRefreshFuntion(() => this.refresh());
  }

  refresh() {
    this.importantStats.refresh();
    this.clientList.refresh();
    this.errorList.refresh();
  }
}
