import { Injectable } from '@angular/core';

export interface ChatRequest {
  query: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/chat';

  async *streamQuery(query: string): AsyncGenerator<string, void, unknown> {
    const response = await fetch(`${this.apiUrl}/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({ query })
    });

    if (!response.body) throw new Error('No response body');

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      // Process complete lines from the buffer
      let lineEnd: number;
      while ((lineEnd = buffer.indexOf('\n')) !== -1) {
        const line = buffer.slice(0, lineEnd);
        buffer = buffer.slice(lineEnd + 1);

        // A blank line means end-of-event in SSE — we don't need to act on it
        // since we yield each data line immediately
        if (line.trim() === '') continue;

        if (line.startsWith('data:')) {
          // Strip only "data:" (5 chars). The space (if any) at index 5 may be
          // part of the token content from Spring AI, so keep it.
          const text = line.slice(5);

          if (text.trim() === '[DONE]') continue;

          // When the LLM emits a newline token, Spring AI sends "data:" with
          // no payload. We must yield "\n" so Markdown can recognize paragraphs
          // and headers. Without this, all text runs together.
          if (text === '') {
            yield '\n';
            continue;
          }

          yield text;
        }
      }
    }

    // Flush remaining data in buffer
    if (buffer.startsWith('data:')) {
      const text = buffer.slice(5);
      if (text && text.trim() !== '[DONE]') yield text;
    }
  }
}