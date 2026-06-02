import { useEffect, useState } from "react";
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import type { Book, Category, BookFormValues } from "@/services";
import { useTranslation } from "react-i18next";

export function BookForm({
  initial,
  categories,
  onSubmit,
  onCancel,
  submitting,
}: {
  initial?: Book;
  categories: Category[];
  onSubmit: (v: BookFormValues) => void;
  onCancel?: () => void;
  submitting?: boolean;
}) {
  const { t } = useTranslation();
  const [values, setValues] = useState<BookFormValues>({
    title: "",
    author: "",
    isbn: "",
    description: "",
    stock: 1,
    publishedDate: "",
    categoryIds: [],
  });

  useEffect(() => {
    if (initial) {
      setValues({
        title: initial.title ?? "",
        author: initial.author ?? "",
        isbn: initial.isbn ?? "",
        description: initial.description ?? "",
        stock: initial.stock ?? 0,
        publishedDate: initial.publishedDate ?? "",
        categoryIds: (initial.categories ?? []).map((c) => c.id),
      });
    }
  }, [initial]);

  return (
    <Card>
      <CardContent sx={{ p: 3 }}>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            onSubmit(values);
          }}
        >
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 8 }}>
              <TextField
                label={t("books.titleCol")}
                value={values.title}
                onChange={(e) => setValues({ ...values, title: e.target.value })}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                label={t("books.isbn")}
                value={values.isbn}
                onChange={(e) => setValues({ ...values, isbn: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                label={t("books.author")}
                value={values.author}
                onChange={(e) => setValues({ ...values, author: e.target.value })}
                required
              />
            </Grid>
            <Grid size={{ xs: 6, md: 3 }}>
              <TextField
                label={t("books.stock")}
                type="number"
                value={values.stock}
                onChange={(e) => setValues({ ...values, stock: Number(e.target.value) })}
                slotProps={{ htmlInput: { min: 0 } }}
                required
              />
            </Grid>
            <Grid size={{ xs: 6, md: 3 }}>
              <TextField
                label={t("books.publishedDate")}
                type="date"
                value={values.publishedDate}
                onChange={(e) => setValues({ ...values, publishedDate: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label={t("books.description")}
                value={values.description}
                onChange={(e) => setValues({ ...values, description: e.target.value })}
                multiline
                minRows={3}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                select
                label={t("books.category")}
                value={values.categoryIds}
                onChange={(e) =>
                  setValues({
                    ...values,
                    categoryIds:
                      typeof e.target.value === "string"
                        ? e.target.value.split(",").map(Number)
                        : (e.target.value as unknown as number[]),
                  })
                }
                slotProps={{
                  select: {
                    multiple: true,
                    renderValue: (selected) => (
                      <Stack direction="row" spacing={0.5} sx={{ flexWrap: "wrap", gap: 0.5 }}>
                        {(selected as number[]).map((id) => {
                          const c = categories.find((x) => x.id === id);
                          return c ? <Chip key={id} label={c.name} size="small" /> : null;
                        })}
                      </Stack>
                    ),
                  },
                }}
              >
                {categories.map((c) => (
                  <MenuItem key={c.id} value={c.id}>
                    {c.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <Stack direction="row" spacing={1} sx={{ justifyContent: "flex-end" }}>
                {onCancel && <Button onClick={onCancel}>{t("books.cancel")}</Button>}
                <Button type="submit" variant="contained" disabled={submitting}>
                  {submitting ? "..." : t("books.save")}
                </Button>
              </Stack>
            </Grid>
          </Grid>
        </Box>
      </CardContent>
    </Card>
  );
}
