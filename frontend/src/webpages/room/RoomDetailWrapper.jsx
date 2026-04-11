import apiClient from '../../apiClient';
import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import RoomDetailsPage from './RoomDetailsPage';
import { useAppData } from '../../App';

const RoomDetailsPageWrapper = () => {
    const { roomId } = useParams();
    const navigate = useNavigate();
    const { rooms } = useAppData();

    const [room, setRoom] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchRoom = async () => {
            // Check cache first for instant loading
            if (rooms && rooms.length > 0) {
                const cachedRoom = rooms.find(r => r.id === roomId);
                if (cachedRoom) {
                    setRoom(cachedRoom);
                    setLoading(false);
                    return;
                }
            }

            // Fallback: network fetch if hard refreshed and cache is empty
            try {
                const response = await apiClient.get(`/api/rooms/${roomId}`, {
                    withCredentials: true,
                });
                setRoom(response.data);
            } catch (error) {
                console.error('Failed to load room:', error);
                navigate('/rooms');
            } finally {
                setLoading(false);
            }
        };
        fetchRoom();
    }, [roomId, navigate, rooms]);

    const handleClose = useCallback(() => {
        navigate('/rooms?manage=true');
    }, [navigate]);

    const handleLeaveRoom = useCallback((memberId) => {
        const leaveRoom = async () => {
            try {
                await apiClient.delete(`/api/rooms/${memberId}/leave/${roomId}`, {
                    withCredentials: true,
                });
                navigate('/rooms');
            } catch (error) {
                console.error('Failed to leave room:', error);
            }
        };
        leaveRoom();
    }, [navigate]);

    const handleDeleteRoom = useCallback(() => {
        const deleteRoom = async () => {
            try {
                await apiClient.delete(`/api/rooms/${roomId}/delete-room`, {
                    withCredentials: true,
                });
                navigate('/rooms');
            } catch (error) {
                console.error('Failed to Delete room:', error);
            }
        };
        deleteRoom();

    }, [navigate, roomId]);

    if (loading) return <div>Loading room details...</div>;

    return (
        <RoomDetailsPage
            show={true}
            room={room}
            onClose={handleClose}
            onLeaveRoom={handleLeaveRoom}
            onDeleteRoom={handleDeleteRoom}
        />
    );
};

export default RoomDetailsPageWrapper;
