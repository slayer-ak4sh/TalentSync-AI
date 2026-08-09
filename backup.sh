#!/bin/bash
mkdir -p backups
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

kubectl exec -n resume-matcher postgres-0 -- pg_dump -U postgres resumematcher > backups/resumematcher_$TIMESTAMP.sql
kubectl exec -n resume-matcher postgres-0 -- pg_dump -U postgres matchingservice > backups/matchingservice_$TIMESTAMP.sql

echo "Backup complete: backups/resumematcher_$TIMESTAMP.sql, backups/matchingservice_$TIMESTAMP.sql"