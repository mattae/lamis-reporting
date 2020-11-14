import {Component, OnDestroy, OnInit} from '@angular/core';
import {EMPTY, Subscription} from 'rxjs';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {ReportService} from '../../services/report.service';
import {saveAs} from 'file-saver';
import {catchError} from 'rxjs/operators';

@Component({
    selector: 'data-conversion',
    templateUrl: './data-conversion.component.html'
})
export class DataConversionComponent implements OnInit, OnDestroy {
    private topicSubscription: Subscription;
    running = false;
    message: any = 'Running';
    finished = false;
    facilities: any[];
    selectedFacilities: any[] = [];
    report: number;
    labTest: any;
    labTests: any[];

    constructor(private stompService: RxStompService, private reportService: ReportService) {
    }

    ngOnInit(): void {
        this.topicSubscription = this.stompService.watch('/topic/data-conversion/status').subscribe((msg: Message) => {
            if (msg.body === 'start') {
                this.running = true
            } else if (msg.body === 'end') {
                this.running = false;
                this.finished = true;
                this.message = 'Finished';
            } else {
                this.message = msg.body;
                this.running = true;
            }
        });

        this.reportService.listFacilities().subscribe(res => this.facilities = res);
        this.reportService.labTest().subscribe(res => this.labTests = res)
    }

    ngOnDestroy(): void {
        this.topicSubscription.unsubscribe()
    }

    convert() {
        this.running = true;
        this.finished = false;
        console.log('Params 1', this.selectedFacilities, this.report, this.labTest);
        this.reportService.convertData(this.selectedFacilities, this.report, this.labTest).pipe(
            catchError((err: any) => {
                this.running = false;
                this.finished = true;
                return EMPTY;
            })).subscribe((res) => {
            this.running = false;
            this.finished = true;
            let name = this.report === 1 ? 'Patient Data' : this.report === 2 ? 'Clinic Data' : this.report === 3 ? 'Laboratory Data' : 'Pharmacy Data';
            const file = new File([res], `${name}.xlsx`, {type: 'application/octet-stream'});
            saveAs(file);
        })
    }

}
