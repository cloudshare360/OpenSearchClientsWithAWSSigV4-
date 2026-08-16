# AWS SigV4 Support for OpenSearch Clients

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Authentication Flow](#authentication-flow)
4. [Language-Specific Implementations](#language-specific-implementations)
   - [Java](#java)
   - [Python](#python)
   - [JavaScript (Node.js)](#javascript-nodejs)
   - [Ruby](#ruby)
   - [.NET](#net)
   - [Rust](#rust)
   - [PHP](#php)
   - [Go](#go)
5. [Security Considerations](#security-considerations)
6. [Troubleshooting](#troubleshooting)
7. [Best Practices](#best-practices)

---

## Overview

AWS Signature Version 4 (SigV4) is the AWS authentication protocol for signing API requests. OpenSearch clients now natively support SigV4 signing, eliminating the need for workarounds when accessing Amazon OpenSearch Service with fine-grained access controls.

### Key Benefits
- **Native Client Support**: Sign requests directly through client APIs
- **Fine-Grained Access Control**: Works with IAM roles/users
- **No Workarounds**: Eliminates cURL requests and proxy solutions
- **Cross-Platform**: Available in all official OpenSearch clients

### Supported Clients
- Java
- Python
- JavaScript (Node.js)
- Ruby
- .NET
- Rust
- PHP
- Go

---

## Prerequisites

### 1. AWS Credentials
Ensure you have AWS credentials configured on your machine:

**Option A: AWS Credentials File**
```bash
# ~/.aws/credentials
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
region = us-east-1
```

**Option B: Environment Variables**
```bash
export AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
export AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY
export AWS_DEFAULT_REGION=us-east-1
```

**Option C: IAM Role (EC2/ECS/Lambda)**
- Automatically retrieved from instance metadata

### 2. OpenSearch Domain Configuration
- Ensure your OpenSearch domain has fine-grained access control enabled
- Configure IAM role/user as the access control type
- Set up domain-level access policies with appropriate permissions

### 3. Required Permissions
The IAM role/user must have the following permissions on the OpenSearch domain:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpPost",
        "es:ESHttpPut",
        "es:ESHttpDelete"
      ],
      "Resource": "arn:aws:es:REGION:ACCOUNT_ID:domain/DOMAIN_NAME/*"
    }
  ]
}
```

---

## Authentication Flow

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Client    │─────▶│   SigV4     │─────▶│  OpenSearch │
│ Application │      │   Signer    │      │   Domain    │
└─────────────┘      └─────────────┘      └─────────────┘
       │                     │                     │
       │  1. Request         │                     │
       │────────────────────▶│                     │
       │                     │  2. Sign Request    │
       │                     │  with AWS Creds     │
       │                     │────────────────────▶│
       │                     │                     │
       │                     │  3. Signed Request  │
       │                     │────────────────────▶│
       │                     │                     │
       │                     │  4. Auth Verified   │
       │                     │◀────────────────────│
       │  5. Response        │                     │
       │◀────────────────────│                     │
```

### SigV4 Signing Process
1. **Canonical Request**: Create canonical request string
2. **String to Sign**: Create string to sign with timestamp
3. **Calculate Signature**: Derive signing key and compute signature
4. **Authorization Header**: Add `Authorization` header with signature
5. **X-Amz-Date**: Include ISO8601 timestamp
6. **X-Amz-Security-Token**: Include session token if using temporary credentials

---

## Language-Specific Implementations

### Java

#### Dependencies (Maven)
```xml
<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-java</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sdk-http</artifactId>
    <version>2.24.0</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>apache-client</artifactId>
    <version>2.24.0</version>
</dependency>
```

#### Implementation
```java
import java.io.IOException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

public class OpenSearchSigV4Example {
    public static void main(final String[] args) throws IOException {
        SdkHttpClient httpClient = ApacheHttpClient.builder().build();
        try {
            OpenSearchClient client = new OpenSearchClient(
                new AwsSdk2Transport(
                    httpClient,
                    "search-xxx.region.es.amazonaws.com",
                    Region.US_WEST_2,
                    AwsSdk2TransportOptions.builder().build()
                )
            );

            InfoResponse info = client.info();
            System.out.println(info.version().distribution() + ": " + info.version().number());
        } finally {
            httpClient.close();
        }
    }
}
```

---

### Python

#### Dependencies
```bash
pip install opensearch-py boto3
```

#### Implementation
```python
from urllib.parse import urlparse
from boto3 import Session
from opensearchpy import AWSV4SignerAuth, OpenSearch, RequestsHttpConnection

url = urlparse("https://search-xxx.region.es.amazonaws.com")
region = 'us-east-1'
credentials = Session().get_credentials()

auth = AWSV4SignerAuth(credentials, region)

client = OpenSearch(
    hosts=[{
        'host': url.netloc,
        'port': url.port or 443
    }],
    http_auth=auth,
    use_ssl=True,
    verify_certs=True,
    connection_class=RequestsHttpConnection
)

info = client.info()
print(f"{info['version']['distribution']}: {info['version']['number']}")
```

---

### JavaScript (Node.js)

#### AWS SDK V2
```javascript
const AWS = require('aws-sdk');
const { Client } = require('@opensearch-project/opensearch');
const { AwsSigv4Signer } = require('@opensearch-project/opensearch/aws');

const client = new Client({
    ...AwsSigv4Signer({
        region: 'us-east-1',
        getCredentials: () =>
            new Promise((resolve, reject) => {
                AWS.config.getCredentials((err, credentials) => {
                    if (err) {
                        reject(err);
                    } else {
                        resolve(credentials);
                    }
                });
            }),
    }),
    node: "https://search-xxx.region.es.amazonaws.com"
});
```

#### AWS SDK V3
```javascript
const { defaultProvider } = require("@aws-sdk/credential-provider-node");
const { Client } = require('@opensearch-project/opensearch');
const { AwsSigv4Signer } = require('@opensearch-project/opensearch/aws');

async function main() {
    const client = new Client({
        ...AwsSigv4Signer({
            region: "us-east-1",
            getCredentials: () => {
                const credentialsProvider = defaultProvider();
                return credentialsProvider();
            },
        }),
        node: "https://search-xxx.region.es.amazonaws.com"
    });

    var info = await client.info();
    var version = info.body.version
    console.log(version.distribution + ": " + version.number);
}

main();
```

---

### Ruby

#### Gem Installation
```bash
gem install opensearch-aws-sigv4 aws-sigv4
```

#### Implementation
```ruby
require 'opensearch-aws-sigv4'
require 'aws-sigv4'

signer = Aws::Sigv4::Signer.new(
  service: 'es',
  region: 'us-east-1',
  access_key_id: '...',
  secret_access_key: '...',
  session_token: '...'
)

client = OpenSearch::Aws::Sigv4Client.new({
    host: "https://search-xxx.region.es.amazonaws.com",
    log: false
}, signer)

info = client.info
puts info['version']['distribution'] + ': ' + info['version']['number']
```

---

### .NET

#### Dependencies (PackageReference)
```xml
<PackageReference Include="OpenSearch.Net" Version="2.6.0" />
<PackageReference Include="OpenSearch.Client" Version="2.6.0" />
<PackageReference Include="AWSSDK.SecurityToken" Version="3.7.0" />
```

#### Implementation
```csharp
using OpenSearch.Client;
using OpenSearch.Net.Auth.AwsSigV4;

namespace Application
{
    class Program
    {
        static void Main(string[] args)
        {
            var endpoint = new Uri("https://search-xxx.region.es.amazonaws.com");
            var connection = new AwsSigV4HttpConnection();
            var config = new ConnectionSettings(endpoint, connection);
            var client = new OpenSearchClient(config);

            Console.WriteLine($"{client.RootNodeInfo().Version.Distribution}: {client.RootNodeInfo().Version.Number}");
        }
    }
}
```

---

### Rust

#### Dependencies (Cargo.toml)
```toml
[dependencies]
opensearch = { version = "2.6.0", features = ["aws-auth"] }
aws-config = "1.5.0"
tokio = { version = "1.0", features = ["full"] }
```

#### Implementation
```rust
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    use std::{convert::TryInto, env, thread, time};
    use serde_json::Value;
    use opensearch::{
        http::transport::{SingleNodeConnectionPool, TransportBuilder},
        OpenSearch,
    };
    use url::Url;

    let url = Url::parse("https://search-xxx.region.es.amazonaws.com");
    let conn_pool = SingleNodeConnectionPool::new(url?);
    let aws_config = aws_config::load_from_env().await.clone();
    let transport = TransportBuilder::new(conn_pool)
        .auth(aws_config.clone().try_into()?)
        .build()?;
    let client = OpenSearch::new(transport);

    let info: Value = client.info().send().await?.json().await?;
    println!(
        "{}: {}",
        info["version"]["distribution"].as_str().unwrap(),
        info["version"]["number"].as_str().unwrap()
    );

    Ok(())
}
```

---

### PHP

#### Dependencies (composer.json)
```json
{
    "require": {
        "opensearch-project/opensearch-php": "^2.6"
    }
}
```

#### Implementation
```php
<?php

require_once __DIR__ . '/vendor/autoload.php';

$client = (new \OpenSearch\ClientBuilder())
  ->setHosts(["https://search-xxx.region.es.amazonaws.com"])
  ->setSigV4Region("us-east-1")
  ->setSigV4CredentialProvider(true)
  ->build();

$info = $client->info();

echo "{$info['version']['distribution']}: {$info['version']['number']}\n";
```

---

### Go

#### Dependencies (go.mod)
```go
module github.com/your-org/opensearch-sigv4

go 1.22

require (
    github.com/aws/aws-sdk-go-v2/config v1.24.0
    github.com/opensearch-project/opensearch-go/v2 v2.6.0
)
```

#### Implementation
```go
package main

import (
    "context"
    "encoding/json"
    "fmt"
    "log"
    "strings"

    "github.com/aws/aws-sdk-go-v2/config"
    "github.com/opensearch-project/opensearch-go/v2"
    requestsigner "github.com/opensearch-project/opensearch-go/v2/signer/awsv2"
)

func main() {
    ctx := context.Background()
    cfg, _ := config.LoadDefaultConfig(ctx)
    signer, _ := requestsigner.NewSigner(cfg)

    endpoint := "https://search-xxx.region.es.amazonaws.com"

    client, _ := opensearch.NewClient(opensearch.Config{
        Addresses: []string{endpoint},
        Signer:    signer,
    })

    if info, err := client.Info(); err != nil {
        log.Fatal("info", err)
    } else {
        var r map[string]interface{}
        json.NewDecoder(info.Body).Decode(&r)
        version := r["version"].(map[string]interface{})
        fmt.Printf("%s: %s\n", version["distribution"], version["number"])
    }
}
```

---

## Security Considerations

### 1. Credential Storage
- **Never hardcode credentials** in source code
- Use environment variables or AWS credential files
- Consider AWS Secrets Manager for production deployments
- Use IAM roles when running on AWS infrastructure (EC2, ECS, Lambda)

### 2. Network Security
- Always use HTTPS (`https://`) for OpenSearch endpoints
- Verify SSL certificates (`verify_certs=True` in Python)
- Configure proper TLS/SSL settings in OpenSearch

### 3. IAM Policy Best Practices
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "DenyUnsecuredAccess",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "es:*",
      "Resource": "arn:aws:es:REGION:ACCOUNT:domain/DOMAIN/*",
      "Condition": {
        "Bool": {
          "aws:SecureTransport": "false"
        }
      }
    },
    {
      "Sid": "AllowSpecificActions",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT:role/ROLE_NAME"
      },
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpPost",
        "es:ESHttpPut",
        "es:ESHttpDelete"
      ],
      "Resource": "arn:aws:es:REGION:ACCOUNT:domain/DOMAIN/*"
    }
  ]
}
```

### 4. Fine-Grained Access Control
- Use **IAM roles/users** (not master user) for production
- Map IAM roles to OpenSearch backend roles
- Configure index-level permissions
- Enable audit logging for security events

---

## Troubleshooting

### Common Issues

#### 1. Signature Mismatch
**Symptoms**: `403 Forbidden` or `SignatureDoesNotMatch` error

**Solutions**:
- Ensure system clock is synchronized (AWS SigV4 uses timestamps)
- Verify AWS credentials are correctly configured
- Check that the region matches the OpenSearch domain region
- Ensure the service name is `es` (not `opensearch`)

#### 2. SSL Certificate Verification Failed
**Symptoms**: `SSL: CERTIFICATE_VERIFY_FAILED`

**Solutions**:
- For local development with self-signed certificates:
  ```python
  # Python - disable verification (NOT for production)
  client = OpenSearch(..., verify_certs=False, ssl_show_warn=False)
  ```
- Properly configure OpenSearch security plugin with valid certificates

#### 3. Access Denied
**Symptoms**: `AccessDeniedException` or `403 Forbidden`

**Solutions**:
- Verify IAM policy attached to role/user
- Check OpenSearch domain access policy
- Ensure IAM role is mapped in OpenSearch security configuration
- Verify the IAM principal has correct permissions

#### 4. Connection Timeout
**Symptoms**: Connection timeout errors

**Solutions**:
- Check network connectivity to OpenSearch endpoint
- Verify security group rules (for AWS-hosted OpenSearch)
- Ensure OpenSearch domain is in `Available` state
- Check firewall/proxy settings

### Debug Logging

Enable debug logging to troubleshoot SigV4 signing:

**Java**:
```properties
logging.level.org.opensearch.client=DEBUG
logging.level.software.amazon.awssdk=DEBUG
```

**Python**:
```python
import logging
logging.basicConfig(level=logging.DEBUG)
```

**JavaScript**:
```javascript
process.env.DEBUG = '@opensearch-project/opensearch:*'
```

---

## Best Practices

### 1. Credential Management
- Use IAM roles instead of access keys when possible
- Rotate credentials regularly
- Use least privilege principle for IAM policies
- Never commit credentials to version control

### 2. Connection Pooling
- Reuse HTTP clients across requests
- Configure appropriate connection pool sizes
- Implement health checks for connection validation

### 3. Error Handling
- Implement retry logic with exponential backoff
- Handle credential refresh for temporary credentials
- Log authentication failures securely (without logging credentials)

### 4. Performance Optimization
- Use bulk operations for indexing multiple documents
- Enable connection pooling
- Configure appropriate timeouts
- Use keep-alive for persistent connections

### 5. Monitoring
- Monitor OpenSearch request metrics
- Track authentication failures
- Log SigV4 signing operations for audit trails
- Set up alerts for access denied events

---

## Additional Resources

- [AWS SigV4 Documentation](https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html)
- [OpenSearch Documentation](https://opensearch.org/docs/)
- [OpenSearch Security Plugin](https://opensearch.org/docs/latest/security/index/)
- [AWS SDK Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [OpenSearch Clients GitHub](https://github.com/opensearch-project/opensearch-clients)

---

*Last Updated: 2026-08-16*
