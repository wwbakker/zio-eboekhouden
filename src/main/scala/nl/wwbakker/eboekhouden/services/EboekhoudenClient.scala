package nl.wwbakker.eboekhouden.services

import eboekhouden.{ArrayOfCMutatieList, CError, CMutatieList, CResultGetMutaties, GetMutatiesResponse, OpenSessionResponse}
import nl.wwbakker.eboekhouden.mapping.Mutaties
import nl.wwbakker.eboekhouden.services.Config.Credentials
import scalaxb.{DispatchHttpClientsAsync, SoapClientsAsync}
import zio.{Scope, ZIO, ZLayer}

object EboekhoudenClient {

  trait Service {
    def session(credentials: Credentials): ZIO[Any with Scope, Throwable, LoggedInService]
  }

  trait LoggedInService {
    def session: Session

    def mutaties: ZIO[Any, Throwable, Seq[CMutatieList]]
  }

  private case class SessionClosed()

  case class Session(sessionId: String, securityCode2: String)

  case class ServiceImpl() extends Service {
    private val service = new eboekhouden.SoapAppSoap12Bindings with SoapClientsAsync with DispatchHttpClientsAsync {}.service

    override def session(credentials: Credentials): ZIO[Any with Scope, Throwable, LoggedInService] =
      ZIO.acquireRelease(login(credentials))(sessionId => logout(sessionId).orDie).map(session => LoggedInServiceImpl(session))

    private def login(credentials: Credentials): ZIO[Any, Throwable, Session] =
      ZIO.fromFuture(implicit ec =>
        service.openSession(
          username = Some(credentials.username),
          securityCode1 = Some(credentials.securityCode1),
          securityCode2 = Some(credentials.securityCode2),
          source = Some("api")
        )
      )
        .flatMap {
          case OpenSessionResponse(Some(eboekhouden.CResultOpenSession(_, Some(sessionId)))) =>
            ZIO.succeed(Session(
              sessionId = sessionId,
              securityCode2 = credentials.securityCode2)
            )
          case OpenSessionResponse(Some(eboekhouden.CResultOpenSession(Some(cError), _))) =>
            mapCError(cError)
          case _ =>
            ZIO.fail(new IllegalStateException("OpenSession failed, but no error code or description found"))
        }

    private def logout(sessionId: Session): ZIO[Any, Throwable, SessionClosed] = {
      ZIO.fromFuture(implicit ec =>
        service.closeSession(
          sessionID = Some(sessionId.sessionId)
        )
      )
        .map(_ => SessionClosed())
    }

    private def mapCError(cError: CError): zio.IO[Throwable, Nothing] =
      ZIO.fail(new IllegalStateException(s"code: ${cError.LastErrorCode}, description: ${cError.LastErrorDescription}"))


    case class LoggedInServiceImpl(override val session: Session) extends LoggedInService {
      override def mutaties: ZIO[Any, Throwable, Seq[CMutatieList]] =
        ZIO.fromFuture(implicit ec =>
          service.getMutaties(
            sessionID = Some(session.sessionId),
            securityCode2 = Some(session.securityCode2),
            cFilter = Some(Mutaties.leegFilter)
          )
        ).flatMap {
          case GetMutatiesResponse(Some(CResultGetMutaties(_, Some(ArrayOfCMutatieList(cMutatieList))))) =>
            ZIO.succeed(cMutatieList.flatten)
          case GetMutatiesResponse(Some(CResultGetMutaties(Some(cError), _))) =>
            mapCError(cError)
          case _ =>
            ZIO.fail(new IllegalStateException("getMutaties failed, but no error code or description found"))
        }
    }
  }


  object ServiceImpl {
    val live: ZLayer[Any, Nothing, Service] =
      ZLayer.succeed(new ServiceImpl())
  }


}
