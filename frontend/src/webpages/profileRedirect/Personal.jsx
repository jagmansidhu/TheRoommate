import apiClient from '../../apiClient';
import { useNavigate } from "react-router-dom";
import React, { useState } from "react";
import axios from "axios";
import { useUser } from '../../App';
import '../../styling/Dashboard.css';

const Personal = () => {
    const navigate = useNavigate();
    const { user: data, userLoading: apiLoading, refreshUser } = useUser();
    const [phone, setPhone] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(false);

        try {
            await apiClient.put(`/api/profile/update_profile`, {
                firstName,
                lastName,
                phone
            }, {
                withCredentials: true,
                headers: { 'Content-Type': 'application/json' },
            });

            await refreshUser(); // invalidate + re-fetch the global user cache
            setSuccess(true);
            setTimeout(() => navigate('/profile'), 1500);
        } catch (err) {
            console.error('Error updating profile:', err);
            setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    if (apiLoading && !data) {
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
                <h1>Update Personal Information</h1>
                <p>Update your name and contact details</p>
            </div>

            <div className="dashboard-content" style={{ display: 'block', maxWidth: '600px' }}>
                <div className="dashboard-section">
                    {error && (
                        <div className="alert alert-error" style={{ marginBottom: 'var(--spacing-4)' }}>
                            {error}
                        </div>
                    )}
                    {success && (
                        <div className="alert alert-success" style={{ marginBottom: 'var(--spacing-4)' }}>
                            Profile updated successfully! Redirecting...
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="firstName">First Name</label>
                            <input
                                type="text"
                                id="firstName"
                                className="form-input"
                                value={firstName}
                                placeholder={data?.firstName || 'Enter your first name'}
                                onChange={(e) => setFirstName(e.target.value)}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="lastName">Last Name</label>
                            <input
                                type="text"
                                id="lastName"
                                className="form-input"
                                value={lastName}
                                placeholder={data?.lastName || 'Enter your last name'}
                                onChange={(e) => setLastName(e.target.value)}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="phoneNumber">Phone Number</label>
                            <input
                                type="text"
                                id="phoneNumber"
                                className="form-input"
                                value={phone}
                                placeholder={data?.phone || 'xxx-xxx-xxxx'}
                                onChange={(e) => setPhone(e.target.value)}
                            />
                        </div>

                        <div className="form-actions">
                            <button 
                                type="submit" 
                                className="btn btn-primary"
                                disabled={saving}
                            >
                                {saving ? 'Saving...' : 'Save Changes'}
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

export default Personal;
