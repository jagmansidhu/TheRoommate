import apiClient from './apiClient';
import React, {createContext, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {BrowserRouter as Router, Link, Route, Routes} from 'react-router-dom';
import Login from './webpages/auth/Login';
import Register from './webpages/auth/Register';
import Dashboard from './webpages/Dashboard';
import Profile from './webpages/Profile';
import Home from './webpages/Home';
import Personal from "./webpages/profileRedirect/Personal";
import Rooms from "./webpages/room/Rooms";
import './styling/App.css';
import RoomDetailsPageWrapper from "./webpages/room/RoomDetailWrapper";
import Calendar from "./component/Calendar";
import PasswordReset from "./webpages/profileRedirect/PasswordReset";
import VerifyHandler from "./webpages/VerifyHandler";

const ThemeContext = createContext();
const AuthContext = createContext();
const UserContext = createContext();
const AppDataContext = createContext();
const OnboardingContext = createContext();

export const useTheme = () => useContext(ThemeContext);
export const useAuth = () => useContext(AuthContext);
export const useUser = () => useContext(UserContext);
export const useAppData = () => useContext(AppDataContext);
export const useOnboarding = () => useContext(OnboardingContext);

const ThemeProvider = ({children}) => {
    const [isDarkMode, setIsDarkMode] = useState(() => {
        const saved = localStorage.getItem('darkMode');
        return saved ? JSON.parse(saved) : false;
    });

    useEffect(() => {
        localStorage.setItem('darkMode', JSON.stringify(isDarkMode));
        document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
    }, [isDarkMode]);

    return (
        <ThemeContext.Provider value={{isDarkMode, toggleTheme: () => setIsDarkMode(prev => !prev)}}>
            {children}
        </ThemeContext.Provider>
    );
};

const AuthProvider = ({children}) => {
    const [isAuthenticated, setIsAuthenticated] = useState(() => localStorage.getItem('appAuth') === 'true');
    const [isLoading, setIsLoading] = useState(!localStorage.getItem('appAuth'));

    useEffect(() => {
        const checkAuthStatus = async () => {
            try {
                const res = await apiClient.get('/user/status');

                if (res.status === 200) {
                    const data = res.data;
                    if (data && (data.username || data.email || data.authenticated === true)) {
                       setIsAuthenticated(true);
                       localStorage.setItem('appAuth', 'true');
                    } else {
                       setIsAuthenticated(false);
                       localStorage.removeItem('appAuth');
                    }
                } else {
                    setIsAuthenticated(false);
                    localStorage.removeItem('appAuth');
                }
            } catch (err) {
                console.error('Error checking auth status:', err);
                setIsAuthenticated(false);
                localStorage.removeItem('appAuth');
            } finally {
                setIsLoading(false);
            }
        };

        checkAuthStatus();
    }, []);

    const login = (token) => {
        if (token) {
            localStorage.setItem('token', token);
        }
        localStorage.setItem('appAuth', 'true');
        setIsAuthenticated(true);
    };

    const logout = async () => {
        try {
            await apiClient.post('/user/logout');
        } catch (err) {
            console.error('Logout failed', err);
        }
        
        ['token', 'appAuth', 'appUser', 'appRooms', 'appChores', 'appUtilities', 'appEvents', 'appRoomData'].forEach(k => localStorage.removeItem(k));
        setIsAuthenticated(false);
        window.location.href = '/';
    };

    return (
        <AuthContext.Provider value={{isAuthenticated, isLoading, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
};

const UserProvider = ({children}) => {
    const {isAuthenticated, isLoading} = useAuth();
    const [user, setUser] = useState(() => {
        const cached = localStorage.getItem('appUser');
        return cached ? JSON.parse(cached) : null;
    });
    const [userLoading, setUserLoading] = useState(false);

    useEffect(() => {
        if (user !== null) {
            localStorage.setItem('appUser', JSON.stringify(user));
        } else {
            localStorage.removeItem('appUser');
        }
    }, [user]);

    const fetchUser = useCallback(async () => {
        setUserLoading(true);
        try {
            const res = await apiClient.get('/api/get-user');
            if (res.status === 200) {
                setUser(res.data);
            } else {
                setUser(null);
            }
        } catch (err) {
            console.error('Error fetching user profile:', err);
            setUser(null);
        } finally {
            setUserLoading(false);
        }
    }, []);

    useEffect(() => {
        if (isAuthenticated && !isLoading) {
            if (!localStorage.getItem('appUser')) fetchUser();
        } else if (!isAuthenticated && !isLoading) {
            setUser(null);
        }
    }, [isAuthenticated, isLoading, fetchUser]);

    return (
        <UserContext.Provider value={{user, userLoading, refreshUser: fetchUser}}>
            {children}
        </UserContext.Provider>
    );
};

// ---------------------------------------------------------------------------
// AppDataProvider
// Caches shared app data so pages don't re-fetch on every navigation.
//
const AppDataProvider = ({children}) => {
    const {isAuthenticated, isLoading} = useAuth();

    const [rooms, setRooms] = useState(() => {
        const cached = localStorage.getItem('appRooms');
        return cached ? JSON.parse(cached) : [];
    });
    const [roomsLoading, setRoomsLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) localStorage.setItem('appRooms', JSON.stringify(rooms));
    }, [rooms, isAuthenticated]);

    const fetchRooms = useCallback(async () => {
        setRoomsLoading(true);
        try {
            const res = await apiClient.get('/api/rooms');
            if (res.status === 200) setRooms(res.data);
        } catch (err) {
            console.error('AppData: failed to fetch rooms', err);
        } finally {
            setRoomsLoading(false);
        }
    }, []);

    const appendRoom       = useCallback(r  => setRooms(prev => [...prev, r]), []);
    const removeRoom       = useCallback(id => setRooms(prev => prev.filter(r => r.id !== id)), []);
    const updateRoom       = useCallback(r  => setRooms(prev => prev.map(x => x.id === r.id ? r : x)), []);

    const [userChores, setUserChores] = useState(() => {
        const cached = localStorage.getItem('appChores');
        return cached ? JSON.parse(cached) : [];
    });
    const [userChoresLoading, setUserChoresLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) localStorage.setItem('appChores', JSON.stringify(userChores));
    }, [userChores, isAuthenticated]);

    const fetchUserChores = useCallback(async () => {
        setUserChoresLoading(true);
        try {
            const res = await apiClient.get('/api/chores/user/me');
            if (res.status === 200) setUserChores(res.data);
        } catch (err) {
            console.error('AppData: failed to fetch user chores', err);
        } finally {
            setUserChoresLoading(false);
        }
    }, []);

    const appendUserChore = useCallback(c  => setUserChores(prev => [...prev, c]), []);
    const removeUserChore = useCallback(id => setUserChores(prev => prev.filter(c => c.id !== id)), []);
    const updateUserChore = useCallback((choreId, patch) => {
        setUserChores(prev => prev.map(chore => chore.id === choreId ? { ...chore, ...patch } : chore));
    }, []);

    // --- user utilities ---
    const [userUtilities, setUserUtilities] = useState(() => {
        const cached = localStorage.getItem('appUtilities');
        return cached ? JSON.parse(cached) : [];
    });
    const [userUtilitiesLoading, setUserUtilitiesLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) localStorage.setItem('appUtilities', JSON.stringify(userUtilities));
    }, [userUtilities, isAuthenticated]);

    const normalizeUtility = useCallback((utility) => ({
        ...utility,
        isCompleted: utility?.isCompleted ?? utility?.completed ?? false,
    }), []);

    const fetchUserUtilities = useCallback(async () => {
        setUserUtilitiesLoading(true);
        try {
            const res = await apiClient.get('/api/utility/user/me');
            if (res.status === 200) setUserUtilities((res.data || []).map(normalizeUtility));
        } catch (err) {
            console.error('AppData: failed to fetch user utilities', err);
        } finally {
            setUserUtilitiesLoading(false);
        }
    }, [normalizeUtility]);

    const appendUserUtility = useCallback(u  => setUserUtilities(prev => [...prev, normalizeUtility(u)]), [normalizeUtility]);
    const removeUserUtility = useCallback(id => setUserUtilities(prev => prev.filter(u => u.id !== id)), []);
    const updateUserUtility = useCallback((utilityId, patch) => {
        setUserUtilities(prev => prev.map(utility => {
            if (utility.id !== utilityId) return utility;
            const merged = { ...utility, ...patch };
            if (Object.prototype.hasOwnProperty.call(patch, 'isCompleted')) {
                merged.completed = patch.isCompleted;
            }
            return merged;
        }));
    }, []);

    // --- per-room data cache (lazy) ---
    // Shape: { [roomId]: { chores, utilities, userUtilities, memberId } }
    const [roomData, setRoomDataState] = useState(() => {
        const cached = localStorage.getItem('appRoomData');
        return cached ? JSON.parse(cached) : {};
    });

    useEffect(() => {
        if (isAuthenticated) localStorage.setItem('appRoomData', JSON.stringify(roomData));
    }, [roomData, isAuthenticated]);

    // Read cached data for a room (returns null if not yet loaded)
    const getRoomData = useCallback((roomId) => roomData[roomId] || null, [roomData]);

    // Write room data into cache after a fetch or mutation
    const setRoomData = useCallback((roomId, data) => {
        setRoomDataState(prev => ({ ...prev, [roomId]: data }));
    }, []);

    // Merge a partial patch into existing room cache (e.g. after a mutation)
    const patchRoomData = useCallback((roomId, patch) => {
        setRoomDataState(prev => ({
            ...prev,
            [roomId]: { ...(prev[roomId] || {}), ...patch },
        }));
    }, []);

    // Drop a room from cache (after leave/delete)
    const invalidateRoomData = useCallback((roomId) => {
        setRoomDataState(prev => {
            const next = { ...prev };
            delete next[roomId];
            return next;
        });
    }, []);

    // Fetch and cache all three room-scoped payloads in one Promise.all.
    // Safe to call on every mount — skips network if already cached.
    const loadRoomData = useCallback(async (roomId, memberId) => {
        if (roomData[roomId]) return; // already cached
        try {
            const [choresRes, utilitiesRes, userUtilitiesRes] = await Promise.all([
                apiClient.get(`/api/chores/${roomId}`),
                apiClient.get(`/api/utility/${roomId}`),
                apiClient.get(`/api/utility/${memberId}/room/${roomId}`),
            ]);
            setRoomData(roomId, {
                memberId,
                chores: choresRes.data || [],
                utilities: (utilitiesRes.data || []),
                userUtilities: (userUtilitiesRes.data || []),
            });
        } catch (err) {
            console.error('AppData: failed to load room data for', roomId, err);
        }
    }, [roomData, setRoomData]);

    // Force-refresh a room (after create/delete mutations)
    const refreshRoomData = useCallback(async (roomId, memberId) => {
        try {
            const [choresRes, utilitiesRes, userUtilitiesRes] = await Promise.all([
                apiClient.get(`/api/chores/${roomId}`),
                apiClient.get(`/api/utility/${roomId}`),
                apiClient.get(`/api/utility/${memberId}/room/${roomId}`),
            ]);
            setRoomData(roomId, {
                memberId,
                chores: choresRes.data || [],
                utilities: (utilitiesRes.data || []),
                userUtilities: (userUtilitiesRes.data || []),
            });
        } catch (err) {
            console.error('AppData: failed to refresh room data for', roomId, err);
        }
    }, [setRoomData]);

    const [events, setEvents] = useState(() => {
        const cached = localStorage.getItem('appEvents');
        return cached ? JSON.parse(cached) : [];
    });
    const [eventsLoading, setEventsLoading] = useState(false);
    const eventsLoadedRef = useRef(!!localStorage.getItem('appEvents') && localStorage.getItem('appEvents') !== '[]');

    useEffect(() => {
        if (isAuthenticated) localStorage.setItem('appEvents', JSON.stringify(events));
    }, [events, isAuthenticated]);

    const fetchEvents = useCallback(async () => {
        setEventsLoading(true);
        try {
            const res = await apiClient.get('/api/events/user');
            if (res.status === 200) setEvents(res.data);
        } catch (err) {
            console.error('AppData: failed to fetch events', err);
        } finally {
            setEventsLoading(false);
        }
    }, []);

    // loadEvents is safe to call on every Calendar mount — it's a no-op after the first load.
    const loadEvents    = useCallback(() => {
        if (eventsLoadedRef.current) return;
        eventsLoadedRef.current = true;
        fetchEvents();
    }, [fetchEvents]);
    const refreshEvents = useCallback(() => { eventsLoadedRef.current = false; fetchEvents(); }, [fetchEvents]);

    const appendEvent = useCallback(e  => setEvents(prev => [...prev, e]), []);
    const removeEvent = useCallback(id => setEvents(prev => prev.filter(e => e.id !== id)), []);

    // --- eager load on login, reset on logout ---
    useEffect(() => {
        if (isAuthenticated && !isLoading) {
            const cr = localStorage.getItem('appRooms');
            const cc = localStorage.getItem('appChores');
            const cu = localStorage.getItem('appUtilities');
            if (!cr || cr === '[]') fetchRooms();
            if (!cc || cc === '[]') fetchUserChores();
            if (!cu || cu === '[]') fetchUserUtilities();
        } else if (!isAuthenticated && !isLoading) {
            setRooms([]);
            setUserChores([]);
            setUserUtilities([]);
            setEvents([]);
            setRoomDataState({});
            eventsLoadedRef.current = false;
        }
    }, [isAuthenticated, isLoading, fetchRooms, fetchUserChores, fetchUserUtilities]);

    return (
        <AppDataContext.Provider value={{
            rooms, roomsLoading,
            refreshRooms: fetchRooms, appendRoom, removeRoom, updateRoom,
            userChores, userChoresLoading,
            refreshUserChores: fetchUserChores, appendUserChore, removeUserChore, updateUserChore,
            userUtilities, userUtilitiesLoading,
            refreshUserUtilities: fetchUserUtilities, appendUserUtility, removeUserUtility, updateUserUtility,
            events, eventsLoading, loadEvents, refreshEvents, appendEvent, removeEvent,
            // per-room cache
            roomData, getRoomData, setRoomData, patchRoomData, invalidateRoomData,
            loadRoomData, refreshRoomData,
        }}>
            {children}
        </AppDataContext.Provider>
    );
};


