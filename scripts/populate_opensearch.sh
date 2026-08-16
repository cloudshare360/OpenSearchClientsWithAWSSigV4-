#!/bin/bash
# Populate OpenSearch with employee data from PostgreSQL
set -e

OPENSEARCH_HOST="${OPENSEARCH_HOST:-localhost}"
OPENSEARCH_PORT="${OPENSEARCH_PORT:-9200}"
INDEX_NAME="employees"
BULK_SIZE=500

echo "Starting OpenSearch data population..."
echo "OpenSearch Host: $OPENSEARCH_HOST:$OPENSEARCH_PORT"

# Wait for OpenSearch to be ready
echo "Waiting for OpenSearch to be ready..."
for i in {1..30}; do
    if curl -s "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/_cluster/health" > /dev/null; then
        echo "OpenSearch is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "OpenSearch did not become ready in time"
        exit 1
    fi
    sleep 2
done

# Create index with mappings if it doesn't exist
echo "Creating index: $INDEX_NAME"
curl -s -X PUT "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/$INDEX_NAME" \
  -H "Content-Type: application/json" \
  -d '{
    "settings": {
      "index": {
        "number_of_shards": 1,
        "number_of_replicas": 0,
        "analysis": {
          "analyzer": {
            "default": {
              "type": "standard"
            }
          }
        }
      }
    },
    "mappings": {
      "properties": {
        "id": { "type": "long" },
        "firstName": { "type": "text", "analyzer": "standard" },
        "lastName": { "type": "text", "analyzer": "standard" },
        "email": { "type": "keyword" },
        "department": { "type": "keyword" },
        "position": { "type": "text", "analyzer": "standard" },
        "salary": { "type": "double" },
        "hireDate": { "type": "date", "format": "yyyy-MM-dd" },
        "fullText": {
          "type": "text",
          "analyzer": "standard"
        }
      }
    }
  }' || true

# Fetch data from PostgreSQL and index into OpenSearch
echo "Fetching employees from PostgreSQL and indexing into OpenSearch..."

psql -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -A -F'|' -c \
  "SELECT id, first_name, last_name, email, department, position, salary, hire_date FROM employees WHERE is_active = true ORDER BY id;" | \
  while IFS='|' read -r id first_name last_name email department position salary hire_date; do
    
    # Create bulk request line
    action="{\"index\":{\"_index\":\"$INDEX_NAME\",\"_id\":\"$id\"}}"
    
    # Escape quotes in values
    first_name=$(echo "$first_name" | sed 's/"/\\"/g')
    last_name=$(echo "$last_name" | sed 's/"/\\"/g')
    email=$(echo "$email" | sed 's/"/\\"/g')
    department=$(echo "$department" | sed 's/"/\\"/g')
    position=$(echo "$position" | sed 's/"/\\"/g')
    
    doc="{\"id\":$id,\"firstName\":\"$first_name\",\"lastName\":\"$last_name\",\"email\":\"$email\",\"department\":\"$department\",\"position\":\"$position\",\"salary\":$salary,\"hireDate\":\"$hire_date\",\"fullText\":\"$first_name $last_name $email $position\"}"
    
    # Add to bulk buffer
    echo "$action" >> /tmp/bulk_$$.json
    echo "$doc" >> /tmp/bulk_$$.json
    
    # Send bulk request when buffer is full
    if [ $(wc -l < /tmp/bulk_$$.json) -ge $((BULK_SIZE * 2)) ]; then
        curl -s -X POST "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/_bulk" \
          -H "Content-Type: application/json" \
          --data-binary @/tmp/bulk_$$.json > /dev/null
        rm /tmp/bulk_$$.json
    fi
  done

# Send remaining items
if [ -f /tmp/bulk_$$.json ]; then
    curl -s -X POST "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/_bulk" \
      -H "Content-Type: application/json" \
      --data-binary @/tmp/bulk_$$.json > /dev/null
    rm /tmp/bulk_$$.json
fi

# Refresh index
curl -s -X POST "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/$INDEX_NAME/_refresh"

# Verify document count
echo "Verifying document count..."
DOC_COUNT=$(curl -s "http://$OPENSEARCH_HOST:$OPENSEARCH_PORT/$INDEX_NAME/_count" | grep -o '"count":[0-9]*' | cut -d':' -f2)
echo "Total documents in index: $DOC_COUNT"

if [ "$DOC_COUNT" -gt 0 ]; then
    echo "OpenSearch data population completed successfully!"
else
    echo "WARNING: No documents found in index!"
fi
