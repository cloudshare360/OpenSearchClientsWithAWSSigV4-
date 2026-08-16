# OpenSearch Basics

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn OpenSearch fundamentals  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is OpenSearch?

OpenSearch is an open-source search and analytics engine. It helps you search, analyze, and visualize large volumes of data quickly.

### Key Concepts

1. **Index** - Collection of documents (like a database table)
2. **Document** - JSON data record (like a database row)
3. **Mapping** - Schema definition for documents
4. **Analyzer** - Processes text for search
5. **Query DSL** - JSON language for searching

---

## Core Concepts

### Index
An index is a collection of documents. Think of it like a table in a database.

```json
// Create an index
PUT /employees
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

### Document
A document is a JSON object stored in an index.

```json
// Index a document
PUT /employees/_doc/1
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "department": "Engineering"
}
```

### Mapping
Mapping defines the structure of documents.

```json
PUT /employees
{
  "mappings": {
    "properties": {
      "firstName": { "type": "text" },
      "email": { "type": "keyword" },
      "salary": { "type": "double" }
    }
  }
}
```

---

## Data Types

| Type | Use Case | Example |
|------|----------|---------|
| `text` | Full-text search | "John Doe" |
| `keyword` | Exact match, sorting | "Engineering" |
| `long` | Integers | 1, 42, 100 |
| `double` | Floating point | 75000.50 |
| `date` | Dates | "2024-01-15" |
| `boolean` | True/False | true |
| `object` | Nested objects | { "address": {...} } |

---

## Basic Operations

### Create Index
```bash
PUT /employees
```

### Index Document
```bash
PUT /employees/_doc/1
{
  "firstName": "John",
  "lastName": "Doe"
}
```

### Get Document
```bash
GET /employees/_doc/1
```

### Update Document
```bash
POST /employees/_update/1
{
  "doc": {
    "position": "Senior Engineer"
  }
}
```

### Delete Document
```bash
DELETE /employees/_doc/1
```

### Search
```bash
GET /employees/_search
{
  "query": {
    "match": {
      "firstName": "John"
    }
  }
}
```

---

## Project-Specific Setup

### Index Configuration
```json
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

## Next Steps

1. Learn [Indexing](indexing.md)
2. Study [Searching](searching.md)
3. Understand [Mappings](mappings.md)
4. Practice [Java Client](java-client.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
