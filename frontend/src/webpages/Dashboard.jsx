import React, { useEffect, useState } from 'react';
import '../styling/Dashboard.css';
import OnboardingPage from './OnboardingPage';
import { useOnboarding, useUser, useAppData } from '../App';
import apiClient from '../apiClient';

const Dashboard = () => {
    const { setIsOnboarding } = useOnboarding();
    const { user } = useUser();
    const email = user?.email || user?.username || null;
    const { rooms, roomsLoading, userChores, userUtilities, updateUserChore, updateUserUtility } = useAppData();
    const [showOnboarding, setShowOnboarding] = useState(false);
    const [hasSeenOnboarding, setHasSeenOnboarding] = useState(() => {
        return localStorage.getItem('hasSeenOnboarding') === 'true';
    });

    const isUtilityCompleted = (utility) => Boolean(utility?.isCompleted ?? utility?.completed);

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

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);

    const choresToday = userChores.filter(chore => {
        if (!chore.dueAt) return false;
        const due = new Date(chore.dueAt);
        return due >= today && due < tomorrow;
    });

    const now = new Date();
    const in28Days = new Date(now);
    in28Days.setDate(now.getDate() + 28);

    const getFrequencyLabel = (freq) => {
        if (freq === 'WEEKLY') return 'Weekly';
        if (freq === 'BIWEEKLY') return 'Biweekly';
        if (freq === 'MONTHLY') return 'Monthly';
        return freq || '';
    };

    // Show all utilities due within the next 4 weeks, regardless of frequency.
    const relevantUtilities = userUtilities.filter(u => {
        if (!u.dueAt) return false;
        const due = new Date(u.dueAt);
        return due >= now && due <= in28Days;
    });

    const toggleChoreCompletion = async (chore) => {
        const nextCompleted = !Boolean(chore.isCompleted);
        updateUserChore(chore.id, { isCompleted: nextCompleted });
        
        try {
            await apiClient.patch(`/api/chores/${chore.id}/completion`, { completed: nextCompleted });
        } catch (error) {
            console.error('Failed to update chore completion', error);
            updateUserChore(chore.id, { isCompleted: !nextCompleted });
        }
    };

    const toggleUtilityCompletion = async (utility) => {
        const nextCompleted = !isUtilityCompleted(utility);
        updateUserUtility(utility.id, { isCompleted: nextCompleted });

        try {
            await apiClient.patch(`/api/utility/${utility.id}/completion`, { completed: nextCompleted });
        } catch (error) {
            console.error('Failed to update utility completion', error);
            updateUserUtility(utility.id, { isCompleted: !nextCompleted });
        }
    };

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
                    <div className="stat-value">{userChores.filter(c => c.isCompleted).length}</div>
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
                                <li key={index} className={chore.isCompleted ? 'dashboard-item-completed' : ''}>
                                    <input
                                        type="checkbox"
                                        checked={Boolean(chore.isCompleted)}
                                        onChange={() => toggleChoreCompletion(chore)}
                                        title={chore.isCompleted ? 'Mark as incomplete' : 'Mark as complete'}
                                    />
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
                                <li key={index} className={isUtilityCompleted(utility) ? 'dashboard-item-completed' : ''}>
                                    <input
                                        type="checkbox"
                                        checked={isUtilityCompleted(utility)}
                                        onChange={() => toggleUtilityCompletion(utility)}
                                        title={isUtilityCompleted(utility) ? 'Mark as incomplete' : 'Mark as complete'}
                                    />
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
