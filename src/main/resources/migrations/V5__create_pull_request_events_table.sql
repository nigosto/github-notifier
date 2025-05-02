CREATE TABLE "pull_request_events" (
    id TEXT PRIMARY KEY,
    action TEXT NOT NULL, -- significantly harder to make into enum
    pullRequestId INT REFERENCES "pull_requests"(id),
    repositoryId INT REFERENCES "repositories"(id),
    senderId INT REFERENCES "users"(id)
);