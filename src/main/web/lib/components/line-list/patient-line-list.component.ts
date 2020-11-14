import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {Facility} from '../art-summary/art-summary.component';
import {saveAs} from 'file-saver';
import {ReportService} from '../../services/report.service';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import * as moment_ from 'moment';

const moment = moment_;

@Component({
    selector: 'report-patient-line-list',
    templateUrl: './patient-line-list.component.html',
    styleUrls: ['./patient-line-list.component.scss']
})
export class PatientLineListComponent implements OnInit, OnDestroy {
    params: { [key: string]: any } = {};
    states: any[];
    regimenTypes: any[];
    lgas: any[];
    private topicSubscription: Subscription;
    facility: Facility;
    running = false;
    finished = false;
    today = moment();
    message: any = 'Running';

    constructor(private service: ReportService, private stompService: RxStompService) {
    }

    ngOnInit() {
        this.service.getActiveFacility().subscribe(res => {
            this.facility = res;
            this.params['facilityId'] = this.facility.id;
        });
        this.topicSubscription = this.stompService.watch('/topic/patient-line-list/status').subscribe((msg: Message) => {
            if (msg.body === 'start') {
                this.running = true;
                this.finished = false;
                this.message = 'Running';
            } else if (msg.body === 'end') {
                this.running = false;
                this.finished = true;
                this.message = 'Finished';
            } else {
                this.message = msg.body;
                this.running = true;
                this.finished = false;
            }
        });
        this.service.getStates().subscribe(res => this.states = res);
        this.service.getRegimenTypes().subscribe(res => this.regimenTypes = res);
    }

    stateChanged(state) {
        if (state && state.id) {
            this.service.getLgasByState(state.id).subscribe(res => this.lgas = res)
        }
    }

    convert() {
        this.running = true;
        this.finished = false;
        this.service.patientLineList(this.params).subscribe((res) => {
            this.running = false;
            this.finished = true;
            let format = this.params.format === 'xlsx' ? 'xlsx' : 'pdf';
            const file = new File([res], `${this.facility.name}_Patient_Line_List.${format}`, {type: 'application/octet-stream'});
            saveAs(file);
        })
    }

    ngOnDestroy(): void {
        this.topicSubscription.unsubscribe()
    }
}
