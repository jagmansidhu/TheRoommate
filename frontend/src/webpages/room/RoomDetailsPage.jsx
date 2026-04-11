import apiClient from '../../apiClient';
import { useState, useEffect } from 'react';
import React from 'react';
import { ROLES } from "../../constants/roles";
import { useUser, useAppData } from '../../App';
import '../../styling/Rooms.css';
import RemoveUtilityModal from './modals/RemoveUtilityModal';
import UtilityModal from './modals/UtilityModal';
import RemoveChoreModal from './modals/RemoveChoreModal';
import ChoreModal from './modals/ChoreModal';
import InviteModal from './modals/InviteModal';
import DeleteConfirmModal from './modals/DeleteConfirmModal';

const RoomDetailsPage = ({
    onClose, room, onLeaveRoom, onDeleteRoom,
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

    const memberCount = room.members?.length || 0;
    const occupancy = Math.min(Math.round((memberCount / 6) * 100), 100);
    const currentMember = room.members?.find(m => m.userId === user?.email);
    const myRole = getRoleLabel(currentMember?.role);

    return (
        <div className="rd-container">

            {/* ── Back nav ── */}
            <nav className="rd-nav">
                <button className="rd-back-btn" onClick={onClose}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="15 18 9 12 15 6" />
                    </svg>
                    Rooms
                </button>
            </nav>

            {/* ── Header Band ── */}
            <div className="rd-header-band">
                <div className="rd-header-band-left">
                    <div className="rd-monogram" aria-hidden="true">
                        {room.name?.[0]?.toUpperCase()}
                    </div>
                    <div className="rd-header-text">
                        <div className="rd-header-eyebrow">Shared Housing · {myRole}</div>
                        <h1 className="rd-room-name">{room.name}</h1>
                        {room.address && (
                            <p className="rd-room-address">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                    <circle cx="12" cy="10" r="3"/>
                                </svg>
                                {room.address}
                            </p>
                        )}
                    </div>
                </div>
                <div className="rd-header-band-right">
                    <div className="rd-header-occ">
                        <div className="rd-header-occ-text">
                            <span className="rd-header-occ-count">{memberCount} <span className="rd-header-occ-max">of 6</span></span>
                            <span className="rd-header-occ-label">Occupied</span>
                        </div>
                        <div className="rd-header-occ-bar">
                            <div className="rd-header-occ-fill" style={{ width: `${occupancy}%` }} />
                        </div>
                    </div>
                    <code className="rd-room-code-badge">{room.roomCode}</code>
                </div>
            </div>

            {/* ── Stat Strip ── */}
            <div className="rd-stat-strip">
                <div className="rd-stat-cell">
                    <span className="rd-stat-num">{memberCount}<span className="rd-stat-denom">/6</span></span>
                    <span className="rd-stat-lbl">Members</span>
                </div>
                <div className="rd-stat-sep" />
                <div className="rd-stat-cell">
                    <span className="rd-stat-num">{chores.length}</span>
                    <span className="rd-stat-lbl">Total Chores</span>
                </div>
                <div className="rd-stat-sep" />
                <div className="rd-stat-cell">
                    <span className="rd-stat-num">{Object.keys(choresByDate).length}</span>
                    <span className="rd-stat-lbl">Due This Month</span>
                </div>
                <div className="rd-stat-sep" />
                <div className="rd-stat-cell">
                    <span className="rd-stat-num">{utilities.length}</span>
                    <span className="rd-stat-lbl">Utilities</span>
                </div>
                <div className="rd-stat-sep" />
                <div className="rd-stat-cell">
                    <span className="rd-stat-num">
                        ${userUtilities.reduce((s, u) => s + (u.utilityPrice || 0), 0).toFixed(0)}
                    </span>
                    <span className="rd-stat-lbl">Your Monthly</span>
                </div>
            </div>

            {/* ── Content Grid ── */}
            <div className="rd-content-grid">

                {/* Left column */}
                <div className="rd-col-left">

                    {/* Members */}
                    <section className="rd-card">
                        <h2 className="rd-card-title">Members</h2>
                        <div className="rd-member-list">
                            {room.members?.map((member) => {
                                const isSelf = member.userId === user.email;
                                return (
                                    <div key={member.id} className="rd-member-row">
                                        <div className="rd-member-avatar">{getMemberInitial(member)}</div>
                                        <div className="rd-member-info">
                                            <div className="rd-member-name">
                                                {member.name}
                                                {isSelf && <span className="rd-you-tag">You</span>}
                                            </div>
                                            <span className={`rd-role-tag ${member.role}`}>
                                                {getRoleLabel(member.role)}
                                            </span>
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

                    {/* Your Utilities */}
                    <section className="rd-card">
                        <h2 className="rd-card-title">Your Utilities</h2>
                        {userUtilities.length === 0 ? (
                            <p className="rd-empty-text">No utilities assigned to you.</p>
                        ) : (
                            <div className="rd-utility-list">
                                {userUtilities.map(u => (
                                    <div key={u.id} className="rd-utility-row">
                                        <div className="rd-utility-dot" />
                                        <span className="rd-utility-name">{u.utilityName}</span>
                                        <span className="rd-utility-freq">
                                            {u.choreFrequencyUnitEnum?.toLowerCase() || '—'}
                                        </span>
                                        <span className="rd-utility-price">${Number(u.utilityPrice).toFixed(2)}</span>
                                    </div>
                                ))}
                                <div className="rd-utility-total">
                                    <span>Total</span>
                                    <span>${userUtilities.reduce((s, u) => s + (u.utilityPrice || 0), 0).toFixed(2)}</span>
                                </div>
                            </div>
                        )}
                    </section>
                </div>

                {/* Right column — Chores */}
                <div className="rd-col-right">
                    <section className="rd-card rd-card-full">
                        <h2 className="rd-card-title">Upcoming Chores</h2>
                        {Object.keys(choresByDate).length === 0 ? (
                            <div className="rd-empty-state">
                                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                                    <path d="M9 11l3 3L22 4"/>
                                    <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/>
                                </svg>
                                <p>No chores in the next 30 days</p>
                            </div>
                        ) : (
                            <div className="rd-timeline">
                                {Object.entries(choresByDate).map(([date, dayChores]) => (
                                    <div key={date} className="rd-timeline-row">
                                        <div className="rd-timeline-date">
                                            <span className="rd-timeline-day">{date.split(' ')[1]}</span>
                                            <span className="rd-timeline-month">{date.split(' ')[0]}</span>
                                        </div>
                                        <div className="rd-timeline-line">
                                            <div className="rd-timeline-dot" />
                                        </div>
                                        <div className="rd-timeline-content">
                                            {dayChores.map(chore => (
                                                <div key={chore.id} className="rd-timeline-item">
                                                    {chore.choreName}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </section>
                </div>
            </div>

            {/* ── Management ── */}
            {(isAssistantRoommate || isHeadRoommate) && (
                <section className="rd-card rd-manage-section">
                    <h2 className="rd-card-title">Room Management</h2>
                    <div className="rd-manage-grid">

                        <div className="rd-action-group">
                            <p className="rd-action-group-label">Add</p>
                            <div className="rd-action-list">
                                {[
                                    { icon: 'rd-icon-invite', label: 'Invite Roommate', desc: 'Send an invite by email', action: () => setShowInviteModal(true) },
                                    { icon: 'rd-icon-chore', label: 'Create Chore', desc: 'Schedule recurring tasks', action: () => setShowChoreModal(true) },
                                    { icon: 'rd-icon-utility', label: 'Add Utility', desc: 'Track bills and split costs', action: () => setShowUtilityModal(true) },
                                ].map(item => (
                                    <button key={item.label} className="rd-action-row" onClick={item.action}>
                                        <div className={`rd-action-icon ${item.icon}`} />
                                        <div className="rd-action-text">
                                            <span className="rd-action-title">{item.label}</span>
                                            <span className="rd-action-desc">{item.desc}</span>
                                        </div>
                                        <div className="rd-chevron" />
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="rd-action-group">
                            <p className="rd-action-group-label">Remove</p>
                            <div className="rd-action-list">
                                {[
                                    { icon: 'rd-icon-remove-chore', label: 'Remove Chore', desc: 'Delete a chore type', action: () => setShowRemoveChoreModal(true) },
                                    { icon: 'rd-icon-remove-utility', label: 'Remove Utility', desc: 'Delete a tracked utility', action: () => setShowRemoveUtilityModal(true) },
                                ].map(item => (
                                    <button key={item.label} className="rd-action-row" onClick={item.action}>
                                        <div className={`rd-action-icon ${item.icon}`} />
                                        <div className="rd-action-text">
                                            <span className="rd-action-title">{item.label}</span>
                                            <span className="rd-action-desc">{item.desc}</span>
                                        </div>
                                        <div className="rd-chevron" />
                                    </button>
                                ))}
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
                                            <span className="rd-action-desc">Permanently remove this room and all data</span>
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
