-- Initialize database with proper character set and collation
CREATE DATABASE IF NOT EXISTS data_import_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create user with proper permissions
CREATE USER IF NOT EXISTS 'import_user'@'%' IDENTIFIED BY 'import_password123';
GRANT ALL PRIVILEGES ON data_import_db.* TO 'import_user'@'%';
FLUSH PRIVILEGES;

-- Set global variables for better performance
SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB
SET GLOBAL max_connections = 200;
SET GLOBAL query_cache_size = 67108864; -- 64MB