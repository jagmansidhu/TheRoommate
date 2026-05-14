import apiClient from '../../apiClient';
import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../../App';
import '../../styling/Dashboard.css';

const PasswordReset = () => {
    const { user: data, userLoading } = useUser();
    const [validationError, setValidationError] = useState('');
    const [apiError, setApiError] = useState('');
    const [saving, setSaving] = useState(false);
    const [password, setPassword] = useState('');
    const [curPassword, setCurPassword] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [showPasswordRules, setShowPasswordRules] = useState(false);

    const navigate = useNavigate();

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


    const handlePasswordReset = async (e) => {
        e.preventDefault();

        const validationErr = validatePassword(password);
        if (validationErr) {
            setValidationError(validationErr);
            return;
        }

        setSaving(true);
        try {
            const response = await apiClient.put(`/user/updateProfile`,
                {
                    email: data.email,
                    curPassword: curPassword,
                    password: password
                },
                { withCredentials: true }
            );

            if (response.status === 200) {
                setSuccessMessage("Password updated successfully!");
                setValidationError('');
                setApiError('');
                setTimeout(() => navigate('/profile'), 2000);
            } else {
                setApiError("Password reset failed.");
            }
        } catch (err) {
            console.error('Error resetting password:', err);
            setApiError("An error occurred while resetting the password.");
        } finally {
            setSaving(false);
        }
    };

    if (userLoading) {
        return (
            <div className="loading">
                <div className="spinner spinner-lg"></div>
                <p>Loading...</p>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <h1>Change Password</h1>
                <p>Update your account password</p>
            </div>

            <div className="dashboard-content" style={{ display: 'block', maxWidth: '600px' }}>
                <div className="dashboard-section">
                    {apiError && (
                        <div className="alert alert-error" style={{ marginBottom: 'var(--spacing-4)' }}>
                            {apiError}
                        </div>
                    )}
                    {validationError && (
                        <div className="alert alert-error" style={{ marginBottom: 'var(--spacing-4)' }}>
                            {validationError}
                        </div>
                    )}
                    {successMessage && (
                        <div className="alert alert-success" style={{ marginBottom: 'var(--spacing-4)' }}>
                            {successMessage} Redirecting...
                        </div>
                    )}

                    <form onSubmit={handlePasswordReset}>
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                className="form-input"
                                value={data?.email || ''}
                                disabled
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="curPassword">Current Password</label>
                            <input
                                type="password"
                                id="curPassword"
                                className="form-input"
                                value={curPassword}
                                onChange={e => setCurPassword(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="newPassword">New Password</label>
                            <input
                                type="password"
                                id="newPassword"
                                className="form-input"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                onFocus={() => setShowPasswordRules(true)}
                                required
                            />
                        </div>

                        {showPasswordRules && (
                            <ul className="password-rules">
                                {passwordRules.map((rule, i) => (
                                    <li
                                        key={i}
                                        className={rule.test.test(password) ? 'rule-valid' : 'rule-invalid'}
                                    >
                                        {rule.message}
                                    </li>
                                ))}
                            </ul>
                        )}

                        <div className="form-actions">
                            <button 
                                type="submit" 
                                className="btn btn-primary"
                                disabled={saving}
                            >
                                Update Password
                            </button>
                            <button 
                                type="button" 
                                className="btn btn-secondary"
                                onClick={() => navigate('/profile')}
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default PasswordReset;
