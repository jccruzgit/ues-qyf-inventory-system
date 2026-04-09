ALTER TABLE inventory_alerts
    DROP CONSTRAINT IF EXISTS chk_inventory_alerts_type;

ALTER TABLE inventory_alerts
    ADD CONSTRAINT chk_inventory_alerts_type
        CHECK (alert_type IN ('LOW_STOCK', 'EXPIRING_BATCH', 'EXPIRED_BATCH'));
