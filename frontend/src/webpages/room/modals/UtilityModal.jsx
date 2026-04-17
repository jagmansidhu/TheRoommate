import React, { useState } from 'react';

const UtilityModal = ({ 
    show, 
    onClose, 
    utilityData, 
    setUtilityData, 
    handleSubmitUtility,
    members = []
}) => {
    const [error, setError] = useState("");

    if (!show) return null;

    const handleCustomSplitChange = (memberId, value) => {
        const numValue = parseFloat(value);
        setUtilityData(prev => ({
            ...prev,
            customSplit: {
                ...prev.customSplit,
                [memberId]: value === "" ? "" : numValue
            }
        }));
    };

    const validateAndSubmit = () => {
        if (!utilityData.startingDate) {
            setError("Starting date is required");
            return;
        }
        if (!utilityData.deadline) {
            setError("End date is required");
            return;
        }
        if (new Date(utilityData.deadline) < new Date(utilityData.startingDate)) {
            setError("End date must be after starting date");
            return;
        }
        if (utilityData.utilDistributionEnum === "CUSTOMSPLIT") {
            const sum = Object.values(utilityData.customSplit || {}).reduce((a, b) => a + (parseFloat(b) || 0), 0);
            
            if (utilityData.splitType === "PERCENT") {
                if (Math.abs(sum - 100) > 0.01) {
                    setError(`Custom split percentages sum to ${sum.toFixed(2)}%, but must equal exactly 100%`);
                    return;
                }
            } else {
                const total = parseFloat(utilityData.utilityPrice) || 0;
                if (Math.abs(sum - total) > 0.01) {
                    setError(`Custom split total ($${sum.toFixed(2)}) must equal total price ($${total.toFixed(2)})`);
                    return;
                }
            }
        }
        setError("");
        handleSubmitUtility();
    };

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
                            <option value="CUSTOMSPLIT">Custom Split</option>
                        </select>
                    </div>
                    
                    <div className="form-group" style={{marginTop: '1rem'}}>
                        <label>Frequency</label>
                        <select
                            className="form-input"
                            value={utilityData.frequencyUnit || "MONTHLY"}
                            onChange={e => setUtilityData({ ...utilityData, frequencyUnit: e.target.value })}
                        >
                            <option value="MONTHLY">Monthly</option>
                            <option value="BIWEEKLY">Biweekly</option>
                            <option value="WEEKLY">Weekly</option>
                        </select>
                    </div>

                    <div style={{display: 'flex', gap: '1rem', marginTop: '1rem'}}>
                        <div className="form-group" style={{flex: 1}}>
                            <label>Starting Date</label>
                            <input
                                type="date"
                                className="form-input"
                                value={utilityData.startingDate || ""}
                                onChange={e => setUtilityData({ ...utilityData, startingDate: e.target.value })}
                                min={new Date().toISOString().split('T')[0]}
                            />
                        </div>

                        <div className="form-group" style={{flex: 1}}>
                            <label>End Date</label>
                            <input
                                type="date"
                                className="form-input"
                                value={utilityData.deadline || ""}
                                onChange={e => setUtilityData({ ...utilityData, deadline: e.target.value })}
                                min={utilityData.startingDate || new Date().toISOString().split('T')[0]}
                                max={(() => {
                                    let d = new Date(utilityData.startingDate || new Date());
                                    d.setFullYear(d.getFullYear() + 1);
                                    return d.toISOString().split('T')[0];
                                })()}
                            />
                        </div>
                    </div>
                    
                    {utilityData.utilDistributionEnum === "CUSTOMSPLIT" && (
                        <>
                            <div className="form-group" style={{marginTop: '1rem'}}>
                                <label>Split Method</label>
                                <div style={{display: 'flex', gap: '1rem'}}>
                                    <label style={{display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 'normal'}}>
                                        <input 
                                            type="radio" 
                                            name="splitType" 
                                            checked={utilityData.splitType === "AMOUNT"}
                                            onChange={() => setUtilityData({...utilityData, splitType: "AMOUNT"})}
                                        /> Exact Amount ($)
                                    </label>
                                    <label style={{display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 'normal'}}>
                                        <input 
                                            type="radio" 
                                            name="splitType" 
                                            checked={utilityData.splitType === "PERCENT"}
                                            onChange={() => setUtilityData({...utilityData, splitType: "PERCENT"})}
                                        /> Percentage (%)
                                    </label>
                                </div>
                            </div>
                            <div className="form-group" style={{marginTop: '1rem'}}>
                                <label>Assign {utilityData.splitType === "PERCENT" ? "Percentages" : "Amounts"}</label>
                                {members.map(member => (
                                    <div key={member.id} style={{display: 'flex', alignItems: 'center', marginBottom: '0.5rem'}}>
                                        <span style={{flex: 1}}>{member.name}</span>
                                        <div style={{position: 'relative', display: 'flex', alignItems: 'center'}}>
                                            {utilityData.splitType === "AMOUNT" && <span style={{position: 'absolute', left: '10px', color: '#666'}}>$</span>}
                                            <input
                                                type="number"
                                                className="form-input"
                                                style={{width: '100px', paddingLeft: utilityData.splitType === "AMOUNT" ? '22px' : '10px', paddingRight: utilityData.splitType === "PERCENT" ? '25px' : '10px'}}
                                                placeholder={utilityData.splitType === "PERCENT" ? "0-100" : "Amount"}
                                                value={utilityData.customSplit?.[member.id] !== undefined ? utilityData.customSplit[member.id] : ""}
                                                onChange={e => handleCustomSplitChange(member.id, e.target.value)}
                                            />
                                            {utilityData.splitType === "PERCENT" && <span style={{position: 'absolute', right: '10px', color: '#666'}}>%</span>}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </>
                    )}
                    {error && <div className="error-text" style={{color: 'red', marginTop: '10px'}}>{error}</div>}
                </div>
                <div className="modal-actions">
                    <button className="btn btn-primary" onClick={validateAndSubmit}>Create Utility</button>
                    <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default UtilityModal;
