import apiClient from '../apiClient';
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useUser, useAppData } from '../App';
import '../styling/Calendar.css';

const Calendar = () => {
    const { user: userData } = useUser();
    const userEmail = userData?.email || userData?.username || null;
    const {
        rooms,
        userChores: chores,
        userUtilities: utilities,
        events, eventsLoading, loadEvents,
        appendEvent, removeEvent,
    } = useAppData();

    const [selectedDate, setSelectedDate] = useState(new Date());
    const [showEventModal, setShowEventModal] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [error, setError] = useState(null);

    const [newEvent, setNewEvent] = useState({
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        roomId: ''
    });

    const [dateError, setDateError] = useState('');

    // Lazy-load events once on first Calendar visit — no-op on subsequent visits.
    useEffect(() => {
        loadEvents();
    }, [loadEvents]);

    const createEvent = async (e) => {
        e.preventDefault();

        setError('');
        setDateError('');

        if (!newEvent.roomId) {
            setError('Please select a room');
            return;
        }

        if (newEvent.startTime && newEvent.endTime) {
            const startDate = new Date(newEvent.startTime);
            const endDate = new Date(newEvent.endTime);

            if (endDate <= startDate) {
                setDateError('End time must be after start time');
                return;
            }
        }

        try {
            const startDateTime = new Date(newEvent.startTime).toISOString();
            const endDateTime = new Date(newEvent.endTime).toISOString();

            const requestData = {
                title: newEvent.title,
                description: newEvent.description,
                startTime: startDateTime,
                endTime: endDateTime
            };

            const response = await apiClient.post(`/api/events/room/${newEvent.roomId}`,
                requestData,
                { withCredentials: true, credentials: 'include' }
            );

            // Append the server-returned event directly into the cache
            if (response.data) {
                appendEvent(response.data);
            }

            setShowEventModal(false);
            setNewEvent({ title: '', description: '', startTime: '', endTime: '', roomId: '' });
            setDateError('');
        } catch (error) {
            console.error('Error creating event:', error);
            if (error.response && error.response.data) {
                if (typeof error.response.data === 'object') {
                    if (error.response.status === 409) {
                        setError('This event was modified by another user. Please refresh and try again.');
                    } else {
                        const errorMessages = Object.values(error.response.data).join(', ');
                        setError(`Validation error: ${errorMessages}`);
                    }
                } else {
                    setError(error.response.data);
                }
            } else {
                setError('Failed to create event');
            }
        }
    };

    const deleteEvent = async (eventId) => {
        if (!window.confirm('Are you sure you want to delete this event?')) return;

        try {
            await apiClient.delete(`/api/events/${eventId}`, {
                withCredentials: true,
                credentials: 'include',
            });
            removeEvent(eventId);
            setSelectedEvent(null);
        } catch (error) {
            console.error('Error deleting event:', error);
            setError('Failed to delete event');
        }
    };

    const getEventsForDate = (date) => {
        return events.filter(event => {
            const eventDate = new Date(event.startTime);
            return eventDate.toDateString() === date.toDateString();
        });
    };

    const getRoomName = (eventRoom) => {
        return eventRoom?.name || 'Unknown Room';
    };

    const formatTime = (dateTime) => {
        return new Date(dateTime).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const formatDate = (date) => {
        return date.toLocaleDateString([], {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDayOfWeek = firstDay.getDay();

        const days = [];

        for (let i = 0; i < startingDayOfWeek; i++) {
            days.push(null);
        }

        for (let i = 1; i <= daysInMonth; i++) {
            days.push(new Date(year, month, i));
        }

        return days;
    };

    const navigateMonth = (direction) => {
        setSelectedDate(prev => {
            const newDate = new Date(prev);
            newDate.setMonth(prev.getMonth() + direction);
            return newDate;
        });
    };

    const openEventModal = (date = null) => {
        if (date) {
            setNewEvent(prev => ({
                ...prev,
                startTime: date.toISOString().slice(0, 16),
                endTime: new Date(date.getTime() + 60 * 60 * 1000).toISOString().slice(0, 16)
            }));
        }
        setShowEventModal(true);
        setDateError('');
    };

    const handleStartTimeChange = (e) => {
        const newStartTime = e.target.value;
        setNewEvent(prev => {
            const updated = { ...prev, startTime: newStartTime };

            if (updated.endTime && newStartTime) {
                const startDate = new Date(newStartTime);
                const endDate = new Date(updated.endTime);

                if (endDate <= startDate) {
                    const newEndTime = new Date(startDate.getTime() + 60 * 60 * 1000);
                    updated.endTime = newEndTime.toISOString().slice(0, 16);
                }
            }

            return updated;
        });
        setDateError('');
    };

    const handleEndTimeChange = (e) => {
        const newEndTime = e.target.value;
        setNewEvent(prev => {
            const updated = { ...prev, endTime: newEndTime };

            if (newEndTime && prev.startTime) {
                const startDate = new Date(prev.startTime);
                const endDate = new Date(newEndTime);

                if (endDate <= startDate) {
                    setDateError('End time must be after start time');
                } else {
                    setDateError('');
                }
            }

            return updated;
        });
    };

    const getChoresForDate = (date) => {
        return chores.filter(chore => {
            const choreDate = new Date(chore.dueAt);
            return choreDate.toDateString() === date.toDateString();
        });
    };

    const getUtilitiesForDate = (date) => {
        return utilities.filter(utility => {
            return date.getDate() === 1;
        });
    };

    if (eventsLoading) {
        return (
            <div className="calendar-container">
                <div className="loading">
                    <div className="spinner"></div>
                    <span>Loading calendar...</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="calendar-container">
                <div className="error">
                    <p>{error}</p>
                    {/*<button onClick={fetchData} className="btn btn-primary">Retry</button>*/}
                </div>
            </div>
        );
    }

    const days = getDaysInMonth(selectedDate);
    const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    return (
        <div className="calendar-container">
            <div className="calendar-header">
                <h2>Calendar</h2>
                <button
                    className="btn btn-primary"
                    onClick={() => openEventModal()}
                >
                    Create Event
                </button>
            </div>

            <div className="calendar-navigation">
                <button onClick={() => navigateMonth(-1)} className="nav-btn">
                    ← Previous
                </button>
                <h3>{formatDate(selectedDate)}</h3>
                <button onClick={() => navigateMonth(1)} className="nav-btn">
                    Next →
                </button>
            </div>

            <div className="calendar-grid">
                <div className="calendar-weekdays">
                    {weekDays.map(day => (
                        <div key={day} className="weekday">{day}</div>
                    ))}
                </div>

                <div className="calendar-days">
                    {days.map((day, index) => (
                        <div
                            key={index}
                            className={`calendar-day ${day ? '' : 'empty'} ${day && day.toDateString() === new Date().toDateString() ? 'today' : ''}`}
                            onClick={() => day && openEventModal(day)}
                        >
                            {day && (
                                <>
                                    <span className="day-number">{day.getDate()}</span>
                                    <div className="day-events">
                                        {getEventsForDate(day).slice(0, 2).map(event => (
                                            <div
                                                key={event.id}
                                                className="event-bar"
                                                title={`${event.title} - ${getRoomName(event.rooms)} (${formatTime(event.startTime)} - ${formatTime(event.endTime)})`}
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setSelectedEvent(event);
                                                }}
                                            >
                                                <span className="event-title">{event.title}</span>
                                            </div>
                                        ))}
                                        {getEventsForDate(day).length > 2 && (
                                            <div className="more-events-bar">
                                                <span className="more-events-text">+{getEventsForDate(day).length - 2} more</span>
                                            </div>
                                        )}
                                        {getChoresForDate(day).map(chore => (
                                            <div key={chore.id} className={`chore-bar ${chore.isCompleted ? 'completed' : ''}`}>
                                                <span className="chore-title">🧹 {chore.choreName}</span>
                                            </div>
                                        ))}

                                        {/* Utilities */}
                                        {getUtilitiesForDate(day).map(utility => (
                                            <div key={utility.id} className="utility-bar">
                                                <span className="utility-title">💰 {utility.utilityName}</span>
                                            </div>
                                        ))}

                                        {/* Extra events overflow */}
                                        {getEventsForDate(day).length > 2 && (
                                            <div className="more-events-bar">
                                                <span className="more-events-text">+{getEventsForDate(day).length - 2} more</span>
                                            </div>
                                        )}
                                    </div>
                                </>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            {/* Event Creation Modal */}
            {showEventModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>Create New Event</h3>
                            <button
                                className="close-btn"
                                onClick={() => setShowEventModal(false)}
                            >
                                ×
                            </button>
                        </div>
                        <form onSubmit={createEvent} className="event-form">
                            <div className="form-group">
                                <label>Title *</label>
                                <input
                                    type="text"
                                    value={newEvent.title}
                                    onChange={(e) => setNewEvent(prev => ({ ...prev, title: e.target.value }))}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Room *</label>
                                <select
                                    value={newEvent.roomId}
                                    onChange={(e) => setNewEvent(prev => ({ ...prev, roomId: e.target.value }))}
                                    required
                                >
                                    <option value="">Select a room</option>
                                    {rooms.map(room => (
                                        <option key={room.id} value={room.id}>
                                            {room.name} - {room.address}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Description</label>
                                <textarea
                                    value={newEvent.description}
                                    onChange={(e) => setNewEvent(prev => ({ ...prev, description: e.target.value }))}
                                    rows="3"
                                />
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label>Start Time *</label>
                                    <input
                                        type="datetime-local"
                                        value={newEvent.startTime}
                                        onChange={handleStartTimeChange}
                                        required
                                    />
                                </div>

                                <div className="form-group">
                                    <label>End Time *</label>
                                    <input
                                        type="datetime-local"
                                        value={newEvent.endTime}
                                        onChange={handleEndTimeChange}
                                        min={newEvent.startTime}
                                        required
                                    />
                                </div>
                            </div>

                            {dateError && (
                                <div className="form-error">
                                    <p className="error-message">{dateError}</p>
                                </div>
                            )}

                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowEventModal(false)} className="btn btn-secondary">
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary">
                                    Create Event
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Event Details Modal */}
            {selectedEvent && (
                <div className="modal-overlay">
                    <div className="modal">
                        <div className="modal-header">
                            <h3>{selectedEvent.title}</h3>
                            <button
                                className="close-btn"
                                onClick={() => setSelectedEvent(null)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="event-details">
                            <p><strong>Room:</strong> {getRoomName(selectedEvent.rooms)}</p>
                            <p><strong>Description:</strong> {selectedEvent.description || 'No description'}</p>
                            <p><strong>Start:</strong> {formatTime(selectedEvent.startTime)} on {new Date(selectedEvent.startTime).toLocaleDateString()}</p>
                            <p><strong>End:</strong> {formatTime(selectedEvent.endTime)} on {new Date(selectedEvent.endTime).toLocaleDateString()}</p>
                            <p><strong>Created by:</strong> {selectedEvent.user?.firstName && selectedEvent.user?.lastName ? `${selectedEvent.user.firstName} ${selectedEvent.user.lastName}` : selectedEvent.user?.email || 'Unknown'}</p>
                        </div>
                        <div className="modal-actions">
                            <button
                                onClick={() => deleteEvent(selectedEvent.id)}
                                className="btn btn-danger"
                            >
                                Delete Event
                            </button>
                            <button
                                onClick={() => setSelectedEvent(null)}
                                className="btn btn-secondary"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Calendar;