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
    if (e.key === 'Enter') doSearch(0);
});

document.getElementById('chat-input').addEventListener('keydown', e => {
    if (e.key === 'Enter') doChat();
});

// ── 현재 검색 상태 저장 (페이징용) ──
let currentKeyword = '';
let currentType = 'keyword';
let currentModel = 'gemini';
const PAGE_SIZE = 20;

// ── 도서 검색 ──
async function doSearch(page) {
    const keyword = document.getElementById('search-input').value.trim();
    if (!keyword) return;

    currentKeyword = keyword;
    currentType = document.querySelector('input[name=search-type]:checked').value;
    currentModel = document.getElementById('model-select').value;

    const loading = document.getElementById('search-loading');
    const error = document.getElementById('search-error');
    const results = document.getElementById('search-results');
    const pagination = document.getElementById('pagination');

    loading.style.display = 'block';
    error.style.display = 'none';
    results.innerHTML = '';
    pagination.innerHTML = '';

    try {
        if (currentType === 'rag') {
            const res = await fetch(`/api/books/recommend/${currentModel}?question=${encodeURIComponent(keyword)}`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            renderRag(data, results);
        } else {
            const res = await fetch(`/api/books/search?keyword=${encodeURIComponent(keyword)}&searchType=${currentType}&page=${page}&size=${PAGE_SIZE}`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            renderSearch(data, results, page);
            renderPagination(data, page, pagination);
        }
    } catch (e) {
        error.textContent = `검색 중 오류가 발생했습니다: ${e.message}`;
        error.style.display = 'block';
        results.innerHTML = '<div class="empty"><i class="ti ti-alert-circle"></i><p>결과를 불러올 수 없습니다</p></div>';
    } finally {
        loading.style.display = 'none';
    }
}

// ── 일반 검색 결과 렌더링 (그리드) ──
function renderSearch(data, container, page) {
    const books = data.content || [];

    if (books.length === 0) {
        container.innerHTML = '<div class="empty"><i class="ti ti-book-off"></i><p>검색 결과가 없습니다</p></div>';
        return;
    }

    const total = data.totalElements || books.length;
    let html = `<div class="result-meta">${total.toLocaleString()}건 (${page + 1}페이지)</div>`;
    html += '<div class="book-grid">';

    books.forEach(book => {
        const imgHtml = book.imageUrl
            ? `<img class="book-card-img" src="${escapeHtml(book.imageUrl)}" alt="${escapeHtml(book.title || '')}" onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
         <div class="book-card-img-placeholder" style="display:none;"><i class="ti ti-book" aria-hidden="true"></i></div>`
            : `<div class="book-card-img-placeholder"><i class="ti ti-book" aria-hidden="true"></i></div>`;

        const ratingHtml = book.averageRating && book.reviewCount
            ? `<div class="book-card-rating"><i class="ti ti-star-filled" style="font-size:11px;" aria-hidden="true"></i> ${book.averageRating.toFixed(1)} (${book.reviewCount}개)</div>`
            : '';

        html += `
      <a class="book-card" href="/books/${book.id}">
        ${imgHtml}
        <div class="book-card-body">
          <div class="book-card-title">${escapeHtml(book.title || '')}</div>
          <div class="book-card-author">${escapeHtml(book.authorName || '')}</div>
          ${ratingHtml}
        </div>
      </a>`;
    });

    html += '</div>';
    container.innerHTML = html;
}

// ── 페이징 렌더링 ──
function renderPagination(data, currentPage, container) {
    const totalPages = data.totalPages || 0;
    if (totalPages <= 1) return;

    const groupSize = 5;
    const groupStart = Math.floor(currentPage / groupSize) * groupSize;
    const groupEnd = Math.min(groupStart + groupSize, totalPages);

    let html = '';

    if (groupStart > 0) {
        html += `<button class="page-btn" onclick="doSearch(${groupStart - 1})"><i class="ti ti-chevron-left" aria-hidden="true"></i></button>`;
    }

    for (let i = groupStart; i < groupEnd; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="doSearch(${i})">${i + 1}</button>`;
    }

    if (groupEnd < totalPages) {
        html += `<button class="page-btn" onclick="doSearch(${groupEnd})"><i class="ti ti-chevron-right" aria-hidden="true"></i></button>`;
    }

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
        const imgHtml = item.imageUrl
            ? `<img class="rag-card-img" src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.title || '')}"
             onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
         <div class="rag-card-img-placeholder" style="display:none;"><i class="ti ti-book" aria-hidden="true"></i></div>`
            : `<div class="rag-card-img-placeholder"><i class="ti ti-book" aria-hidden="true"></i></div>`;

        html += `
      <a class="rag-card" href="/books/${item.id}">
        <div class="rag-rank">${i + 1}</div>
        <div class="rag-card-img-wrap">${imgHtml}</div>
        <div class="rag-info">
          <div class="rag-title">${escapeHtml(item.title || '')}</div>
          <div class="rag-meta">${escapeHtml(item.authorName || '')} · ${escapeHtml(item.publisherName || '')}</div>
          <div><span class="badge badge-relevance">관련성 ${item.relevance}점</span></div>
          <div class="rag-why">${escapeHtml(item.why || '')}</div>
        </div>
      </a>`;
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
        const res = await fetch(`/api/chat?question=${encodeURIComponent(question)}&model=ollama`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
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