import {Component, OnInit} from '@angular/core';
import {Activity} from "../../models/activity";
import {ActivitiesService} from "../../services/activities.service";
import {AlertService} from "../../services/alert.service";
import {Type} from "../../models/type";
import {Router} from "@angular/router";
import {DateFormatter} from "@angular/common/src/pipes/deprecated/intl";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {

  activities: Activity[];
  active: Activity;
  types = {
    SM: "Scrum Master",
    DEV: "Developer"
  };
  Type = Type;

  constructor(private router: Router,
              private activitiesService: ActivitiesService,
              private alertService: AlertService) {
  }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.getActivities();
    this.getActive();
  }

  getActive() {
    this.activitiesService.active().subscribe(result => {
      this.active = result;
      this.active.time = new Date(this.active.startTime);
      console.log(new Date(this.active.startTime));
    }, error => {
      this.alertService.error("Error while fetching active task");
      console.error(error);
    })
    ;
  }

  stopActive() {
    this.activitiesService.stop().subscribe(result => {
      this.alertService.success(`+ ${result.minutes} ${Type[result.type]}`, true);
      this.reloadPage()
    }, error => {
      this.alertService.error("Error while stoping activity");
      console.error(error);
    })

  }

  getActivities() {
    console.log("Getting activities");
    this.activitiesService.list().subscribe(result => {
      this.activities = result;
    }, error => {
      this.alertService.error("Error while fetching list of activities");
      console.error(error);
    })
  }

  startActivity(type: Type) {
    this.activitiesService.start(type).subscribe(result => {
      this.active = result;
    }, error => {
      this.alertService.error("Error while starting activity");
      console.error(error);
    })
  }

  reloadPage() {
    this.router.routeReuseStrategy.shouldReuseRoute = function () {
      return false;
    };
    let currentUrl = this.router.url + '?';
    this.router.navigateByUrl(currentUrl)
      .then(() => {
        this.router.navigated = false;
        this.router.navigate([this.router.url]);
      });
  }
}
