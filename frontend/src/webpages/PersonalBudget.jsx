import React, { useState, useRef, useCallback } from 'react';
import axios from 'axios';
import '../styling/Dashboard.css';
import '../styling/PersonalBudget.css';

const WEBHOOK_URL =
    'https://primary-production-0fe8e.up.railway.app/webhook/ed83c46d-f6fd-4c1e-acbc-a6166842f8c1';

const ACCEPTED_TYPES = [
    'image/jpeg',
    'image/png',
    'application/pdf',
];
const ACCEPTED_EXTENSIONS = '.jpg,.jpeg,.png,.pdf';
const MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB per file

const formatFileSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const getFileExtension = (name) => {
    const parts = name.split('.');
    return parts.length > 1 ? parts.pop().toUpperCase() : '?';
};

const PersonalBudget = () => {
    const [files, setFiles] = useState([]);
    const [dragOver, setDragOver] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [alert, setAlert] = useState(null); // { type: 'success'|'error', message }
    const inputRef = useRef(null);

    // ---- helpers ----

    const validateAndAdd = useCallback((incoming) => {
        const valid = [];
        const errors = [];

        Array.from(incoming).forEach((file) => {
            if (!ACCEPTED_TYPES.includes(file.type)) {
                errors.push(`${file.name}: unsupported file type`);
            } else if (file.size > MAX_FILE_SIZE) {
                errors.push(`${file.name}: exceeds 20 MB limit`);
            } else {
                valid.push(file);
            }
        });

        if (errors.length > 0) {
            setAlert({ type: 'error', message: errors.join('. ') });
        }

        if (valid.length > 0) {
            setFiles((prev) => [...prev, ...valid]);
            if (errors.length === 0) setAlert(null);
        }
    }, []);

    const removeFile = (index) => {
        setFiles((prev) => prev.filter((_, i) => i !== index));
    };

    const clearFiles = () => {
        setFiles([]);
        setAlert(null);
        if (inputRef.current) inputRef.current.value = '';
    };

    // ---- drag & drop ----

    const handleDragOver = (e) => {
        e.preventDefault();
        setDragOver(true);
    };

    const handleDragLeave = () => setDragOver(false);

    const handleDrop = (e) => {
        e.preventDefault();
        setDragOver(false);
        validateAndAdd(e.dataTransfer.files);
    };

    const handleFileChange = (e) => {
        validateAndAdd(e.target.files);
        e.target.value = ''; // allow re-selecting the same file
    };

    // ---- upload ----

    const handleUpload = async () => {
        if (files.length === 0) return;
        setUploading(true);
        setUploadProgress(0);
        setAlert(null);

        const formData = new FormData();
        files.forEach((file) => {
            formData.append('file', file);
        });

        try {
            await axios.post(WEBHOOK_URL, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                onUploadProgress: (e) => {
                    if (e.total) {
                        setUploadProgress(Math.round((e.loaded * 100) / e.total));
                    }
                },
            });

            setAlert({ type: 'success', message: 'Files uploaded successfully!' });
            setFiles([]);
            if (inputRef.current) inputRef.current.value = '';
        } catch (err) {
            console.error('Upload failed:', err);
            const msg =
                err.response?.data?.message ||
                err.message ||
                'Upload failed. Please try again.';
            setAlert({ type: 'error', message: msg });
        } finally {
            setUploading(false);
            setUploadProgress(0);
        }
    };

    // ---- render ----

    return (
        <div className="dashboard-container">
            {/* Header */}
            <div className="dashboard-header">
                <h1>Personal Budget</h1>
                <p>Upload receipts, invoices, or statements to track your spending</p>
            </div>

            {/* Upload Card */}
            <div className="dashboard-content" style={{ display: 'block', maxWidth: '720px' }}>
                <div className="dashboard-section">
                    <h3>Upload Documents</h3>

                    {/* Drop zone */}
                    <div
                        className={`budget-upload-zone${dragOver ? ' drag-over' : ''}`}
                        onDragOver={handleDragOver}
                        onDragLeave={handleDragLeave}
                        onDrop={handleDrop}
                        onClick={() => inputRef.current?.click()}
                        role="button"
                        tabIndex={0}
                        aria-label="Upload files"
                    >
                        <input
                            ref={inputRef}
                            type="file"
                            accept={ACCEPTED_EXTENSIONS}
                            multiple
                            onChange={handleFileChange}
                            tabIndex={-1}
                        />

                        <div className="upload-icon">
                            <svg viewBox="0 0 24 24">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                                <polyline points="17 8 12 3 7 8" />
                                <line x1="12" y1="3" x2="12" y2="15" />
                            </svg>
                        </div>

                        <div className="upload-text">
                            <p>Drag & drop files here, or <span>browse</span></p>
                            <small>Accepts JPEG, PNG, PDF — up to 20 MB each</small>
                        </div>
                    </div>

                    {/* File list */}
                    {files.length > 0 && (
                        <div className="budget-file-list">
                            <h3>Selected Files ({files.length})</h3>
                            {files.map((file, idx) => (
                                <div
                                    className="budget-file-item"
                                    key={`${file.name}-${file.size}-${idx}`}
                                    style={{ animationDelay: `${idx * 0.05}s` }}
                                >
                                    <div className="file-type-badge">{getFileExtension(file.name)}</div>
                                    <div className="file-info">
                                        <div className="file-name">{file.name}</div>
                                        <div className="file-size">{formatFileSize(file.size)}</div>
                                    </div>
                                    <button
                                        className="file-remove-btn"
                                        onClick={() => removeFile(idx)}
                                        title="Remove file"
                                        type="button"
                                    >
                                        <svg viewBox="0 0 24 24">
                                            <line x1="18" y1="6" x2="6" y2="18" />
                                            <line x1="6" y1="6" x2="18" y2="18" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Upload progress */}
                    {uploading && (
                        <div className="budget-upload-progress">
                            <div className="progress-header">
                                <span>Uploading…</span>
                                <span>{uploadProgress}%</span>
                            </div>
                            <div className="progress-track">
                                <div
                                    className="progress-fill"
                                    style={{ width: `${uploadProgress}%` }}
                                />
                            </div>
                        </div>
                    )}

                    {/* Alert */}
                    {alert && (
                        <div className={`budget-alert budget-alert-${alert.type}`}>
                            <svg viewBox="0 0 24 24">
                                {alert.type === 'success' ? (
                                    <>
                                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                                        <polyline points="22 4 12 14.01 9 11.01" />
                                    </>
                                ) : (
                                    <>
                                        <circle cx="12" cy="12" r="10" />
                                        <line x1="15" y1="9" x2="9" y2="15" />
                                        <line x1="9" y1="9" x2="15" y2="15" />
                                    </>
                                )}
                            </svg>
                            <span>{alert.message}</span>
                        </div>
                    )}

                    {/* Actions */}
                    {files.length > 0 && !uploading && (
                        <div className="budget-upload-actions">
                            <button
                                className="btn btn-primary"
                                onClick={handleUpload}
                                disabled={uploading}
                                type="button"
                                id="upload-files-btn"
                            >
                                Upload {files.length} {files.length === 1 ? 'File' : 'Files'}
                            </button>
                            <button
                                className="btn btn-secondary"
                                onClick={clearFiles}
                                disabled={uploading}
                                type="button"
                                id="clear-files-btn"
                            >
                                Clear All
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PersonalBudget;
