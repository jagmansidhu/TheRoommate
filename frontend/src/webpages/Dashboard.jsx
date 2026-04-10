import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styling/Dashboard.css';
import OnboardingPage from './OnboardingPage';
import { useOnboarding, useUser } from '../App';

const Dashboard = () => {
    const { setIsOnboarding } = useOnboarding();
    const { user } = useUser();
    const email = user?.email || user?.username || null;
    const [loading, setLoading] = useState(false);
    const [chores, setChores] = useState([]);
    const [utilities, setUtilities] = useState([]);
    const [rooms, setRooms] = useState([]);
    const [roomsLoaded, setRoomsLoaded] = useState(false);
    const [showOnboarding, setShowOnboarding] = useState(false);
    const [hasSeenOnboarding, setHasSeenOnboarding] = useState(() => {
        return localStorage.getItem('hasSeenOnboarding') === 'true';
    });

    useEffect(() => {
        if (!email) return;
        
        axios.get(`${process.env.REACT_APP_BASE_API_URL}/api/rooms`, { withCredentials: true })
            .then(res => {
                setRooms(res.data || []);
                setRoomsLoaded(true);
                if ((res.data || []).length === 0 && !hasSeenOnboarding) {
                    setShowOnboarding(true);
                }
            })
            .catch(() => setRoomsLoaded(true));

        // Fetches chores assigned to the current user from the backend.
        // The backend resolves identity from the JWT cookie — no user ID needed in the request.
        // Returns a list of ChoreDto: { id, choreName, dueAt, roomName }
        axios.get(`${process.env.REACT_APP_BASE_API_URL}/api/chores/user/me`, { withCredentials: true })
            .then(res => setChores(res.data || []))
            .catch(() => {});
        axios.get(`${process.env.REACT_APP_BASE_API_URL}/api/utility/user/me`, { withCredentials: true })
            .then(res => setUtilities(res.data || []))
            .catch(() => {});
    }, [email, hasSeenOnboarding]);

    useEffect(() => {
        setIsOnboarding(showOnboarding);
    }, [showOnboarding, setIsOnboarding]);

    const handleOnboardingComplete = () => {
        setShowOnboarding(false);
        setHasSeenOnboarding(true);
        localStorage.setItem('hasSeenOnboarding', 'true');
    };

    if (loading || !roomsLoaded) {
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

    const upcomingChores = chores.slice(0, 5);
    const upcomingUtilities = utilities.slice(0, 5);

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
                    <div className="stat-value">{chores.length}</div>
                    <div className="stat-label">Pending Chores</div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon stat-icon-bills"></div>
                    <div className="stat-value">{utilities.length}</div>
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
                                        <div className="item-title">{chore.name || chore.title || 'Untitled Chore'}</div>
                                        <div className="item-meta">
                                            {chore.dueDate ? `Due: ${new Date(chore.dueDate).toLocaleDateString()}` : 'No due date'}
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
                                        <div className="item-title">{utility.name || utility.title || 'Untitled Bill'}</div>
                                        <div className="item-meta">
                                            {utility.dueDate ? `Due: ${new Date(utility.dueDate).toLocaleDateString()}` : 'No due date'}
                                            {utility.amount && ` - $${utility.amount}`}
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


