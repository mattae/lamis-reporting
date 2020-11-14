import {Inject, Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {DATE_FORMAT, SERVER_API_URL_CONFIG, ServerApiUrlConfig} from "@lamis/web-core";
import {Facility} from "../components/art-summary/art-summary.component";
import {Observable} from "rxjs";
import * as moment_ from 'moment';

const moment = moment_;

@Injectable({
    providedIn: 'root'
})
export class ReportService {
    public resourceUrl = '';

    constructor(private http: HttpClient, @Inject(SERVER_API_URL_CONFIG) private serverUrl: ServerApiUrlConfig) {
        this.resourceUrl = serverUrl.SERVER_API_URL + '/api/reporting';
    }

    artSummary(reportingPeriod: Date, id: number, today: boolean) {
        let params = new HttpParams();
        params = params.append('reportingPeriod', moment(reportingPeriod).format(DATE_FORMAT));
        params = params.append("id", id.toString());
        params = params.append("today", today.toString());
        return this.http.get(`${this.resourceUrl}/art-summary`, {params, responseType: 'blob'})
    }

    patientLineList(params: any) {
        params.dateCurrentStatusBegin = params.dateCurrentStatusBegin != null && params.dateCurrentStatusBegin.isValid() ? params.dateCurrentStatusBegin.format(DATE_FORMAT) : null;
        params.dateCurrentStatusEnd = params.dateCurrentStatusEnd != null && params.dateCurrentStatusEnd.isValid() ? params.dateCurrentStatusEnd.format(DATE_FORMAT) : null;
        params.dateLastViralLoadBegin = params.dateLastViralLoadBegin != null && params.dateLastViralLoadBegin.isValid() ? params.dateLastViralLoadBegin.format(DATE_FORMAT) : null;
        params.dateLastViralLoadEnd = params.dateLastViralLoadEnd != null && params.dateLastViralLoadEnd.isValid() ? params.dateLastViralLoadEnd.format(DATE_FORMAT) : null;
        params.dateRegistrationBegin = params.dateRegistrationBegin != null && params.dateRegistrationBegin.isValid() ? params.dateRegistrationBegin.format(DATE_FORMAT) : null;
        params.dateRegistrationEnd = params.dateRegistrationEnd != null && params.dateRegistrationEnd.isValid() ? params.dateRegistrationEnd.format(DATE_FORMAT) : null;
        params.dateStartedBegin = params.dateStartedBegin != null && params.dateStartedBegin.isValid() ? params.dateStartedBegin.format(DATE_FORMAT) : null;
        params.dateStartedEnd = params.dateStartedEnd != null && params.dateStartedEnd.isValid() ? params.dateStartedEnd.format(DATE_FORMAT) : null;
        return this.http.post(`${this.resourceUrl}/patient-line-list`, params, {responseType: 'blob'})
    }

    biometricReport(param: any) {
        let params = new HttpParams();
        params = params.append('start', moment(param.start).format(DATE_FORMAT));
        params = params.append("facilityId", param.facilityId.toString());
        params = params.append('end', moment(param.end).format(DATE_FORMAT));
        return this.http.get(`${this.resourceUrl}/biometric-report`, {params, responseType: 'blob'})
    }

    devolveReport(param: any) {
        let params = new HttpParams();
        params = params.append("facilityId", param.facilityId.toString());
        params = params.append("cparp", param.cparp.toString());
        params = params.append('end', moment(param.end).format(DATE_FORMAT));
        return this.http.get(`${this.resourceUrl}/devolve-report`, {params, responseType: 'blob'})
    }

    appointmentReport(param: any) {
        let params = new HttpParams();
        params = params.append('start', moment(param.start).format(DATE_FORMAT));
        params = params.append("facilityId", param.facilityId.toString());
        params = params.append("type", param.type.toString());
        params = params.append('end', moment(param.end).format(DATE_FORMAT));
        return this.http.get(`${this.resourceUrl}/appointment-report`, {params, responseType: 'blob'})
    }

    convertData(ids: number[], report: number, labTest?: number) {
        let params = new HttpParams();
        ids.forEach(id => params = params.append("ids", id.toString()));
        if (!!labTest) {
            params = params.append("labTest", labTest.toString());
        }
        params = params.append("report", report.toString());
        return this.http.get(`${this.resourceUrl}/convert-data`, {params, responseType: 'blob'})
    }

    getRegimenTypes() {
        return this.http.get<any[]>(`${this.resourceUrl}/regimen-types`)
    }


    getStates() {
        return this.http.get<any[]>('/api/states')
    }

    getLgasByState(id) {
        return this.http.get<any[]>(`/api/provinces/state/${id}`)
    }

    getActiveFacility() {
        return this.http.get<Facility>('/api/facilities/active')
    }

    listFacilities() {
        return this.http.get<Facility[]>(`${this.resourceUrl}/list-facilities`)
    }

    labTest() {
        return this.http.get<any[]>(`${this.resourceUrl}/lab-tests`)
    }

    download(name: string): Observable<Blob> {
        return this.http.get(`${this.resourceUrl}/download/${name}`, {responseType: 'blob'})
    }

    listFiles() {
        return this.http.get<string[]>(`${this.resourceUrl}/list-files`)
    }
}
