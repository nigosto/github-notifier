package setup

import cats.effect.IO
import cats.effect.kernel.Resource
import scala.concurrent.ExecutionContext
import database.{DatabaseModule, DatabaseTransactor}
import com.typesafe.config.ConfigFactory
import config.AppConfig
import cats.effect.unsafe.implicits.global
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec

/*
  Note: this trait contains some side effects in order to initialize
  the test database, which cannot be avoided. However every class or trait
  that extends it, does not know about these side effects and can itself
  contain only pure code. Minor drawback is that if there is a need for
  `val` members that depend on the `transactor` (directly or indirectly), 
  they need to be `lazy`.
*/
trait TestDatabaseSetup 
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with BeforeAndAfterAll :
  private def transactorResource: Resource[IO, DatabaseTransactor] = for
    config <- Resource
      .eval(IO.blocking(ConfigFactory.load()))
      .map(_.getConfig("app"))
      .map(AppConfig.fromConfig)

    databaseModule <- DatabaseModule(config.testDatabase)
  yield databaseModule.transactor

  // The docs suggested using `var` and `unsafeRunSync()` when doing setups and teardowns for the test suites.
  private var unsafeTransactor: Option[(DatabaseTransactor, IO[Unit])] = None
  def transactor = unsafeTransactor.getOrElse(sys.error("error while connecting to test database"))._1

  def init(): Unit = 
    unsafeTransactor = Some(transactorResource.allocated.unsafeRunSync())

  def clear(): Unit = 
    unsafeTransactor.foreach(_._2.unsafeRunSync())
    unsafeTransactor = None

  override def beforeAll(): Unit =
    init()
    beforeAllWithSetup.unsafeRunSync()

  override def afterAll(): Unit =
    afterAllWithTeardown.unsafeRunSync()
    clear()

  def beforeAllWithSetup: IO[Any] = IO.unit
  def afterAllWithTeardown: IO[Any] = IO.unit