import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { GroupPage } from './group-page';

describe('GroupPage', () => {
  let component: GroupPage;
  let fixture: ComponentFixture<GroupPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupPage],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GroupPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
