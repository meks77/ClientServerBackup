import { Injectable } from '@angular/core';
import {formatNumber} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class ByteFormatterService {

  constructor() { }

  public formatToHumanReadable(sizeInMb:number):string {
    let unit = 'MB';
    let size = sizeInMb;
    if (sizeInMb > 1200) {
      unit = 'GB';
      size = sizeInMb / 1024;
    }
    if (size > 1200) {
      unit = 'TB';
      size = size / 1024;
    }
    if (size > 1200) {
      unit = 'PB';
      size = size / 1024;
    }
    if (size > 1200) {
      unit = 'EB';
      size = size / 1024;
    }
    return formatNumber(size, 'en', '0.1-1') + ' ' + unit;
  }
}
