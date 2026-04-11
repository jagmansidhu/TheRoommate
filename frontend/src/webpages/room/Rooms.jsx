import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styling/Rooms.css';

import CreateRoom from '../room/CreateRoom';
import JoinRoom from '../room/JoinRoom';
import RoleManagement from '../room/RoleManage';
import { ROLES } from '../../constants/roles';
import useCurrentUser from './useCurrentUser';
import { useAppData } from '../../App';

// ── helper: deterministic gradient per room index ──────────────────────────
const CARD_GRADIENTS = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
];

const getRoleLabel = (role) => {
    if (role === ROLES.HEAD_ROOMMATE) return 'Owner';
    if (role === ROLES.ASSISTANT) return 'Manager';
    return 'Resident';
};

const getMemberInitials = (member) => {
    if (member.name) {
        return member.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    }
    return (member.userId || '?')[0].toUpperCase();
};

const Rooms = () => {
    const navigate = useNavigate();
    const { currentUser, loadingUser } = useCurrentUser();
    const { rooms, roomsLoading, appendRoom, refreshRooms } = useAppData();

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showJoinModal, setShowJoinModal] = useState(false);
    const [error, setError] = useState(null);

    const [selectedRoom, setSelectedRoom] = useState(null);
    const [showRoleManagement, setShowRoleManagement] = useState(false);

    // Auto-redirect if only one room exists
    React.useEffect(() => {
        if (!roomsLoading && rooms?.length === 1 && !window.location.search.includes('manage=true')) {
            navigate(`/rooms/${rooms[0].id}`, { replace: true });
        }
    }, [rooms, roomsLoading, navigate]);

    const handleCreateRoom = (newRoomData) => appendRoom(newRoomData);
    const handleRoomJoined = () => refreshRooms();
    const openRoomDetails = (room) => navigate(`/rooms/${room.id}`);
    const openRoleManagement = (room) => {
        setSelectedRoom(room);
        setShowRoleManagement(true);
    };

    if (roomsLoading || loadingUser) {
        return (
            <div className="pm-container">
                <div className="pm-loading">
                    <div className="pm-loading-spinner"></div>
                    <span>Loading properties…</span>
                </div>
            </div>
        );
    }

    const totalMembers = rooms.reduce((sum, r) => sum + (r.members?.length || 0), 0);
    const occupancyRate = rooms.length > 0
        ? Math.round((totalMembers / (rooms.length * 6)) * 100)
        : 0;

    return (
        <div className="pm-container">

            {/* ── Page Header ─────────────────────────────────────── */}
            <div className="pm-header">
                <div className="pm-header-text">
                    <h1 className="pm-title">Properties</h1>
                    <p className="pm-subtitle">Manage your shared living spaces</p>
                </div>
                <div className="pm-header-actions">
                    <button className="pm-btn pm-btn-secondary" onClick={() => setShowJoinModal(true)}>
                        <span className="pm-btn-icon">+</span>
                        Join Room
                    </button>
                    <button className="pm-btn pm-btn-primary" onClick={() => setShowCreateModal(true)}>
                        <span className="pm-btn-icon">+</span>
                        New Property
                    </button>
                </div>
            </div>

            {error && (
                <div className="pm-alert">
                    {error}
                    <button className="pm-alert-close" onClick={() => setError(null)}>×</button>
                </div>
            )}

            {/* ── Summary Strip ────────────────────────────────────── */}
            {rooms.length > 0 && (
                <div className="pm-summary-strip">
                    <div className="pm-summary-stat">
                        <span className="pm-summary-value">{rooms.length}</span>
                        <span className="pm-summary-label">Properties</span>
                    </div>
                    <div className="pm-summary-divider" />
                    <div className="pm-summary-stat">
                        <span className="pm-summary-value">{totalMembers}</span>
                        <span className="pm-summary-label">Total Residents</span>
                    </div>
                    <div className="pm-summary-divider" />
                    <div className="pm-summary-stat">
                        <span className="pm-summary-value">{occupancyRate}%</span>
                        <span className="pm-summary-label">Occupancy</span>
                    </div>
                    <div className="pm-summary-divider" />
                    <div className="pm-summary-stat">
                        <span className="pm-summary-value">{3 - rooms.length}</span>
                        <span className="pm-summary-label">Slots Available</span>
                    </div>
                </div>
            )}

            {/* ── Property Grid ────────────────────────────────────── */}
            {rooms.length === 0 ? (
                <div className="pm-empty">
                    <div className="pm-empty-icon">🏠</div>
                    <h2 className="pm-empty-title">No properties yet</h2>
                    <p className="pm-empty-body">Create your first property or join an existing room using a room code.</p>
                    <div className="pm-empty-actions">
                        <button className="pm-btn pm-btn-primary" onClick={() => setShowCreateModal(true)}>
                            Create Property
                        </button>
                        <button className="pm-btn pm-btn-secondary" onClick={() => setShowJoinModal(true)}>
                            Join with Code
                        </button>
                    </div>
                </div>
            ) : (
                <div className="pm-grid">
                    {rooms.map((room, idx) => {
                        const currentMember = room.members?.find(m => m.userId === currentUser?.email);
                        const role = currentMember?.role;
                        const isHead = role === ROLES.HEAD_ROOMMATE;
                        const isAssistant = role === ROLES.ASSISTANT;
                        const gradient = CARD_GRADIENTS[idx % CARD_GRADIENTS.length];
                        const memberCount = room.members?.length || 0;
                        const occupancy = Math.round((memberCount / 6) * 100);

                        return (
                            <div key={room.id} className="pm-card" onClick={() => openRoomDetails(room)}>
                                {/* Card banner */}
                                <div className="pm-card-banner" style={{ background: gradient }}>
                                    <div className="pm-card-banner-overlay" />
                                    <div className="pm-card-banner-content">
                                        <span className="pm-card-property-type">Shared Housing</span>
                                        {isHead && <span className="pm-card-owner-badge">Owner</span>}
                                        {!isHead && isAssistant && <span className="pm-card-manager-badge">Manager</span>}
                                    </div>
                                    {/* Big initial */}
                                    <div className="pm-card-initial">{room.name?.[0]?.toUpperCase()}</div>
                                </div>

                                {/* Card body */}
                                <div className="pm-card-body">
                                    <div className="pm-card-title-row">
                                        <h2 className="pm-card-name">{room.name}</h2>
                                        <span className={`pm-card-role-pill ${isHead ? 'owner' : isAssistant ? 'manager' : 'resident'}`}>
                                            {getRoleLabel(role)}
                                        </span>
                                    </div>

                                    {room.address && (
                                        <p className="pm-card-address">
                                            <span className="pm-card-address-icon">📍</span>
                                            {room.address}
                                        </p>
                                    )}

                                    {room.description && (
                                        <p className="pm-card-desc">{room.description}</p>
                                    )}

                                    {/* Occupancy bar */}
                                    <div className="pm-card-occupancy">
                                        <div className="pm-card-occupancy-row">
                                            <span className="pm-card-occupancy-label">Occupancy</span>
                                            <span className="pm-card-occupancy-numbers">{memberCount} / 6</span>
                                        </div>
                                        <div className="pm-card-occupancy-bar">
                                            <div
                                                className="pm-card-occupancy-fill"
                                                style={{ width: `${occupancy}%`, background: gradient }}
                                            />
                                        </div>
                                    </div>

                                    {/* Member avatars */}
                                    <div className="pm-card-members">
                                        <div className="pm-avatar-stack">
                                            {room.members?.slice(0, 4).map((m, i) => (
                                                <div
                                                    key={m.id || i}
                                                    className="pm-avatar"
                                                    style={{ zIndex: 4 - i }}
                                                    title={m.name || m.userId}
                                                >
                                                    {getMemberInitials(m)}
                                                </div>
                                            ))}
                                            {memberCount > 4 && (
                                                <div className="pm-avatar pm-avatar-more">+{memberCount - 4}</div>
                                            )}
                                        </div>
                                        <span className="pm-card-member-label">
                                            {memberCount} {memberCount === 1 ? 'resident' : 'residents'}
                                        </span>
                                    </div>

                                    {/* Actions */}
                                    <div className="pm-card-actions" onClick={e => e.stopPropagation()}>
                                        <button
                                            className="pm-card-btn pm-card-btn-primary"
                                            onClick={() => openRoomDetails(room)}
                                        >
                                            View Details
                                        </button>
                                        {(isHead || isAssistant) && (
                                            <button
                                                className="pm-card-btn pm-card-btn-ghost"
                                                onClick={() => openRoleManagement(room)}
                                            >
                                                Manage Roles
                                            </button>
                                        )}
                                    </div>
                                </div>

                                {/* Room code chip */}
                                <div className="pm-card-code-chip">
                                    <span className="pm-card-code-label">Code</span>
                                    <code className="pm-card-code-value">{room.roomCode}</code>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            <CreateRoom
                show={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                onCreateRoom={handleCreateRoom}
            />
            <JoinRoom
                show={showJoinModal}
                onClose={() => setShowJoinModal(false)}
                onRoomJoined={handleRoomJoined}
            />
            <RoleManagement
                show={showRoleManagement}
                room={selectedRoom}
                onClose={() => setShowRoleManagement(false)}
                onUpdate={refreshRooms}
            />
        </div>
    );
};

export default Rooms;
