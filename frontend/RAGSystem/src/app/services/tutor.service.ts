import { Injectable } from '@angular/core';

export interface TutorRequest {
  query: string;
  targetLanguage: string;
}

@Injectable({
  providedIn: 'root'
})
export class TutorService {
  private apiUrl = 'http://localhost:8080/api/tutor';

  async *streamQuery(query: string, targetLanguage: string): AsyncGenerator<string, void, unknown> {
    const response = await fetch(`${this.apiUrl}/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({ query, targetLanguage })
    });

    if (!response.body) throw new Error('No response body');

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      let lineEnd: number;
      while ((lineEnd = buffer.indexOf('\n')) !== -1) {
        const line = buffer.slice(0, lineEnd);
        buffer = buffer.slice(lineEnd + 1);

        if (line.trim() === '') continue;

        if (line.startsWith('data:')) {
          const text = line.slice(5);

          if (text.trim() === '[DONE]') continue;

          if (text === '') {
            yield '\n';
            continue;
          }

          yield text;
        }
      }
    }

    if (buffer.startsWith('data:')) {
      const text = buffer.slice(5);
      if (text && text.trim() !== '[DONE]') yield text;
    }
  }
}
