import React from 'react';

const ChoreModal = ({ 
    show, 
    onClose, 
    isCustomChore, 
    setIsCustomChore, 
    choreData, 
    setChoreData, 
    CHORE_OPTIONS, 
    addChoreToList, 
    pendingChores, 
    removeChoreFromList, 
    handleSubmitChores, 
    isValidDeadline 
}) => {
    if (!show) return null;

    return (
        <div className="modal-overlay">
            <div className="modal modal-large">
                <div className="modal-header">
                    <h3>Create Chores</h3>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    <div className="form-group">
                        <label>Chore Type</label>
                        <select
                            className="form-input"
                            value={isCustomChore ? "Other" : choreData.choreName}
                            onChange={e => {
                                if (e.target.value === "Other") {
                                    setIsCustomChore(true);
                                    setChoreData({ ...choreData, choreName: "" });
                                } else {
                                    setIsCustomChore(false);
                                    setChoreData({ ...choreData, choreName: e.target.value });
                                }
                            }}
                        >
                            <option value="">Select chore</option>
                            {CHORE_OPTIONS.map(opt => (<option key={opt} value={opt}>{opt}</option>))}
                        </select>
                    </div>

                    {isCustomChore && (
                        <div className="form-group">
                            <label>Custom Chore Name</label>
                            <input
                                type="text"
                                className="form-input"
                                placeholder="Custom chore name"
                                value={choreData.choreName}
                                onChange={e => setChoreData({ ...choreData, choreName: e.target.value })}
                            />
                        </div>
                    )}

                    <div className="form-group">
                        <label>Frequency</label>
                        <div style={{ display: 'flex', gap: 'var(--spacing-2)' }}>
                            <input
                                type="number"
                                className="form-input"
                                placeholder="Times"
                                value={choreData.frequency}
                                min={1}
                                onChange={e => setChoreData({ ...choreData, frequency: parseInt(e.target.value) })}
                                style={{ width: '100px' }}
                            />
                            <select
                                className="form-input"
                                value={choreData.frequencyUnit}
                                onChange={e => setChoreData({ ...choreData, frequencyUnit: e.target.value })}
                            >
                                <option value="WEEKLY">Weekly</option>
                                <option value="BIWEEKLY">Biweekly</option>
                                <option value="MONTHLY">Monthly</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Deadline</label>
                        <input
                            type="date"
                            className="form-input"
                            value={choreData.deadline}
                            onChange={e => setChoreData({ ...choreData, deadline: e.target.value })}
                            min={new Date().toISOString().split('T')[0]}
                            max={(() => {
                                let d = new Date();
                                d.setFullYear(d.getFullYear() + 1);
                                return d.toISOString().split('T')[0];
                            })()}
                        />
                    </div>

                    <button
                        className="btn btn-secondary"
                        onClick={addChoreToList}
                        disabled={!choreData.choreName || !isValidDeadline(choreData.deadline)}
                    >
                        Add to List
                    </button>

                    <h4 style={{ marginTop: 'var(--spacing-4)' }}>Chores to be created:</h4>
                    {pendingChores.length === 0 ? (
                        <p className="empty-message">No chores added yet.</p>
                    ) : (
                        <ul>
                            {pendingChores.map((chore, idx) => (
                                <li key={idx}>
                                    <div className="item-content">
                                        <div className="item-title">
                                            {chore.choreName} - {chore.frequencyUnit} - {chore.frequency}x
                                        </div>
                                        <div className="item-meta">
                                            Until: {new Date(chore.deadline).toLocaleDateString()}
                                        </div>
                                    </div>
                                    <button className="btn btn-danger" onClick={() => removeChoreFromList(idx)}>
                                        Remove
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <div className="modal-actions">
                    <button
                        className="btn btn-primary"
                        onClick={handleSubmitChores}
                        disabled={pendingChores.length === 0}
                    >
                        Submit All Chores
                    </button>
                    <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default ChoreModal;
