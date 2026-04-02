import { Routes } from '@angular/router';

import { ChatPageComponent } from './pages/chat/chat.component';

// No routes necessary: everything is loaded on the main app.component layout
export const routes: Routes = [
    { path: 'chat', component: ChatPageComponent },
    { path: '', redirectTo: 'chat', pathMatch: 'full' },
];
