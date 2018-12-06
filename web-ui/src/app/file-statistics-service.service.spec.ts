import { TestBed } from '@angular/core/testing';

import { FileStatisticsService } from './file-statistics.service';

describe('FileStatisticsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: FileStatisticsService = TestBed.get(FileStatisticsService);
    expect(service).toBeTruthy();
  });
});
