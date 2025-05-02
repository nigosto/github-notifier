CREATE TABLE "pull_requests" (
    id INT PRIMARY KEY,
    title TEXT NOT NULL,
    body TEXT,
    repositoryId INT REFERENCES "repositories"(id),
    authorId INT REFERENCES "users"(id)
);