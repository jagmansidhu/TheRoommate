import apiClient from '../../apiClient';
import { useState, useEffect } from 'react';
import React from 'react';
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

    const isValidDeadline = (deadline) => {
        if (!deadline) return false;
        const d = new Date(deadline);
        const now = new Date();
        const oneYearAhead = new Date();
        oneYearAhead.setFullYear(now.getFullYear() + 1);
        return d > now && d <= oneYearAhead;
    };

    useEffect(() => {
        if (!room?.id || !user?.email) return;
        const fetchAll = async () => {
            try {
                const memberRes = await apiClient.get(`/api/rooms/${room.id}/member-id/${user.email}`, { withCredentials: true });
                const mId = memberRes.data;
                setMemberId(mId);

                const [choresRes, utilitiesRes, userUtilitiesRes] = await Promise.all([
                    apiClient.get(`/api/chores/${room.id}`, { withCredentials: true }),
                    apiClient.get(`/api/utility/${room.id}`, { withCredentials: true }),
                    apiClient.get(`/api/utility/${mId}/room/${room.id}`, { withCredentials: true }),
                ]);
                setChores(choresRes.data);
                setUtilities(utilitiesRes.data);
                setUserUtilities(userUtilitiesRes.data);
            } catch (err) {
                console.error("Error loading room data:", err);
            }
        };
        fetchAll();
    }, [room?.id, user?.email]);

    const memberRole = room?.members?.find(m => m.userId === user?.email)?.role;
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
            const response = await apiClient.post(`/api/utility/create`, payload,
                { withCredentials: true, headers: { "Content-Type": "application/json" } }
            );
            if (response.status === 200) {
                setShowUtilityModal(false);
                setUtilityData({ utilityName: "", description: "", utilityPrice: 0, utilDistributionEnum: "EQUALSPLIT", customSplit: {} });
                if (memberId) {
                    const r = await apiClient.get(`/api/utility/${memberId}/room/${room.id}`, { withCredentials: true });
                    setUserUtilities(r.data);
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
            await apiClient.delete(`/api/utility/${selectedUtilityId}`, { withCredentials: true });
            if (memberId) {
                const r = await apiClient.get(`/api/utility/${memberId}/room/${room.id}`, { withCredentials: true });
                setUserUtilities(r.data);
                refreshUserUtilities();
            }
            setShowRemoveUtilityModal(false);
            setSelectedUtilityId("");
        } catch (err) {
            console.error("Error removing utility:", err);
        }
    };

    const CHORE_OPTIONS = ["Broom", "Sweep", "Trash", "Mop", "Vacuum", "Kitchen", "Other"];

    const handleSubmitChores = async () => {
        try {
            await Promise.all(
                pendingChores.map(chore =>
                    apiClient.post(`/api/chores/${room.id}`, chore, { withCredentials: true })
                )
            );
            const r = await apiClient.get(`/api/chores/${room.id}`, { withCredentials: true });
            setChores(r.data);
            setPendingChores([]);
            setShowChoreModal(false);
            refreshUserChores();
        } catch (err) {
            console.error("Error submitting chores:", err);
        }
    };

    const handleRemoveChoresByType = async () => {
        if (!selectedChoreType) return;
        try {
            await apiClient.delete(`/api/chores/${room.id}/type/${selectedChoreType}`, { withCredentials: true });
            const r = await apiClient.get(`/api/chores/${room.id}`, { withCredentials: true });
            setChores(r.data);
            setSelectedChoreType('');
            refreshUserChores();
        } catch (err) {
            console.error("Error removing chores:", err);
        }
    };

    const getChoresByDate = () => {
        const now = new Date();
        const oneMonthAhead = new Date();
        oneMonthAhead.setMonth(now.getMonth() + 1);
        const map = {};
        chores
            .filter(c => { const d = new Date(c.dueAt); return d >= now && d <= oneMonthAhead; })
            .sort((a, b) => new Date(a.dueAt) - new Date(b.dueAt))
            .forEach(c => {
                const d = new Date(c.dueAt);
                const key = d.toLocaleString('default', { month: 'short' }) + ' ' + d.getDate();
                if (!map[key]) map[key] = [];
                map[key].push(c);
            });
        return map;
    };

    const choresByDate = getChoresByDate();

    const getMemberInitial = (m) => m.name?.charAt(0)?.toUpperCase() || '?';
    const getRoleLabel = (role) => {
        if (role === ROLES.HEAD_ROOMMATE) return 'Owner';
        if (role === ROLES.ASSISTANT) return 'Manager';
        return 'Resident';
    };

    return (
        <div className="rd-container">

            {/* ── Topbar ── */}
            <div className="rd-topbar">
                <button className="rd-back-btn" onClick={onClose}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                        <polyline points="15 18 9 12 15 6" />
                    </svg>
                    Rooms
                </button>
            </div>

            {/* ── Room Header ── */}
            <div className="rd-header">
                <div className="rd-header-left">
                    <div className="rd-monogram">{room.name?.[0]?.toUpperCase()}</div>
                    <div>
                        <h1 className="rd-room-name">{room.name}</h1>
                        {room.address && <p className="rd-room-address">{room.address}</p>}
                    </div>
                </div>
                <div className="rd-header-right">
                    <div className="rd-header-stat">
                        <span className="rd-header-stat-value">
                            {room.members?.length || 0}
                            <span className="rd-header-stat-denom">/6</span>
                        </span>
                        <span className="rd-header-stat-label">Members</span>
                    </div>
                    <div className="rd-header-stat">
                        <span className="rd-header-stat-value">{chores.length}</span>
                        <span className="rd-header-stat-label">Chores</span>
                    </div>
                    <div className="rd-header-stat">
                        <span className="rd-header-stat-value">{utilities.length}</span>
                        <span className="rd-header-stat-label">Utilities</span>
                    </div>
                    <code className="rd-room-code">{room.roomCode}</code>
                </div>
            </div>

            <div className="rd-divider" />

            {/* ── Two columns: Members + Utilities ── */}
            <div className="rd-grid-2">

                <section className="rd-section">
                    <h2 className="rd-section-title">Members</h2>
                    <div className="rd-member-list">
                        {room.members?.map((member) => {
                            const isSelf = member.userId === user.email;
                            return (
                                <div key={member.id} className="rd-member-row">
                                    <div className="rd-member-avatar">{getMemberInitial(member)}</div>
                                    <div className="rd-member-info">
                                        <span className="rd-member-name">
                                            {member.name}
                                            {isSelf && <span className="rd-you-tag">You</span>}
                                        </span>
                                        <span className={`rd-role-tag ${member.role}`}>{getRoleLabel(member.role)}</span>
                                    </div>
                                    {isSelf && member.role !== ROLES.HEAD_ROOMMATE && (
                                        <button className="rd-leave-btn" onClick={() => onLeaveRoom(member.id)}>
                                            Leave
                                        </button>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </section>

                <section className="rd-section">
                    <h2 className="rd-section-title">Your Utilities</h2>
                    {userUtilities.length === 0 ? (
                        <p className="rd-empty">No utilities assigned to you.</p>
                    ) : (
                        <div className="rd-utility-list">
                            {userUtilities.map(u => (
                                <div key={u.id} className="rd-utility-row">
                                    <div className="rd-utility-icon">
                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                            <line x1="12" y1="1" x2="12" y2="23"/>
                                            <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                                        </svg>
                                    </div>
                                    <span className="rd-utility-name">{u.utilityName}</span>
                                    <span className="rd-utility-price">${u.utilityPrice.toFixed(2)}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

            </div>

            {/* ── Upcoming Chores ── */}
            <section className="rd-section rd-section-full">
                <h2 className="rd-section-title">Upcoming Chores</h2>
                {Object.keys(choresByDate).length === 0 ? (
                    <p className="rd-empty">No chores in the next 30 days.</p>
                ) : (
                    <div className="rd-chore-timeline">
                        {Object.entries(choresByDate).map(([date, choresForDate]) => (
                            <div key={date} className="rd-chore-group">
                                <div className="rd-chore-date">{date}</div>
                                <div className="rd-chore-chips">
                                    {choresForDate.map(chore => (
                                        <span key={chore.id} className="rd-chore-chip">
                                            {chore.choreName}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* ── Management ── */}
            {(isAssistantRoommate || isHeadRoommate) && (
                <section className="rd-section rd-section-full">
                    <h2 className="rd-section-title">Room Management</h2>
                    <div className="rd-action-groups">

                        <div className="rd-action-group">
                            <p className="rd-action-group-label">Add</p>
                            <div className="rd-action-list">
                                <button className="rd-action-row" onClick={() => setShowInviteModal(true)}>
                                    <div className="rd-action-icon rd-icon-invite" />
                                    <div className="rd-action-text">
                                        <span className="rd-action-title">Invite Roommate</span>
                                        <span className="rd-action-desc">Send an invite by email</span>
                                    </div>
                                    <div className="rd-chevron" />
                                </button>
                                <button className="rd-action-row" onClick={() => setShowChoreModal(true)}>
                                    <div className="rd-action-icon rd-icon-chore" />
                                    <div className="rd-action-text">
                                        <span className="rd-action-title">Create Chore</span>
                                        <span className="rd-action-desc">Schedule recurring tasks</span>
                                    </div>
                                    <div className="rd-chevron" />
                                </button>
                                <button className="rd-action-row" onClick={() => setShowUtilityModal(true)}>
                                    <div className="rd-action-icon rd-icon-utility" />
                                    <div className="rd-action-text">
                                        <span className="rd-action-title">Add Utility</span>
                                        <span className="rd-action-desc">Track bills and split costs</span>
                                    </div>
                                    <div className="rd-chevron" />
                                </button>
                            </div>
                        </div>

                        <div className="rd-action-group">
                            <p className="rd-action-group-label">Remove</p>
                            <div className="rd-action-list">
                                <button className="rd-action-row" onClick={() => setShowRemoveChoreModal(true)}>
                                    <div className="rd-action-icon rd-icon-remove-chore" />
                                    <div className="rd-action-text">
                                        <span className="rd-action-title">Remove Chore</span>
                                        <span className="rd-action-desc">Delete a chore type</span>
                                    </div>
                                    <div className="rd-chevron" />
                                </button>
                                <button className="rd-action-row" onClick={() => setShowRemoveUtilityModal(true)}>
                                    <div className="rd-action-icon rd-icon-remove-utility" />
                                    <div className="rd-action-text">
                                        <span className="rd-action-title">Remove Utility</span>
                                        <span className="rd-action-desc">Delete a tracked utility</span>
                                    </div>
                                    <div className="rd-chevron" />
                                </button>
                            </div>
                        </div>

                        {isHeadRoommate && (
                            <div className="rd-action-group">
                                <p className="rd-action-group-label rd-danger-label">Danger Zone</p>
                                <div className="rd-action-list rd-action-list-danger">
                                    <button className="rd-action-row rd-action-danger" onClick={() => setShowDeleteConfirmModal(true)}>
                                        <div className="rd-action-icon rd-icon-delete" />
                                        <div className="rd-action-text">
                                            <span className="rd-action-title rd-danger-title">Delete Room</span>
                                            <span className="rd-action-desc">Permanently remove this room</span>
                                        </div>
                                        <div className="rd-chevron" />
                                    </button>
                                </div>
                            </div>
                        )}

                    </div>
                </section>
            )}

            {/* ── Modals ── */}
            <RemoveUtilityModal show={showRemoveUtilityModal} onClose={() => setShowRemoveUtilityModal(false)} utilities={utilities} selectedUtilityId={selectedUtilityId} setSelectedUtilityId={setSelectedUtilityId} handleRemoveUtility={handleRemoveUtility} />
            <UtilityModal show={showUtilityModal} onClose={() => setShowUtilityModal(false)} utilityData={utilityData} setUtilityData={setUtilityData} handleSubmitUtility={handleSubmitUtility} />
            <RemoveChoreModal show={showRemoveChoreModal} onClose={() => setShowRemoveChoreModal(false)} selectedChoreType={selectedChoreType} setSelectedChoreType={setSelectedChoreType} handleRemoveChoresByType={handleRemoveChoresByType} CHORE_OPTIONS={CHORE_OPTIONS} />
            <ChoreModal show={showChoreModal} onClose={() => setShowChoreModal(false)} isCustomChore={isCustomChore} setIsCustomChore={setIsCustomChore} choreData={choreData} setChoreData={setChoreData} CHORE_OPTIONS={CHORE_OPTIONS} addChoreToList={addChoreToList} pendingChores={pendingChores} removeChoreFromList={removeChoreFromList} handleSubmitChores={handleSubmitChores} isValidDeadline={isValidDeadline} />
            <InviteModal show={showInviteModal} onClose={() => setShowInviteModal(false)} inviteEmail={inviteEmail} setInviteEmail={setInviteEmail} inviteStatus={inviteStatus} handleInviteUser={handleInviteUser} />
            <DeleteConfirmModal show={showDeleteConfirmModal} onClose={() => setShowDeleteConfirmModal(false)} onConfirm={onDeleteRoom} roomName={room.name} />
        </div>
    );
};

export default RoomDetailsPage;
