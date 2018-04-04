import {Component, OnInit} from '@angular/core';
import {Activity} from "../../models/activity";
import {ActivitiesService} from "../../services/activities.service";
import {AlertService} from "../../services/alert.service";
import {Type} from "../../models/type";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {

  activities: Activity[];
  active: Activity;
  loading = false;
  types = {
    SM: "Scrum Master",
    DEV: "Developer"
  };
  Type = Type;

  constructor(private activitiesService: ActivitiesService, private alertService: AlertService) {
  }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.getActivities();
    this.getActive();
  }

  getActive() {
    this.loading = false;
    this.activitiesService.active().subscribe(result => {
      this.active = result;
      this.loading = false;
    }, error => {
      this.alertService.error("Error while fetching active task");
      console.error(error);
      this.loading = false;
    })
    ;
  }

  stopActive() {
    this.activitiesService.stop().subscribe(result => {
      this.alertService.success(`+ ${result.minutes} ${Type[result.type]}`);
      this.refresh()
    }, error => {
      this.alertService.error("Error while stoping activity");
      console.error(error);
      this.loading = false;
    })

  }

  getActivities() {
    this.loading = true;
    console.log("Getting activities");
    this.activitiesService.list().subscribe(result => {
      this.activities = result;
      this.loading = false;
    }, error => {
      this.alertService.error("Error while fetching list of activities");
      console.error(error);
      this.loading = false;
    })
  }

  startActivity(type: Type) {
    this.activitiesService.start(type).subscribe(result => {
      this.active = result;
      this.loading = false;
    }, error => {
      this.alertService.error("Error while starting activity");
      console.error(error);
      this.loading = false;
    })
  }
}
