import apiClient from '../apiClient';
import React, { useEffect } from 'react';

const VerificationPopup = ({ onPopupVisibilityChange }) => {
    const [visible, setVisible] = React.useState(true);
    const [resendMessage, setResendMessage] = React.useState('');

    useEffect(() => {
        if (onPopupVisibilityChange) {
            onPopupVisibilityChange(visible);
        }
    }, [visible, onPopupVisibilityChange]);

    const handleClose = () => setVisible(false);

    const handleResend = async () => {
        try {
            const res = await apiClient.post('/user/resend-verification');
            if (res.status === 200) {
                setResendMessage('Verification email has been resent! Check your inbox.');
            } else {
                setResendMessage('Failed to resend verification email. Try again later.');
            }
        } catch (err) {
            console.error(err);
            setResendMessage('An error occurred. Please try again.');
        }
    };

    if (!visible) return null;

    return (
        <div className="verification-popup">
            <div className="popup-content">
                <h2>Email Verification Required</h2>
                <p>
                    Your email is not verified. Please check your inbox and follow the link to verify your account.
                </p>
                {resendMessage && <p className="resend-message">{resendMessage}</p>}
                <button onClick={handleResend}>Resend Verification Email</button>
                <button onClick={handleClose}>Close</button>
            </div>
        </div>
    );
};

export default VerificationPopup;