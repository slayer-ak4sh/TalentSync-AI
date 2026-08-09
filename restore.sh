#!/bin/bash
# Usage: ./restore.sh backups/resumematcher_20260809_143000.sql resumematcher

FILE=$1
DB=$2

if [ -z "$FILE" ] || [ -z "$DB" ]; then
  echo "Usage: ./restore.sh <backup-file> <database-name>"
  exit 1
fi

cat "$FILE" | kubectl exec -i -n resume-matcher postgres-0 -- psql -U postgres -d "$DB"
echo "Restored $DB from $FILE"