import { useEffect, useState } from 'react';
import { Plus, Edit, CheckCircle } from 'lucide-react';
import { DataTable, ColumnDef, RowAction } from '@/components/DataTable';
import { ConfirmationDialog } from '@/components/ConfirmationDialog';
import { ActionButton } from '@/components/ui/ActionButton';
import { formatCurrency } from '@/utils/validation';
import { getDepartments, deleteDepartment } from '@/api/department.api';
import type { Department } from '@/types/department';
import { CreateDepartmentDialog } from './components/CreateDepartmentDialog';
import { EditDepartmentDialog } from './components/EditDepartmentDialog';

export const DepartmentPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [openCreate, setOpenCreate] = useState(false);
  const [openCreateSuccessDialog, setOpenCreateSuccessDialog] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState<string>('');
  const [editing, setEditing] = useState<Department | null>(null);
  const [openEditSuccessDialog, setOpenEditSuccessDialog] = useState(false);
  const [editErrorMessage, setEditErrorMessage] = useState<string>('');
  const [toDelete, setToDelete] = useState<Department | null>(null);

  const canEdit = true; // mesmo pattern do UserPage

  async function load() {
    try {
      setLoading(true);
      const data = await getDepartments();
      console.log('[DepartmentPage] Loaded departments:', data);
      // Verify all departments have currencyName
      data.forEach((d, idx) => {
        console.log(`[DepartmentPage] Department ${idx}:`, {
          id: d.id,
          name: d.name,
          currencyId: d.currencyId,
          currencyName: d.currencyName,
          type: typeof d.currencyName,
        });
      });
      setDepartments([...data]);
    } catch (err) {
      console.error('[DepartmentPage] Error loading departments:', err);
      setError('Failed to load departments');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const columns: ColumnDef<Department>[] = [
    {
      key: 'name',
      label: 'Name',
      render: (row: Department) => {
        return <span className='font-medium'>{row.name}</span>;
      },
    },
    {
      key: 'currencyName',
      label: 'Currency',
    },
    {
      key: 'dailyBudget',
      label: 'Daily Budget',
      headerAlign: 'right',
      render: (row: Department) => {
        const currency = row.currencyName || 'USD';
        console.log(
          `[DepartmentPage:dailyBudget] rendering with currency=${currency}`,
          row
        );
        return (
          <span className='text-right block'>
            {formatCurrency(row.dailyBudget ?? 0, currency)}
          </span>
        );
      },
    },
    {
      key: 'monthlyBudget',
      label: 'Monthly Budget',
      headerAlign: 'right',
      render: (row: Department) => {
        const currency = row.currencyName || 'USD';
        console.log(
          `[DepartmentPage:monthlyBudget] rendering with currency=${currency}`,
          row
        );
        return (
          <span className='text-right block'>
            {formatCurrency(row.monthlyBudget ?? 0, currency)}
          </span>
        );
      },
    },
  ];

  const actions: RowAction<Department>[] = [
    {
      label: 'Edit',
      icon: <Edit className='h-4 w-4' />,
      onClick: (row) => setEditing(row),
      shouldShow: () => canEdit,
    },
  ];

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold text-gray-900 dark:text-white'>
          Departments
        </h1>
        <p className='mt-2 text-sm text-gray-600 dark:text-gray-400'>
          Manage departments and budgets
        </p>
      </div>

      {canEdit && (
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-end'>
            <ActionButton
              label='Create Department'
              icon={<Plus className='h-4 w-4' />}
              onClick={() => setOpenCreate(true)}
            />
          </div>
        </div>
      )}

      <DataTable
        columns={columns}
        data={departments}
        actions={actions}
        emptyMessage={loading ? 'Loading...' : error || 'No departments found'}
      />

      <CreateDepartmentDialog
        open={openCreate}
        onOpenChange={(open) => {
          if (open) {
            setCreateErrorMessage('');
          }
          setOpenCreate(open);
        }}
        onSuccess={async () => {
          setOpenCreate(false);
          setCreateErrorMessage('');
          await load();
          setOpenCreateSuccessDialog(true);
        }}
        error={createErrorMessage}
        onError={setCreateErrorMessage}
      />

      <ConfirmationDialog
        open={openCreateSuccessDialog}
        onOpenChange={setOpenCreateSuccessDialog}
        title='Department Created'
        description='The department has been successfully created.'
        confirmText='Done'
        variant='success'
        icon={<CheckCircle className='h-6 w-6 text-green-600' />}
        onConfirm={() => setOpenCreateSuccessDialog(false)}
      />

      {editing && (
        <EditDepartmentDialog
          department={editing}
          open={!!editing}
          onOpenChange={(open) => {
            if (!open) {
              setEditing(null);
              setEditErrorMessage('');
            } else {
              setEditErrorMessage('');
            }
          }}
          onSuccess={() => {
            setEditing(null);
            setEditErrorMessage('');
            load();
            setOpenEditSuccessDialog(true);
          }}
          error={editErrorMessage}
          onError={setEditErrorMessage}
        />
      )}

      <ConfirmationDialog
        open={openEditSuccessDialog}
        onOpenChange={setOpenEditSuccessDialog}
        title='Department Updated'
        description='The department has been successfully updated.'
        confirmText='Done'
        variant='success'
        icon={<CheckCircle className='h-6 w-6 text-green-600' />}
        onConfirm={() => setOpenEditSuccessDialog(false)}
      />

      {toDelete && (
        <ConfirmationDialog
          open={!!toDelete}
          title='Delete Department'
          description={`Are you sure you want to delete ${toDelete.name}?`}
          confirmText='Delete'
          variant='danger'
          onConfirm={async () => {
            await deleteDepartment(toDelete.id);
            setToDelete(null);
            load();
          }}
          onOpenChange={() => setToDelete(null)}
        />
      )}
    </div>
  );
};
