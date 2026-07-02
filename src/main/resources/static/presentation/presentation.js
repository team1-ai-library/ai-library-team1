document.addEventListener('keydown', (e) => {
    const target = document.activeElement;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
        return;
    }
    if (e.key === 'ArrowRight' || e.key === 'PageDown') {
        const next = document.querySelector('[data-nav="next"]');
        if (next) window.location.href = next.href;
    } else if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
        const prev = document.querySelector('[data-nav="prev"]');
        if (prev) window.location.href = prev.href;
    }
});