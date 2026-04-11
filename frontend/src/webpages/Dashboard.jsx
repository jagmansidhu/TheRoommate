import React, { useEffect, useState } from 'react';
import '../styling/Dashboard.css';
import OnboardingPage from './OnboardingPage';
import { useOnboarding, useUser, useAppData } from '../App';

const Dashboard = () => {
    const { setIsOnboarding } = useOnboarding();
    const { user } = useUser();
    const email = user?.email || user?.username || null;
    const { rooms, roomsLoading, userChores, userUtilities } = useAppData();
    const [showOnboarding, setShowOnboarding] = useState(false);
    const [hasSeenOnboarding, setHasSeenOnboarding] = useState(() => {
        return localStorage.getItem('hasSeenOnboarding') === 'true';
    });

    // Show onboarding once rooms have loaded and the user has none
    useEffect(() => {
        if (!roomsLoading && rooms.length === 0 && !hasSeenOnboarding) {
            setShowOnboarding(true);
        }
    }, [roomsLoading, rooms.length, hasSeenOnboarding]);

    useEffect(() => {
        setIsOnboarding(showOnboarding);
    }, [showOnboarding, setIsOnboarding]);

    const handleOnboardingComplete = () => {
        setShowOnboarding(false);
        setHasSeenOnboarding(true);
        localStorage.setItem('hasSeenOnboarding', 'true');
    };

    if (roomsLoading) {
        return (
            <div className="loading">
                <div className="spinner spinner-lg"></div>
                <p>Loading...</p>
            </div>
        );
    }

    if (showOnboarding) {
        return <OnboardingPage onComplete={handleOnboardingComplete} />;
    }

    const upcomingChores = userChores.slice(0, 5);
    const upcomingUtilities = userUtilities.slice(0, 5);

    return (
        <div className="dashboard-container">
            {/* Dashboard Header */}
            <div className="dashboard-header">
                <h1>Dashboard</h1>
                <p>Welcome back, {email}</p>
            </div>

            {/* Stats Overview */}
            <div className="dashboard-stats">
                <div className="stat-card">
                    <div className="stat-icon stat-icon-chores"></div>
                    <div className="stat-value">{userChores.length}</div>
                    <div className="stat-label">Pending Chores</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-bills"></div>
                    <div className="stat-value">{userUtilities.length}</div>
                    <div className="stat-label">Upcoming Bills</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-rooms"></div>
                    <div className="stat-value">{rooms.length}</div>
                    <div className="stat-label">Active Rooms</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-complete"></div>
                    <div className="stat-value">-</div>
                    <div className="stat-label">Completed This Week</div>
                </div>
            </div>

            {/* Main Content Grid */}
            <div className="dashboard-content">
                {/* Upcoming Chores */}
                <div className="dashboard-section">
                    <h3>Upcoming Chores</h3>
                    {upcomingChores.length > 0 ? (
                        <ul>
                            {upcomingChores.map((chore, index) => (
                                <li key={index}>
                                    <div className="item-icon item-icon-chore"></div>
                                    <div className="item-content">
                                        <div className="item-title">{chore.choreName || 'Untitled Chore'}</div>
                                        <div className="item-meta">
                                            {chore.dueAt ? `Due: ${new Date(chore.dueAt).toLocaleDateString()}` : 'No due date'}
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">No upcoming chores. You're all caught up!</p>
                    )}
                </div>

                {/* Upcoming Utilities */}
                <div className="dashboard-section">
                    <h3>Upcoming Bills</h3>
                    {upcomingUtilities.length > 0 ? (
                        <ul>
                            {upcomingUtilities.map((utility, index) => (
                                <li key={index}>
                                    <div className="item-icon item-icon-bill"></div>
                                    <div className="item-content">
                                        <div className="item-title">{utility.utilityName || 'Untitled Bill'}</div>
                                        <div className="item-meta">
                                            {utility.dueAt ? `Due: ${new Date(utility.dueAt).toLocaleDateString()}` : 'No due date'}
                                            {utility.utilityPrice && ` - $${utility.utilityPrice}`}
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">No upcoming bills. All caught up!</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;


