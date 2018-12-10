import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {LogService} from "./log.service";

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {

  constructor(private logger: LogService) { }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      console.error(error); // log to console instead
      this.logger.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
