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
    selector: 'art-summary',
    templateUrl: './art-summary.component.html'
})
export class ArtSummaryComponent implements OnInit, OnDestroy {
    private topicSubscription: Subscription;
    facility: Facility;
    files: string[];
    running = false;
    message: any = 'Running';
    finished = false;
    reportingPeriod: Date = new Date();
    today = new Date();
    todaySelectable = true;

    current: boolean = false;

    constructor(private service: ReportService, private stompService: RxStompService, private domSanitizer: DomSanitizer) {
    }

    ngOnInit() {
        this.service.getActiveFacility().subscribe(res => this.facility = res);
        this.topicSubscription = this.stompService.watch('/topic/art-summary/status').subscribe((msg: Message) => {
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

    download(name: string) {
        this.service.download(name).subscribe(res => {
            const file = new File([res], name + 'ART Summary Report.pdf', {type: 'application/octet-stream'});
            saveAs(file);
        });
    }

    tabChanged(event) {
        if (event.index === 1) {
            this.service.listFiles().subscribe(res => {
                this.files = res;
            })
        }
    }

    monthChanged(month: Date) {
        this.todaySelectable = new Date().getMonth() === month.getMonth()
    }

    convert() {
        this.running = true;
        this.finished = false;
        this.service.artSummary(this.reportingPeriod, this.facility.id, this.current).subscribe((res) => {
            const file = new File([res], this.facility.name + '_ART Summary Report.pdf', {type: 'application/octet-stream'});
            saveAs(file);
        })
    }

    ngOnDestroy(): void {
        this.topicSubscription.unsubscribe()
    }
}
