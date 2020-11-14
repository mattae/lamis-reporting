import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDividerModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatProgressBarModule,
    MatRadioModule,
    MatSelectModule,
    MatTabsModule
} from '@angular/material';
import {RouterModule} from '@angular/router';
import {ArtSummaryComponent} from './components/art-summary/art-summary.component';
import {ROUTES} from './services/reports.route';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DropDownListModule} from '@syncfusion/ej2-angular-dropdowns';
import {DatePickerModule, DateRangePickerModule} from '@syncfusion/ej2-angular-calendars';
import {PatientLineListComponent} from './components/line-list/patient-line-list.component';
import {MatDateFormatModule} from '@lamis/web-core';
import {DataConversionComponent} from './components/data-conversion/data-conversion.component';
import {DevolveComponent} from './components/devolve/devolve.component';
import {BiometricReportComponent} from './components/biometric/biometric-report.component';
import {AppointmentReportComponent} from './components/appointment/appointment-report.component';

@NgModule({
    declarations: [
        AppointmentReportComponent,
        ArtSummaryComponent,
        BiometricReportComponent,
        DataConversionComponent,
        DevolveComponent,
        PatientLineListComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatInputModule,
        MatIconModule,
        MatDividerModule,
        MatCardModule,
        MatSelectModule,
        MatButtonModule,
        MatTabsModule,
        MatDatepickerModule,
        RouterModule.forChild(ROUTES),
        MatProgressBarModule,
        MatListModule,
        MatCheckboxModule,
        DateRangePickerModule,
        DropDownListModule,
        DatePickerModule,
        MatDateFormatModule,
        MatRadioModule
    ],
    exports: [
        ArtSummaryComponent,
        DataConversionComponent,
        PatientLineListComponent
    ],
    providers: []
})
export class ReportsModule {
}
