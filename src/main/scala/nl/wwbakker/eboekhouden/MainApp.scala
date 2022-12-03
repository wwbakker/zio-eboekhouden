package nl.wwbakker.eboekhouden

import nl.wwbakker.eboekhouden.services.{Config, EboekhoudenClient}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, Console}

object MainApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val program = for {
      configService <- ZIO.service[Config.Service]
      ebhClient <- ZIO.service[EboekhoudenClient.Service]
      config <- configService.load()
      mutaties <- ZIO.scoped {
        for {
          ebhLoggedIn <- ebhClient.session(config.credentials)
          mutaties <- ebhLoggedIn.mutaties
        } yield mutaties
      }
      _ <- Console.printLine(mutaties.mkString("\n\n\n"))
    } yield ()


    program.provide(
      Config.ServiceImpl.live ++ EboekhoudenClient.ServiceImpl.live
    )
  }
}
