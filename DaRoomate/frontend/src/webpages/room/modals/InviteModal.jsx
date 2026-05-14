import React from 'react';

const InviteModal = ({ 
    show, 
    onClose, 
    inviteEmail, 
    setInviteEmail, 
    inviteStatus, 
    handleInviteUser 
}) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Invite a Roommate</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <div className="form-group">
                        <label>Email Address</label>
                        <input
                            type="email"
                            className="form-input"
                            placeholder="Enter email address"
                            value={inviteEmail}
                            onChange={e => setInviteEmail(e.target.value)}
                        />
                    </div>
                    {inviteStatus && (
                        <div className={`alert ${inviteStatus.includes('success') ? 'alert-success' : 'alert-error'}`}>
                            {inviteStatus}
                        </div>
                    )}
                </div>
                <div className="modal-actions">
                    <button className="btn btn-primary" onClick={handleInviteUser}>Send Invite</button>
                    <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default InviteModal;
