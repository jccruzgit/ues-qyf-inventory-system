import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/common/ProtectedRoute';
import PublicOnlyRoute from './components/common/PublicOnlyRoute';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/auth/LoginPage';
import AuthSupportPage from './pages/auth/AuthSupportPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import ModulePlaceholderPage from './pages/modules/ModulePlaceholderPage';
import ProductsPage from './pages/products/ProductsPage';

function App() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/" element={<LoginPage />} />
        <Route
          path="/forgot-password"
          element={<AuthSupportPage mode="forgot-password" />}
        />
        <Route
          path="/request-access"
          element={<AuthSupportPage mode="request-access" />}
        />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/inventory" element={<ModulePlaceholderPage />} />
          <Route path="/movements" element={<ModulePlaceholderPage />} />
          <Route path="/batches" element={<ModulePlaceholderPage />} />
          <Route path="/alerts" element={<ModulePlaceholderPage />} />
          <Route path="/support" element={<ModulePlaceholderPage />} />
          <Route path="/archive" element={<ModulePlaceholderPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
