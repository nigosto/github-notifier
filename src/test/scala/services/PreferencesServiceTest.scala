package services

import setup.TestDatabaseSetup
import users.preferences.{PreferencesDao, PreferencesService}
import users.UserDao
import repositories.RepositoryDao
import cats.effect.IO
import setup.Stubs.*
import users.preferences.Preference

class PreferencesServiceTest extends TestDatabaseSetup:
  private lazy val userDao = new UserDao(transactor)
  private lazy val repositoryDao = new RepositoryDao(transactor)
  private lazy val preferencesDao = new PreferencesDao(transactor)
  private lazy val preferencesService = new PreferencesService(preferencesDao)

  override def beforeAllWithSetup =
    userDao.create(userStub) >>
      userDao.create(userWithoutEmailStub) >>
      userDao.create(newUserStub) >>
      repositoryDao.create(repositoryStub) >>
      preferencesDao.create(
        userWithoutEmailStub.id,
        repositoryStub.id,
        List(Preference.PullRequests, Preference.Pushes)
      ) >>
      preferencesDao.create(
        newUserStub.id,
        repositoryStub.id,
        List(Preference.PullRequests, Preference.Creations)
      ) >>
      preferencesDao.create(
        userStub.id,
        repositoryStub.id,
        List(Preference.Creations, Preference.Pushes)
      )

  override def afterAllWithTeardown =
    preferencesDao.deleteAll >> repositoryDao.deleteAll >> userDao.deleteAll

  "PreferencesService" - {
    "findUsersByPreferenceForRepository" - {
      "returns only the users that have PullRequests preference for a specific repository" in {
        preferencesService
          .findUsersByPreferenceForRepository(Preference.PullRequests, repositoryStub.id)
          .asserting(_ should contain theSameElementsAs List(userWithoutEmailStub, newUserStub))
      }

      "returns empty list if there are no users with Issues preference for a specific repository" in {
        preferencesService
          .findUsersByPreferenceForRepository(Preference.Issues, repositoryStub.id)
          .asserting(_ shouldBe List.empty)
      }

      "returns empty list if the repository does not exist" in {
        preferencesService
          .findUsersByPreferenceForRepository(Preference.PullRequests, invalidRepositoryId)
          .asserting(_ shouldBe List.empty)
      }
    }

    "updateByUserAndRepositoryId" - {
      "if all current preferences are passed, keeps them and adds the new ones" in {
        (preferencesService.updateByUserAndRepositoryId(
          userStub.id,
          repositoryStub.id,
          List(Preference.Pushes, Preference.Creations, Preference.PullRequests)
        ) >> preferencesDao.findPreferencesForUserByRepositoryId(userStub.id, repositoryStub.id))
          .asserting:
            _ should contain theSameElementsAs List(
              Preference.PullRequests,
              Preference.Pushes,
              Preference.Creations
            )
      }

      "if some of the current preferences are not passed, they are removed" in {
        (preferencesService.updateByUserAndRepositoryId(
          newUserStub.id,
          repositoryStub.id,
          List(Preference.PullRequests)
        ) >> preferencesDao.findPreferencesForUserByRepositoryId(newUserStub.id, repositoryStub.id))
          .asserting(_ should contain theSameElementsAs List(Preference.PullRequests))
      }

      "if the list of preferences is empty, clears the preferences for the specific user and repo" in {
        (preferencesService.updateByUserAndRepositoryId(userStub.id, repositoryStub.id, List.empty) >> preferencesDao
          .findPreferencesForUserByRepositoryId(userStub.id, repositoryStub.id))
          .asserting(_ shouldBe List.empty)
      }

      "returns the combined result of passing new preferences and omitting some of the current preferences" in {
        (preferencesService.updateByUserAndRepositoryId(
          userWithoutEmailStub.id,
          repositoryStub.id,
          List(Preference.PullRequests, Preference.Issues, Preference.Creations)
        ) >> preferencesDao.findPreferencesForUserByRepositoryId(userWithoutEmailStub.id, repositoryStub.id))
          .asserting:
            _ should contain theSameElementsAs List(Preference.PullRequests, Preference.Issues, Preference.Creations)
      }
    }
  }
