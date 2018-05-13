import {Component, OnInit} from '@angular/core';
import {ActivitiesService} from "../../services/activities.service";
import {Type, Types} from "../../models/type";
import {DatePipe} from "@angular/common";
import {DateRange, Range, RDate} from "../../models/report";
import {IMyDateRangeModel, IMyDrpOptions} from "mydaterangepicker";
import {chartsOptions} from "./chart.options";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html'
})
export class ChartComponent implements OnInit {

  dateRange: DateRange = this.createDataRange();
  toDate: Date;
  fromDate: Date;
  range: Range;
  public chartsOptions = chartsOptions;
  public reportDatasets: Array<any> = [{data: [0], label: ''}];
  public reportChartLabels: Array<any> = [''];
  public distDatasets: Array<any> = [5, 5, 0];

  // Range options
  myDateRangePickerOptions: IMyDrpOptions = {
    // other options...
    showClearBtn: false,
    showApplyBtn: false,
    dateFormat: 'dd.mm.yyyy',
    minYear: 2018,
    disableSince: this.createAvailableRange(),
    showClearDateRangeBtn: false,
    editableDateRangeField: false,
    openSelectorOnInputClick: true,
    height: "45px"
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

  onDateRangeChanged(event: IMyDateRangeModel) {
    this.dateRange = new DateRange();
    this.dateRange.beginDate.IMDate(event.beginDate);
    this.dateRange.endDate.IMDate(event.endDate);
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
      data.push(dist.NON_WORKING);
      this.distDatasets = data;
    })
  }

  private getReport() {
    let dev = [];
    let sm = [];
    let non = [];
    let labels = [];
    this.activitiesService.report(this.range).subscribe(reports => {
      this.reportDatasets.length = 0;
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
        let nonValue = report.hours.NON_WORKING;
        if (!nonValue) {
          nonValue = 0
        }
        dev.push(devValue);
        sm.push(smValue);
        non.push(nonValue);
      });
      this.reportDatasets.push({data: dev, label: Types[Type.DEV]});
      this.reportDatasets.push({data: sm, label: Types[Type.SM]});
      this.reportDatasets.push({data: non, label: Types[Type.NON_WORKING]});
      this.reportChartLabels = labels;
    });
  }

  private getFromInitDate(): Date {
    let date = new Date();
    date.setDate(date.getDate() - 7);
    return date;
  }

  private createDataRange(): DateRange {
    let today = new Date();
    let dateFrom = new Date();
    dateFrom.setDate(dateFrom.getDate() - 14);
    let range = new DateRange();
    range.beginDate.Date(dateFrom);
    range.endDate.Date(today);
    return range;
  }

  private createRange(): Range {
    let range = new Range();
    range.from = this.datepipe.transform(this.dateRange.beginDate.toDate(), 'yyyy-MM-dd');
    range.to = this.datepipe.transform(this.dateRange.endDate.toDate(), 'yyyy-MM-dd');
    return range;
  }

  private createAvailableRange(): RDate {
    let date = new RDate();
    let futureRange = new Date();
    futureRange.setDate(futureRange.getDate() + 1);
    date.Date(futureRange);
    return date;
  }
}
