package com.roomate.app.entities.room;

public enum RoomMemberEnum {
    LANDLORD, // Property owner - can add rent/expenses
    HEAD_ROOMMATE, // Leader - decides expense splits
    ROOMMATE, // Regular member
    ASSISTANT, // Helper with some privileges
    GUEST // Limited access
}