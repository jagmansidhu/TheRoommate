import React from 'react';

const RoomOnboarding = ({ onCreateRoom, onJoinRoom }) => {
    return (
        <div className="room-onboarding">
            <div className="onboarding-welcome">
                <h2>Welcome to DaRoommate! 🏠</h2>
                <p>Get started by creating a new room or joining an existing one</p>
            </div>
            
            <div className="onboarding-options">
                <div className="onboarding-card" onClick={onCreateRoom}>
                    <div className="onboarding-icon">🏡</div>
                    <h3>Create a Room</h3>
                    <p>Start a new shared space and invite your roommates to join</p>
                    <button className="btn btn-primary">Create Room</button>
                </div>
                
                <div className="onboarding-card" onClick={onJoinRoom}>
                    <div className="onboarding-icon">🔑</div>
                    <h3>Join a Room</h3>
                    <p>Have a room code from a roommate? Join their space</p>
                    <button className="btn btn-secondary">Join Room</button>
                </div>
            </div>
        </div>
    );
};

export default RoomOnboarding;
