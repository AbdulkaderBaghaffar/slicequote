ALTER TABLE upload ADD COLUMN guest_key varchar(64);

CREATE INDEX idx_upload_guest_key ON upload(guest_key);
