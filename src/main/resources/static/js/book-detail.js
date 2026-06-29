const params = new URLSearchParams(window.location.search);
const bookId = params.get('id');

function escapeHtml(str) {
    return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>');
}

async function loadBook() {
    const content = document.getElementById('content');

    if (!bookId) {
        content.innerHTML = `
            <div class="error-wrap">
                <i class="ti ti-alert-circle" aria-hidden="true"></i>
                도서 ID가 없습니다.
            </div>`;
        return;
    }

    try {
        const res = await fetch('/books/' + bookId);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const book = await res.json();

        document.title = (book.title || '도서 상세') + ' - AI 도서관';

        const ratingBadge = (book.averageRating && book.reviewCount)
            ? `<span class="badge badge-rating"><i class="ti ti-star-filled" style="font-size:11px;" aria-hidden="true"></i> ${Number(book.averageRating).toFixed(1)} (${book.reviewCount}개 리뷰)</span>`
            : '';
        const isbnBadge = book.isbn
            ? `<span class="badge badge-isbn">ISBN ${escapeHtml(book.isbn)}</span>`
            : '';
        const priceBadge = book.price
            ? `<span class="badge badge-price">${Number(book.price).toLocaleString()}원</span>`
            : '';

        const coverHtml = book.imageUrl
            ? `<img src="${escapeHtml(book.imageUrl)}" alt="도서 표지" onerror="this.outerHTML='<div class=\\'no-cover\\'><i class=\\'ti ti-book\\'></i></div>'">`
            : `<div class="no-cover"><i class="ti ti-book" aria-hidden="true"></i></div>`;

        const contentSection = book.bookContent ? `
            <div>
                <div class="detail-section-title">도서 소개</div>
                <div class="detail-section-body">${escapeHtml(book.bookContent)}</div>
            </div>` : '';

        const reviewSection = book.reviewSummary ? `
            <div>
                <div class="detail-section-title">리뷰 요약</div>
                <div class="review-summary-box">${escapeHtml(book.reviewSummary)}</div>
            </div>` : '';

        content.innerHTML = `
            <div class="detail-card">
                <div class="detail-hero">
                    <div class="detail-cover">${coverHtml}</div>
                    <div class="detail-meta">
                        <div class="detail-title">${escapeHtml(book.title || '')}</div>
                        ${book.volumeTitle ? `<div class="detail-volume">${escapeHtml(book.volumeTitle)}</div>` : ''}
                        <div class="detail-sub">
                            ${escapeHtml(book.authorName || '')}
                            ${book.publisherName ? ' · ' + escapeHtml(book.publisherName) : ''}
                            ${book.editionPublishDate ? ' · ' + book.editionPublishDate : ''}
                        </div>
                        <div class="detail-badges">
                            ${ratingBadge}${priceBadge}${isbnBadge}
                        </div>
                    </div>
                </div>
                <div class="detail-sections">
                    ${contentSection}
                    ${reviewSection}
                </div>
            </div>`;
    } catch (e) {
        content.innerHTML = `
            <div class="error-wrap">
                <i class="ti ti-alert-circle" aria-hidden="true"></i>
                도서 정보를 불러올 수 없습니다: ${escapeHtml(e.message)}
            </div>`;
    }
}

loadBook();
