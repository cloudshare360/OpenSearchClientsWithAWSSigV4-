#!/usr/bin/env python3
"""
AWS Setup Script for LocalStack
Creates IAM roles, policies, Secrets Manager secrets, and OpenSearch role mappings
"""

import os
import sys
import json
import time
import boto3
from botocore.exceptions import ClientError

# Configuration
LOCALSTACK_ENDPOINT = os.environ.get('LOCALSTACK_ENDPOINT', 'http://localhost:4566')
AWS_REGION = os.environ.get('AWS_DEFAULT_REGION', 'us-east-1')
OPENSEARCH_ENDPOINT = os.environ.get('OPENSEARCH_ENDPOINT', 'http://localhost:9200')
SECRET_NAME = 'opensearch/credentials'
IAM_ROLE_NAME = 'opensearch-role'
IAM_POLICY_NAME = 'opensearch-access-policy'

# Initialize boto3 clients for LocalStack
session = boto3.Session(
    aws_access_key_id='test',
    aws_secret_access_key='test',
    region_name=AWS_REGION
)

iam_client = session.client('iam', endpoint_url=LOCALSTACK_ENDPOINT)
secrets_client = session.client('secretsmanager', endpoint_url=LOCALSTACK_ENDPOINT)
sts_client = session.client('sts', endpoint_url=LOCALSTACK_ENDPOINT)

def create_iam_role():
    """Create IAM role for OpenSearch access"""
    print(f"Creating IAM role: {IAM_ROLE_NAME}")
    
    trust_policy = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "Service": "opensearch.amazonaws.com"
                },
                "Action": "sts:AssumeRole"
            }
        ]
    }
    
    try:
        response = iam_client.create_role(
            RoleName=IAM_ROLE_NAME,
            AssumeRolePolicyDocument=json.dumps(trust_policy),
            Description="Role for OpenSearch access with SigV4 signing"
        )
        print(f"✓ Created IAM role: {response['Role']['Arn']}")
        return response['Role']['Arn']
    except ClientError as e:
        if 'EntityAlreadyExists' in str(e):
            print(f"✓ IAM role already exists")
            role = iam_client.get_role(RoleName=IAM_ROLE_NAME)
            return role['Role']['Arn']
        else:
            print(f"✗ Error creating IAM role: {e}")
            sys.exit(1)

def create_iam_policy(role_arn):
    """Create IAM policy for OpenSearch access"""
    print(f"Creating IAM policy: {IAM_POLICY_NAME}")
    
    policy_document = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "AWS": role_arn
                },
                "Action": [
                    "es:ESHttpGet",
                    "es:ESHttpPost",
                    "es:ESHttpPut",
                    "es:ESHttpDelete",
                    "es:ESHttpHead"
                ],
                "Resource": "arn:aws:es:us-east-1:000000000000:domain/opensearch-node/employees/*"
            }
        ]
    }
    
    try:
        response = iam_client.create_policy(
            PolicyName=IAM_POLICY_NAME,
            PolicyDocument=json.dumps(policy_document),
            Description="Policy for OpenSearch employee index access"
        )
        print(f"✓ Created IAM policy: {response['Policy']['Arn']}")
        return response['Policy']['Arn']
    except ClientError as e:
        if 'EntityAlreadyExists' in str(e):
            print(f"✓ IAM policy already exists")
            account_id = sts_client.get_caller_identity()['Account']
            policy_arn = f"arn:aws:iam::{account_id}:policy/{IAM_POLICY_NAME}"
            return policy_arn
        else:
            print(f"✗ Error creating IAM policy: {e}")
            sys.exit(1)

def attach_policy_to_role(role_name, policy_arn):
    """Attach policy to IAM role"""
    print(f"Attaching policy to role...")
    try:
        iam_client.attach_role_policy(
            RoleName=role_name,
            PolicyArn=policy_arn
        )
        print(f"✓ Policy attached to role")
    except ClientError as e:
        print(f"✗ Error attaching policy: {e}")
        sys.exit(1)

