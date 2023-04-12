CREATE USER orderservice WITH PASSWORD 'orderservicepw';
CREATE DATABASE orderdb;
GRANT ALL PRIVILEGES ON DATABASE orderdb to orderservice;

CREATE USER customerservice WITH PASSWORD 'customerservicepw';
CREATE DATABASE customerdb;
GRANT ALL PRIVILEGES ON DATABASE customerdb to customerservice;

CREATE USER kitchenservice WITH PASSWORD 'kitchenservicepw';
CREATE DATABASE kitchendb;
GRANT ALL PRIVILEGES ON DATABASE kitchendb to kitchenservice;

CREATE user accountingservice WITH PASSWORD 'accountingservicepw';
CREATE DATABASE accountingdb;
GRANT ALL PRIVILEGES ON DATABASE accountingdb to accountingservice;
