# AWS SigV4 Documentation

This folder contains documentation for AWS SigV4 support in OpenSearch clients, sourced directly from the official OpenSearch blog.

## Files

### [raw-content.md](raw-content.md)
Exact content extracted from https://opensearch.org/blog/aws-sigv4-support-for-clients/ (Last updated: 2025-06-18). Contains:
- Overview of AWS SigV4 support for OpenSearch clients
- Fine-grained access control setup instructions
- Code examples for all supported languages:
  - Java
  - Python
  - JavaScript (AWS SDK V2 and V3)
  - Ruby
  - .NET
  - Rust
  - PHP
  - Go
- Amazon OpenSearch Serverless usage reference

### [index.md](index.md)
Structured documentation derived from the blog post, including:
- Overview and key benefits
- Prerequisites and setup instructions
- Authentication flow diagram
- Language-specific implementations with dependencies
- Security considerations
- Troubleshooting guide
- Best practices

---

## Quick Reference

| Language | Key Class/Module | Dependencies |
|----------|-----------------|--------------|
| **Java** | `AwsSdk2Transport` | opensearch-java, aws-sdk-http, apache-client |
| **Python** | `AWSV4SignerAuth` | opensearch-py, boto3 |
| **JavaScript** | `AwsSigv4Signer` | @opensearch-project/opensearch, aws-sdk |
| **Ruby** | `OpenSearch::Aws::Sigv4Client` | opensearch-aws-sigv4, aws-sigv4 |
| **.NET** | `AwsSigV4HttpConnection` | OpenSearch.Net, OpenSearch.Client |
| **Rust** | `Credentials::AwsSigV4` | opensearch (aws-auth feature), aws-config |
| **PHP** | `setSigV4Region()`, `setSigV4CredentialProvider()` | opensearch-project/opensearch-php |
| **Go** | `requestsigner.NewSigner()` | opensearch-go/v2, aws-sdk-go-v2/config |

---

## Source

- **Blog Post:** https://opensearch.org/blog/aws-sigv4-support-for-clients/
- **Authors:** Vacha Shah, Daniel Doubrovkine, Harsha Vamsi Kalluri, Theo Truong, Thomas Hurney, Thomas Farr, Monica Kugler, Matt Timmermans, Soner Sayakci

---

## Usage

1. Read [raw-content.md](raw-content.md) for the original blog content
2. Read [index.md](index.md) for structured, enhanced documentation
3. Copy code examples for your specific language
4. Ensure AWS credentials are configured before running examples

---

*Part of the OpenSearchClientsWithAWSSigV4 project*
