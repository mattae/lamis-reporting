import {Component, OnDestroy, OnInit} from '@angular/core';
import {ReportService} from '../../services/report.service';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {Subscription} from 'rxjs';
import {DomSanitizer} from '@angular/platform-browser';
import {saveAs} from 'file-saver';
import {DateRange} from '@syncfusion/ej2-calendars';

export interface Facility {
    id: number;
    name: string;
    selected: boolean;
}

@Component({
    selector: 'biometric-report',
    templateUrl: './biometric-report.component.html'
})
export class BiometricReportComponent implements OnInit, OnDestroy {
    private topicSubscription: Subscription;
    facility: Facility;
    files: string[];
    running = false;
    message: any = 'Running';
    finished = false;
    dateRange: DateRange = {
        start: new Date(),
        end: new Date()
    };
    today = new Date();
    todaySelectable = true;
    pdf = true;
    current: boolean = false;

    constructor(private service: ReportService, private stompService: RxStompService, private domSanitizer: DomSanitizer) {
    }

    ngOnInit() {
        this.service.getActiveFacility().subscribe(res => this.facility = res);
        this.topicSubscription = this.stompService.watch('/topic/biometric-report/status').subscribe((msg: Message) => {
            if (msg.body === 'start') {
                this.running = true
            } else if (msg.body === 'end') {
                this.running = false;
                this.finished = true;
                this.message = 'Finished';
                this.service.listFiles().subscribe(res => {
                    this.files = res;
                })
            } else {
                this.message = msg.body;
                this.running = true;
            }
        })
    }

    convert() {
        this.running = true;
        this.finished = false;
        this.service.biometricReport({
            start: this.dateRange.start,
            end: this.dateRange.end,
            facilityId: this.facility.id,
            pdf: !!this.pdf
        }).subscribe((res) => {
            const file = new File([res], this.facility.name + '_Biometric Report.' + (this.pdf ? 'pdf' : 'xlsx'), {type: 'application/octet-stream'});
            saveAs(file);
        })
    }

    ngOnDestroy(): void {
        this.topicSubscription.unsubscribe()
    }
}
