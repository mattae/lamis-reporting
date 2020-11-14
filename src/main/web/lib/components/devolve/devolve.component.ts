import {Component, OnDestroy, OnInit} from '@angular/core';
import {ReportService} from '../../services/report.service';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {Subscription} from 'rxjs';
import {DomSanitizer} from '@angular/platform-browser';
import {saveAs} from 'file-saver';

export interface Facility {
    id: number;
    name: string;
    selected: boolean;
}

@Component({
    selector: 'devolve-report',
    templateUrl: './devolve.component.html'
})
export class DevolveComponent implements OnInit, OnDestroy {
    private topicSubscription: Subscription;
    facility: Facility;
    files: string[];
    running = false;
    message: any = 'Running';
    finished = false;
    cparp: boolean = false;
    todaySelectable = true;

    current: boolean = false;

    constructor(private service: ReportService, private stompService: RxStompService, private domSanitizer: DomSanitizer) {
    }

    ngOnInit() {
        this.service.getActiveFacility().subscribe(res => this.facility = res);
        this.topicSubscription = this.stompService.watch('/topic/devolve-report/status').subscribe((msg: Message) => {
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
        this.service.devolveReport({
            facilityId: this.facility.id,
            cparp: this.cparp
        }).subscribe((res) => {
            const file = new File([res], this.facility.name + (this.cparp ? '_CPARP Devolvement Report.pdf'
                : '_Devolvment Report.pdf'), {type: 'application/octet-stream'});
            saveAs(file);
        })
    }

    ngOnDestroy(): void {
        this.topicSubscription.unsubscribe()
    }
}
