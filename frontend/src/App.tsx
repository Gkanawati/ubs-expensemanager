import { AuthenticatedLayout } from '@/components/Layout';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { PublicRoute } from '@/components/PublicRoute';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AnalyticsPage } from './pages/Analytics/AnalyticsPage';
import { ApprovalsPage } from './pages/Approvals/ApprovalsPage';
import { DashboardPage } from './pages/Dashboard/DashboardPage';
import { ExpensesPage } from './pages/Expenses/ExpensesPage';
import { ManageExpensesPage } from './pages/ManageExpenses/ManageExpensesPage';
import { ExpensesReport } from './pages/ExpensesReport/ExpensesReport';
import { LoginPage } from './pages/Login';
import { ProfilePage } from './pages/Profile/ProfilePage';
import { UsersPage } from './pages/Users/UsersPage';
import { DepartmentPage } from './pages/Department';
import { CategoriesPage } from './pages/Categories/CategoryPage';
import { AlertsPage } from './pages/Alerts/AlertsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path='/'
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          element={
            <ProtectedRoute>
              <AuthenticatedLayout />
            </ProtectedRoute>
          }
        >
          <Route path='/approvals' element={<ApprovalsPage />} />
          <Route path='/dashboard' element={
              <ProtectedRoute allowedRoles={['ROLE_MANAGER', 'ROLE_FINANCE', 'ROLE_EMPLOYEE']}>
                <DashboardPage />
              </ProtectedRoute>
              } />
          <Route path='/department' element={
              <ProtectedRoute allowedRoles={['ROLE_FINANCE']}>
                <DepartmentPage />
              </ProtectedRoute>
              } />
          <Route path='/expenses' element={
            <ProtectedRoute allowedRoles={['ROLE_EMPLOYEE']}>
              <ExpensesPage />
            </ProtectedRoute>
          } />
          <Route path='/manage-expenses' element={
              <ProtectedRoute allowedRoles={['ROLE_MANAGER', 'ROLE_FINANCE']}>
                <ManageExpensesPage />
              </ProtectedRoute>
              } />
          <Route path='/expenses-report' element={<ExpensesReport />} />
          <Route path='/analytics' element={<AnalyticsPage />} />
          <Route path='/users' element={
              <ProtectedRoute allowedRoles={['ROLE_FINANCE']}>
                <UsersPage />
              </ProtectedRoute>
              } />
          <Route path='/profile' element={<ProfilePage />} />
          <Route path='/category' element={
              <ProtectedRoute allowedRoles={['ROLE_FINANCE']}>
                <CategoriesPage />
              </ProtectedRoute>
              } />
          <Route path='/alert' element={
              <ProtectedRoute allowedRoles={['ROLE_FINANCE']}>
                <AlertsPage />
              </ProtectedRoute>
              } />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;