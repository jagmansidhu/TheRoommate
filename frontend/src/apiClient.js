import axios from 'axios';

const apiClient = axios.create({
    baseURL: process.env.REACT_APP_BASE_API_URL,
    withCredentials: true,
});

// Helper function to get CSRF token from cookies
const getCsrfToken = () => {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');
    for (let cookie of cookieArray) {
        cookie = cookie.trim();
        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length);
        }
    }
    return null;
};

// Request interceptor to add CSRF token to state-changing requests
apiClient.interceptors.request.use((config) => {
    // Only add CSRF token for state-changing requests (not GET, HEAD, OPTIONS)
    const stateChangingMethods = ['POST', 'PUT', 'DELETE', 'PATCH'];
    if (stateChangingMethods.includes(config.method.toUpperCase())) {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            config.headers['X-CSRF-TOKEN'] = csrfToken;
        }
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

apiClient.interceptors.response.use((response) => response, (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        ['appUser', 'appRooms', 'appChores', 'appUtilities', 'appEvents', 'appAuth'].forEach(k => localStorage.removeItem(k));
        if (!['/', '/login', '/register', '/verify'].includes(window.location.pathname)) {
            window.location.href = '/login';
        }
    }
    return Promise.reject(error);
});

export default apiClient;