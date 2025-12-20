// Initialize Mermaid safely
if (typeof mermaid !== 'undefined') {
    try {
        mermaid.initialize({ startOnLoad: true, theme: 'default' });
    } catch (e) {
        console.warn('Mermaid failed to initialize:', e);
    }
}

// Mobile Menu
const hamburger = document.getElementById('hamburger');
const sidebarEl = document.getElementById('sidebar');
const overlay = document.getElementById('sidebarOverlay');

hamburger?.addEventListener('click', () => {
    sidebarEl.classList.toggle('open');
    overlay.classList.toggle('open');
});

overlay?.addEventListener('click', () => {
    sidebarEl.classList.remove('open');
    overlay.classList.remove('open');
});

// Close sidebar when clicking a link (mobile)
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
        sidebarEl.classList.remove('open');
        overlay.classList.remove('open');
    });
});

// Active nav link highlighting
const navLinks = document.querySelectorAll('.nav-link');
const sections = document.querySelectorAll('.section');


// Reading & Progress Bar
const progressBar = document.getElementById('progressBar');
const readingProgress = document.getElementById('readingProgress');

window.addEventListener('scroll', () => {
    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrolled = (winScroll / height) * 100;

    if (progressBar) progressBar.style.width = scrolled + '%';
    if (readingProgress) readingProgress.style.width = scrolled + '%';
});

// Back to Top
const backToTop = document.getElementById('backToTop');

window.addEventListener('scroll', () => {
    if (window.scrollY > 500) {
        backToTop.classList.add('visible');
    } else {
        backToTop.classList.remove('visible');
    }
});

backToTop?.addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

// Dark Mode
const themeToggle = document.getElementById('themeToggle');
const savedTheme = localStorage.getItem('theme');

// Function to update Mermaid theme
function updateMermaidTheme(isDark) {
    if (typeof mermaid !== 'undefined') {
        try {
            mermaid.initialize({
                startOnLoad: false,
                theme: isDark ? 'dark' : 'default'
            });
            // Re-render all mermaid diagrams
            document.querySelectorAll('.mermaid').forEach(el => {
                const code = el.textContent || el.innerText;
                el.removeAttribute('data-processed');
                el.innerHTML = code;
            });
            mermaid.init(undefined, '.mermaid');
        } catch (e) {
            console.warn('Mermaid theme update failed:', e);
        }
    }
}

if (savedTheme === 'dark') {
    document.body.classList.add('dark-mode');
    if (themeToggle) themeToggle.textContent = '☀️';
    // Update Mermaid on page load if dark mode
    document.addEventListener('DOMContentLoaded', () => updateMermaidTheme(true));
}

themeToggle?.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    themeToggle.textContent = isDark ? '☀️' : '🌙';
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateMermaidTheme(isDark);
});

// Search
const searchModal = document.getElementById('searchModal');
const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');

function openSearch() {
    searchModal.classList.add('open');
    searchInput.focus();
}

function closeSearch() {
    searchModal.classList.remove('open');
    searchInput.value = '';
    searchResults.innerHTML = '<div class="search-hint">Press ESC to close</div>';
}

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        openSearch();
    }
    if (e.key === 'Escape') {
        closeSearch();
    }
});

searchModal?.addEventListener('click', (e) => {
    if (e.target === searchModal) closeSearch();
});

function performSearch() {
    const query = searchInput.value.toLowerCase().trim();
    if (query.length < 2) {
        searchResults.innerHTML = '<div class="search-hint">Type at least 2 characters...</div>';
        return;
    }

    const results = [];
    sections.forEach(section => {
        const title = section.querySelector('.section-title')?.textContent || '';
        const content = section.textContent.toLowerCase();

        if (content.includes(query)) {
            // Fix: Check index bounds for substring
            const index = content.indexOf(query);
            const start = Math.max(0, index - 30);
            const end = Math.min(content.length, index + 50);
            const previewText = content.substring(start, end);

            results.push({
                id: section.id,
                title: title.replace(/^\d+/, '').trim(),
                preview: previewText
            });
        }
    });

    if (results.length === 0) {
        searchResults.innerHTML = '<div class="search-hint">No results found</div>';
    } else {
        searchResults.innerHTML = results.map(r => `
            <div class="search-result-item" onclick="goToSection('${r.id}')">
                <strong>${r.title}</strong>
                <div style="font-size:0.85rem;color:var(--gray-500);">...${r.preview}...</div>
            </div>
        `).join('');
    }
}

function goToSection(id) {
    closeSearch();
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
}

// Copy Button for Code Blocks
document.querySelectorAll('pre:not(.mermaid)').forEach(pre => {
    const wrapper = document.createElement('div');
    wrapper.className = 'code-wrapper';
    pre.parentNode.insertBefore(wrapper, pre);
    wrapper.appendChild(pre);

    const btn = document.createElement('button');
    btn.className = 'copy-btn';
    btn.textContent = 'Copy';

    btn.onclick = () => {
        navigator.clipboard.writeText(pre.textContent);
        btn.textContent = 'Copied!';
        btn.classList.add('copied');

        setTimeout(() => {
            btn.textContent = 'Copy';
            btn.classList.remove('copied');
        }, 2000);
    };

    wrapper.appendChild(btn);
});

// Lightbox Logic
const lightbox = document.createElement('div');
lightbox.className = 'lightbox';
lightbox.innerHTML = `
    <div class="lightbox-close">&times;</div>
    <img src="" alt="Full size screenshot" class="lightbox-img">
`;
document.body.appendChild(lightbox);

const lightboxImg = lightbox.querySelector('.lightbox-img');
const lightboxClose = lightbox.querySelector('.lightbox-close');

document.querySelectorAll('.screenshot-img').forEach(img => {
    img.style.cursor = 'zoom-in';
    img.addEventListener('click', () => {
        lightboxImg.src = img.src;
        lightbox.classList.add('active');
        document.body.style.overflow = 'hidden'; // Prevent scroll
    });
});

function closeLightbox() {
    lightbox.classList.remove('active');
    document.body.style.overflow = '';
}

lightboxClose.addEventListener('click', closeLightbox);
lightbox.addEventListener('click', (e) => {
    if (e.target === lightbox) closeLightbox();
});

// Close on ESC
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && lightbox.classList.contains('active')) {
        closeLightbox();
    }
});

// Enhanced Scroll-Spy (Intersection Observer)
const observerOptions = {
    root: null,
    rootMargin: '-20% 0px -70% 0px',
    threshold: 0
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const id = entry.target.getAttribute('id');
            navLinks.forEach(link => {
                link.classList.toggle('active', link.getAttribute('href') === `#${id}`);
            });
        }
    });
}, observerOptions);

sections.forEach(section => observer.observe(section));
