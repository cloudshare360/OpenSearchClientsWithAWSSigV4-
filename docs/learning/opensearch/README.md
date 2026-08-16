# OpenSearch Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn OpenSearch for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is OpenSearch?

OpenSearch is an open-source search and analytics engine. It allows you to store, search, and analyze large volumes of data quickly. In this project, we use it for full-text employee search.

### Key Concepts

1. **Index** - A collection of documents (like a database table)
2. **Document** - A JSON object (like a database row)
3. **Mapping** - Schema definition for documents
4. **Analyzer** - Processes text for search
5. **Query DSL** - JSON-based search language

---

## Core Concepts

### Index
An index is a collection of documents. In our project:
- Index name: `employees`
- Contains employee documents
- Has specific mappings for each field

### Document
A document is a JSON object. Example employee document:
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "department": "Engineering",
  "position": "Software Engineer",
  "salary": 100000,
  "hireDate": "2024-01-15",
  "fullText": "John Doe john.doe@example.com Software Engineer"
}
```

### Mapping
Mapping defines the schema for documents:
```json
{
  "mappings": {
    "properties": {
      "firstName": { "type": "text", "analyzer": "standard" },
      "email": { "type": "keyword" },
      "salary": { "type": "double" }
    }
  }
}
```

---

## Search Types in This Project

### 1. Full-Text Search
Search across multiple text fields:

```json
GET /employees/_search
{
  "query": {
    "multi_match": {
      "query": "john engineer",
      "fields": ["firstName", "lastName", "position", "fullText"]
    }
  }
}
```

### 2. Term Search
Exact match on keyword fields:

```json
GET /employees/_search
{
  "query": {
    "term": {
      "department": "Engineering"
    }
  }
}
```

### 3. Range Query
Search within a range:

```json
GET /employees/_search
{
  "query": {
    "range": {
      "salary": {
        "gte": 50000,
        "lte": 150000
      }
    }
  }
}
```

---

## Indexing Data

### Single Document Index
```json
PUT /employees/_doc/1
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com"
}
```

### Bulk Indexing
```json
POST /employees/_bulk
{ "index": { "_id": "1" } }
{ "firstName": "John", "lastName": "Doe" }
{ "index": { "_id": "2" } }
{ "firstName": "Jane", "lastName": "Smith" }
```

---

## Java Client Usage

### Basic Search
```java
SearchRequest request = SearchRequest.of(s -> s
    .index("employees")
    .query(q -> q
        .multiMatch(m -> m
            .query("john")
            .fields("firstName", "lastName", "email", "position")
        )
    )
);

SearchResponse<Map> response = client.search(request, Map.class);
```

### Index Document
```java
IndexRequest request = IndexRequest.of(i -> i
    .index("employees")
    .id("1")
    .document(employeeMap)
);

IndexResponse response = client.index(request);
```

### Bulk Index
```java
BulkRequest request = BulkRequest.of(b -> b
    .operations(op -> op
        .index(i -> i.index("employees").id("1").document(doc1))
    )
    .operations(op -> op
        .index(i -> i.index("employees").id("2").document(doc2))
    )
);

BulkResponse response = client.bulk(request);
```

---

## Project-Specific Configuration

### Index Creation
```bash
PUT /employees
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "analysis": {
      "analyzer": {
        "default": {
          "type": "standard"
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
      "hireDate": { "type": "date" },
      "fullText": { "type": "text", "analyzer": "standard" }
    }
  }
}
```

---

## Common Operations

### Check Cluster Health
```bash
GET /_cluster/health
```

### Get Index Stats
```bash
GET /employees/_stats
```

### Count Documents
```bash
GET /employees/_count
```

### Delete Index
```bash
DELETE /employees
```

---

## Best Practices

1. **Use Keyword for Exact Match:** Email, department, ID
2. **Use Text for Full-Text:** Names, positions, descriptions
3. **Use Copy_to:** Combine fields for unified search
4. **Configure Shards:** Based on data volume
5. **Use Bulk API:** For multiple documents
6. **Refresh Strategy:** Control when changes are visible
7. **Connection Pooling:** Reuse HTTP clients

---

## Troubleshooting

### Connection Issues
```bash
# Check if OpenSearch is running
curl http://localhost:9200/_cluster/health

# Check cluster status
curl http://localhost:9200/_cluster/health?pretty=true
```

### Query Issues
- Use `_validate/query` to test queries
- Check mapping for field types
- Use `_search` with `explain: true` for debugging

---

## Next Steps

1. Practice basic CRUD operations
2. Learn advanced search queries
3. Understand mapping configurations
4. Practice with the Java client
5. Study performance optimization

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
