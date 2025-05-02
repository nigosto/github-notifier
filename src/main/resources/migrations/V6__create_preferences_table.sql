CREATE TABLE "preferences" (
    userId INT REFERENCES "users"(id),
    preference TEXT NOT NULL,
    PRIMARY KEY (userId, preference)
);