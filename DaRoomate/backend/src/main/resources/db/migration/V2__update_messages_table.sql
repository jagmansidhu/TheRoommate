-- First add the columns as nullable
ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_user_id bigint;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_read boolean DEFAULT false;

-- Update any existing null receiver_user_id values (if needed)
-- This assumes you have a default user or want to set it to null
UPDATE messages SET receiver_user_id = NULL WHERE receiver_user_id IS NULL;

-- Now add the NOT NULL constraint
ALTER TABLE messages ALTER COLUMN receiver_user_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE messages 
    ADD CONSTRAINT fk_messages_receiver 
    FOREIGN KEY (receiver_user_id) 
    REFERENCES users(id); 