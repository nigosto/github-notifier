CREATE TABLE "users" (
    id INT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    avatarUrl TEXT NOT NULL 
);