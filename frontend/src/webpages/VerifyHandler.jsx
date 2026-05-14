import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";

const VerifyHandler = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [status, setStatus] = useState("pending");
    const [errorMsg, setErrorMsg] = useState("");

    const params = new URLSearchParams(location.search);
    const token = params.get("token");

    useEffect(() => {
        if (!token) {
            setStatus("missing");
            return;
        }

        const verifyEmail = async () => {
            try {
                const res = await axios.get(`/api/verify?token=${token}`);
                if (res.data === true) {
                    setStatus("success");
                } else {
                    setStatus("failed");
                    setErrorMsg("Verification failed. You can resend a verification email.");
                }
            } catch (err) {
                console.error("Verification request failed", err);
                setStatus("failed");
                setErrorMsg("Server error. Please try again later.");
            }
        };

        verifyEmail();
    }, [token]);

    const handleResend = async () => {
        try {
            await axios.post(`/api/user/resend-verification`, {}, { withCredentials: true });
            alert("Verification email sent!");
        } catch (err) {
            console.error("Resend failed", err);
            alert("Failed to resend verification email.");
        }
    };

    const handleCancel = () => {
        navigate("/login", { replace: true });
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen">
            {status === "pending" && <p className="text-lg">Verifying your email...</p>}
            {status === "success" && (
                <>
                    <p className="text-lg text-green-600">Email successfully verified!</p>
                    <button
                        className="btn btn-primary mt-4"
                        onClick={() => navigate("/login", { replace: true })}
                    >
                        Go to Login
                    </button>
                </>
            )}
            {status === "failed" && (
                <>
                    <p className="text-lg text-red-600">{errorMsg}</p>
                    <div className="mt-4">
                        <button className="btn btn-primary mr-2" onClick={handleResend}>
                            Resend Verification Email
                        </button>
                        <button className="btn btn-secondary" onClick={handleCancel}>
                            Back To Login
                        </button>
                    </div>
                </>
            )}
            {status === "missing" && (
                <>
                    <p className="text-lg text-red-600">Missing token in the URL.</p>
                    <button className="btn btn-secondary mt-4" onClick={handleCancel}>
                        Go to Login
                    </button>
                </>
            )}
        </div>
    );
};

export default VerifyHandler;
