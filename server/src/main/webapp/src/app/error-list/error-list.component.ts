import { Component, OnInit } from '@angular/core';
import {HealthService} from "../health.service";
import {ErrorLog} from "../error-log";

@Component({
  selector: 'app-error-list',
  templateUrl: './error-list.component.html',
  styleUrls: ['./error-list.component.css']
})
export class ErrorListComponent implements OnInit {

  errors: ErrorLog[];

  constructor(private healthService:HealthService) { }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.healthService.getErrors().subscribe(errors => this.errors = errors);
  }
}
