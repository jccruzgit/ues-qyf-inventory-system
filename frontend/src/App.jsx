import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/common/ProtectedRoute';
import PublicOnlyRoute from './components/common/PublicOnlyRoute';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/auth/LoginPage';
import AuthSupportPage from './pages/auth/AuthSupportPage';
import AlertsPage from './pages/alerts/AlertsPage';
import BatchesPage from './pages/batches/BatchesPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import InventoryEntryCreatePage from './pages/inventory/InventoryEntryCreatePage';
import InventoryExitCreatePage from './pages/inventory/InventoryExitCreatePage';
import InventoryStockPage from './pages/inventory/InventoryStockPage';
import ManufacturedProductsPage from './pages/manufactured-products/ManufacturedProductsPage';
import ModulePlaceholderPage from './pages/modules/ModulePlaceholderPage';
import MovementsPage from './pages/movements/MovementsPage';
import ProductionRunCreatePage from './pages/production/ProductionRunCreatePage';
import ProductCreatePage from './pages/products/ProductCreatePage';
import ProductsPage from './pages/products/ProductsPage';
import RecipesPage from './pages/recipes/RecipesPage';

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
          <Route path="/products/new" element={<ProductCreatePage />} />
          <Route path="/inventory" element={<InventoryStockPage />} />
          <Route path="/inventory/entries/new" element={<InventoryEntryCreatePage />} />
          <Route path="/inventory/exits/new" element={<InventoryExitCreatePage />} />
          <Route path="/production" element={<ProductionRunCreatePage />} />
          <Route path="/recipes" element={<RecipesPage />} />
          <Route path="/manufactured-products" element={<ManufacturedProductsPage />} />
          <Route path="/movements" element={<MovementsPage />} />
          <Route path="/batches" element={<BatchesPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
          <Route path="/support" element={<ModulePlaceholderPage />} />
          <Route path="/archive" element={<ModulePlaceholderPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
