import {Component, OnInit} from '@angular/core';
import {Type, Types} from "../../models/type";

@Component({
  selector: 'app-add-activity',
  templateUrl: './add-activity.component.html'
})
export class AddActivityComponent implements OnInit {

  public Types = Types;
  public Type = Type;


  constructor() {
  }

  ngOnInit() {
  }

}
