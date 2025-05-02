package services

import setup.TestDatabaseSetup
import users.UserDao
import repositories.{RepositoryDao, RepositoriesService}
import users.UsersService
import setup.Stubs.*
import entities.InvalidEntityError

class RepositoriesServiceTest extends TestDatabaseSetup:
  private lazy val userDao = new UserDao(transactor)
  private lazy val repositoryDao = new RepositoryDao(transactor)
  private lazy val usersService = new UsersService(userDao)
  private lazy val repositoriesService = new RepositoriesService(repositoryDao, usersService)

  override def beforeAllWithSetup =
    userDao.create(userStub) >> repositoryDao.create(repositoryStub)

  override def afterAllWithTeardown = repositoryDao.deleteAll >> userDao.deleteAll

  "RepositoriesService" - {
    "find" - {
      "finds repository by id" in {
        repositoriesService.find(repositoryStub.id).asserting(_ shouldBe Some(repositoryStub))
      }

      "does not find non-existing repository" in {
        repositoriesService.find(invalidRepositoryId).asserting(_ shouldBe None)
      }
    }

    "findOrCreateSingle" - {
      "creates repository if there is no such repository and the owner exists" in {
        repositoriesService.findOrCreateSingle(newRepositoryStub).asserting(_ shouldBe newRepositoryStub)
      }

      "returns the repository if it exists" in {
        repositoriesService.findOrCreateSingle(repositoryStub).asserting(_ shouldBe repositoryStub)
      }

      "throw error if there is no such repository and the owner does not exist" in {
        repositoriesService.findOrCreateSingle(newRepositoryWithoutOwnerStub).assertThrows[InvalidEntityError]
      }
    }

    "findOrCreate" - {
      "creates only the repository if it does not exist, but its owner exists" in {
        repositoriesService.findOrCreate(newRepositoryPayloadStub).asserting(_ shouldBe newRepositoryStub)
      }

      "returns the repository if it exists" in {
        repositoriesService.findOrCreate(repositoryPayloadStub).asserting(_ shouldBe repositoryStub)
      }

      "creates repository alongside its owner if the repository and owner do not exist" in {
        repositoriesService
          .findOrCreate(newRepositoryPayloadWithoutOwnerStub)
          .asserting(_ shouldBe newRepositoryWithoutOwnerStub) >> usersService
          .find(newRepositoryWithoutOwnerStub.ownerId)
          .asserting(_ shouldBe Some(userWithoutEmailStub))
      }
    }
  }
