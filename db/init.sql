CREATE TABLE IF NOT EXISTS rules (
  id          SERIAL PRIMARY KEY,
  product_id  VARCHAR(20)     NOT NULL,
  condition   VARCHAR(10)     NOT NULL,
  threshold   NUMERIC(18, 8)  NOT NULL,
  enabled     BOOLEAN         NOT NULL DEFAULT true,
  created_at  TIMESTAMPTZ     NOT NULL DEFAULT now()
);