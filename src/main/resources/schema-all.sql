DROP TABLE IF EXISTS student;

CREATE TABLE student (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    course VARCHAR(50)
);
