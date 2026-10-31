import axios from 'axios';
import { getCookie } from './webpages/utils/cookies';

const apiClient = axios.create({
    baseURL: process.env.REACT_APP_BASE_API_URL,
    withCredentials: true,
});

// XSRF-TOKEN is set on the API host, so a split-origin page reads X-CSRF-TOKEN instead.
let csrfToken = null;

function rememberCsrf(response) {
    const headers = response && response.headers;
    if (!headers) {
        return;
    }
    const token = typeof headers.get === 'function' ? headers.get('x-csrf-token') : headers['x-csrf-token'];
    if (token) {
        csrfToken = token;
    }
}

apiClient.interceptors.request.use((config) => {
    const method = (config.method || 'get').toLowerCase();
    if (method === 'post' || method === 'put' || method === 'patch' || method === 'delete') {
        const token = csrfToken || getCookie('XSRF-TOKEN');
        if (token) {
            config.headers['X-CSRF-TOKEN'] = token;
        }
    }
    return config;
});

apiClient.interceptors.response.use((response) => {
    rememberCsrf(response);
    return response;
}, (error) => {
    rememberCsrf(error.response);
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