import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportantStatisticsComponent } from './important-statistics.component';

describe('ImportantStatisticsComponent', () => {
  let component: ImportantStatisticsComponent;
  let fixture: ComponentFixture<ImportantStatisticsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportantStatisticsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImportantStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
