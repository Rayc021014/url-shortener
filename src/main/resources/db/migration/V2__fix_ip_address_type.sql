-- V2__fix_ip_address_type.sql
-- Change ip_address from PostgreSQL's native INET type to VARCHAR(45)
-- so that JPA/Hibernate can bind plain Java String values without a cast error.
-- VARCHAR(45) is sufficient for both IPv4 (15 chars) and IPv6 (39 chars).

ALTER TABLE clicks
    ALTER COLUMN ip_address TYPE VARCHAR(45)
    USING ip_address::VARCHAR;
