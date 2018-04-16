import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Activity} from "../models/activity";
import {environment} from "../../environments/environment";
import {Type} from "../models/type";
import {DEVSM, Range, ReportData} from "../models/report";

@Injectable()
export class ActivitiesService {

  constructor(private http: HttpClient) {
  }

  active(): Observable<Activity> {
    return this.http.get<Activity>(environment.baseURL + 'active').map(activity => ActivitiesService.transform(activity));
  }

  list(): Observable<Activity[]> {
    return this.http.get<Activity[]>(environment.baseURL + 'list').map(result => {
        result.forEach(activity => ActivitiesService.transform(activity));
        return result;
      }
    );
  }

  report(range: Range): Observable<ReportData[]> {
    return this.http.post<ReportData[]>(environment.baseURL + 'report', range);
  }

  distribution(range: Range): Observable<DEVSM> {
    return this.http.post<DEVSM>(environment.baseURL + 'distribution', range);
  }


  stop(): Observable<Activity> {
    return this.http.post<Activity>(environment.baseURL + 'stop', {});
  }

  start(type: Type): Observable<Activity> {
    return this.http.post<Activity>(environment.baseURL + 'start', {type: type});
  }

  static transform(activity) {
//TODO transform date fields via map
    return activity
  }

}
