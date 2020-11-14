import {Routes} from '@angular/router';
import {ArtSummaryComponent} from '../components/art-summary/art-summary.component';
import {PatientLineListComponent} from '../components/line-list/patient-line-list.component';
import {DataConversionComponent} from '../components/data-conversion/data-conversion.component';
import {BiometricReportComponent} from '../components/biometric/biometric-report.component';
import {AppointmentReportComponent} from '../components/appointment/appointment-report.component';
import {DevolveComponent} from '../components/devolve/devolve.component';


export const ROUTES: Routes = [
    {
        path: '',
        data: {
            title: 'Reports',
            breadcrumb: 'REPORTS'
        },
        children: [
            {
                path: 'art-summary',
                component: ArtSummaryComponent,
                data: {
                    breadcrumb: 'ART SUMMARY REPORT',
                    title: 'ART Summary Report'
                },
            },
            {
                path: 'biometric-report',
                component: BiometricReportComponent,
                data: {
                    breadcrumb: 'BIOMETRIC REPORT',
                    title: 'Biometric Report'
                },
            },
            {
                path: 'appointment-report',
                component: AppointmentReportComponent,
                data: {
                    breadcrumb: 'APPOINTMENT REPORT',
                    title: 'Appointment Report'
                },
            },
            {
                path: 'devolve-report',
                component: DevolveComponent,
                data: {
                    breadcrumb: 'DEVOLVE REPORT',
                    title: 'Devolve Report'
                },
            },
            {
                path: 'patients',
                children: [
                    {
                        path: 'line-list',
                        component: PatientLineListComponent,
                        data: {
                            breadcrumb: 'PATIENT LINE LIST',
                            title: 'Patient Line List'
                        }
                    }
                ],
                data: {
                    breadcrumb: 'PATIENT REPORTS',
                    title: 'Patient Reports'
                }
            },
            {
                path: 'data-conversion',
                component: DataConversionComponent,
                data: {
                    breadcrumb: 'DATA CONVERSION',
                    title: 'Data Conversion'
                }
            }
        ]
    }
];

