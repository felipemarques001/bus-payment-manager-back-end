CREATE TABLE IF NOT EXISTS TB_TUITION (
    id UUID PRIMARY KEY,
    payment_id UUID REFERENCES TB_PAYMENT(id) ON DELETE CASCADE,
    student_id UUID REFERENCES TB_STUDENT(id),
    payment_type VARCHAR(10),
    is_paid BOOLEAN NOT NULL,
    paid_at TIMESTAMP,
    CONSTRAINT payment_type_required_if_is_paid CHECK (NOT(is_paid = true AND payment_type IS NULL)),
    CONSTRAINT paid_at_required_if_is_paid CHECK (NOT(is_paid = true AND paid_at IS NULL))
);
