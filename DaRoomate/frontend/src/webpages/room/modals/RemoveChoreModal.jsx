import React from 'react';

const RemoveChoreModal = ({ 
    show, 
    onClose, 
    selectedChoreType, 
    setSelectedChoreType, 
    handleRemoveChoresByType, 
    CHORE_OPTIONS 
}) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Remove Chores</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <p className="delete-warning-message" style={{ marginBottom: '1rem', padding: '0' }}>
                        This will delete ALL chores of the selected type.
                    </p>
                    <div className="form-group">
                        <label htmlFor="choreTypeSelectModal">Select chore type:</label>
                        <select
                            id="choreTypeSelectModal"
                            className="form-input"
                            value={selectedChoreType}
                            onChange={e => setSelectedChoreType(e.target.value)}
                        >
                            <option value="">Select a chore type</option>
                            {CHORE_OPTIONS.map(opt => (
                                <option key={opt} value={opt}>{opt}</option>
                            ))}
                        </select>
                    </div>
                </div>
                <div className="modal-actions">
                    <button 
                        className="btn btn-danger" 
                        onClick={() => {
                            handleRemoveChoresByType();
                            onClose();
                        }} 
                        disabled={!selectedChoreType}
                    >
                        Remove All
                    </button>
                    <button className="btn btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RemoveChoreModal;
