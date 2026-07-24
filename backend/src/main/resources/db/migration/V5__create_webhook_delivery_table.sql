CREATE TABLE webhook_delivery (
    delivery_id VARCHAR(36) PRIMARY KEY,
    event       VARCHAR(64) NOT NULL,
    action      VARCHAR(64) NOT NULL,
    payload     JSONB NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index to quickly query deliveries by event type if needed later
CREATE INDEX idx_webhook_delivery_event ON webhook_delivery(event, action);
