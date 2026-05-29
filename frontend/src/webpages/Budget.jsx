import React, { useState, useRef, useCallback, useEffect } from 'react';
import apiClient from '../apiClient';
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

const CATEGORIES = [
    'Food & Dining',
    'Groceries',
    'Transport',
    'Rent & Utilities',
    'Shopping',
    'Entertainment',
    'Health',
    'Education',
    'Other'
];

function formatBytes(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(amount);
}

/* ── SVG Icons ───────────────────────────── */
const UploadIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
    </svg>
);

const RemoveIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <line x1="18" y1="6" x2="6" y2="18"/>
        <line x1="6" y1="6" x2="18" y2="18"/>
    </svg>
);

const EditIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
    </svg>
);

const PlusIcon = () => (
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <line x1="12" y1="5" x2="12" y2="19"></line>
        <line x1="5" y1="12" x2="19" y2="12"></line>
    </svg>
);

/* ── Component ───────────────────────────── */
const ExpandableDescription = ({ text }) => {
    const [expanded, setExpanded] = useState(false);
    
    if (!text) return null;
    
    if (text.length <= 40) {
        return <span>{text}</span>;
    }
    
    return (
        <span>
            {expanded ? text : `${text.substring(0, 40)}...`}
            <button 
                onClick={() => setExpanded(!expanded)} 
                style={{
                    background: 'none', 
                    border: 'none', 
                    color: 'var(--lp-blue)', 
                    fontSize: '12px', 
                    cursor: 'pointer', 
                    padding: '0 0 0 5px',
                    textDecoration: 'underline'
                }}
            >
                {expanded ? 'Show less' : 'See more'}
            </button>
        </span>
    );
};

