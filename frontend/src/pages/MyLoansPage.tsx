import { useQuery } from "@tanstack/react-query";
import { Box, Card, Chip } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { loansService, type Loan, type LoanStatus } from "@/services";
import { PageHeader } from "@/components/PageHeader";
import { useTranslation } from "react-i18next";

const statusColor: Record<LoanStatus, "default" | "success" | "warning" | "error" | "info"> = {
  PENDING: "default",
  ACTIVE: "info",
  RETURNED: "success",
  OVERDUE: "error",
};

export function MyLoansPage() {
  const { t } = useTranslation();
  const { data, isLoading } = useQuery({
    queryKey: ["my-loans"],
    queryFn: () => loansService.getMyLoans({ page: 0, size: 50 }),
  });

  const columns: GridColDef<Loan>[] = [
    { field: "book", headerName: t("loans.book"), flex: 1.2, valueGetter: (_, r) => r.book?.title },
    { field: "loanDate", headerName: t("loans.loanDate"), width: 150 },
    { field: "expectedReturnDate", headerName: t("loans.expectedReturn"), width: 150 },
    { field: "actualReturnDate", headerName: t("loans.returnDate"), width: 150 },
    {
      field: "status",
      headerName: t("loans.status"),
      width: 130,
      renderCell: (p) => (
        <Chip label={p.value as string} color={statusColor[p.value as LoanStatus]} size="small" />
      ),
    },
  ];

  return (
    <Box>
      <PageHeader title={t("loans.myTitle")} subtitle={t("loans.mySubtitle")} />
      <Card>
        <DataGrid
          autoHeight
          rows={data?.content?.filter((row) => row.id != null) ?? []}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          getRowId={(row) => row.id ?? Math.random()}
          sx={{ border: 0, "& .MuiDataGrid-columnHeaders": { bgcolor: "#f7f9fc" } }}
        />
      </Card>
    </Box>
  );
}
