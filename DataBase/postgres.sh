#!/bin/bash

#Variables
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
DB_NAME="bombapetit"
USER="postgres"

USER1="kali"
USER_PASSWORD1="kali"

POPULATE_SQL_SCRIPT="$SCRIPT_DIR/BombApetit.sql"
QUERIES_SQL_SCRIPT="$SCRIPT_DIR/queries.sql"

#Change to a directory accessible by the postgres user
cd /tmp || exit

#Terminate existing connections to the database
echo "Terminating existing connections to the database..."
psql -U postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '$DB_NAME' AND pid <> pg_backend_pid();"

echo "Configuring user $USER..."
psql -U postgres -c "DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$USER1') THEN
        CREATE USER $USER1 WITH PASSWORD '$USER_PASSWORD1';
        ALTER USER $USER1 CREATEDB;
    END IF;
END\$\$;"


#Drop the existing database (if it exists) and create a new one
echo "Dropping existing database (if it exists) and creating a new one..."
PGPASSWORD=$USER_PASSWORD1 psql -d template1 -U $USER1 -c "DROP DATABASE IF EXISTS $DB_NAME;"
PGPASSWORD=$USER_PASSWORD1 psql -d template1 -U $USER1 -c "CREATE DATABASE $DB_NAME;"

#psql -U postgres -c "GRANT SELECT, UPDATE, INSERT, DELETE ON  $DB_NAME TO $USER1;"


#Create the uuid-ossp extension as superuser
echo "Creating uuid-ossp extension..."
psql -U postgres -d $DB_NAME -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

#Populate the new database using the SQL script
echo "Populating the database with data..."
PGPASSWORD=$USER_PASSWORD1 psql -d $DB_NAME -U $USER1 -f $POPULATE_SQL_SCRIPT
PGPASSWORD=$USER_PASSWORD1 psql -d $DB_NAME -U $USER1 -f $QUERIES_SQL_SCRIPT

#Enter the psql prompt
echo "Entering the psql prompt for the database..."
PGPASSWORD=$USER_PASSWORD1 psql -d $DB_NAME -U $USER1
