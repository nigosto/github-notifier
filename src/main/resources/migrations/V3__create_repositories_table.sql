CREATE TABLE "repositories" (
    id INT PRIMARY KEY,
    name TEXT NOT NULL,
    ownerId INT REFERENCES "users"(id),
    description TEXT
);