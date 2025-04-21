# file-mover-service
Microfunction to move files from S3 to SFTP using secrets

## Getting Started

1. Get the Docker running for both AWS locakstack and SFTP-Server

        docker run -d -p 4566:4566 -p 4510-4559:4510-4559 --name localstack localstack/localstack
        
        docker run -d -p 2222:22 -v ~/sftp-data:/home/user -e SFTP_USERS="user:pass" --name sftp-server atmoz/sftp

2. Create a bucket on AWS local S3 and put a file

        awslocal s3 mb s3://test-bucket
        
        echo "This is a sample text file." > sample.txt
        
        awslocal s3 cp sample.txt s3://test-bucket/sample.txt 

3. AWS local SecretsManager to store SFTP-creds

        awslocal secretsmanager create-secret --name sftp-creds --secret-string '{"username":"user","password":"pass"}'

4. Check SFTP connection

        sftp -P 2222 user@localhost

        ssh-keygen -R "[localhost]:2222" //remove outdated entry if exists

5. Create destination location

        mkdir -p ~/sftp-data/upload

6. Hit the endpoint via Postman - POST

        http://localhost:8080/move?bucket=test-bucket&key=sample.txt

7. Validate the File moved successfully

* from terminal --

        sftp -P 2222 user@localhost

* inside the SFTP session --

        cd upload
        ls -l
        get sample.txt