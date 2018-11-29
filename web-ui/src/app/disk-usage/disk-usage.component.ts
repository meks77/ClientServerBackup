import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-disk-usage',
  templateUrl: './disk-usage.component.html',
  styleUrls: ['./disk-usage.component.css']
})
export class DiskUsageComponent implements OnInit {

  data:any;

  constructor() {
    this.data = {
      labels: ['Device 1','Device 2','Device 3'],
      datasets: [
        {
          data: [300, 50, 100],
          backgroundColor: [
            "#FF6384",
            "#36A2EB",
            "#FFCE56"
          ],
          hoverBackgroundColor: [
            "#FF6384",
            "#36A2EB",
            "#FFCE56"
          ]
        }]
    };
  }

  ngOnInit() {

  }

}
