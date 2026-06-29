// ── 탭 전환 ──
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
        tab.classList.add('active');
        document.getElementById('panel-' + tab.dataset.tab).classList.add('active');
    });
});

// ── RAG 선택 시 모델 선택 노출 ──
document.querySelectorAll('input[name=search-type]').forEach(r => {
    r.addEventListener('change', () => {
        const wrap = document.getElementById('model-select-wrap');
        wrap.classList.toggle('visible', r.value === 'rag' && r.checked);
    });
});

// ── Enter 키 처리 ──
document.getElementById('search-input').addEventListener('keydown', e => {
    if (e.key === 'Enter') doSearch();
});

document.getElementById('chat-input').addEventListener('keydown', e => {
    if (e.key === 'Enter') doChat();
});
document.getElementById('chat-send').addEventListener('click', doChat);

// ── 도서 검색 ──
async function doSearch() {
    const keyword = document.getElementById('search-input').value.trim();
    if (!keyword) return;

    const type = document.querySelector('input[name=search-type]:checked').value;
    const model = document.getElementById('model-select').value;

    const loading = document.getElementById('search-loading');
    const error = document.getElementById('search-error');
    const results = document.getElementById('search-results');

    loading.style.display = 'block';
    error.style.display = 'none';
    results.innerHTML = '';

    try {
        if (type === 'rag') {
            const res = await fetch(`/books/recommend/${model}?question=${encodeURIComponent(keyword)}`);
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }
            const data = await res.json();
            renderRag(data, results);
        } else {
            const res = await fetch(`/books/search?keyword=${encodeURIComponent(keyword)}&searchType=${type}&page=0&size=10`);
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }
            const data = await res.json();
            renderSearch(data, results);
        }
    } catch (e) {
        error.textContent = `검색 중 오류가 발생했습니다: ${e.message}`;
        error.style.display = 'block';
        results.innerHTML = '<div class="empty"><i class="ti ti-alert-circle"></i><p>결과를 불러올 수 없습니다</p></div>';
    } finally {
        loading.style.display = 'none';
    }
}

// ── 일반 검색 결과 렌더링 ──
function renderSearch(data, container) {
    const books = data.content || [];

    if (books.length === 0) {
        container.innerHTML = '<div class="empty"><i class="ti ti-book-off"></i><p>검색 결과가 없습니다</p></div>';
        return;
    }

    const total = data.totalElements || books.length;
    let html = `<div class="result-meta">${total.toLocaleString()}건 중 ${books.length}건</div>`;

    books.forEach((book, i) => {
        const content = book.bookContent
            ? book.bookContent.substring(0, 150) + (book.bookContent.length > 150 ? '...' : '')
            : '';

        const ratingBadge = book.averageRating && book.reviewCount
            ? `<span class="badge badge-rating"><i class="ti ti-star-filled" style="font-size:11px;" aria-hidden="true"></i> ${book.averageRating.toFixed(1)} (${book.reviewCount}개)</span>`
            : '';

        html += `
      <div class="book-card">
        <div class="book-rank">${i + 1}</div>
        <div class="book-info">
          <div class="book-title">${escapeHtml(book.title || '')}</div>
          <div class="book-meta">${escapeHtml(book.authorName || '')} · ${escapeHtml(book.publisherName || '')} ${ratingBadge}</div>
          ${content ? `<div class="book-content">${escapeHtml(content)}</div>` : ''}
          ${book.reviewSummary ? `<div class="book-why">${escapeHtml(book.reviewSummary.substring(0, 100))}</div>` : ''}
        </div>
      </div>`;
    });

    container.innerHTML = html;
}

// ── RAG 추천 결과 렌더링 ──
function renderRag(data, container) {
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="empty"><i class="ti ti-book-off"></i><p>추천 결과가 없습니다</p></div>';
        return;
    }

    let html = `<div class="result-meta">AI 추천 도서 ${data.length}건</div>`;

    data.forEach((item, i) => {
        html += `
      <div class="book-card">
        <div class="book-rank">${i + 1}</div>
        <div class="book-info">
          <div class="book-title">도서 ID: ${item.id}</div>
          <div class="book-meta">
            <span class="badge badge-relevance">관련성 ${item.relevance}점</span>
          </div>
          <div class="book-why">${escapeHtml(item.why || '')}</div>
        </div>
      </div>`;
    });

    container.innerHTML = html;
}

// ── 챗봇 ──
async function doChat() {
    const input = document.getElementById('chat-input');
    const question = input.value.trim();
    if (!question) return;

    const messages = document.getElementById('chat-messages');
    const sendBtn = document.getElementById('chat-send');

    input.value = '';
    sendBtn.disabled = true;

    messages.innerHTML += `
    <div class="msg user">
      <div class="msg-avatar">나</div>
      <div class="msg-bubble">${escapeHtml(question)}</div>
    </div>`;

    const typingId = 'typing-' + Date.now();
    messages.innerHTML += `
    <div class="msg bot" id="${typingId}">
      <div class="msg-avatar"><i class="ti ti-robot" aria-hidden="true"></i></div>
      <div class="typing"><span></span><span></span><span></span></div>
    </div>`;

    messages.scrollTop = messages.scrollHeight;

    try {
        const res = await fetch(`/chat?question=${encodeURIComponent(question)}&model=ollama`);
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }
        const text = await res.text();

        document.getElementById(typingId)?.remove();

        messages.innerHTML += `
      <div class="msg bot">
        <div class="msg-avatar"><i class="ti ti-robot" aria-hidden="true"></i></div>
        <div class="msg-bubble">${escapeHtml(text)}</div>
      </div>`;
    } catch (e) {
        document.getElementById(typingId)?.remove();

        messages.innerHTML += `
      <div class="msg bot">
        <div class="msg-avatar"><i class="ti ti-robot" aria-hidden="true"></i></div>
        <div class="msg-bubble" style="color:#a32d2d;">오류가 발생했습니다: ${escapeHtml(e.message)}</div>
      </div>`;
    } finally {
        sendBtn.disabled = false;
        messages.scrollTop = messages.scrollHeight;
    }
}

// ── 유틸 ──
function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>');
}