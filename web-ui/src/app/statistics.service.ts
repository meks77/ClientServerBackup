import { Injectable } from '@angular/core';
import {catchError, tap} from 'rxjs/operators';
import {Observable, of} from "rxjs";

import {HttpClient } from "@angular/common/http";

import {FileStatistics} from "./file-statistics";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private fileStatisticsUrl = "http://localhost:8080/api/v1.0/statistics/fileStatistics";
  private clientCountUrl = "http://localhost:8080/api/v1.0/statistics/clients/count";

  constructor( private http: HttpClient) { }

  getFileStatistics(): Observable<FileStatistics> {
    return this.http.get<FileStatistics>(this.fileStatisticsUrl)
      .pipe(
        tap(() => this.log(`fetched fileStats`)),
        catchError(this.handleError<FileStatistics>(`fetching file statistics failed`))
    )
  }

  getClientCount(): Observable<number> {
    return this.http.get<number>(this.clientCountUrl)
      .pipe(
        catchError(this.handleError<number>(`fetching client count failed`))
      )

  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      console.error(error); // log to console instead
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  private log(message: string) {
    console.log(message);
  }
}