const Budget = () => {
    // File upload state
    const [files, setFiles]       = useState([]);
    const [dragging, setDragging] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [toast, setToast]       = useState(null);
    const inputRef                = useRef(null);

    // Budget data state
    const [stats, setStats] = useState(null);
    const [entries, setEntries] = useState([]);
    const [loadingData, setLoadingData] = useState(true);

    // Filters
    const currentDate = new Date();
    const [filterYear, setFilterYear] = useState(currentDate.getFullYear());
    const [filterMonth, setFilterMonth] = useState(currentDate.getMonth() + 1);

    // UI state
    const [showManualEntry, setShowManualEntry] = useState(false);
    const [editingBudget, setEditingBudget] = useState(false);
    const [budgetInput, setBudgetInput] = useState('');
    
    // Manual entry form
    const [newEntry, setNewEntry] = useState({
        amount: '',
        category: 'Other',
        description: '',
        status: '',
        paymentDate: ''
    });

    const showToast = (type, message) => {
        setToast({ type, message });
        setTimeout(() => setToast(null), 5000);
    };

    // Load Data
    const loadBudgetData = useCallback(async (forceFetch = false) => {
        const cacheKey = `budget-${filterYear}-${filterMonth}`;
        const cached = localStorage.getItem(cacheKey);

        if (cached && !forceFetch) {
            try {
                const data = JSON.parse(cached);
                setStats(data.stats);
                setEntries(data.entries);
                setBudgetInput(data.stats.monthlyBudget.toString());
                setLoadingData(false);
                return;
            } catch (e) {
                console.error("Cache parse error", e);
            }
        }

        setLoadingData(true);
        try {
            const [statsRes, entriesRes] = await Promise.all([
                apiClient.get(`/api/budget/stats?year=${filterYear}&month=${filterMonth}`),
                apiClient.get(`/api/budget/entries?year=${filterYear}&month=${filterMonth}`)
            ]);
            setStats(statsRes.data);
            setEntries(entriesRes.data);
            setBudgetInput(statsRes.data.monthlyBudget.toString());
            localStorage.setItem(cacheKey, JSON.stringify({ stats: statsRes.data, entries: entriesRes.data }));
        } catch (err) {
            console.error('Failed to load budget data', err);
            showToast('error', 'Failed to load budget data');
        } finally {
            setLoadingData(false);
        }
    }, [filterYear, filterMonth]);

    useEffect(() => {
        loadBudgetData();
    }, [loadBudgetData]);

    /* ── File Upload Logic ── */
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

    const onDragOver = (e) => { e.preventDefault(); setDragging(true); };
    const onDragLeave = () => setDragging(false);
    const onDrop = (e) => {
        e.preventDefault();
        setDragging(false);
        mergeFiles(e.dataTransfer.files);
    };
    const onInputChange = (e) => {
        mergeFiles(e.target.files);
        e.target.value = '';
    };
    const removeFile = (index) => setFiles(prev => prev.filter((_, i) => i !== index));

    const handleUpload = async () => {
        if (files.length === 0) return;
        setUploading(true);
        setToast(null);

        const formData = new FormData();
        files.forEach(f => formData.append('files[]', f, f.name));


        try {
            const res = await fetch(WEBHOOK_URL, {
                method: 'POST',
                body: formData,
            });

            if (!res.ok) throw new Error(`Server responded with status ${res.status}`);

            showToast('success', `${files.length} file(s) uploaded! It may take a minute for entries to appear.`);
            setFiles([]);
            
            // Reload data after a short delay to let webhook process
            setTimeout(() => loadBudgetData(true), 3000);
        } catch (err) {
            showToast('error', 'Upload failed. Please try again.');
        } finally {
            setUploading(false);
        }
    };

    /* ── API Operations ── */
    const updateBudgetSetting = async () => {
        try {
            await apiClient.put('/api/budget/settings', { monthlyBudget: parseFloat(budgetInput) });
            setEditingBudget(false);
            loadBudgetData(true);
            showToast('success', 'Budget updated');
        } catch (err) {
            showToast('error', 'Failed to update budget');
        }
    };

    const handleCreateEntry = async (e) => {
        e.preventDefault();
        try {
            await apiClient.post('/api/budget/entries', {
                ...newEntry,
                amount: parseFloat(newEntry.amount),
                paymentDate: newEntry.paymentDate ? new Date(newEntry.paymentDate).toISOString() : null
            });
            setShowManualEntry(false);
            setNewEntry({ amount: '', category: 'Other', description: '', status: '', paymentDate: '' });
            loadBudgetData(true);
            showToast('success', 'Entry added');
        } catch (err) {
            showToast('error', 'Failed to add entry');
        }
    };

    const updateCategory = async (id, category) => {
        try {
            await apiClient.put(`/api/budget/entries/${id}/category`, { category });
            loadBudgetData(true);
        } catch (err) {
            showToast('error', 'Failed to update category');
        }
    };

    const deleteEntry = async (id) => {
        if (!window.confirm('Delete this entry?')) return;
        try {
            await apiClient.delete(`/api/budget/entries/${id}`);
            loadBudgetData(true);
            showToast('success', 'Entry deleted');
        } catch (err) {
            showToast('error', 'Failed to delete entry');
        }
    };

    /* ── Helpers ── */
    const getProgressColor = (spent, budget) => {
        if (!budget || budget === 0) return 'var(--lp-orange)';
        const ratio = spent / budget;
        if (ratio < 0.7) return 'var(--lp-green)';
        if (ratio < 0.9) return 'var(--lp-yellow)';
        return 'var(--lp-orange)';
    };

    return (
        <div className="personal-container">
            <div className="personal-header">
                <h1>Personal Budget</h1>
                <p>Track your spending, upload receipts, and manage your budget.</p>
            </div>

            {toast && (
                <div className={`personal-toast personal-toast-${toast.type}`} role="alert" style={{marginBottom: '20px'}}>
                    {toast.message}
                </div>
            )}

            <div className="budget-dashboard-vertical">
                {/* ── Top Row: Upload & Stats ── */}
                <div className="budget-top-row">
                    
                    {/* Add Documents Card */}
                    <div className="budget-top-col">
                        <div className="upload-card compact">
                            <h2 className="upload-card-title">Add Documents</h2>
                            <div
                                className={`upload-zone compact-zone${dragging ? ' dragging' : ''}`}
                                onDragOver={onDragOver}
                                onDragLeave={onDragLeave}
                                onDrop={onDrop}
                                onClick={() => inputRef.current?.click()}
                                role="button"
                                tabIndex={0}
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
                                <div className="upload-zone-icon"><UploadIcon /></div>
                                <div className="upload-zone-label">
                                    <strong>{dragging ? 'Drop files' : 'Drag & drop'}</strong>
                                    <span>or click to browse</span>
                                </div>
                            </div>

                            {files.length > 0 && (
                                <div className="file-list compact">
                                    {files.map((file, idx) => (
                                        <div className="file-item" key={idx}>
                                            <div className="file-item-info">
                                                <div className="file-item-name">{file.name}</div>
                                                <div className="file-item-size">{formatBytes(file.size)}</div>
                                            </div>
                                            <button className="file-item-remove" onClick={() => removeFile(idx)} disabled={uploading}>
                                                <RemoveIcon />
                                            </button>
                                        </div>
                                    ))}
                                    <button className="btn btn-primary btn-sm mt-3" onClick={handleUpload} disabled={uploading} style={{width: '100%'}}>
                                        {uploading ? 'Uploading…' : 'Upload & Analyze'}
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Stats Overview */}
                    <div className="budget-top-col">
                        {stats && (
                            <div className="budget-stats-card">
                                <div className="stats-header">
                                    <h2 className="upload-card-title">Overview</h2>
                                    <div className="month-selector">
                                        <select value={filterMonth} onChange={(e) => setFilterMonth(parseInt(e.target.value))}>
                                            {Array.from({length: 12}, (_, i) => (
                                                <option key={i+1} value={i+1}>{new Date(2000, i).toLocaleString('default', { month: 'short' })}</option>
                                            ))}
                                        </select>
                                        <select value={filterYear} onChange={(e) => setFilterYear(parseInt(e.target.value))}>
                                            {[currentDate.getFullYear() - 1, currentDate.getFullYear(), currentDate.getFullYear() + 1].map(y => (
                                                <option key={y} value={y}>{y}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="budget-target">
                                    <div className="budget-target-header">
                                        <span>Monthly Budget</span>
                                        {editingBudget ? (
                                            <div className="budget-edit-form">
                                                <span className="currency-symbol">$</span>
                                                <input 
                                                    type="number" 
                                                    value={budgetInput} 
                                                    onChange={(e) => setBudgetInput(e.target.value)}
                                                    className="form-input budget-input"
                                                />
                                                <button onClick={updateBudgetSetting} className="btn btn-sm btn-primary">Save</button>
                                                <button onClick={() => setEditingBudget(false)} className="btn btn-sm btn-secondary">Cancel</button>
                                            </div>
                                        ) : (
                                            <div className="budget-target-value">
                                                {formatCurrency(stats.monthlyBudget)}
                                                <button onClick={() => setEditingBudget(true)} className="icon-btn" aria-label="Edit budget"><EditIcon /></button>
                                            </div>
                                        )}
                                    </div>

                                    <div className="budget-progress-container">
                                        <div className="budget-progress-bar">
                                            <div 
                                                className="budget-progress-fill" 
                                                style={{
                                                    width: `${stats.monthlyBudget > 0 ? Math.min(100, (stats.totalSpent / stats.monthlyBudget) * 100) : 0}%`,
                                                    backgroundColor: getProgressColor(stats.totalSpent, stats.monthlyBudget)
                                                }}
                                            />
                                        </div>
                                        <div className="budget-progress-labels">
                                            <span>{formatCurrency(stats.totalSpent)} spent</span>
                                            <span>{formatCurrency(stats.remainingBudget)} left</span>
                                        </div>
                                    </div>
                                </div>

                                {Object.keys(stats.spentByCategory || {}).length > 0 && (
                                    <div className="category-breakdown">
                                        <h3>By Category</h3>
                                        <div className="category-list">
                                            {Object.entries(stats.spentByCategory)
                                                .sort(([,a], [,b]) => b - a)
                                                .map(([cat, amount]) => (
                                                <div className="category-row" key={cat}>
                                                    <span className="category-name">{cat}</span>
                                                    <span className="category-amount">{formatCurrency(amount)}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                {/* ── Bottom Row: Entries ── */}
                <div className="budget-bottom-row">
                    <div className="entries-card">
                        <div className="entries-header">
                            <h2 className="upload-card-title mb-0">Recent Entries</h2>
                            <button 
                                className="btn btn-sm btn-secondary"
                                onClick={() => setShowManualEntry(!showManualEntry)}
                            >
                                <PlusIcon /> Add Manual
                            </button>
                        </div>

                        {showManualEntry && (
                            <form className="manual-entry-form" onSubmit={handleCreateEntry}>
                                <h3>New Entry</h3>
                                <div className="form-group-row">
                                    <div className="form-group">
                                        <label>Amount</label>
                                        <input 
                                            type="number" step="0.01" required 
                                            className="form-input"
                                            value={newEntry.amount}
                                            onChange={e => setNewEntry({...newEntry, amount: e.target.value})}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Category</label>
                                        <select 
                                            className="form-input"
                                            value={newEntry.category}
                                            onChange={e => setNewEntry({...newEntry, category: e.target.value})}
                                        >
                                            {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                                        </select>
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Description</label>
                                    <input 
                                        type="text" required
                                        className="form-input"
                                        value={newEntry.description}
                                        onChange={e => setNewEntry({...newEntry, description: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Merchant / Source</label>
                                    <input 
                                        type="text"
                                        className="form-input"
                                        value={newEntry.status}
                                        onChange={e => setNewEntry({...newEntry, status: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Payment Date (Optional)</label>
                                    <input 
                                        type="date"
                                        className="form-input"
                                        value={newEntry.paymentDate}
                                        onChange={e => setNewEntry({...newEntry, paymentDate: e.target.value})}
                                    />
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowManualEntry(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary">Save Entry</button>
                                </div>
                            </form>
                        )}

                        <div className="entries-table-wrapper">
                            {loadingData ? (
                                <div className="entries-empty">Loading entries...</div>
                            ) : entries.length === 0 ? (
                                <div className="entries-empty">No entries found for this month.</div>
                            ) : (
                                <table className="entries-table">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Description</th>
                                            <th>Category</th>
                                            <th className="text-right">Amount</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {entries.map(entry => (
                                            <tr key={entry.id}>
                                                <td className="date-cell">
                                                    {new Date(entry.paymentDate || entry.submittedAt).toLocaleDateString(undefined, {month: 'short', day: 'numeric'})}
                                                </td>
                                                <td>
                                                    <div className="entry-desc">
                                                        <ExpandableDescription text={entry.description} />
                                                        {entry.s3Url && (
                                                            <a href={entry.s3Url} target="_blank" rel="noopener noreferrer" style={{marginLeft: '8px', fontSize: '12px', color: 'var(--lp-blue)', textDecoration: 'underline'}}>
                                                                [View Receipt]
                                                            </a>
                                                        )}
                                                    </div>
                                                    <div className="entry-status">{entry.status}</div>
                                                </td>
                                                <td>
                                                    <select 
                                                        className="category-select"
                                                        value={entry.category}
                                                        onChange={(e) => updateCategory(entry.id, e.target.value)}
                                                    >
                                                        {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                                                    </select>
                                                </td>
                                                <td className="amount-cell text-right">{formatCurrency(entry.amount)}</td>
                                                <td className="action-cell">
                                                    <button className="icon-btn text-danger" onClick={() => deleteEntry(entry.id)}>
                                                        <RemoveIcon />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Budget;

