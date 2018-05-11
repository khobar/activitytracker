import {Component, OnInit, ViewChild} from '@angular/core';
import {Type, Types} from "../../models/type";
import {IMyDateModel, IMyDpOptions, MyDatePicker} from "mydatepicker";
import {ModalDirective} from "angular-bootstrap-md/modals/modal.directive";
import {ActivitiesService} from "../../services/activities.service";
import {AlertService} from "../../services/alert.service";
import {Activity} from "../../models/activity";

@Component({
  selector: 'app-add-activity',
  templateUrl: './add-activity.component.html'
})
export class AddActivityComponent implements OnInit {

  @ViewChild('form') modal: ModalDirective;
  @ViewChild('dp') dp: MyDatePicker;

  public Types = Types;
  public Type = Type;
  public myDatePickerOptions: IMyDpOptions = {
    // other options...
    dateFormat: 'yyyy-mm-dd',
    editableDateField: false,
    openSelectorOnInputClick: true,
    height: "45px"
  };
  start:string;
  end:string;
  activity = new Activity();
  eventDate: string;
  datePickerDate: any;


  constructor(private activitiesService: ActivitiesService, private alertService: AlertService) {
  }

  ngOnInit() {
  }

  addActivity() {
    this.activity.type = Type.SM;
    this.activity.startTime = `${this.eventDate}T${this.start}`;
    this.activity.endTime = `${this.eventDate}T${this.end}`;
    this.activitiesService.add(this.activity).subscribe(result => {
      this.alertService.success(`+ ${result.minutes}m  ${this.Types.SM} activity has been added `);
      this.dismiss()
    })
  }


  onDateChanged(event: IMyDateModel) {
    this.eventDate = event.formatted;
  }

  dismiss() {
    this.dp.clearDate();
    this.modal.hide();
  }
}
