import React from 'react';

const UtilityModal = ({ 
    show, 
    onClose, 
    utilityData, 
    setUtilityData, 
    handleSubmitUtility 
}) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Create Utility</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <div className="form-group">
                        <label>Utility Name</label>
                        <input
                            type="text"
                            className="form-input"
                            placeholder="Utility name"
                            value={utilityData.utilityName}
                            onChange={e => setUtilityData({ ...utilityData, utilityName: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea
                            className="form-input"
                            placeholder="Description"
                            value={utilityData.description}
                            onChange={e => setUtilityData({ ...utilityData, description: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Total Price</label>
                        <input
                            type="number"
                            className="form-input"
                            placeholder="Total Price"
                            value={utilityData.utilityPrice || ""}
                            onChange={e => {
                                const value = parseFloat(e.target.value);
                                if (value >= 0 || e.target.value === "") {
                                    setUtilityData({
                                        ...utilityData, utilityPrice: e.target.value === "" ? "" : value
                                    });
                                }
                            }}
                        />
                    </div>
                    <div className="form-group">
                        <label>Distribution</label>
                        <select
                            className="form-input"
                            value={utilityData.utilDistributionEnum}
                            onChange={e => setUtilityData({ ...utilityData, utilDistributionEnum: e.target.value })}
                        >
                            <option value="EQUALSPLIT">Equal Split</option>
                        </select>
                    </div>
                </div>
                <div className="modal-actions">
                    <button className="btn btn-primary" onClick={handleSubmitUtility}>Create Utility</button>
                    <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default UtilityModal;
