#!/usr/bin/env bash

# Simple script to build the database schema

PGUSER=
DBHOST=localhost
DBPORT=5432
DBNAME=wdp

FILES=`ls *.sql`

for f in $FILES; do
    psql -U $DBUSER -h $DBHOST -p $DBPORT $DBNAME < $f
done;

