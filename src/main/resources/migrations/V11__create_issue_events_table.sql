CREATE TABLE "issue_events" (
    id TEXT PRIMARY KEY,
    action TEXT,
    issueId INT REFERENCES "issues"(id),
    repositoryId INT REFERENCES "repositories"(id),
    senderId INT REFERENCES "users"(id),
    createdAt TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);