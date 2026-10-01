import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import '../styling/Home.css';

const TOUR_TABS = [
    {
        id: 'room',
        label: 'Room',
        src: '/landing/tour-room.webp',
        width: 2240,
        height: 1770,
        alt: 'Oak Street room: members Maya, Jordan, and Sam; internet, electric, and water splits; chores on a dated timeline.',
        caption: 'Oak Street — members, a join code, bills split, chores on a timeline.',
    },
    {
        id: 'today',
        label: 'Today',
        src: '/landing/tour-today.webp',
        width: 2240,
        height: 1416,
        alt: 'Dashboard for Maya: one chore due today, upcoming internet and electric bills, water marked paid.',
        caption: 'Open the app and see what you owe the house today.',
    },
    {
        id: 'calendar',
        label: 'Calendar',
        src: '/landing/tour-calendar.webp',
        width: 2240,
        height: 1916,
        alt: 'August 2026 house calendar with a meeting, chores, a guest weekend, and utility due dates.',
        caption: 'House events, chores, and bills on one month.',
    },
    {
        id: 'budget',
        label: 'Budget',
        src: '/landing/tour-budget.webp',
        width: 2240,
        height: 1822,
        alt: 'Personal budget with receipt drop zone, $1,200 monthly target, and grocery and utility entries.',
        caption: 'Your spending and receipt uploads. Splitting a receipt to the room is still coming.',
    },
];

const LIVE_FEATURES = [
    {
        id: 'rooms',
        kicker: 'Rooms',
        title: 'A digital house, with a door code',
        copy: 'Create a room, join with a code, see occupancy and roles. Three rooms per person, six people per room.',
        src: '/landing/live-rooms.webp',
        width: 2240,
        height: 1070,
        alt: 'My Rooms list showing Oak Street (owner, 3 of 6) and Cedar House (member, 1 of 6), each with a join code.',
    },
    {
        id: 'room',
        kicker: 'Room',
        title: 'Chores rotate. Bills split.',
        copy: 'Assign recurring chores, split utilities by equal or custom share, invite by email. Owner, manager, member.',
        src: '/landing/tour-room.webp',
        width: 2240,
        height: 1770,
        alt: 'Oak Street room details with members, utility splits, and upcoming chores.',
    },
    {
        id: 'today',
        kicker: 'Today',
        title: 'What is due, on one screen',
        copy: 'The dashboard lists chores due today and bills in the next four weeks. Check them off when they are done.',
        src: '/landing/tour-today.webp',
        width: 2240,
        height: 1416,
        alt: 'Dashboard with chores due today and upcoming bills.',
    },
    {
        id: 'calendar',
        kicker: 'Calendar',
        title: 'The house month, not a group chat',
        copy: 'Put a house meeting or a guest weekend next to the chores and bills already on the calendar.',
        src: '/landing/tour-calendar.webp',
        width: 2240,
        height: 1916,
        alt: 'Shared calendar for August 2026 with events, chores, and bills.',
    },
    {
        id: 'budget',
        kicker: 'Budget',
        title: 'Your receipts, your ledger',
        copy: 'Track personal spending and drop in a receipt photo. This is yours — not the house split (yet).',
        src: '/landing/tour-budget.webp',
        width: 2240,
        height: 1822,
        alt: 'Personal budget overview, receipt upload, and recent grocery entries.',
    },
];

const COMING_FEATURES = [
    {
        title: 'Shared grocery lists',
        copy: 'The API can store a house list. There is no grocery screen in the app yet.',
    },
    {
        title: 'Room chat',
        copy: 'Realtime chat is scaffolded and not routed. Keep using the thread you already have.',
    },
    {
        title: 'Friends',
        copy: 'Not built. You join a room with a code or an invite email, not a friend graph.',
    },
    {
        title: 'Landlord documents',
        copy: 'A later product. This app is for the people who live in the house.',
    },
    {
        title: 'Receipt split to the room',
        copy: 'Receipts already land in personal budget. Sending a share to roommates is next.',
    },
];

