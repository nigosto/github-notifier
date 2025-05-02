CREATE TABLE "create_events" (
    id INT PRIMARY KEY,
    ref TEXT NOT NULL,
    refType TEXT NOT NULL,
    repositoryId INT REFERENCES "repositories"(id),
    authorId INT REFERENCES "users"(id),
    createdAt TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);