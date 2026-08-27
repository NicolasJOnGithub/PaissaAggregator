CREATE TABLE datacenters (
    id   INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE worlds (
    id            INTEGER PRIMARY KEY,
    name          TEXT NOT NULL,
    datacenter_id INTEGER NOT NULL REFERENCES datacenters (id)
);

CREATE INDEX idx_worlds_datacenter_id ON worlds (datacenter_id);

CREATE TABLE districts (
    id   INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE wards (
    id          BIGSERIAL PRIMARY KEY,
    world_id    INTEGER NOT NULL REFERENCES worlds (id),
    district_id INTEGER NOT NULL REFERENCES districts (id),
    ward_number INTEGER NOT NULL,
    UNIQUE (world_id, district_id, ward_number)
);

CREATE INDEX idx_wards_world_id ON wards (world_id);
CREATE INDEX idx_wards_district_id ON wards (district_id);

CREATE TABLE plots (
    id                 BIGSERIAL PRIMARY KEY,
    ward_id            BIGINT NOT NULL REFERENCES wards (id) ON DELETE CASCADE,
    plot_number        INTEGER NOT NULL,
    size               INTEGER NOT NULL,
    price              BIGINT NOT NULL,
    purchase_system    INTEGER NOT NULL,
    lotto_entries      INTEGER,
    lotto_phase        INTEGER,
    lotto_phase_until  BIGINT,
    first_seen_time    DOUBLE PRECISION NOT NULL,
    last_updated_time  DOUBLE PRECISION NOT NULL,
    UNIQUE (ward_id, plot_number)
);

CREATE INDEX idx_plots_ward_id ON plots (ward_id);
CREATE INDEX idx_plots_size_purchase ON plots (size, purchase_system);

CREATE TABLE refresh_status (
    id              INTEGER PRIMARY KEY DEFAULT 1,
    last_started_at   TIMESTAMPTZ,
    last_completed_at TIMESTAMPTZ,
    in_progress       BOOLEAN NOT NULL DEFAULT FALSE,
    worlds_synced     INTEGER,
    last_error        TEXT,
    CONSTRAINT single_row CHECK (id = 1)
);

INSERT INTO refresh_status (id, in_progress) VALUES (1, FALSE);
