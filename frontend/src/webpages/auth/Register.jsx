import apiClient from '../../apiClient';
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import '../../styling/Components.css';

const Register = () => {
    const navigate = useNavigate();
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [showPasswordRules, setShowPasswordRules] = useState(false);

    const passwordRules = [
        { test: /.{8,}/, message: "At least 8 characters" },
        { test: /[A-Z]/, message: "At least one uppercase letter" },
        { test: /[a-z]/, message: "At least one lowercase letter" },
        { test: /[0-9]/, message: "At least one number" },
        { test: /[@#$%^&+=!]/, message: "At least one special character (@#$%^&+=!)" },
    ];

    function validatePassword(password) {
        for (let rule of passwordRules) {
            if (!rule.test.test(password)) {
                return rule.message;
            }
        }
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        const validationError = validatePassword(password);
        if (validationError) {
            setError(validationError);
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            await apiClient.post(`/user/register`, {
                firstName,
                lastName,
                email,
                password
            });
            navigate('/login');
        } catch (err) {
            if (err.response) {
                if (err.response.status === 429) {
                    setError('Too many requests. Please wait a moment and try again.');
                } else if (err.response.status === 409) {
                    setError(err.response.data.message || 'User with this email already exists.');
                } else {
                    setError('Failed to register. Please try again later.');
                }
            } else if (err.request) {
                setError('No response from the server. Check your internet connection.');
            } else {
                setError('An unknown error occurred.');
            }
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>Create an account</h1>
                    <p>Join TheRoomate to manage your shared living</p>
                </div>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="flex gap-4">
                        <div className="form-group" style={{ flex: 1 }}>
                            <label className="form-label" htmlFor="firstName">
                                First name
                            </label>
                            <input
                                id="firstName"
                                type="text"
                                value={firstName}
                                onChange={e => setFirstName(e.target.value)}
                                placeholder="John"
                                required
                                autoComplete="given-name"
                            />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label className="form-label" htmlFor="lastName">
                                Last name
                            </label>
                            <input
                                id="lastName"
                                type="text"
                                value={lastName}
                                onChange={e => setLastName(e.target.value)}
                                placeholder="Doe"
                                required
                                autoComplete="family-name"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="email">
                            Email address
                        </label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            placeholder="you@example.com"
                            required
                            autoComplete="email"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="password">
                            Password
                        </label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            onFocus={() => setShowPasswordRules(true)}
                            onBlur={() => setShowPasswordRules(false)}
                            placeholder="Create a strong password"
                            required
                            autoComplete="new-password"
                        />
                        
                        {showPasswordRules && (
                            <ul className="password-rules" style={{ 
                                listStyle: 'none', 
                                padding: 0, 
                                marginTop: 'var(--spacing-3)',
                                fontSize: 'var(--font-size-sm)'
                            }}>
                                {passwordRules.map((rule, i) => (
                                    <li
                                        key={i}
                                        style={{
                                            color: rule.test.test(password) 
                                                ? 'var(--success-600)' 
                                                : 'var(--text-muted)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 'var(--spacing-2)',
                                            marginBottom: 'var(--spacing-1)'
                                        }}
                                    >
                                        <span>{rule.test.test(password) ? 'ok' : '--'}</span>
                                        {rule.message}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <button 
                        type="submit" 
                        className="btn btn-primary btn-lg w-full auth-submit"
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <>
                                <span className="spinner"></span>
                                Creating account...
                            </>
                        ) : (
                            'Create account'
                        )}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>
                        Already have an account?{' '}
                        <Link to="/login">Sign in</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Register;
