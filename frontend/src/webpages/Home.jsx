import React from 'react';
import { Link } from 'react-router-dom';
import '../styling/Home.css';

const Home = () => {
    return (
        <div className="landing-page">
            {/* Hero */}
            <section className="hero-section">
                <div className="hero-content">
                    <span className="hero-eyebrow">Roommate Management</span>
                    <h1 className="hero-title">
                        Living together,<br />
                        <span className="highlight">simplified.</span>
                    </h1>
                    <p className="hero-subtitle">
                        Track chores, split expenses, and stay organized with your roommates. 
                        No more awkward conversations.
                    </p>
                    <div className="hero-buttons">
                        <Link to="/register" className="btn btn-primary">
                            Get Started
                        </Link>
                        <Link to="/login" className="btn btn-secondary">
                            Sign In →
                        </Link>
                    </div>
                </div>
            </section>

            {/* Features */}
            <section className="features-section">
                <div className="container">
                    <div className="section-header">
                        <h2>Built for shared spaces</h2>
                    </div>
                    <div className="features-grid">
                        <div className="feature-card">
                            <h3>Chores</h3>
                            <p>Rotating schedules that actually work. Everyone knows what's expected.</p>
                        </div>
                        <div className="feature-card">
                            <h3>Expenses</h3>
                            <p>Split bills fairly. Track who owes what with transparent ledgers.</p>
                        </div>
                        <div className="feature-card">
                            <h3>Groceries</h3>
                            <p>Shared lists that sync instantly. No more duplicate purchases.</p>
                        </div>
                        <div className="feature-card">
                            <h3>Calendar</h3>
                            <p>Shared events and reminders. Stay on the same page.</p>
                        </div>
                        <div className="feature-card">
                            <h3>Reminders</h3>
                            <p>Gentle nudges so nothing slips through the cracks.</p>
                        </div>
                        <div className="feature-card">
                            <h3>Rooms</h3>
                            <p>Create spaces for different living situations. Simple invites.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* How It Works */}
            <section className="how-it-works-section">
                <div className="container">
                    <div className="section-header">
                        <h2>Three steps to harmony</h2>
                    </div>
                    <div className="steps-container">
                        <div className="step-item">
                            <h3>Create a room</h3>
                            <p>Set up your shared space in seconds. Give it a name that feels like home.</p>
                        </div>
                        <div className="step-item">
                            <h3>Invite your roommates</h3>
                            <p>Share a simple code. They're in instantly, no friction.</p>
                        </div>
                        <div className="step-item">
                            <h3>Start organizing</h3>
                            <p>Add chores, expenses, and groceries. Watch your household run smoother.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="cta-section">
                <div className="cta-content">
                    <h2>Ready for a better way?</h2>
                    <p>Join roommates who've simplified their shared living.</p>
                    <div className="cta-buttons">
                        <Link to="/register" className="btn btn-primary">
                            Create Free Account
                        </Link>
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="landing-footer">
                <div className="footer-content">
                    <span className="footer-logo">TheRoommate</span>
                    <div className="footer-links">
                        <a href="#features">Features</a>
                        <a href="#about">About</a>
                        <a href="#privacy">Privacy</a>
                    </div>
                    <span className="footer-copyright">© 2026</span>
                </div>
            </footer>
        </div>
    );
};

export default Home;