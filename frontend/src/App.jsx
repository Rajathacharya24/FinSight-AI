import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Upload from './pages/Upload.jsx';
import Documents from './pages/Documents.jsx';
import Analytics from './pages/Analytics.jsx';
import Layout from './components/Layout.jsx';
import { useAuth } from './hooks/useAuth.js';

function PrivateRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/upload" replace />} />
        <Route path="upload" element={<Upload />} />
        <Route path="documents" element={<Documents />} />
        <Route path="analytics" element={<Analytics />} />
      </Route>
    </Routes>
  );
}
