CREATE TABLE "push_events" (
    id TEXT PRIMARY KEY,
    previousRef TEXT NOT NULL,
    repositoryId INT REFERENCES "repositories"(id),
    senderId INT REFERENCES "users"(id),
    createdAt TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);