const Home = () => {
    const [activeId, setActiveId] = useState(TOUR_TABS[0].id);
    const tabRefs = useRef({});

    const activeIndex = TOUR_TABS.findIndex((tab) => tab.id === activeId);

    useEffect(() => {
        TOUR_TABS.slice(1).forEach((tab) => {
            const img = new Image();
            img.src = tab.src;
        });
    }, []);

    const selectTab = (id, moveFocus = false) => {
        setActiveId(id);
        if (moveFocus) {
            tabRefs.current[id]?.focus();
        }
    };

    const onTabKeyDown = (event) => {
        const last = TOUR_TABS.length - 1;
        let nextIndex = activeIndex;

        if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
            nextIndex = (activeIndex + 1) % TOUR_TABS.length;
        } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
            nextIndex = (activeIndex - 1 + TOUR_TABS.length) % TOUR_TABS.length;
        } else if (event.key === 'Home') {
            nextIndex = 0;
        } else if (event.key === 'End') {
            nextIndex = last;
        } else {
            return;
        }

        event.preventDefault();
        selectTab(TOUR_TABS[nextIndex].id, true);
    };

    return (
        <div className="landing-page">
            <section className="hero-band">
                <div className="hero-copy">
                    <h1 className="hero-brand">TheRoommate</h1>
                    <p className="hero-line">Chores, bills, and the house calendar. One place.</p>
                </div>
                <div className="hero-actions">
                    <Link to="/register" className="btn btn-primary btn-lg" id="hero-cta-register">
                        Get started
                    </Link>
                    <Link to="/login" className="btn-link" id="hero-cta-login">
                        Sign in
                    </Link>
                </div>
            </section>

            <section className="tour-section" id="tour" aria-label="Product tour">
                <div
                    className="tour-tabs"
                    role="tablist"
                    aria-label="Product surfaces"
                    onKeyDown={onTabKeyDown}
                >
                    {TOUR_TABS.map((tab) => {
                        const selected = tab.id === activeId;
                        return (
                            <button
                                key={tab.id}
                                type="button"
                                role="tab"
                                id={`tour-tab-${tab.id}`}
                                aria-selected={selected}
                                aria-controls={`tour-panel-${tab.id}`}
                                tabIndex={selected ? 0 : -1}
                                className={`tour-tab${selected ? ' is-active' : ''}`}
                                ref={(el) => { tabRefs.current[tab.id] = el; }}
                                onClick={() => selectTab(tab.id)}
                            >
                                {tab.label}
                            </button>
                        );
                    })}
                </div>

                {TOUR_TABS.map((tab) => {
                    const selected = tab.id === activeId;
                    return (
                        <div
                            key={tab.id}
                            role="tabpanel"
                            id={`tour-panel-${tab.id}`}
                            aria-labelledby={`tour-tab-${tab.id}`}
                            hidden={!selected}
                            className="tour-panel"
                        >
                            {selected && (
                                <>
                                    <div className="tour-stage">
                                        <img
                                            key={tab.id}
                                            className="tour-shot"
                                            src={tab.src}
                                            alt={tab.alt}
                                            width={tab.width}
                                            height={tab.height}
                                            fetchPriority="high"
                                        />
                                    </div>
                                    <p className="tour-caption">{tab.caption}</p>
                                </>
                            )}
                        </div>
                    );
                })}
            </section>

            <section className="live-section" id="live">
                <header className="section-head">
                    <span className="section-kicker">Live now</span>
                    <h2 className="section-title">What you can use today</h2>
                </header>
                {LIVE_FEATURES.map((feature, index) => (
                    <article
                        key={feature.id}
                        className={`live-row${index % 2 === 1 ? ' live-row--flip' : ''}`}
                    >
                        <div className="live-shot">
                            <img
                                src={feature.src}
                                alt={feature.alt}
                                width={feature.width}
                                height={feature.height}
                                loading="lazy"
                            />
                        </div>
                        <div className="live-copy">
                            <span className="live-kicker">{feature.kicker}</span>
                            <h3>{feature.title}</h3>
                            <p>{feature.copy}</p>
                        </div>
                    </article>
                ))}
            </section>

            <section className="coming-section" id="coming">
                <header className="section-head">
                    <span className="section-kicker">Coming next</span>
                    <h2 className="section-title">On the list. Not in the app.</h2>
                </header>
                <div className="coming-grid">
                    {COMING_FEATURES.map((item) => (
                        <article key={item.title} className="coming-card">
                            <span className="coming-tag">Coming</span>
                            <h3>{item.title}</h3>
                            <p>{item.copy}</p>
                        </article>
                    ))}
                </div>
            </section>

            <section className="cta-band">
                <div className="cta-inner">
                    <h2>Create a room. Invite with a code or an email.</h2>
                    <p>Free to start. No landlord portal hiding behind the signup.</p>
                    <div className="cta-actions">
                        <Link to="/register" className="btn btn-primary btn-lg" id="cta-register-btn">
                            Get started
                        </Link>
                        <Link to="/login" className="btn-link" id="cta-login-btn">
                            Sign in
                        </Link>
                    </div>
                </div>
            </section>

            <footer className="landing-footer">
                <div className="footer-inner">
                    <div>
                        <span className="footer-logo">TheRoommate</span>
                        <p className="footer-tagline">Chores, bills, the house calendar.</p>
                    </div>
                    <nav className="footer-nav">
                        <a href="#tour">Tour</a>
                        <a href="#live">Live now</a>
                        <a href="#coming">Coming next</a>
                        <Link to="/register">Get started</Link>
                        <Link to="/login">Sign in</Link>
                    </nav>
                    <span className="footer-copy">© 2026 TheRoommate</span>
                </div>
            </footer>
        </div>
    );
};

export default Home;
