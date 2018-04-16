import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ActivitiesService} from "../../services/activities.service";
import {Type} from "../../models/type";
import {DatepickerOptions} from "ng2-datepicker";
import * as plLocale from 'date-fns/locale/pl';
import {DatePipe} from "@angular/common";
import {Range} from "../../models/report";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html'
})
export class ChartComponent implements OnInit, OnChanges {

  types = {
    SM: "Scrum Master",
    DEV: "Developer"
  };

  toDate: Date;
  fromDate: Date;
  range: Range;

//TODO move chart options to ohter file?
  public reportChartType: string = 'line';
  public reportDatasets: Array<any> = [{data: [0], label: ''}];
  public reportChartLabels: Array<any> = [''];
  public distChartType: string = 'doughnut';
  public distDatasets: Array<any> = [5, 5];
  public distChartLabels: Array<any> = [this.types[Type.DEV], this.types[Type.SM]];

  public reportColors: Array<any> = [
    {
      backgroundColor: 'rgba(44,90,160,1)',
      borderColor: 'rgba(220,220,220,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(220,220,220,1)',
      pointBorderColor: '#294284',
      pointHoverBackgroundColor: '#294284',
      pointHoverBorderColor: 'rgba(220,220,220,1)'
    },
    {
      backgroundColor: 'rgba(40,160,89,1)',
      borderColor: 'rgba(151,187,205,1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(151,187,205,1)',
      pointBorderColor: '#2a6f3c',
      pointHoverBackgroundColor: '#2a6f3c',
      pointHoverBorderColor: 'rgba(151,187,205,1)'
    }
  ];

  public distChartColors: Array<any> = [{
    hoverBorderColor: ['rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)'],
    hoverBorderWidth: 0,
    backgroundColor: ["#2c5aa0", "#28a059"],
    hoverBackgroundColor: ["#2d72b9", "#28a059"]
  }];

  public reportOptions: any = {
    responsive: true,
    scales: {
      yAxes: [{
        stacked: true
      }]
    }
  };
  public distOptions: any = {
    responsive: false
  };

  // Range options
  dateOptions: DatepickerOptions = {
    minYear: 2018,
    displayFormat: 'DD.MM.YYYY',
    barTitleFormat: 'MMMM YYYY',
    dayNamesFormat: 'dd',
    firstCalendarDay: 1, // 0 - Sunday, 1 - Monday
    locale: plLocale,
    maxDate: new Date(Date.now()),  // Maximal selectable date
  };

  public chartClicked(e: any): void {

  }

  public chartHovered(e: any): void {

  }


  constructor(private activitiesService: ActivitiesService, public datepipe: DatePipe) {
    this.toDate = new Date();
    this.fromDate = this.getFromInitDate();
    this.range = this.createRange();
  }

  ngOnInit() {
    this.getReport();
    this.getDistribution();
  }

  reload() {
    this.range = this.createRange();
    this.getReport();
    this.getDistribution();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.range = this.createRange();
    this.getReport();
    this.getDistribution();
  }

  private getDistribution() {
    let data = [];
    this.activitiesService.distribution(this.range).subscribe(dist => {
      this.distDatasets.pop();
      data.push(dist.DEV);
      data.push(dist.SM);
      this.distDatasets = data;
      console.log(this.distDatasets);
    })
  }

  private getReport() {
    let dev = [];
    let sm = [];
    let labels = [];
    this.activitiesService.report(this.range).subscribe(reports => {
      this.reportDatasets.pop();
      this.reportDatasets.pop();
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
      this.reportDatasets.push({data: dev, label: this.types[Type.DEV]});
      this.reportDatasets.push({data: sm, label: this.types[Type.SM]});
      this.reportChartLabels = labels;
      console.log(this.reportDatasets);
    });
  }

  test() {
    console.log(this.range);
  }

  private getFromInitDate(): Date {
    let date = new Date();
    date.setDate(date.getDate() - 7);
    return date;
  }

  private createRange(): Range {
    let range = new Range();
    range.from = this.datepipe.transform(this.fromDate, 'yyyy-MM-dd');
    range.to = this.datepipe.transform(this.toDate, 'yyyy-MM-dd');
    return range;
  }
}
