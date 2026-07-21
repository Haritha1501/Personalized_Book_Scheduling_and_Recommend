# ReadQuest Database Setup

This folder contains all the necessary SQL scripts to setup and seed the PostgreSQL database for **ReadQuest**.

## Files Overview
1. `schema.sql`: Initial schema namespace setup.
2. `tables.sql`: Definitions for all 15 tables.
3. `constraints.sql`: Foreign keys, checks, and unique constraints.
4. `indexes.sql`: Optimizations for faster queries.
5. `sample_data.sql`: Seed data containing roles, achievements, default users (`reader` and `admin`), and classifications.

## Execution Order
Execute the scripts in the following order using a PostgreSQL client (e.g., `psql`, pgAdmin, DBeaver):

```sql
\i schema.sql
\i tables.sql
\i constraints.sql
\i indexes.sql
\i sample_data.sql
```

## Seed User Credentials
Two users are pre-configured:
- **General User**: `reader@readquest.com` / Password: `password`
- **Admin User**: `admin@readquest.com` / Password: `password`
