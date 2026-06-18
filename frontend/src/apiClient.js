import axios from 'axios';

const apiClient = axios.create({
    baseURL: process.env.REACT_APP_BASE_API_URL,
    withCredentials: true,
});



apiClient.interceptors.response.use((response) => response, (error) => {
    if (error.response && error.response.status === 401) {
        const msg = error.response.data?.message || '';
        const isAuthError = msg.toLowerCase().includes('log in') || msg.toLowerCase().includes('unauthorized');
        if (isAuthError && !['/', '/login', '/register', '/verify'].includes(window.location.pathname)) {
            ['appUser', 'appRooms', 'appChores', 'appUtilities', 'appEvents', 'appAuth'].forEach(k => localStorage.removeItem(k));
            window.location.href = '/login';
        }
    }
    return Promise.reject(error);
});

export default apiClient;