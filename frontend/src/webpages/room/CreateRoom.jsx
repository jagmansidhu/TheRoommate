import apiClient from '../../apiClient';
import React, {useState} from 'react';
import axios from 'axios';

const CreateRoomModal = ({show, onClose, onCreateRoom}) => {
    const [newRoom, setNewRoom] = useState({
        name: '', address: '', description: '',
    });
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            const response = await apiClient.post(`/api/rooms`, newRoom, {
                withCredentials: true,
            });

            onCreateRoom(response.data);
            setNewRoom({name: '', address: '', description: ''});
            onClose();
        } catch (err) {
            console.error('Error creating room:', err);
            setError('Failed to create room. Please try again.');
        } finally {
            setLoading(false);
        }
    };
    if (!show) return null;

    return (<div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h2>Create New Room</h2>
                    <button className="modal-close" onClick={onClose}>
                        ×
                    </button>
                </div>
                {error && <div className="alert alert-error">{error}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="roomName">Room Name</label>
                        <input
                            type="text"
                            id="roomName"
                            value={newRoom.name}
                            onChange={(e) => setNewRoom({...newRoom, name: e.target.value})}
                            placeholder="Enter room name"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="roomAddress">Address</label>
                        <input
                            type="text"
                            id="roomAddress"
                            value={newRoom.address}
                            onChange={(e) => setNewRoom({...newRoom, address: e.target.value})}
                            placeholder="Enter address"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="roomDescription">Description</label>
                        <textarea
                            id="roomDescription"
                            value={newRoom.description}
                            onChange={(e) => setNewRoom({...newRoom, description: e.target.value})}
                            placeholder="Enter description"
                            rows="3"
                        />
                    </div>
                    <div className="modal-actions">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Room'}
                        </button>
                    </div>
                </form>
            </div>
        </div>);
};

export default CreateRoomModal;
