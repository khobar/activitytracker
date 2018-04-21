import {Component, OnInit, ViewChild} from '@angular/core';
import {Type, Types} from "../../models/type";
import {IMyDateModel, IMyDpOptions, MyDatePicker} from "mydatepicker";
import {ActivitiesService} from "../../services/activities.service";
import {ModalDirective} from "angular-bootstrap-md/modals/modal.directive";
import {AlertService} from "../../services/alert.service";

@Component({
  selector: 'app-non-working',
  templateUrl: './non-working.component.html'
})
export class NonWorkingComponent implements OnInit {

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

  datePickerDate: any;
  nonWorkingDate:String;

  constructor(private activitiesService: ActivitiesService, private alertService: AlertService) {
  }

  ngOnInit() {
  }

  onDateChanged(event: IMyDateModel) {
    this.nonWorkingDate = event.formatted;
  }

  addNonWorkingDay() {
    this.activitiesService.nonWorking(this.nonWorkingDate).subscribe(result => {
      this.alertService.success(`${this.nonWorkingDate} has been set as non-working day`);
      this.dismiss()
    })
  }
  dismiss(){
    this.dp.clearDate();
    this.modal.hide();
  }

}
