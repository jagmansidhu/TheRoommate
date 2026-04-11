import React, { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import '../styling/Home.css';


const MARQUEE_ITEMS = [
    'Chore rotation', '·', 'Shared groceries', '·',
    'Bill splitting', '·', 'Room invites', '·',
    'Shared calendar', '·', 'Smart reminders', '·',
    'Chore rotation', '·', 'Shared groceries', '·',
    'Bill splitting', '·', 'Room invites', '·',
    'Shared calendar', '·', 'Smart reminders', '·',
];

const STEPS = [
    {
        num: '01', icon: '🏠',
        title: 'Create your room',
        desc: 'Set up your shared space in under a minute. Name it, and you\'re live.',
    },
    {
        num: '02', icon: '🤝',
        title: 'Invite your crew',
        desc: 'Send a room code. Roommates join instantly — no extra accounts.',
    },
    {
        num: '03', icon: '⚡',
        title: 'Live in sync',
        desc: 'Chores, bills, groceries — all real-time, all in one place.',
    },
];

const Home = () => {
    const observerRef = useRef(null);

    useEffect(() => {
        observerRef.current = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) entry.target.classList.add('visible');
                });
            },
            { threshold: 0.08, rootMargin: '0px 0px -48px 0px' }
        );
        document.querySelectorAll('.reveal').forEach((el) => observerRef.current.observe(el));
        return () => observerRef.current?.disconnect();
    }, []);

    return (
        <div className="landing-page">

            {/* ── HERO ── */}
            <section className="hero-section">

                {/* Text column */}
                <div className="hero-text-col">
                    <span className="hero-eyebrow">
                        <span className="eyebrow-line" />
                        Shared living, sorted
                    </span>

                    <h1 className="hero-title">
                        <span className="hero-title-normal">Your home,</span>
                        <span className="hero-accent">finally in sync.</span>
                    </h1>

                    <p className="hero-sub">
                        Chores, bills, groceries — handled together.
                        TheRoommate turns shared living from stressful to seamless.
                    </p>

                    <div className="hero-actions">
                        <Link to="/register" className="btn-primary-lp" id="hero-cta-register">
                            Get Started
                            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                                <path d="M2.5 7h9M8 3.5L11.5 7 8 10.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </Link>
                        <Link to="/login" className="btn-ghost-lp" id="hero-cta-login">
                            Sign in
                        </Link>
                    </div>
                </div>

            </section>

            {/* ── MARQUEE STRIP ── */}
            <div className="marquee-strip" aria-hidden="true">
                <div className="marquee-track">
                    {MARQUEE_ITEMS.map((item, i) => (
                        <span key={i} className={item === '·' ? 'marquee-dot' : ''}>
                            {item}
                        </span>
                    ))}
                    {/* Duplicate for seamless loop */}
                    {MARQUEE_ITEMS.map((item, i) => (
                        <span key={`b-${i}`} className={item === '·' ? 'marquee-dot' : ''}>
                            {item}
                        </span>
                    ))}
                </div>
            </div>

            {/* ── FEATURES BENTO ── */}
            <section className="features-section" id="features">

                <div className="features-header">
                    <div>
                        <span className="section-kicker">What we built</span>
                        <h2 className="section-heading">
                            Built for the way<br />
                            you <em>actually</em> live
                        </h2>
                    </div>
                    <p className="features-header-right">
                        Six tools woven into one shared space.
                        No switching apps, no group chats,
                        no passive-aggressive sticky notes.
                    </p>
                </div>

                <div className="features-bento">

                    {/* Chores — big */}
                    <div className="bento-card bento-card--big">
                        <span className="bento-icon">🗂️</span>
                        <h3>Chore Schedules</h3>
                        <p>Rotating assignments that auto-update. Everyone knows their job.</p>
                        <div className="chore-preview">
                            <div className="cp-row">
                                <span className="cp-avatar">A</span>
                                <span>Vacuum living room</span>
                                <span className="cp-done">✓</span>
                            </div>
                            <div className="cp-row">
                                <span className="cp-avatar" style={{ background: '#C94A1A' }}>M</span>
                                <span>Do the dishes</span>
                                <span className="cp-done">✓</span>
                            </div>
                            <div className="cp-row">
                                <span className="cp-avatar" style={{ background: '#1E3B2A' }}>S</span>
                                <span>Take out trash</span>
                                <span className="cp-pending">○</span>
                            </div>
                        </div>
                    </div>

                    {/* Expenses — tall */}
                    <div className="bento-card bento-card--tall">
                        <span className="bento-icon">💰</span>
                        <h3>Expense Splitting</h3>
                        <p>Fair ledgers, clear balances. No math required.</p>
                        <div className="expense-bars">
                            {[
                                { name: 'Rent',      pct: 75, color: '#1E3B2A' },
                                { name: 'Utilities', pct: 45, color: '#C94A1A' },
                                { name: 'Internet',  pct: 30, color: '#8A7E72' },
                            ].map(b => (
                                <div className="ebar" key={b.name}>
                                    <span>{b.name}</span>
                                    <div className="ebar-track">
                                        <div className="ebar-fill" style={{ width: `${b.pct}%`, background: b.color }} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Grocery */}
                    <div className="bento-card bento-card--small">
                        <span className="bento-icon">🛒</span>
                        <h3>Shared Groceries</h3>
                        <p>One list, everyone adds. Zero duplicate oat milk.</p>
                    </div>

                    {/* Calendar */}
                    <div className="bento-card bento-card--small">
                        <span className="bento-icon">📅</span>
                        <h3>Shared Calendar</h3>
                        <p>House events, guests, maintenance — always synced.</p>
                    </div>

                    {/* Rooms — wide */}
                    <div className="bento-card bento-card--wide">
                        <span className="bento-icon">🏠</span>
                        <h3>Room Management</h3>
                        <p>Create a digital home for your space. Invite with a code — no friction.</p>
                        <div className="invite-demo">
                            <span className="invite-code">ROOM-4829</span>
                            <span className="invite-tag">Copy invite</span>
                        </div>
                    </div>

                    {/* Reminders */}
                    <div className="bento-card bento-card--mini">
                        <span className="bento-icon">🔔</span>
                        <h3>Reminders</h3>
                        <p>Gentle nudges so nothing slips through the cracks.</p>
                    </div>

                </div>
            </section>

            {/* ── HOW IT WORKS ── */}
            <section className="hiw-section" id="how-it-works">
                <div className="hiw-inner">

                    <div className="hiw-header reveal">
                        <span className="section-kicker">How it works</span>
                        <h2 className="section-heading">
                            Three steps<br />to <em>harmony</em>
                        </h2>
                    </div>

                    <div className="hiw-steps">
                        {STEPS.map((step, i) => (
                            <div className="hiw-step reveal" key={step.num} style={{ '--i': i }}>
                                <div className="hiw-step-num">{step.num}</div>
                                <div className="hiw-step-icon">{step.icon}</div>
                                <h3>{step.title}</h3>
                                <p>{step.desc}</p>
                            </div>
                        ))}
                    </div>


                </div>
            </section>

            {/* ── CTA ── */}
            <section className="cta-section">
                <div className="cta-inner reveal">
                    <span className="cta-kicker">Start today — it's free</span>
                    <h2>
                        Ready for a<br />
                        <em>better way</em> to live?
                    </h2>
                    <p>Start managing your shared space the smart way.</p>
                    <div className="cta-actions">
                        <Link to="/register" className="btn-cta-primary" id="cta-register-btn">
                            Create Free Account
                            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                                <path d="M2.5 7h9M8 3.5L11.5 7 8 10.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </Link>
                        <Link to="/login" className="btn-cta-ghost" id="cta-login-btn">
                            Already have an account?
                        </Link>
                    </div>
                </div>
            </section>

            {/* ── FOOTER ── */}
            <footer className="landing-footer">
                <div className="footer-inner">
                    <div>
                        <span className="footer-logo">TheRoommate</span>
                        <p className="footer-tagline">Making shared living simple.</p>
                    </div>
                    <nav className="footer-nav">
                        <a href="#features">Features</a>
                        <a href="#how-it-works">How it Works</a>
                        <Link to="/register">Get Started</Link>
                        <Link to="/login">Sign In</Link>
                    </nav>
                    <span className="footer-copy">© 2026 TheRoommate</span>
                </div>
            </footer>

        </div>
    );
};

export default Home;