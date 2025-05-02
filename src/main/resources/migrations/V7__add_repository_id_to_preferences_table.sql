ALTER TABLE "preferences"
ADD COLUMN repositoryId INT REFERENCES "repositories"(id);