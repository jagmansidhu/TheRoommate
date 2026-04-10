import { useState, useEffect } from 'react';
import axios from 'axios';
import { ROLES } from "../../constants/roles";
import { useUser } from '../../App';
import '../../styling/Rooms.css';
import '../../styling/Dashboard.css';

const RoomDetailsPage = ({
    show, onClose, room, onLeaveRoom, onDeleteRoom, onManageRolesClick,
}) => {
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviteStatus, setInviteStatus] = useState('');
    const { user } = useUser();
    const [utilities, setUtilities] = useState([]);
    const [showRemoveUtilityModal, setShowRemoveUtilityModal] = useState(false);
    const [selectedUtilityId, setSelectedUtilityId] = useState("");
    const [showUtilityModal, setShowUtilityModal] = useState(false);
    const [utilityData, setUtilityData] = useState({
        utilityName: "", description: "", utilityPrice: 0, utilDistributionEnum: "EQUALSPLIT", customSplit: {}
    });
    const [showChoreModal, setShowChoreModal] = useState(false);
    const [showRemoveChoreModal, setShowRemoveChoreModal] = useState(false);
    const [pendingChores, setPendingChores] = useState([]);
    const [choreData, setChoreData] = useState({
        choreName: '', frequency: 1, frequencyUnit: 'WEEKLY', deadline: ''
    });
    const [selectedChoreType, setSelectedChoreType] = useState('');
    const [chores, setChores] = useState([]);
    const [showRoleModal, setShowRoleModal] = useState(false);
    const [userUtilities, setUserUtilities] = useState([]);
    const [memberId, setMemberId] = useState(null);
    const [isCustomChore, setIsCustomChore] = useState(false);
    const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false);

    const resetChoreData = () => setChoreData({ choreName: '', frequency: 1, frequencyUnit: 'WEEKLY' });

    const addChoreToList = () => {
        if (!choreData.choreName || choreData.frequency < 1 || !isValidDeadline(choreData.deadline)) return;
        setPendingChores([...pendingChores, { ...choreData, deadline: new Date(choreData.deadline).toISOString() }]);
        resetChoreData();
    };

    const removeChoreFromList = (idx) => {
        setPendingChores(pendingChores.filter((_, i) => i !== idx));
    };

    const handleSubmitChores = async () => {
        if (pendingChores.length === 0) return;
        try {
            const response = await axios.post(
                `${process.env.REACT_APP_BASE_API_URL}/api/chores/room/createChores/${room.id}`,
                pendingChores,
                { withCredentials: true, headers: { 'Content-Type': 'application/json' } }
            );
            if (response.status === 200) {
                setShowChoreModal(false);
                setPendingChores([]);
                const choresResponse = await axios.get(
                    `${process.env.REACT_APP_BASE_API_URL}/api/chores/room/${room.id}`,
                    { withCredentials: true }
                );
                setChores(choresResponse.data);
            }
        } catch (error) {
            console.error('Error creating chores:', error);
        }
    };

    const isValidDeadline = (dateStr) => {
        if (!dateStr) return false;
        const date = new Date(dateStr);
        const now = new Date();
        const max = new Date();
        max.setFullYear(now.getFullYear() + 1);
        return date > now && date <= max;
    };

    const handleRemoveChoresByType = async () => {
        if (!selectedChoreType) return;
        try {
            await axios.delete(
                `${process.env.REACT_APP_BASE_API_URL}/api/chores/room/${room.id}/type/${selectedChoreType}`,
                { withCredentials: true }
            );
            const choresResponse = await axios.get(
                `${process.env.REACT_APP_BASE_API_URL}/api/chores/room/${room.id}`,
                { withCredentials: true }
            );
            setChores(choresResponse.data);
        } catch (error) {
            console.error('Error removing chores:', error);
        }
    };


    useEffect(() => {
        const fetchChores = async () => {
            if (room?.id) {
                try {
                    const response = await axios.get(
                        `${process.env.REACT_APP_BASE_API_URL}/api/chores/room/${room.id}`,
                        { withCredentials: true }
                    );
                    setChores(response.data);
                } catch (error) {
                    console.error('Error fetching chores:', error);
                }
            }
        };

        const fetchAllUtilities = async () => {
            if (room?.id) {
                try {
                    const response = await axios.get(
                        `${process.env.REACT_APP_BASE_API_URL}/api/utility/room/${room.id}`,
                        { withCredentials: true }
                    );
                    setUtilities(response.data);
                } catch (err) {
                    console.error("Error fetching utilities:", err);
                }
            }
        };

        const fetchUserUtilities = async () => {
            if (room?.id && user?.email) {
                try {
                    const memberId = room.members?.find(m => m.userId === user.email)?.id;
                    setMemberId(memberId);
                    if (memberId) {
                        const response = await axios.get(
                            `${process.env.REACT_APP_BASE_API_URL}/api/utility/${memberId}/room/${room.id}`,
                            { withCredentials: true }
                        );
                        setUserUtilities(response.data);
                    }
                } catch (err) {
                    console.error("Error fetching user utilities:", err);
                }
            }
        };

        fetchChores();
        fetchAllUtilities();
        fetchUserUtilities();
    }, [room, user]);

    if (!show || !room || !user) return null;

    const memberRole = room.members?.find(m => m.userId === user.email)?.role;
    const isHeadRoommate = memberRole === ROLES.HEAD_ROOMMATE;
    const isAssistantRoommate = memberRole === ROLES.ASSISTANT;

    const handleInviteUser = async () => {
        try {
            const response = await axios.post(
                `${process.env.REACT_APP_BASE_API_URL}/api/rooms/invite`,
                { email: inviteEmail, roomId: room.id },
                { withCredentials: true, headers: { 'Content-Type': 'application/json' } }
            );
            if (response.status === 200) {
                setInviteStatus('Invite sent successfully!');
                setInviteEmail('');
            } else {
                setInviteStatus('Failed to send invite.');
            }
        } catch (error) {
            console.error(error);
            setInviteStatus('Failed to send invite.');
        }
    };

    const handleSubmitUtility = async () => {
        try {
            const payload = { ...utilityData, roomId: room.id };
            const response = await axios.post(
                `${process.env.REACT_APP_BASE_API_URL}/api/utility/create`,
                payload,
                { withCredentials: true, headers: { "Content-Type": "application/json" } }
            );
            if (response.status === 200) {
                setShowUtilityModal(false);
                setUtilityData({
                    utilityName: "", description: "", utilityPrice: 0,
                    utilDistributionEnum: "EQUALSPLIT", customSplit: {}
                });
                if (memberId) {
                    const userUtilitiesResponse = await axios.get(
                        `${process.env.REACT_APP_BASE_API_URL}/api/utility/${memberId}/room/${room.id}`,
                        { withCredentials: true }
                    );
                    setUserUtilities(userUtilitiesResponse.data);
                }
            }
        } catch (err) {
            console.error("Error creating utility:", err);
        }
    };

    const handleRemoveUtility = async () => {
        if (!selectedUtilityId) return;
        try {
            await axios.delete(
                `${process.env.REACT_APP_BASE_API_URL}/api/utility/${selectedUtilityId}`,
                { withCredentials: true }
            );
            if (memberId) {
                const userUtilitiesResponse = await axios.get(
                    `${process.env.REACT_APP_BASE_API_URL}/api/utility/${memberId}/room/${room.id}`,
                    { withCredentials: true }
                );
                setUserUtilities(userUtilitiesResponse.data);
            }
            setShowRemoveUtilityModal(false);
            setSelectedUtilityId("");
        } catch (err) {
            console.error("Error removing utility:", err);
        }
    };

    const CHORE_OPTIONS = ["Broom", "Sweep", "Trash", "Mop", "Vacuum", "Kitchen", "Other"];

    // Calculate upcoming chores grouped by date
    const getChoresByDate = () => {
        const now = new Date();
        const oneMonthAhead = new Date();
        oneMonthAhead.setMonth(now.getMonth() + 1);
        const choresByDate = {};
        
        chores
            .filter(chore => {
                const dueDate = new Date(chore.dueAt);
                return dueDate >= now && dueDate <= oneMonthAhead;
            })
            .sort((a, b) => new Date(a.dueAt) - new Date(b.dueAt))
            .forEach(chore => {
                const dueDate = new Date(chore.dueAt);
                const key = dueDate.toLocaleString('default', { month: 'short' }) + ' ' + dueDate.getDate();
                if (!choresByDate[key]) choresByDate[key] = [];
                choresByDate[key].push(chore);
            });
        
        return choresByDate;
    };

    const choresByDate = getChoresByDate();

    return (
        <div className="dashboard-container">
            {/* Room Header - Compact with member count */}
            <div className="room-details-header">
                <div className="room-details-title">
                    <h1>{room.name}</h1>
                    <p className="room-address-code">
                        {room.address} <span className="room-code-inline">Code: <code>{room.roomCode}</code></span>
                    </p>
                </div>
                <div className="room-member-count">
                    <span className="member-count-value">{room.members?.length || 0}</span>
                    <span className="member-count-label">/ 6 Members</span>
                </div>
            </div>

            {/* Members and Utilities Side by Side */}
            <div className="dashboard-content">
                {/* Members */}
                <div className="dashboard-section">
                    <h3>Members</h3>
                    <div className="members-list">
                        {room.members?.map((member) => {
                            const isSelf = member.userId === user.email;
                            return (
                                <div key={member.id} className="member-item">
                                    <div className="member-avatar">
                                        {member.name?.charAt(0)?.toUpperCase() || '?'}
                                    </div>
                                    <div className="member-info">
                                        <div className="member-name">{member.name}</div>
                                        <span className={`role-badge ${member.role}`}>{member.role}</span>
                                    </div>
                                    {isSelf && member.role !== ROLES.HEAD_ROOMMATE && (
                                        <button className="btn btn-danger btn-sm" onClick={() => onLeaveRoom(member.id)}>
                                            Leave
                                        </button>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* Your Utilities */}
                <div className="dashboard-section">
                    <h3>Your Utilities</h3>
                    {userUtilities.length === 0 ? (
                        <p className="empty-message">No utilities assigned.</p>
                    ) : (
                        <ul>
                            {userUtilities.map(u => (
                                <li key={u.id}>
                                    <div className="item-content">
                                        <div className="item-title">{u.utilityName}</div>
                                        <div className="item-meta">${u.utilityPrice.toFixed(2)}</div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>

            {/* Upcoming Chores */}
            <div className="dashboard-section">
                <h3>Upcoming Chores</h3>
                {Object.keys(choresByDate).length === 0 ? (
                    <p className="empty-message">No upcoming chores.</p>
                ) : (
                    <div className="chores-list">
                        {Object.entries(choresByDate).map(([date, choresForDate]) => (
                            <div key={date} className="chore-date-group">
                                <div className="chore-date-badge">{date}</div>
                                <div className="chore-items">
                                    {choresForDate.map(chore => (
                                        <div key={chore.id} className="chore-item">
                                            <div className="chore-status-indicator pending"></div>
                                            <div className="chore-info">
                                                <div className="chore-name">{chore.choreName}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Management Actions - Apple-style Action List */}
            {(isAssistantRoommate || isHeadRoommate) && (
                <div className="dashboard-section">
                    <h3>Room Management</h3>
                    
                    {/* Add Actions Group */}
                    <div className="action-group">
                        <div className="action-group-label">Add New</div>
                        <div className="action-list">
                            <button className="action-item" onClick={() => setShowInviteModal(true)}>
                                <div className="action-icon action-icon-invite"></div>
                                <div className="action-content">
                                    <div className="action-title">Invite Roommate</div>
                                    <div className="action-description">Send an invitation by email</div>
                                </div>
                                <div className="action-chevron"></div>
                            </button>
                            <button className="action-item" onClick={() => setShowChoreModal(true)}>
                                <div className="action-icon action-icon-chore"></div>
                                <div className="action-content">
                                    <div className="action-title">Create Chore</div>
                                    <div className="action-description">Schedule recurring chores</div>
                                </div>
                                <div className="action-chevron"></div>
                            </button>
                            <button className="action-item" onClick={() => setShowUtilityModal(true)}>
                                <div className="action-icon action-icon-utility"></div>
                                <div className="action-content">
                                    <div className="action-title">Add Utility</div>
                                    <div className="action-description">Track bills and split costs</div>
                                </div>
                                <div className="action-chevron"></div>
                            </button>
                        </div>
                    </div>

                    {/* Remove Actions Group */}
                    <div className="action-group">
                        <div className="action-group-label">Remove</div>
                        <div className="action-list">
                            <button className="action-item" onClick={() => setShowRemoveChoreModal(true)}>
                                <div className="action-icon action-icon-remove-chore"></div>
                                <div className="action-content">
                                    <div className="action-title">Remove Chore</div>
                                    <div className="action-description">Delete a scheduled chore type</div>
                                </div>
                                <div className="action-chevron"></div>
                            </button>
                            <button className="action-item" onClick={() => setShowRemoveUtilityModal(true)}>
                                <div className="action-icon action-icon-remove-utility"></div>
                                <div className="action-content">
                                    <div className="action-title">Remove Utility</div>
                                    <div className="action-description">Delete a tracked utility</div>
                                </div>
                                <div className="action-chevron"></div>
                            </button>
                        </div>
                    </div>

                    {/* Danger Zone */}
                    {isHeadRoommate && (
                        <div className="action-group">
                            <div className="action-group-label">Danger Zone</div>
                            <div className="action-list action-list-danger">
                                <button className="action-item action-item-danger" onClick={() => setShowDeleteConfirmModal(true)}>
                                    <div className="action-icon action-icon-delete"></div>
                                    <div className="action-content">
                                        <div className="action-title">Delete Room</div>
                                        <div className="action-description">Permanently remove this room</div>
                                    </div>
                                    <div className="action-chevron"></div>
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* Remove Utility Modal */}
            {showRemoveUtilityModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Remove Utility</h3>
                            <button className="modal-close" onClick={() => setShowRemoveUtilityModal(false)}>×</button>
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
                            <button className="btn btn-secondary" onClick={() => setShowRemoveUtilityModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Utility Modal */}
            {showUtilityModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Create Utility</h3>
                            <button className="modal-close" onClick={() => setShowUtilityModal(false)}>×</button>
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
                            <button className="btn btn-secondary" onClick={() => setShowUtilityModal(false)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Remove Chore Modal */}
            {showRemoveChoreModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Remove Chores</h3>
                            <button className="modal-close" onClick={() => setShowRemoveChoreModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div className="form-group">
                                <label htmlFor="choreTypeSelectModal">Select chore type:</label>
                                <select
                                    id="choreTypeSelectModal"
                                    className="form-input"
                                    value={selectedChoreType}
                                    onChange={e => setSelectedChoreType(e.target.value)}
                                >
                                    <option value="">Select chore type</option>
                                    {[...new Set(chores.map(chore => chore.choreName))].map(name => (
                                        <option key={name} value={name}>{name}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <div className="modal-actions">
                            <button
                                className="btn btn-danger"
                                onClick={() => {
                                    handleRemoveChoresByType();
                                    setShowRemoveChoreModal(false);
                                }}
                                disabled={!selectedChoreType}
                            >
                                Remove
                            </button>
                            <button className="btn btn-secondary" onClick={() => setShowRemoveChoreModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Chore Modal */}
            {showChoreModal && (
                <div className="modal-overlay">
                    <div className="modal modal-large">
                        <div className="modal-header">
                            <h3>Create Chores</h3>
                            <button className="modal-close" onClick={() => setShowChoreModal(false)}>×</button>
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
                            <button className="btn btn-secondary" onClick={() => setShowChoreModal(false)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Invite Modal */}
            {showInviteModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Invite a Roommate</h3>
                            <button className="modal-close" onClick={() => setShowInviteModal(false)}>×</button>
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
                            <button className="btn btn-secondary" onClick={() => setShowInviteModal(false)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Delete Room Confirmation Modal */}
            {showDeleteConfirmModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Delete Room</h3>
                            <button className="modal-close" onClick={() => setShowDeleteConfirmModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div className="delete-warning">
                                <div className="delete-warning-icon"></div>
                                <p className="delete-warning-title">Are you sure you want to delete this room?</p>
                                <p className="delete-warning-message">
                                    This will permanently delete <strong>"{room.name}"</strong> and remove all members, 
                                    chores, and utilities. This action cannot be undone.
                                </p>
                            </div>
                        </div>
                        <div className="modal-actions">
                            <button 
                                className="btn btn-danger" 
                                onClick={() => {
                                    setShowDeleteConfirmModal(false);
                                    onDeleteRoom();
                                }}
                            >
                                Yes, Delete Room
                            </button>
                            <button className="btn btn-secondary" onClick={() => setShowDeleteConfirmModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RoomDetailsPage;
