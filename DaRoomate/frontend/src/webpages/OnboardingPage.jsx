import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styling/Onboarding.css';

const OnboardingPage = ({ onComplete }) => {
    const navigate = useNavigate();

    const handleCreateRoom = () => {
        onComplete();
        navigate('/rooms', { state: { openCreate: true } });
    };

    const handleJoinRoom = () => {
        onComplete();
        navigate('/rooms', { state: { openJoin: true } });
    };

    const handleSkip = () => {
        onComplete();
    };

    return (
        <div className="onboarding-page">
            <div className="onboarding-container">
                <div className="onboarding-header">
                    <div className="onboarding-logo">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M3 10.5L12 3L21 10.5V20C21 20.5 20.5 21 20 21H4C3.5 21 3 20.5 3 20V10.5Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M9 21V12H15V21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <h1>Welcome to DaRoommate</h1>
                    <p>Your all-in-one platform for managing shared living spaces. Get started by creating your first room or joining an existing one.</p>
                </div>

                <div className="onboarding-cards">
                    <div className="onboarding-card" onClick={handleCreateRoom}>
                        <div className="card-icon">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </div>
                        <h2>Create a Room</h2>
                        <p>Start a new shared living space and invite your roommates to join</p>
                        <button className="btn btn-primary">Get Started</button>
                    </div>

                    <div className="onboarding-card" onClick={handleJoinRoom}>
                        <div className="card-icon">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M15 3H19C20.1 3 21 3.9 21 5V19C21 20.1 20.1 21 19 21H15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                <path d="M10 17L15 12L10 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                <path d="M15 12H3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </div>
                        <h2>Join a Room</h2>
                        <p>Have a room code from a roommate? Enter it to join their space</p>
                        <button className="btn btn-secondary">Enter Code</button>
                    </div>
                </div>

                <button className="onboarding-skip" onClick={handleSkip}>
                    Skip for now, I'll do this later
                </button>
            </div>
        </div>
    );
};

export default OnboardingPage;
