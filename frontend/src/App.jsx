import React, {createContext, useCallback, useContext, useEffect, useState} from 'react';
import {BrowserRouter as Router, Link, Route, Routes, useNavigate} from 'react-router-dom';
import Login from './webpages/auth/Login';
import Register from './webpages/auth/Register';
import Dashboard from './webpages/Dashboard';
import Profile from './webpages/Profile';
import Home from './webpages/Home';
import useProfileCompletionRedirect from "./component/userProfileRedirection";
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
const OnboardingContext = createContext();

export const useTheme = () => useContext(ThemeContext);
export const useAuth = () => useContext(AuthContext);
export const useUser = () => useContext(UserContext);
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
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const checkAuthStatus = async () => {
            try {
                const res = await fetch(`${process.env.REACT_APP_BASE_API_URL}/user/status`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (res.ok) {
                    const contentType = res.headers.get("content-type");
                    if (contentType && contentType.indexOf("application/json") !== -1) {
                        const data = await res.json();
                        if (data && (data.username || data.email || data.authenticated === true)) {
                           setIsAuthenticated(true);
                        } else {
                           setIsAuthenticated(true);
                        }
                    } else {
                         console.warn("Auth check returned non-JSON:", contentType);
                         setIsAuthenticated(false);
                    }
                } else {
                    setIsAuthenticated(false);
                }
            } catch (err) {
                console.error('Error checking auth status:', err);
                setIsAuthenticated(false);
            } finally {
                setIsLoading(false);
            }
        };

        checkAuthStatus();
    }, []);

    const login = () => {
        setIsAuthenticated(true);
    };

    const logout = async () => {
        try {
            await fetch(`${process.env.REACT_APP_BASE_API_URL}/user/logout`, {
                method: 'POST',
                credentials: 'include'
            });
        } catch (err) {
            console.error('Logout failed', err);
        }

        setIsAuthenticated(false);
        window.location.href = '/';
    };

    return (
        <AuthContext.Provider value={{isAuthenticated, isLoading, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
};

// Fetches /api/get-user exactly once after login, then caches in context.
// Call refreshUser() explicitly after any mutation that changes user data.
const UserProvider = ({children}) => {
    const {isAuthenticated, isLoading} = useAuth();
    const [user, setUser] = useState(null);
    const [userLoading, setUserLoading] = useState(false);

    const fetchUser = useCallback(async () => {
        setUserLoading(true);
        try {
            const res = await fetch(`${process.env.REACT_APP_BASE_API_URL}/api/get-user`, {
                credentials: 'include',
            });
            if (res.ok) {
                const data = await res.json();
                setUser(data);
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
            fetchUser();
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


const LoggedOutNavbar = () => {
    return (
        <header className="App-header">
            <div className="header-content">
                <Link to="/" className="logo">
                    <span>TheRoomate</span>
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
    const {toggleTheme, isDarkMode} = useTheme();

    return (
        <header className="App-header">
            <div className="header-content">
                <Link to="/dashboard" className="logo">
                </Link>
                <nav className="nav">
                    <Link to="/dashboard" className="nav-link">Dashboard</Link>
                    <Link to="/calendar" className="nav-link">Calendar</Link>
                    <Link to="/profile" className="nav-link">Profile</Link>
                    <Link to="/rooms" className="nav-link">Rooms</Link>
                    <button onClick={toggleTheme} className="theme-toggle">
                        {isDarkMode ? "☀️" : "🌙"}
                    </button>
                    <button onClick={logout} className="btn btn-secondary">Logout</button>
                </nav>
            </div>
        </header>
    );
};

const CheckEmailPage = () => {
    const [hasSent, setHasSent] = useState(false);
    const [isVisible, setIsVisible] = useState(true);

    const sendVerification = async () => {
        try {
            await fetch(`${process.env.REACT_APP_BASE_API_URL}/user/resend-verification`, {
                method: 'POST',
                credentials: 'include'
            });
            setHasSent(true);
        } catch (err) {
            console.error('Error sending verification email:', err);
        }
    };

    const handleClose = () => {
        setIsVisible(false);
    };

    if (!isVisible) return null;

    return (
        <div className="check-email-page">
            <h2>Email Verification Required</h2>
            <p>
                Your email is not verified. Please check your inbox and follow the link to verify your account.
            </p>
            <button onClick={sendVerification} disabled={hasSent}>
                {hasSent ? "Verification Sent" : "Resend Verification Email"}
            </button>
            <button onClick={handleClose} style={{marginLeft: '10px'}}>
                Close
            </button>
        </div>
    );
};


const AppContent = () => {
    const {isAuthenticated, isLoading, logout} = useAuth();
    const navigate = useNavigate();
    const [userVerified, setUserVerified] = useState(null); // null = unknown, true/false = known
    const [isOnboarding, setIsOnboarding] = useState(false);
    const hideNavbarPaths = ['/complete-profile'];

    useProfileCompletionRedirect();

    useEffect(() => {
        if (isAuthenticated && !isLoading) {
            const checkVerification = async () => {
                try {
                    const res = await fetch(`${process.env.REACT_APP_BASE_API_URL}/user/verify-status`, {
                        method: 'GET',
                        credentials: 'include'
                    });
                    if (res.ok) {
                        const data = await res.json();
                        setUserVerified(data.verified);
                    } else {
                        setUserVerified(false);
                    }
                } catch (err) {
                    console.error('Error checking verification status:', err);
                    setUserVerified(false);
                }
            };

            checkVerification();
        } else if (!isAuthenticated && !isLoading) {
            setUserVerified(null);
        }
    }, [isAuthenticated, isLoading]);

    // Public paths that don't need auth — render immediately without waiting for the backend.
    const publicPaths = ['/', '/login', '/register', '/verify'];
    const isPublicPath = publicPaths.includes(window.location.pathname);

    // Only block render on authenticated routes — avoids a blank screen on the home page
    // while waiting for the /user/status round-trip to the backend.
    if (!isPublicPath && (isLoading || (isAuthenticated && userVerified === null))) {
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
                            isAuthenticated ? (
                                userVerified ? <Dashboard/> : <CheckEmailPage/>
                            ) : <Home/>
                        }/>
                        <Route path="/dashboard" element={
                            isAuthenticated ? (
                                userVerified ? <Dashboard/> : <CheckEmailPage/>
                            ) : <Login/>
                        }/>
                        <Route path="/profile" element={
                            isAuthenticated ? (
                                userVerified ? <Profile/> : <CheckEmailPage/>
                            ) : <Login/>
                        }/>
                        <Route path="/rooms" element={
                            isAuthenticated ? (
                                userVerified ? <Rooms/> : <CheckEmailPage/>
                            ) : <Login/>
                        }/>
                        <Route path="/calendar" element={
                            isAuthenticated ? (
                                userVerified ? <Calendar/> : <CheckEmailPage/>
                            ) : <Login/>
                        }/>
                        <Route path="/rooms/:roomId" element={
                            isAuthenticated ? (
                                userVerified ? <RoomDetailsPageWrapper/> : <CheckEmailPage/>
                            ) : <Login/>
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
                    <Router>
                        <AppContent/>
                    </Router>
                </UserProvider>
            </AuthProvider>
        </ThemeProvider>
    );
}