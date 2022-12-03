package nl.wwbakker.eboekhouden.services

import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._
import zio.{Task, ZLayer}

import java.nio.file.Paths

object Config {

  case class Credentials(username: String, securityCode1: String, securityCode2: String)
  case class Config(credentials: Credentials)

  trait Service {
    def load(): Task[Config]
  }

  case class ServiceImpl() extends Service {
    def load(): Task[Config] = {
      val source = ConfigSource
        .fromHoconFile(Paths.get(System.getProperty("user.home"), ".eboekhouden", "eboekhouden.conf").toFile)

      read(descriptor[Config].from(source)).mapError(re => new IllegalStateException(re.prettyPrint()))
    }
  }

  object ServiceImpl {
    val live: ZLayer[Any, Nothing, Service] =
      ZLayer.succeed(new ServiceImpl())
  }

}
