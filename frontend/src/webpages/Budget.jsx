import React, { useState, useRef, useCallback } from 'react';
import '../styling/Personal.css';

const WEBHOOK_URL =
    'https://primary-production-0fe8e.up.railway.app/webhook/ed83c46d-f6fd-4c1e-acbc-a6166842f8c1';

const ACCEPTED_TYPES = [
    'image/jpeg',
    'image/png',
    'application/pdf',
    'image/webp',
    'image/gif',
    'text/plain',
    'text/csv',
];

const ACCEPTED_EXT = '.jpg,.jpeg,.png,.pdf,.webp,.gif,.txt,.csv';

function formatBytes(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/* ── SVG Icons ───────────────────────────── */
const UploadIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
    </svg>
);

const FileIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
    </svg>
);

const RemoveIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <line x1="18" y1="6" x2="6" y2="18"/>
        <line x1="6" y1="6" x2="18" y2="18"/>
    </svg>
);

/* ── Component ───────────────────────────── */
const Budget = () => {
    const [files, setFiles]       = useState([]);
    const [dragging, setDragging] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [toast, setToast]       = useState(null); // { type: 'success'|'error', message: string }
    const inputRef                = useRef(null);

    const showToast = (type, message) => {
        setToast({ type, message });
        setTimeout(() => setToast(null), 5000);
    };

    /* Merge new files, skip duplicates by name+size */
    const mergeFiles = useCallback((incoming) => {
        const valid = [...incoming].filter(f => ACCEPTED_TYPES.includes(f.type));
        if (valid.length < incoming.length) {
            showToast('error', 'Some files were skipped — only JPG, PNG, PDF and similar formats are accepted.');
        }
        setFiles(prev => {
            const existing = new Set(prev.map(f => `${f.name}-${f.size}`));
            const fresh = valid.filter(f => !existing.has(`${f.name}-${f.size}`));
            return [...prev, ...fresh];
        });
    }, []);

    /* Drag events */
    const onDragOver = (e) => { e.preventDefault(); setDragging(true); };
    const onDragLeave = () => setDragging(false);
    const onDrop = (e) => {
        e.preventDefault();
        setDragging(false);
        mergeFiles(e.dataTransfer.files);
    };

    /* Native input change */
    const onInputChange = (e) => {
        mergeFiles(e.target.files);
        e.target.value = ''; // reset so same file can be re-added after removal
    };

    /* Remove a queued file */
    const removeFile = (index) =>
        setFiles(prev => prev.filter((_, i) => i !== index));

    /* POST to webhook */
    const handleUpload = async () => {
        if (files.length === 0) {
            showToast('error', 'Please select at least one file before uploading.');
            return;
        }

        setUploading(true);
        setToast(null);

        const formData = new FormData();
        files.forEach(f => formData.append('files[]', f, f.name));

        // Attach the app's JWT so the n8n webhook can verify the caller
        const token = localStorage.getItem('token');
        const headers = token ? { Authorization: `Bearer ${token}` } : {};

        try {
            const res = await fetch(WEBHOOK_URL, {
                method: 'POST',
                headers,
                body: formData,
            });

            if (!res.ok) {
                const text = await res.text().catch(() => '');
                throw new Error(text || `Server responded with status ${res.status}`);
            }

            showToast('success', `${files.length} file${files.length > 1 ? 's' : ''} uploaded successfully! Your budget analysis is being processed.`);
            setFiles([]);
        } catch (err) {
            console.error('Budget upload error:', err);
            showToast('error', err.message || 'Upload failed. Please try again.');
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="personal-container">
            {/* ── Page Header ── */}
            <div className="personal-header">
                <h1>Personal</h1>
                <p>Upload your bank statements, receipts, or invoices for AI-powered budget analysis.</p>
            </div>

            {/* ── Upload Card ── */}
            <div className="upload-card">
                <h2 className="upload-card-title">Upload Documents</h2>

                {/* Drop zone */}
                <div
                    className={`upload-zone${dragging ? ' dragging' : ''}`}
                    onDragOver={onDragOver}
                    onDragLeave={onDragLeave}
                    onDrop={onDrop}
                    onClick={() => inputRef.current?.click()}
                    role="button"
                    tabIndex={0}
                    aria-label="Upload files"
                    onKeyDown={(e) => e.key === 'Enter' && inputRef.current?.click()}
                >
                    <input
                        ref={inputRef}
                        type="file"
                        id="budget-file-input"
                        multiple
                        accept={ACCEPTED_EXT}
                        onChange={onInputChange}
                        onClick={(e) => e.stopPropagation()}
                    />

                    <div className="upload-zone-icon">
                        <UploadIcon />
                    </div>

                    <div className="upload-zone-label">
                        <strong>{dragging ? 'Drop files here' : 'Drag & drop files here'}</strong>
                        <span>or click to browse — JPG, PNG, PDF, CSV, TXT accepted</span>
                    </div>
                </div>

                {/* Queued file list */}
                {files.length > 0 && (
                    <div className="file-list">
                        <p className="file-list-header">
                            {files.length} file{files.length > 1 ? 's' : ''} queued
                        </p>
                        {files.map((file, idx) => (
                            <div className="file-item" key={`${file.name}-${file.size}-${idx}`}>
                                <div className="file-item-icon">
                                    <FileIcon />
                                </div>
                                <div className="file-item-info">
                                    <div className="file-item-name">{file.name}</div>
                                    <div className="file-item-size">{formatBytes(file.size)}</div>
                                </div>
                                <button
                                    className="file-item-remove"
                                    onClick={() => removeFile(idx)}
                                    aria-label={`Remove ${file.name}`}
                                    disabled={uploading}
                                >
                                    <RemoveIcon />
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {/* Toast */}
                {toast && (
                    <div className={`personal-toast personal-toast-${toast.type}`} role="alert">
                        {toast.message}
                    </div>
                )}

                {/* Actions */}
                <div className="upload-actions">
                    <button
                        id="budget-upload-btn"
                        className="btn btn-primary"
                        onClick={handleUpload}
                        disabled={uploading || files.length === 0}
                    >
                        {uploading && <span className="btn-spinner" aria-hidden="true" />}
                        {uploading ? 'Uploading…' : 'Upload & Analyze'}
                    </button>

                    {files.length > 0 && !uploading && (
                        <button
                            className="btn btn-secondary"
                            onClick={() => setFiles([])}
                        >
                            Clear All
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Budget;
