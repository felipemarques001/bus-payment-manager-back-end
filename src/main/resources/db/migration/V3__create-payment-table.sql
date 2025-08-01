CREATE TABLE IF NOT EXISTS TB_PAYMENT (
    id UUID PRIMARY KEY,
    invoice_month VARCHAR(9) NOT NULL,
    invoice_year VARCHAR(4) NOT NULL,
    total_amount NUMERIC(8, 2) NOT NULL,
    total_to_be_paid NUMERIC(8, 2) NOT NULL,
    tuition_amount NUMERIC(7, 2) NOT NULL
);