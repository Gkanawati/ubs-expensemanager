import { useAuth } from '@/hooks/useAuth';
import { CheckCircle, Clock, Receipt, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';
import { getExpensesSummary, ExpensesSummary } from '@/api/dashboard.api';
import { ExpenseStatus } from '@/api/expense.api';

const getStatusBadgeClasses = (status: ExpenseStatus) => {
  switch (status) {
    case 'APPROVED_BY_MANAGER':
    case 'APPROVED_BY_FINANCE':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    case 'REJECTED':
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    case 'REQUIRES_REVISION':
      return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200';
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
  }
};

const getStatusLabel = (status: ExpenseStatus) => {
  switch (status) {
    case 'APPROVED_BY_MANAGER':
      return 'Approved by Manager';
    case 'APPROVED_BY_FINANCE':
      return 'Approved';
    case 'PENDING':
      return 'Pending';
    case 'REJECTED':
      return 'Rejected';
    default:
      return status;
  }
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

export const DashboardPage = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<ExpensesSummary>({
    totalExpenses: 0,
    approvedExpensesCount: 0,
    pendingExpensesCount: 0,
    expensesThisMonth: 0,
    lastExpenses: [],
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const data = await getExpensesSummary();
        setSummary(data);
      } catch (error) {
        console.error('Failed to fetch expenses summary:', error);
        // Keep default zero values on error
      } finally {
        setLoading(false);
      }
    };

    fetchSummary();
  }, []);

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold text-gray-900 dark:text-white'>
          Dashboard
        </h1>
        <p className='mt-2 text-sm text-gray-600 dark:text-gray-400'>
          Welcome back, {user?.email}!
        </p>
      </div>

      <div className='grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4'>
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Total Expenses
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                ${loading ? '...' : summary.totalExpenses.toFixed(2)}
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900'>
              <Receipt className='h-6 w-6 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Approved
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                {loading ? '...' : summary.approvedExpensesCount}
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-green-100 dark:bg-green-900'>
              <CheckCircle className='h-6 w-6 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Pending
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                {loading ? '...' : summary.pendingExpensesCount}
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 dark:bg-yellow-900'>
              <Clock className='h-6 w-6 text-yellow-600 dark:text-yellow-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                This Month
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                ${loading ? '...' : summary.expensesThisMonth.toFixed(2)}
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-purple-100 dark:bg-purple-900'>
              <TrendingUp className='h-6 w-6 text-purple-600 dark:text-purple-400' />
            </div>
          </div>
        </div>
      </div>

      <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
        <h2 className='text-lg font-semibold text-gray-900 dark:text-white'>
          Last Expenses
        </h2>
        <div className='mt-4 space-y-3'>
          {loading ? (
            <p className='text-sm text-gray-600 dark:text-gray-400'>Loading...</p>
          ) : summary.lastExpenses.length === 0 ? (
            <p className='text-sm text-gray-600 dark:text-gray-400'>
              No last expenses
            </p>
          ) : (
            summary.lastExpenses.map((expense, index) => (
              <div
                key={index}
                className={`flex items-center justify-between ${
                  index < summary.lastExpenses.length - 1
                    ? 'border-b border-gray-100 pb-3 dark:border-gray-800'
                    : ''
                }`}
              >
                <div>
                  <p className='font-medium text-gray-900 dark:text-white'>
                    {expense.description}
                  </p>
                  <p className='text-sm text-gray-600 dark:text-gray-400'>
                    {formatDate(expense.date)}
                  </p>
                </div>
                <span
                  className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${getStatusBadgeClasses(
                    expense.status
                  )}`}
                >
                  {getStatusLabel(expense.status)}
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
