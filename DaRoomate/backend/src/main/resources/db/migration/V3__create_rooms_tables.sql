-- Create room table
CREATE TABLE room (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      name VARCHAR(255) NOT NULL,
                      address TEXT NOT NULL,
                      description TEXT,
                      room_code VARCHAR(6) NOT NULL UNIQUE,
                      head_roommate_id VARCHAR(255) NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP
);

-- Create room_member table (singular to match entity)
CREATE TABLE room_member (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             room_id UUID NOT NULL,
                             user_id BIGINT NOT NULL, -- Changed to BIGINT to match users table
                             role VARCHAR(20) NOT NULL DEFAULT 'ROOMMATE',
                             joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP,
                             FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- Add FK to users table
                             UNIQUE(room_id, user_id)
);

-- Create indexes (fixed table names)
CREATE INDEX idx_room_head_roommate_id ON room(head_roommate_id);
CREATE INDEX idx_room_member_room_id ON room_member(room_id);
CREATE INDEX idx_room_member_user_id ON room_member(user_id);
CREATE INDEX idx_room_member_room_user ON room_member(room_id, user_id);