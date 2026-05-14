-- Drop existing room_member table if it exists
DROP TABLE IF EXISTS room_member CASCADE;

-- Recreate room_member table with correct structure
CREATE TABLE room_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROOMMATE',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(room_id, user_id)
);

-- Create indexes
CREATE INDEX idx_room_member_room_id ON room_member(room_id);
CREATE INDEX idx_room_member_user_id ON room_member(user_id);
CREATE INDEX idx_room_member_room_user ON room_member(room_id, user_id); 