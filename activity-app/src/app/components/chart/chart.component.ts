import {Component, OnInit} from '@angular/core';
import {ActivitiesService} from "../../services/activities.service";
import {Type} from "../../models/type";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html'
})
export class ChartComponent implements OnInit {

  type = 'column2d';

  public chartType: string = 'line';

  public chartDatasets: Array<any> = [{data: [0], label: ''}];

  public chartLabels: Array<any> = [''];

  public chartColors: Array<any> = [
    {
      backgroundColor: 'rgba(220,220,220,0.2)',
      borderColor: 'rgba(220,220,220,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(220,220,220,1)',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(220,220,220,1)'
    },
    {
      backgroundColor: 'rgba(151,187,205,0.2)',
      borderColor: 'rgba(151,187,205,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(151,187,205,1)',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(151,187,205,1)'
    }
  ];

  public chartOptions: any = {
    responsive: true,
    scales:{
      yAxes:[{
        stacked:true
      }]
    }

  };

  public chartClicked(e: any): void {

  }

  public chartHovered(e: any): void {

  }


  constructor(private activitiesService: ActivitiesService) {
  }

  ngOnInit() {
    let dev = [];
    let sm = [];
    let labels = [];

    this.activitiesService.report().subscribe(reports => {
      this.chartDatasets.pop();
      reports.forEach(report => {
        labels.push(report.date);
        let smValue = report.hours.SM;
        if (!smValue) {
          smValue = 0
        }
        let devValue = report.hours.DEV;
        if (!devValue) {
          devValue = 0
        }
        dev.push(devValue);
        sm.push(smValue);
      });
      this.chartDatasets.push({data: dev, label: Type.DEV});
      this.chartDatasets.push({data: sm, label: Type.SM});
      this.chartLabels = labels;
      console.log(this.chartDatasets);
    });
  }
}
