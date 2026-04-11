import React from 'react';

const DeleteConfirmModal = ({ show, onClose, onConfirm, roomName }) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Delete Room</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <div className="delete-warning">
                        <div className="delete-warning-icon"></div>
                        <p className="delete-warning-title">Are you sure you want to delete this room?</p>
                        <p className="delete-warning-message">
                            This will permanently delete <strong>"{roomName}"</strong> and remove all members, 
                            chores, and utilities. This action cannot be undone.
                        </p>
                    </div>
                </div>
                <div className="modal-actions">
                    <button 
                        className="btn btn-danger" 
                        onClick={() => {
                            onClose();
                            onConfirm();
                        }}
                    >
                        Yes, Delete Room
                    </button>
                    <button className="btn btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DeleteConfirmModal;
