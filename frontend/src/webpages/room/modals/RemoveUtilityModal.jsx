import React from 'react';

const RemoveUtilityModal = ({ 
    show, 
    onClose, 
    utilities, 
    selectedUtilityId, 
    setSelectedUtilityId, 
    handleRemoveUtility 
}) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Remove Utility</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <div className="form-group">
                        <label htmlFor="utilitySelectModal">Select utility:</label>
                        <select
                            id="utilitySelectModal"
                            className="form-input"
                            value={selectedUtilityId}
                            onChange={e => setSelectedUtilityId(e.target.value)}
                        >
                            <option value="">Select a utility</option>
                            {utilities.map(u => (
                                <option key={u.id} value={u.id}>
                                    {u.utilityName} - ${u.utilityPrice.toFixed(2)}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
                <div className="modal-actions">
                    <button className="btn btn-danger" onClick={handleRemoveUtility} disabled={!selectedUtilityId}>
                        Remove
                    </button>
                    <button className="btn btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RemoveUtilityModal;