const LoggedOutNavbar = () => {
    return (
        <header className="App-header">
            <div className="header-content">
                <Link to="/" className="logo">
                    <span>TheRoommate</span>
                </Link>
                <nav className="nav">
                    <Link to="/login" className="btn btn-secondary">Sign In</Link>
                </nav>
            </div>
        </header>
    );
};


const LoggedInNavbar = () => {
    const {logout} = useAuth();

    return (
        <header className="App-header">
            <div className="header-content">
                <Link to="/dashboard" className="logo">
                    <span>TheRoommate</span>
                </Link>
                <nav className="nav">
                    <Link to="/dashboard" className="nav-link">Dashboard</Link>
                    <Link to="/calendar" className="nav-link">Calendar</Link>
                    <Link to="/profile" className="nav-link">Profile</Link>
                    <Link to="/rooms" className="nav-link">Rooms</Link>
                    <button onClick={logout} className="btn btn-secondary">Logout</button>
                </nav>
            </div>
        </header>
    );
};


const AppContent = () => {
    const {isAuthenticated, isLoading} = useAuth();
    const [isOnboarding, setIsOnboarding] = useState(false);
    const hideNavbarPaths = ['/complete-profile'];

    // Public paths that don't need auth — render immediately without waiting for the backend.
    const publicPaths = ['/', '/login', '/register', '/verify'];
    const isPublicPath = publicPaths.includes(window.location.pathname);

    // Only block render on authenticated routes — avoids a blank screen on the home page
    // while waiting for the /user/status round-trip to the backend.
    if (!isPublicPath && isLoading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <span>Loading...</span>
            </div>
        );
    }

    const shouldHideNavbar = hideNavbarPaths.includes(window.location.pathname) || isOnboarding;
    const showLoggedOutNavbar = window.location.pathname === '/verify';

    return (
        <OnboardingContext.Provider value={{isOnboarding, setIsOnboarding}}>
        <div className="App">
            {!shouldHideNavbar && (showLoggedOutNavbar ? <LoggedOutNavbar/> : (isAuthenticated ? <LoggedInNavbar/> :
                <LoggedOutNavbar/>))}
            <main className="main-content">
                <div className="content-wrapper">
                    <Routes>
                        <Route path="/" element={
                            isAuthenticated ? <Dashboard/> : <Home/>
                        }/>
                        <Route path="/dashboard" element={
                            isAuthenticated ? <Dashboard/> : <Login/>
                        }/>
                        <Route path="/profile" element={
                            isAuthenticated ? <Profile/> : <Login/>
                        }/>
                        <Route path="/rooms" element={
                            isAuthenticated ? <Rooms/> : <Login/>
                        }/>
                        <Route path="/calendar" element={
                            isAuthenticated ? <Calendar/> : <Login/>
                        }/>
                        <Route path="/rooms/:roomId" element={
                            isAuthenticated ? <RoomDetailsPageWrapper/> : <Login/>
                        }/>
                        <Route path="/reset-password" element={isAuthenticated ? <PasswordReset/> : <Login/>}/>
                        <Route path="/update-personal" element={isAuthenticated ? <Personal/> : <Login/>}/>
                        <Route path="/login" element={<Login/>}/>
                        <Route path="/verify" element={<VerifyHandler/>}/>
                        <Route path="/register" element={<Register/>}/>
                    </Routes>
                </div>
            </main>
        </div>
        </OnboardingContext.Provider>
    );
};

export default function App() {
    return (
        <ThemeProvider>
            <AuthProvider>
                <UserProvider>
                    <AppDataProvider>
                        <Router>
                            <AppContent/>
                        </Router>
                    </AppDataProvider>
                </UserProvider>
            </AuthProvider>
        </ThemeProvider>
    );
}