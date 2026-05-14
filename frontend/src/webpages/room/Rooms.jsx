import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styling/Rooms.css';

import CreateRoom from '../room/CreateRoom';
import JoinRoom from '../room/JoinRoom';
import RoleManagement from '../room/RoleManage';
import { ROLES } from '../../constants/roles';
import useCurrentUser from './useCurrentUser';
import { useAppData } from '../../App';

const getRoleLabel = (role) => {
    if (role === ROLES.HEAD_ROOMMATE) return 'Owner';
    if (role === ROLES.ASSISTANT) return 'Manager';
    return 'Resident';
};

const getMemberInitials = (member) => {
    if (member.name) {
        const parts = member.name.trim().split(' ');
        return parts.length > 1
            ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
            : parts[0][0].toUpperCase();
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
                    <div className="pm-loading-spinner" />
                    <span>Loading…</span>
                </div>
            </div>
        );
    }

    const totalMembers = rooms.reduce((sum, r) => sum + (r.members?.length || 0), 0);

    return (
        <div className="pm-container">

            {/* ── Page Header ── */}
            <div className="pm-header">
                <div>
                    <p className="pm-eyebrow">Property Portfolio</p>
                    <h1 className="pm-title">My Rooms</h1>
                </div>
                <div className="pm-header-actions">
                    <button className="pm-btn pm-btn-ghost" onClick={() => setShowJoinModal(true)}>
                        Join with Code
                    </button>
                    <button className="pm-btn pm-btn-primary" onClick={() => setShowCreateModal(true)}>
                        + New Room
                    </button>
                </div>
            </div>

            {error && (
                <div className="pm-alert">
                    {error}
                    <button className="pm-alert-close" onClick={() => setError(null)}>×</button>
                </div>
            )}

            {/* ── Stats Row ── */}
            {rooms.length > 0 && (
                <div className="pm-stats-row">
                    <div className="pm-stat">
                        <span className="pm-stat-value">{rooms.length}</span>
                        <span className="pm-stat-label">Rooms</span>
                    </div>
                    <div className="pm-stat-sep" />
                    <div className="pm-stat">
                        <span className="pm-stat-value">{totalMembers}</span>
                        <span className="pm-stat-label">Residents</span>
                    </div>
                    <div className="pm-stat-sep" />
                    <div className="pm-stat">
                        <span className="pm-stat-value">{3 - rooms.length}</span>
                        <span className="pm-stat-label">Available Slots</span>
                    </div>
                </div>
            )}

            {/* ── Room Cards ── */}
            {rooms.length === 0 ? (
                <div className="pm-empty">
                    <div className="pm-empty-icon">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                            <path d="M3 10.5L12 3l9 7.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V10.5z"/>
                            <path d="M9 21V12h6v9"/>
                        </svg>
                    </div>
                    <h2 className="pm-empty-title">No rooms yet</h2>
                    <p className="pm-empty-body">Create a room or join an existing one with a room code.</p>
                    <div className="pm-empty-actions">
                        <button className="pm-btn pm-btn-primary" onClick={() => setShowCreateModal(true)}>Create Room</button>
                        <button className="pm-btn pm-btn-ghost" onClick={() => setShowJoinModal(true)}>Join with Code</button>
                    </div>
                </div>
            ) : (
                <div className="pm-grid">
                    {rooms.map((room) => {
                        const currentMember = room.members?.find(m => m.userId === currentUser?.email);
                        const role = currentMember?.role;
                        const isHead = role === ROLES.HEAD_ROOMMATE;
                        const isAssistant = role === ROLES.ASSISTANT;
                        const memberCount = room.members?.length || 0;
                        const occupancy = Math.min(Math.round((memberCount / 6) * 100), 100);

                        return (
                            <div
                                key={room.id}
                                className="pm-card"
                                onClick={() => openRoomDetails(room)}
                                role="button"
                                tabIndex={0}
                                onKeyDown={e => e.key === 'Enter' && openRoomDetails(room)}
                            >
                                {/* Card Header */}
                                <div className="pm-card-header">
                                    <div className="pm-card-monogram">
                                        {room.name?.[0]?.toUpperCase()}
                                    </div>
                                    <div className="pm-card-meta">
                                        <h2 className="pm-card-name">{room.name}</h2>
                                        {room.address && (
                                            <p className="pm-card-address">{room.address}</p>
                                        )}
                                    </div>
                                    <span className={`pm-role-tag ${isHead ? 'owner' : isAssistant ? 'manager' : 'resident'}`}>
                                        {getRoleLabel(role)}
                                    </span>
                                </div>

                                {/* Divider */}
                                <div className="pm-card-divider" />

                                {/* Card Body */}
                                <div className="pm-card-body">
                                    {room.description && (
                                        <p className="pm-card-desc">{room.description}</p>
                                    )}

                                    {/* Occupancy */}
                                    <div className="pm-card-occupancy">
                                        <div className="pm-card-occupancy-header">
                                            <span className="pm-card-occ-label">Occupancy</span>
                                            <span className="pm-card-occ-count">{memberCount} of 6</span>
                                        </div>
                                        <div className="pm-card-occ-track">
                                            <div className="pm-card-occ-fill" style={{ width: `${occupancy}%` }} />
                                        </div>
                                    </div>

                                    {/* Members */}
                                    <div className="pm-card-members">
                                        <div className="pm-avatar-row">
                                            {room.members?.slice(0, 5).map((m, i) => (
                                                <div
                                                    key={m.id || i}
                                                    className="pm-avatar"
                                                    title={m.name || m.userId}
                                                >
                                                    {getMemberInitials(m)}
                                                </div>
                                            ))}
                                            {memberCount > 5 && (
                                                <div className="pm-avatar pm-avatar-overflow">+{memberCount - 5}</div>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                {/* Card Footer */}
                                <div className="pm-card-footer" onClick={e => e.stopPropagation()}>
                                    <code className="pm-room-code">{room.roomCode}</code>
                                    <div className="pm-card-footer-actions">
                                        {(isHead || isAssistant) && (
                                            <button
                                                className="pm-card-action-btn"
                                                onClick={() => openRoleManagement(room)}
                                            >
                                                Manage Roles
                                            </button>
                                        )}
                                        <button
                                            className="pm-card-action-btn pm-card-action-primary"
                                            onClick={() => openRoomDetails(room)}
                                        >
                                            Open →
                                        </button>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            <CreateRoom show={showCreateModal} onClose={() => setShowCreateModal(false)} onCreateRoom={handleCreateRoom} />
            <JoinRoom show={showJoinModal} onClose={() => setShowJoinModal(false)} onRoomJoined={handleRoomJoined} />
            <RoleManagement show={showRoleManagement} room={selectedRoom} onClose={() => setShowRoleManagement(false)} onUpdate={refreshRooms} />
        </div>
    );
};

export default Rooms;
