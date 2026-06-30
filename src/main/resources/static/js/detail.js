const bookId = document.getElementById('detail-wrap').dataset.bookId;

window.addEventListener('DOMContentLoaded', async () => {
    await loadBookDetail();
    await loadReviews();
});

// ── 도서 상세 조회 ──
async function loadBookDetail() {
    const loading = document.getElementById('detail-loading');
    const error = document.getElementById('detail-error');
    const content = document.getElementById('detail-content');

    loading.style.display = 'block';

    try {
        const res = await fetch(`/api/books/${bookId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const book = await res.json();

        document.title = `${book.title} - AI 도서관`;

        const imgHtml = book.imageUrl
            ? `<img src="${escapeHtml(book.imageUrl)}" alt="${escapeHtml(book.title || '')}"
             onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
         <div class="detail-img-placeholder" style="display:none;"><i class="ti ti-book" aria-hidden="true"></i></div>`
            : `<div class="detail-img-placeholder"><i class="ti ti-book" aria-hidden="true"></i></div>`;

        const ratingHtml = book.averageRating && book.reviewCount
            ? `<span class="badge badge-rating">
           <i class="ti ti-star-filled" style="font-size:11px;" aria-hidden="true"></i>
           ${parseFloat(book.averageRating).toFixed(1)} (${book.reviewCount}개 리뷰)
         </span>`
            : '';

        content.innerHTML = `
      <div class="detail-card">
        <div class="detail-img">${imgHtml}</div>
        <div class="detail-info">
          <div class="detail-title">${escapeHtml(book.title || '')}</div>
          <div class="detail-meta">
            <span>${escapeHtml(book.authorName || '')}</span>
            <span>${escapeHtml(book.publisherName || '')}</span>
            ${book.editionPublishDate ? `<span>${book.editionPublishDate}</span>` : ''}
          </div>
          ${ratingHtml}
          ${book.bookContent ? `
            <div class="detail-section">
              <div class="detail-section-title">도서 소개</div>
              <div class="detail-body">${escapeHtml(book.bookContent)}</div>
            </div>` : ''}
          ${book.reviewSummary ? `
            <div class="detail-section">
              <div class="detail-section-title">AI 리뷰 요약</div>
              <div class="detail-review-summary">${escapeHtml(book.reviewSummary.trim())}</div>
            </div>` : ''}
        </div>
      </div>
    `;
    } catch (e) {
        error.textContent = `도서 정보를 불러올 수 없습니다: ${e.message}`;
        error.style.display = 'block';
    } finally {
        loading.style.display = 'none';
    }
}

// ── 리뷰 조회 ──
async function loadReviews() {
    try {
        const res = await fetch(`/api/books/${bookId}/reviews`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        renderReviewStats(data);
        renderReviewList(data.reviews);
    } catch (e) {
        document.getElementById('review-stats').innerHTML =
            `<p style="color:#a32d2d;">리뷰를 불러올 수 없습니다: ${e.message}</p>`;
    }
}

// ── 리뷰 통계 렌더링 ──
function renderReviewStats(data) {
    const statsEl = document.getElementById('review-stats');

    if (!data.averageRating || data.reviewCount === 0) {
        statsEl.innerHTML = '';
        return;
    }

    const total = data.reviewCount;
    const bars = [5, 4, 3, 2, 1].map(star => {
        const count = data[`rating${star}Count`] || 0;
        const pct = total > 0 ? Math.round((count / total) * 100) : 0;
        return `
      <div class="rating-bar-row">
        <span class="rating-bar-label">${star}점</span>
        <div class="rating-bar-track">
          <div class="rating-bar-fill" style="width:${pct}%"></div>
        </div>
        <span class="rating-bar-count">${count}</span>
      </div>`;
    }).join('');

    statsEl.innerHTML = `
    <div class="review-stats-wrap">
      <div class="review-avg">
        <div class="review-avg-score">${parseFloat(data.averageRating).toFixed(1)}</div>
        <div class="review-avg-label">/ 5.0</div>
        <div class="review-avg-count">${data.reviewCount}개 리뷰</div>
      </div>
      <div class="rating-bars">${bars}</div>
    </div>`;
}

// ── 리뷰 목록 렌더링 ──
function renderReviewList(reviews) {
    const listEl = document.getElementById('review-list');

    if (!reviews || reviews.length === 0) {
        listEl.innerHTML = '<p class="review-empty">첫 번째 리뷰를 작성해보세요!</p>';
        return;
    }

    listEl.innerHTML = reviews.map(review => `
    <div class="review-item">
      <div class="review-item-header">
        <span class="review-rating">${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</span>
        <span class="review-date">${new Date(review.createdAt).toLocaleDateString('ko-KR')}</span>
      </div>
      <div class="review-content">${escapeHtml(review.content)}</div>
    </div>`).join('');
}

// ── 리뷰 작성 ──
async function submitReview() {
    const content = document.getElementById('review-content').value.trim();
    const ratingEl = document.querySelector('input[name=review-rating]:checked');

    if (!content) {
        alert('리뷰 내용을 입력하세요.');
        return;
    }
    if (!ratingEl) {
        alert('별점을 선택하세요.');
        return;
    }

    const rating = parseInt(ratingEl.value);

    try {
        const res = await fetch(`/api/books/${bookId}/reviews`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({content, rating})
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // 폼 초기화
        document.getElementById('review-content').value = '';
        document.querySelectorAll('input[name=review-rating]').forEach(r => r.checked = false);

        // 리뷰 목록 갱신
        await loadReviews();
    } catch (e) {
        alert(`리뷰 작성에 실패했습니다: ${e.message}`);
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