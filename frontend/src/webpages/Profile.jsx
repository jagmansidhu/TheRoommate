import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import '../styling/Dashboard.css';

const Profile = () => {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [apiLoading, setApiLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const response = await axios.get(`${process.env.REACT_APP_BASE_API_URL}/api/get-user`, {
                    withCredentials: true,
                });
                setData(response.data);
            } catch (err) {
                console.error('Error fetching user data:', err);
                setError(err);
            } finally {
                setApiLoading(false);
            }
        };

        fetchUserData();
    }, []);

    if (apiLoading) {
        return (
            <div className="loading">
                <div className="spinner spinner-lg"></div>
                <p>Loading...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="dashboard-container">
                <div className="dashboard-header">
                    <h1>Profile</h1>
                </div>
                <div className="dashboard-section">
                    <p className="empty-message">Failed to load profile data. Please try again later.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            {/* Profile Header */}
            <div className="dashboard-header">
                <h1>Profile</h1>
                <p>Manage your account settings and personal information</p>
            </div>

            {/* Main Content Grid */}
            <div className="dashboard-content">
                <div className="dashboard-section profile-info-section">
                    <h3>Login Information</h3>
                    <ul>
                        <li>
                            <div className="item-icon item-icon-email"></div>
                            <div className="item-content">
                                <div className="item-title">Email</div>
                                <div className="item-meta">{data?.email || 'Not set'}</div>
                            </div>
                        </li>
                        <li>
                            <div className="item-icon item-icon-password"></div>
                            <div className="item-content">
                                <div className="item-title">Password</div>
                                <div className="item-meta">••••••••</div>
                            </div>
                        </li>
                    </ul>
                    <div className="quick-actions" style={{ marginTop: 'var(--spacing-4)' }}>
                        <button className="quick-action-btn" onClick={() => navigate('/reset-password')}>
                            Change Login Credentials
                        </button>
                    </div>
                </div>

                <div className="dashboard-section profile-info-section">
                    <h3>Personal Information</h3>
                    <ul>
                        <li>
                            <div className="item-icon item-icon-user"></div>
                            <div className="item-content">
                                <div className="item-title">First Name</div>
                                <div className="item-meta">{data?.firstName || 'Not set'}</div>
                            </div>
                        </li>
                        <li>
                            <div className="item-icon item-icon-user"></div>
                            <div className="item-content">
                                <div className="item-title">Last Name</div>
                                <div className="item-meta">{data?.lastName || 'Not set'}</div>
                            </div>
                        </li>
                        <li>
                            <div className="item-icon item-icon-phone"></div>
                            <div className="item-content">
                                <div className="item-title">Phone</div>
                                <div className="item-meta">{data?.phone || 'Not set'}</div>
                            </div>
                        </li>
                    </ul>
                    <div className="quick-actions" style={{ marginTop: 'var(--spacing-4)' }}>
                        <button className="quick-action-btn" onClick={() => navigate('/update-personal')}>
                            Update Personal Info
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;