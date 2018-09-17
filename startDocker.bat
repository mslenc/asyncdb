docker run --rm --name asyncdb-mysql-56 -e MYSQL_ROOT_PASSWORD=testpassword -e MYSQL_USER=asyncdb -e MYSQL_PASSWORD=asyncdb -e MYSQL_DATABASE=asyncdb -p 3356:3306 mysql/mysql-server:5.6
