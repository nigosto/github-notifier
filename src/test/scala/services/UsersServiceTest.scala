package services

import setup.TestDatabaseSetup
import setup.Stubs.*
import users.{UserDao, UsersService}
import entities.{UserId, User}
import database.DatabaseTransactor
import cats.effect.IO

class UsersServiceTest extends TestDatabaseSetup:
  private lazy val userDao = new UserDao(transactor)
  private lazy val usersService = new UsersService(userDao)

  override def beforeAllWithSetup = 
    userDao.create(userStub) >> userDao.create(userWithoutEmailStub)

  override def afterAllWithTeardown = userDao.deleteAll

  "UsersService" - {
    "find" - {
      "finds user by id" in {
        usersService.find(userStub.id).asserting(_ shouldBe Some(userStub))
      }

      "does not find non-existing user" in {
        usersService.find(invalidUserId).asserting(_ shouldBe None)
      }
    }

    "findOrCreateSingle" - {
      "creates user if there is no such user" in {
        usersService.findOrCreateSingle(newUserStub).asserting(_ shouldBe newUserStub)
      }

      "returns the user if it exists" in {
        usersService.findOrCreateSingle(userStub).asserting(_ shouldBe userStub)
      }
    }

    "updateEmail" - {
      "sets user's email if it is missing" in {
        (usersService.updateEmail(userWithoutEmailStub.id, "new-email@gmail.com") >> usersService.find(
          userWithoutEmailStub.id
        )).asserting(
          _ shouldBe Some(
            User(
              userWithoutEmailStub.id,
              userWithoutEmailStub.username,
              Some("new-email@gmail.com"),
              userWithoutEmailStub.avatarUrl
            )
          )
        )
      }

      "replaces user's email if it is present" in {
        (usersService.updateEmail(userStub.id, "test-email2@gmail.com") >> usersService.find(
          userStub.id
        )).asserting(
          _ shouldBe Some(
            User(
              userStub.id,
              userStub.username,
              Some("test-email2@gmail.com"),
              userStub.avatarUrl
            )
          )
        )
      }
    }
  }
