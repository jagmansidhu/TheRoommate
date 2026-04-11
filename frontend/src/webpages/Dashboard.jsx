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

    // --- Chores: only those due today ---
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);

    const choresToday = userChores.filter(chore => {
        if (!chore.dueAt) return false;
        const due = new Date(chore.dueAt);
        return due >= today && due < tomorrow;
    });

    // --- Utilities: bucket by frequency so user sees bills relevant to their period ---
    const now = new Date();
    const in7Days = new Date(now);
    in7Days.setDate(now.getDate() + 7);
    const in30Days = new Date(now);
    in30Days.setDate(now.getDate() + 30);

    const getFrequencyLabel = (freq) => {
        if (freq === 'WEEKLY') return 'Weekly';
        if (freq === 'BIWEEKLY') return 'Biweekly';
        if (freq === 'MONTHLY') return 'Monthly';
        return freq || '';
    };

    // Weekly & biweekly: show bills due within 7 days
    const weeklyBills = userUtilities.filter(u => {
        const freq = u.choreFrequencyUnitEnum;
        if (freq !== 'WEEKLY' && freq !== 'BIWEEKLY') return false;
        if (!u.dueAt) return true;
        const due = new Date(u.dueAt);
        return due >= now && due <= in7Days;
    });

    // Monthly: show bills due within 30 days
    const monthlyBills = userUtilities.filter(u => {
        const freq = u.choreFrequencyUnitEnum;
        if (freq !== 'MONTHLY') return false;
        if (!u.dueAt) return true;
        const due = new Date(u.dueAt);
        return due >= now && due <= in30Days;
    });

    // Utilities with no frequency set — always show them
    const miscBills = userUtilities.filter(u => !u.choreFrequencyUnitEnum);

    const relevantUtilities = [...weeklyBills, ...monthlyBills, ...miscBills];

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
                    <div className="stat-value">{choresToday.length}</div>
                    <div className="stat-label">Chores Due Today</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-bills"></div>
                    <div className="stat-value">{relevantUtilities.length}</div>
                    <div className="stat-label">Upcoming Bills</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-rooms"></div>
                    <div className="stat-value">{rooms.length}</div>
                    <div className="stat-label">Active Rooms</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-complete"></div>
                    <div className="stat-value">{userChores.filter(c => c.completed).length}</div>
                    <div className="stat-label">Completed This Week</div>
                </div>
            </div>

            {/* Main Content Grid */}
            <div className="dashboard-content">
                {/* Chores Due Today */}
                <div className="dashboard-section">
                    <h3>Chores Due Today</h3>
                    {choresToday.length > 0 ? (
                        <ul>
                            {choresToday.map((chore, index) => (
                                <li key={index}>
                                    <div className="item-icon item-icon-chore"></div>
                                    <div className="item-content">
                                        <div className="item-title">{chore.choreName || 'Untitled Chore'}</div>
                                        <div className="item-meta">
                                            Due: {new Date(chore.dueAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                            {chore.choreFrequencyUnitEnum && ` · ${getFrequencyLabel(chore.choreFrequencyUnitEnum)}`}
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">No chores due today. Enjoy your day! 🎉</p>
                    )}
                </div>

                {/* Relevant Bills bucketed by frequency */}
                <div className="dashboard-section">
                    <h3>Upcoming Bills</h3>
                    {relevantUtilities.length > 0 ? (
                        <ul>
                            {relevantUtilities.map((utility, index) => (
                                <li key={index}>
                                    <div className="item-icon item-icon-bill"></div>
                                    <div className="item-content">
                                        <div className="item-title">{utility.utilityName || 'Untitled Bill'}</div>
                                        <div className="item-meta">
                                            {utility.choreFrequencyUnitEnum && (
                                                <span className="badge-frequency">{getFrequencyLabel(utility.choreFrequencyUnitEnum)}</span>
                                            )}
                                            {utility.dueAt && ` Due: ${new Date(utility.dueAt).toLocaleDateString()}`}
                                            {utility.utilityPrice != null && ` · $${Number(utility.utilityPrice).toFixed(2)}`}
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">No upcoming bills this period. All caught up!</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
