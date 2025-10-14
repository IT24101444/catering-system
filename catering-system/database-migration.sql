-- Migration: add address to employees table if not exists
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address VARCHAR(500);

-- Ensure employee_code is unique and not null by generating codes for rows without it
-- Note: This is a simplistic placeholder; real systems would use a function or script.
-- UPDATE employees SET employee_code = CONCAT('EMP', UPPER(LEFT(COALESCE(first_name,''),1)), UPPER(LEFT(COALESCE(last_name,''),1)), LPAD(id,5,'0')) WHERE employee_code IS NULL OR employee_code = '';
-- Database migration script to fix column names
-- Run this script to fix the drivers table structure

-- Check if password_hash column exists, if not rename password to password_hash
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'password')
BEGIN
    -- Rename password column to password_hash
    EXEC sp_rename 'drivers.password', 'password_hash', 'COLUMN';
    PRINT 'Renamed password column to password_hash';
END
ELSE IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'password_hash')
BEGIN
    -- Add password_hash column if neither exists
    ALTER TABLE drivers ADD password_hash VARCHAR(255) NOT NULL DEFAULT '';
    PRINT 'Added password_hash column';
END
ELSE
BEGIN
    PRINT 'password_hash column already exists';
END

-- Check if status column exists, if not add it
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'status')
BEGIN
    ALTER TABLE drivers ADD status VARCHAR(40) NOT NULL DEFAULT 'AVAILABLE';
    PRINT 'Added status column';
END
ELSE
BEGIN
    PRINT 'status column already exists';
END

-- Check if name column exists, if not add it
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'name')
BEGIN
    ALTER TABLE drivers ADD name VARCHAR(120) NOT NULL DEFAULT '';
    PRINT 'Added name column';
END
ELSE
BEGIN
    PRINT 'name column already exists';
END

-- Check if email column exists, if not add it
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'email')
BEGIN
    ALTER TABLE drivers ADD email VARCHAR(320) NOT NULL DEFAULT '';
    PRINT 'Added email column';
END
ELSE
BEGIN
    PRINT 'email column already exists';
END

-- Check if driver_id column exists, if not add it
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'drivers' AND COLUMN_NAME = 'driver_id')
BEGIN
    ALTER TABLE drivers ADD driver_id BIGINT IDENTITY(1,1) PRIMARY KEY;
    PRINT 'Added driver_id column';
END
ELSE
BEGIN
    PRINT 'driver_id column already exists';
END

-- Show current table structure
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'drivers'
ORDER BY ORDINAL_POSITION;
