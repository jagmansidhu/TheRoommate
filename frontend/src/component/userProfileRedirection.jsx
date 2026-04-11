import apiClient from '../apiClient';
import {useEffect, useRef} from 'react';
import {useAuth0} from '@auth0/auth0-react';
import {useNavigate} from 'react-router-dom';
import axios from 'axios';

const useProfileCompletionRedirect = () => {
    const {isAuthenticated, isLoading, getAccessTokenSilently} = useAuth0();
    const navigate = useNavigate();
    const hasCheckedProfile = useRef(false);

    useEffect(() => {
        const checkProfileAndRedirect = async () => {
            if (!isAuthenticated || isLoading || hasCheckedProfile.current) {
                return;
            }

            try {
                        const accessToken = await getAccessTokenSilently();

                const response = await apiClient.get(`/api/profile-status`, {
                    headers: {
                        Authorization: `Bearer ${accessToken}`,
                    },
                });

                const isProfileComplete = response.data.isComplete;
                console.log("Profile completion status from backend:", isProfileComplete);

                if (!isProfileComplete && window.location.pathname !== '/complete-profile') {
                    navigate('/complete-profile');
                } else if (isProfileComplete && window.location.pathname === '/complete-profile') {
                    navigate('/dashboard');
                }
                hasCheckedProfile.current = true;

            } catch (error) {
                console.error("Error checking profile status:", error);
            }
        };

        if (isAuthenticated && !isLoading) {
            checkProfileAndRedirect();
        }

    }, [isAuthenticated, isLoading, navigate, getAccessTokenSilently]);
};

export default useProfileCompletionRedirect;