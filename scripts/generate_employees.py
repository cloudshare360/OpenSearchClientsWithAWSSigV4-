#!/usr/bin/env python3
"""
Generate 10,000 employee records and insert them into PostgreSQL.
This script is designed to run inside a Docker container.
"""

import os
import sys
import random
import csv
from datetime import datetime, timedelta
from faker import Faker

# Configuration from environment
POSTGRES_HOST = os.environ.get('POSTGRES_HOST', 'localhost')
POSTGRES_USER = os.environ.get('POSTGRES_USER', 'admin')
POSTGRES_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'admin123')
POSTGRES_DB = os.environ.get('POSTGRES_DB', 'employeedb')
OUTPUT_DIR = '/scripts'
NUM_EMPLOYEES = 10000

# Initialize Faker
fake = Faker()
Faker.seed(42)
random.seed(42)

def generate_employee_csv(filename, num_employees):
    """Generate employee records and save to CSV."""
    departments = ['Engineering', 'Sales', 'Marketing', 'HR', 'Finance']
    positions = {
        'Engineering': ['Software Engineer', 'Senior Engineer', 'Tech Lead', 'DevOps Engineer', 'QA Engineer', 'Architect'],
        'Sales': ['Sales Representative', 'Sales Manager', 'Account Executive', 'Sales Director'],
        'Marketing': ['Marketing Specialist', 'Marketing Manager', 'Content Writer', 'SEO Specialist'],
        'HR': ['HR Specialist', 'HR Manager', 'Recruiter', 'HR Director'],
        'Finance': ['Financial Analyst', 'Accountant', 'Finance Manager', 'Controller']
    }

    print(f"Generating {num_employees} employee records...")
    
    with open(filename, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['first_name', 'last_name', 'email', 'department', 'position', 'salary', 'hire_date'])
        
        for i in range(num_employees):
            department = random.choice(departments)
            position = random.choice(positions[department])
            first_name = fake.first_name()
            last_name = fake.last_name()
            email = f"{first_name.lower()}.{last_name.lower()}@{fake.domain_name()}"
            
            # Generate salary based on position
            if 'Manager' in position or 'Director' in position or 'Lead' in position:
                salary = random.randint(80000, 180000)
            elif 'Senior' in position or 'Architect' in position:
                salary = random.randint(90000, 160000)
            elif 'Engineer' in position or 'Analyst' in position:
                salary = random.randint(60000, 140000)
            else:
                salary = random.randint(40000, 100000)
            
            # Generate hire date (between 2010 and 2024)
            hire_date = fake.date_between(start_date='-15y', end_date='today')
            
            writer.writerow([
                first_name,
                last_name,
                email,
                department,
                position,
                salary,
                hire_date.strftime('%Y-%m-%d')
            ])
            
            if (i + 1) % 1000 == 0:
                print(f"Generated {i + 1} records...")

    print(f"Employee CSV generated: {filename}")

def insert_into_postgres(csv_file):
    """Insert CSV data into PostgreSQL."""
    print("Inserting data into PostgreSQL...")
    
    # Use psql command to insert data
    sql_command = f"""
    \copy employees(first_name, last_name, email, department, position, salary, hire_date) 
    FROM '{csv_file}' 
    WITH (FORMAT csv, HEADER true, DELIMITER ',', NULL '');
    """
    
    # Write SQL to file and execute
    sql_file = os.path.join(OUTPUT_DIR, 'insert_employees.sql')
    with open(sql_file, 'w') as f:
        f.write(sql_command)
    
    # Execute using psql
    cmd = f"psql -h {POSTGRES_HOST} -U {POSTGRES_USER} -d {POSTGRES_DB} -f {sql_file}"
    
    import subprocess
    env = os.environ.copy()
    env['PGPASSWORD'] = POSTGRES_PASSWORD
    
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, env=env)
    
    if result.returncode == 0:
        print("Successfully inserted data into PostgreSQL")
        print(result.stdout)
    else:
        print("Error inserting data:")
        print(result.stderr)
        sys.exit(1)

def main():
    csv_file = os.path.join(OUTPUT_DIR, 'employees.csv')
    
    # Step 1: Generate CSV
    generate_employee_csv(csv_file, NUM_EMPLOYEES)
    
    # Step 2: Insert into PostgreSQL
    insert_into_postgres(csv_file)
    
    print("Data generation completed successfully!")
    print(f"Total records generated: {NUM_EMPLOYEES}")

if __name__ == '__main__':
    main()
