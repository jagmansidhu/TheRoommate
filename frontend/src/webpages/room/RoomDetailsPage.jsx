import apiClient from '../../apiClient';
import { useState, useEffect } from 'react';
import axios from 'axios';
import { ROLES } from "../../constants/roles";
import { useUser, useAppData } from '../../App';
import '../../styling/Rooms.css';
import '../../styling/Dashboard.css';
import RemoveUtilityModal from './modals/RemoveUtilityModal';
import UtilityModal from './modals/UtilityModal';
import RemoveChoreModal from './modals/RemoveChoreModal';
import ChoreModal from './modals/ChoreModal';
import InviteModal from './modals/InviteModal';
import DeleteConfirmModal from './modals/DeleteConfirmModal';

const RoomDetailsPage = ({
    show, onClose, room, onLeaveRoom, onDeleteRoom, onManageRolesClick,
}) => {
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviteStatus, setInviteStatus] = useState('');
    const { user } = useUser();
    const { refreshUserChores, refreshUserUtilities } = useAppData();
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
            const response = await apiClient.post(`/api/chores/room/createChores/${room.id}`,
                pendingChores,
                { withCredentials: true, headers: { 'Content-Type': 'application/json' } }
            );
            if (response.status === 200) {
                setShowChoreModal(false);
                setPendingChores([]);
                const choresResponse = await apiClient.get(`/api/chores/room/${room.id}`,
                    { withCredentials: true }
                );
                setChores(choresResponse.data);
                refreshUserChores();
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
            await apiClient.delete(`/api/chores/room/${room.id}/type/${selectedChoreType}`,
                { withCredentials: true }
            );
            const choresResponse = await apiClient.get(`/api/chores/room/${room.id}`,
                { withCredentials: true }
            );
            setChores(choresResponse.data);
            refreshUserChores();
        } catch (error) {
            console.error('Error removing chores:', error);
        }
    };


    useEffect(() => {
        const fetchChores = async () => {
            if (room?.id) {
                try {
                    const response = await apiClient.get(`/api/chores/room/${room.id}`,
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
                    const response = await apiClient.get(`/api/utility/room/${room.id}`,
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
                        const response = await apiClient.get(`/api/utility/${memberId}/room/${room.id}`,
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
            const response = await apiClient.post(`/api/rooms/invite`,
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
            const response = await apiClient.post(`/api/utility/create`,
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
                    const userUtilitiesResponse = await apiClient.get(`/api/utility/${memberId}/room/${room.id}`,
                        { withCredentials: true }
                    );
                    setUserUtilities(userUtilitiesResponse.data);
                    refreshUserUtilities();
                }
            }
        } catch (err) {
            console.error("Error creating utility:", err);
        }
    };

    const handleRemoveUtility = async () => {
        if (!selectedUtilityId) return;
        try {
            await apiClient.delete(`/api/utility/${selectedUtilityId}`,
                { withCredentials: true }
            );
            if (memberId) {
                const userUtilitiesResponse = await apiClient.get(`/api/utility/${memberId}/room/${room.id}`,
                    { withCredentials: true }
                );
                setUserUtilities(userUtilitiesResponse.data);
                refreshUserUtilities();
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

            <RemoveUtilityModal
                show={showRemoveUtilityModal}
                onClose={() => setShowRemoveUtilityModal(false)}
                utilities={utilities}
                selectedUtilityId={selectedUtilityId}
                setSelectedUtilityId={setSelectedUtilityId}
                handleRemoveUtility={handleRemoveUtility}
            />

            <UtilityModal
                show={showUtilityModal}
                onClose={() => setShowUtilityModal(false)}
                utilityData={utilityData}
                setUtilityData={setUtilityData}
                handleSubmitUtility={handleSubmitUtility}
            />

            <RemoveChoreModal
                show={showRemoveChoreModal}
                onClose={() => setShowRemoveChoreModal(false)}
                selectedChoreType={selectedChoreType}
                setSelectedChoreType={setSelectedChoreType}
                handleRemoveChoresByType={handleRemoveChoresByType}
                CHORE_OPTIONS={CHORE_OPTIONS}
            />

            <ChoreModal
                show={showChoreModal}
                onClose={() => setShowChoreModal(false)}
                isCustomChore={isCustomChore}
                setIsCustomChore={setIsCustomChore}
                choreData={choreData}
                setChoreData={setChoreData}
                CHORE_OPTIONS={CHORE_OPTIONS}
                addChoreToList={addChoreToList}
                pendingChores={pendingChores}
                removeChoreFromList={removeChoreFromList}
                handleSubmitChores={handleSubmitChores}
                isValidDeadline={isValidDeadline}
            />

            <InviteModal
                show={showInviteModal}
                onClose={() => setShowInviteModal(false)}
                inviteEmail={inviteEmail}
                setInviteEmail={setInviteEmail}
                inviteStatus={inviteStatus}
                handleInviteUser={handleInviteUser}
            />

            <DeleteConfirmModal
                show={showDeleteConfirmModal}
                onClose={() => setShowDeleteConfirmModal(false)}
                onConfirm={onDeleteRoom}
                roomName={room.name}
            />
        </div>
    );
};

export default RoomDetailsPage;
