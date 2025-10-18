#!/bin/bash
set -e
shopt -s nullglob

echo '=== MongoDB Data Import ==='

# Wait for MongoDB
until mongosh --username "$MONGO_INITDB_ROOT_USERNAME" \
              --password "$MONGO_INITDB_ROOT_PASSWORD" \
              --authenticationDatabase admin \
              --eval 'db.adminCommand({ping:1})'; do
    echo 'Waiting for MongoDB...'
    sleep 5
done

cd /docker-entrypoint-initdb.d/data
files=(*.json)

if [ ${#files[@]} -eq 0 ]; then
    echo "No JSON files found, skipping."
    exit 0
fi

echo "Found ${#files[@]} JSON files"
for file in "${files[@]}"; do
    collection=${file%.json}
    echo "Importing $collection..."
    mongoimport \
      --username "$MONGO_INITDB_ROOT_USERNAME" \
      --password "$MONGO_INITDB_ROOT_PASSWORD" \
      --authenticationDatabase admin \
      --db "$MONGO_INITDB_DATABASE" \
      --collection "$collection" \
      --file "$file" \
      --jsonArray \
      --drop
done

echo "✓ All imports complete!"