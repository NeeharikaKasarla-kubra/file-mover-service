# file-mover-service
Microfunction to move files from S3 to SFTP using secrets

## Getting Started

1. Get the Docker running for both AWS locakstack and SFTP-Server

`docker run --rm -it -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack`

`docker run -d -p 2222:22 --name sftp-server -e SFTP_USERS="user:pass" atmoz/sftp`

2. Create a bucket on AWS local S3 and put a file

`awslocal s3 mb s3://test-bucket`

`echo "This is a sample text file." > sample.txt`

`awslocal s3 cp sample.txt s3://test-bucket/sample.txt` (or) `aws --endpoint-url=http://localhost:4566 s3 cp sample.txt s3://test-bucket/sample.txt`

3. AWS local SecretsManager to store SFTP-creds

`awslocal secretsmanager create-secret --name sftp-creds --secret-string '{"username":"user","password":"pass"}'`

4. Check SFTP connection 

`sftp -P 2222 user@localhost`

5. Create destination location

`mkdir -p ~/sftp-data/upload`