def create_secrets_manager_secret():
    """Create Secrets Manager secret with OpenSearch credentials"""
    print(f"Creating Secrets Manager secret: {SECRET_NAME}")
    
    secret_value = {
        "accessKey": "test",
        "secretKey": "test",
        "sessionToken": None,
        "opensearchEndpoint": OPENSEARCH_ENDPOINT,
        "indexName": "employees"
    }
    
    try:
        response = secrets_client.create_secret(
            Name=SECRET_NAME,
            SecretString=json.dumps(secret_value),
            Description="OpenSearch credentials for SigV4 authentication"
        )
        print(f"✓ Created secret: {response['ARN']}")
        return response['ARN']
    except ClientError as e:
        if 'ResourceExistsException' in str(e):
            print(f"✓ Secret already exists, updating...")
            response = secrets_client.update_secret(
                SecretId=SECRET_NAME,
                SecretString=json.dumps(secret_value)
            )
            print(f"✓ Updated secret: {response['ARN']}")
            return response['ARN']
        else:
            print(f"✗ Error creating secret: {e}")
            sys.exit(1)

def configure_opensearch_role_mappings():
    """Configure OpenSearch role mappings via security API"""
    print("Configuring OpenSearch role mappings...")
    
    import requests
    
    # Wait for OpenSearch to be ready
    for i in range(30):
        try:
            response = requests.get(f"{OPENSEARCH_ENDPOINT}/_cluster/health", timeout=5)
            if response.status_code == 200:
                break
        except:
            pass
        time.sleep(2)
    
    # Configure role mappings
    role_mappings = {
        "admin": {
            "backend_roles": ["admin"],
            "users": ["*"],
            "admin_roles": ["admin"]
        },
        "employee-reader": {
            "backend_roles": ["employee-reader"],
            "users": ["*"],
            "index_permissions": [
                {
                    "index_patterns": ["employees"],
                    "allowed_actions": [
                        "indices:data/read/search",
                        "indices:data/read/get"
                    ]
                }
            ]
        },
        "employee-writer": {
            "backend_roles": ["employee-writer"],
            "users": ["*"],
            "index_permissions": [
                {
                    "index_patterns": ["employees"],
                    "allowed_actions": [
                        "indices:data/write/index",
                        "indices:data/write/bulk",
                        "indices:data/write/delete"
                    ]
                }
            ]
        }
    }
    
    # Note: In a real environment, this would use proper authentication
    # For LocalStack/development, we're using no authentication
    try:
        for role_name, mapping in role_mappings.items():
            response = requests.put(
                f"{OPENSEARCH_ENDPOINT}/_plugins/_security/api/rolesmapping/{role_name}",
                json=mapping,
                headers={"Content-Type": "application/json"}
            )
            if response.status_code in [200, 201]:
                print(f"✓ Configured role mapping: {role_name}")
            else:
                print(f"⚠ Failed to configure role mapping {role_name}: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"⚠ Error configuring role mappings: {e}")
        print("  This is expected for local development without security plugin")

def main():
    print("=" * 60)
    print("AWS Setup for LocalStack")
    print("=" * 60)
    
    # Step 1: Create IAM Role
    role_arn = create_iam_role()
    
    # Step 2: Create IAM Policy
    policy_arn = create_iam_policy(role_arn)
    
    # Step 3: Attach policy to role
    attach_policy_to_role(IAM_ROLE_NAME, policy_arn)
    
    # Step 4: Create Secrets Manager secret
    secret_arn = create_secrets_manager_secret()
    
    # Step 5: Configure OpenSearch role mappings
    configure_opensearch_role_mappings()
    
    print("=" * 60)
    print("✓ AWS Setup completed successfully!")
    print("=" * 60)
    print(f"IAM Role ARN: {role_arn}")
    print(f"Policy ARN: {policy_arn}")
    print(f"Secret ARN: {secret_arn}")
    print(f"OpenSearch Endpoint: {OPENSEARCH_ENDPOINT}")
    print("=" * 60)

if __name__ == '__main__':
    main()
