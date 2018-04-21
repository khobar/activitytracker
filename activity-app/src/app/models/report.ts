import {IMyDate} from "mydaterangepicker/dist/interfaces/my-date.interface";

export class ReportData {
  date: string;
  minutes: DEVSMNON;
  hours: DEVSMNON;
}

export class DEVSMNON {
  DEV: number;
  SM: number;
  NON_WORKING:number
}

export class Range {
  from: string;
  to: string;
}

export class DateRange {
  beginDate: RDate;
  endDate: RDate;

  constructor() {
    this.beginDate = new RDate();
    this.endDate = new RDate();

  }
}

export class RDate {
  year: number;
  month: number;
  day: number;

  constructor() {
  }

  toDate() {
    return new Date(this.year, this.month - 1, this.day)
  }

  IMDate(imDate: IMyDate) {
    this.year = imDate.year;
    this.month = imDate.month;
    this.day = imDate.day;
  }

  Date(date: Date) {
    this.year = date.getFullYear();
    this.month = date.getMonth() + 1;
    this.day = date.getDate();
  }

  toString() {
    return `${this.year}-${this.month}-${this.day}`;
  }
}
