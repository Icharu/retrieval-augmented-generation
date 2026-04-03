import { Routes } from '@angular/router';

import { ChatPageComponent } from './pages/chat/chat.component';
import { TutorPageComponent } from './pages/tutor/tutor.component';

// No routes necessary: everything is loaded on the main app.component layout
export const routes: Routes = [
    { path: 'chat', component: ChatPageComponent },
    { path: 'tutor', component: TutorPageComponent },
    { path: '', redirectTo: 'chat', pathMatch: 'full' },
];
