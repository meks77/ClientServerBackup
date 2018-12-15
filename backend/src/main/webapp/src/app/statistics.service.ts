import { Injectable } from '@angular/core';
import {catchError} from 'rxjs/operators';
import {Observable} from "rxjs";

import {HttpClient } from "@angular/common/http";

import {FileStatistics} from "./file-statistics";
import {ErrorHandlingService} from "./error-handling.service";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private fileStatisticsUrl = "/api/v1.0/statistics/fileStatistics";
  private clientCountUrl = "/api/v1.0/statistics/clients/count";

  constructor( private http: HttpClient,
               private errorHandler: ErrorHandlingService) { }

  getFileStatistics(): Observable<FileStatistics> {
    return this.http.get<FileStatistics>(this.fileStatisticsUrl)
      .pipe(
        catchError(this.errorHandler.whenErrorOnHttpRequest<FileStatistics>(`fetching file statistics failed`))
    )
  }

  getClientCount(): Observable<number> {
    return this.http.get<number>(this.clientCountUrl)
      .pipe(
        catchError(this.errorHandler.whenErrorOnHttpRequest<number>(`fetching client count failed`))
      )

  }



}
