import apiClient from '../../apiClient';
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ROLES, ROLE_RANK } from '../../constants/roles';
import useCurrentUser from './useCurrentUser';
import '../../styling/Rooms.css';

const RoleManagement = ({ show, room, onClose, onUpdate }) => {
    const { currentUser, loadingUser, errorUser } = useCurrentUser();

    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (room && room.members) {
            setMembers(room.members);
        } else {
            setMembers([]);
        }
    }, [room]);

    const updateMemberRole = async (memberId, newRole) => {
        setLoading(true);
        setError(null);
        try {
            await apiClient.put(`/api/rooms/${room.id}/members/${memberId}/role`,
                { role: newRole },
                { withCredentials: true }
            );
            setMembers(members.map((member) => (member.id === memberId ? { ...member, role: newRole } : member)));
            onUpdate();
        } catch (err) {
            console.error('Error updating member role:', err);
            setError('Failed to update member role.');
        } finally {
            setLoading(false);
        }
    };

    const removeMember = async (memberId) => {
        if (!window.confirm('Are you sure you want to remove this member from the room?')) return;
        setLoading(true);
        setError(null);
        try {
            await apiClient.delete(`/api/rooms/${room.id}/members/${memberId}`, {
                withCredentials: true,
            });
            setMembers(members.filter((m) => m.id !== memberId));
            onUpdate();
        } catch (err) {
            console.error('Error removing member:', err);
            setError('Failed to remove member.');
        } finally {
            setLoading(false);
        }
    };

    const getCurrentUserRole = () => {
        if (!currentUser) return null;
        const currentMember = members.find((m) => m.userId === currentUser.email);
        return currentMember?.role || null;
    };

    const canEditMember = (targetRole, isSelf) => {
        const currentUserRole = getCurrentUserRole();
        if (!currentUserRole) return false;
        if (isSelf) return false;

        return ROLE_RANK[currentUserRole] > ROLE_RANK[targetRole];
    };

    if (loadingUser) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <span>Loading user info...</span>
            </div>
        );
    }
    if (errorUser) return <div className="alert alert-error">Error loading user info.</div>;
    if (!show || !room) return null;

    const currentUserRole = getCurrentUserRole();
    const isAuthorized = currentUserRole === ROLES.HEAD_ROOMMATE || currentUserRole === ROLES.ASSISTANT;
    if (!isAuthorized) return null;

    return (
        <div className="modal-overlay">
            <div className="modal modal-large">
                <div className="modal-header">
                    <h2>Manage Roles</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                
                {error && <div className="alert alert-error">{error}</div>}
                
                <div className="modal-body">
                    <div className="members-list">
                        {members.map((member) => {
                            const isSelf = member.email === currentUser.email;
                            return (
                                <div key={member.id} className="member-item">
                                    <div className="member-avatar">
                                        {member.name?.charAt(0)?.toUpperCase() || '?'}
                                    </div>
                                    <div className="member-info">
                                        <div className="member-name">{member.name}</div>
                                        <span className={`role-badge ${member.role}`}>{member.role}</span>
                                    </div>
                                    <div className="member-actions">
                                        {canEditMember(member.role, isSelf) ? (
                                            <>
                                                <select
                                                    className="form-input"
                                                    value={member.role}
                                                    onChange={(e) => updateMemberRole(member.id, e.target.value)}
                                                    disabled={loading}
                                                >
                                                    <option value={ROLES.GUEST}>Guest</option>
                                                    <option value={ROLES.ROOMMATE}>Roommate</option>
                                                    <option value={ROLES.ASSISTANT}>Assistant</option>
                                                </select>
                                                <button
                                                    className="btn btn-danger"
                                                    onClick={() => removeMember(member.id)}
                                                    disabled={loading}
                                                >
                                                    Remove
                                                </button>
                                            </>
                                        ) : (
                                            <span className="text-muted">—</span>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>

                <div className="modal-actions">
                    <button className="btn btn-secondary" onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
};

export default RoleManagement;
