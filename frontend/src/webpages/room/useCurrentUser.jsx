import { useUser } from '../../App';

// Delegates to the global UserContext so we never hit /api/get-user more than once per session.
// The public API (currentUser, loadingUser, errorUser) is preserved for all existing callers.
const useCurrentUser = () => {
  const { user: currentUser, userLoading: loadingUser } = useUser();
  return { currentUser, loadingUser, errorUser: null };
};

export default useCurrentUser;
