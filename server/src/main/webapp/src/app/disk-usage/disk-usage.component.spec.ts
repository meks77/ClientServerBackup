import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiskUsageComponent } from './disk-usage.component';

describe('DiskUsageComponent', () => {
  let component: DiskUsageComponent;
  let fixture: ComponentFixture<DiskUsageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiskUsageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiskUsageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
