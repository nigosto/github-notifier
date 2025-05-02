package users.preferences

import entities.{UserId, RepositoryId}

class PreferencesService(preferencesDao: PreferencesDao):
  def findUsersByPreferenceForRepository(preference: Preference, repositoryId: RepositoryId) = 
    preferencesDao.findUsersByPreferenceForRepository(preference, repositoryId)

  def updateByUserAndRepositoryId(userId: UserId, repositoryId: RepositoryId, preferences: List[Preference]) = 
    for 
      previous <- preferencesDao.findPreferencesForUserByRepositoryId(userId, repositoryId)
      _ <- preferencesDao.create(userId, repositoryId, preferences.filter(!previous.contains(_)))
      _ <- preferencesDao.delete(userId, repositoryId, previous.filter(!preferences.contains(_)))
    yield ()
